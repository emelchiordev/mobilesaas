package re.melchior.saviomobile.ui.screen.intervention

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import re.melchior.saviomobile.data.local.dao.EquipmentDao
import re.melchior.saviomobile.data.local.dao.PendingOperationDao
import re.melchior.saviomobile.data.local.entity.CatalogEquipmentSearchRow
import re.melchior.saviomobile.data.local.entity.EquipmentEntity
import re.melchior.saviomobile.data.local.entity.PendingOperationEntity
import re.melchior.saviomobile.data.repository.CatalogSyncRepository
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CatalogSearchViewModel @Inject constructor(
    private val catalogSyncRepository: CatalogSyncRepository,
    private val equipmentDao: EquipmentDao,
    private val pendingOperationDao: PendingOperationDao,
) : ViewModel() {

    val brands = catalogSyncRepository.getBrands()
    val equipmentTypes = catalogSyncRepository.getEquipmentTypes()

    private val _query = MutableStateFlow("")
    private val _selectedBrandId = MutableStateFlow<String?>(null)
    private val _selectedTypeId = MutableStateFlow<String?>(null)

    val query: StateFlow<String> = _query.asStateFlow()
    val selectedBrandId: StateFlow<String?> = _selectedBrandId.asStateFlow()
    val selectedTypeId: StateFlow<String?> = _selectedTypeId.asStateFlow()

    val results = combine(_query, _selectedBrandId, _selectedTypeId) { q, brand, type ->
        Triple(q, brand, type)
    }.flatMapLatest { (q, brand, type) ->
        catalogSyncRepository.searchEquipment(q, brand, type, null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val energies = catalogSyncRepository.getEnergies()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _pendingSelection = MutableStateFlow<PendingCatalogSelection?>(null)
    val pendingSelection: StateFlow<PendingCatalogSelection?> = _pendingSelection.asStateFlow()

    private val _isCreating = MutableStateFlow(false)
    val isCreating: StateFlow<Boolean> = _isCreating.asStateFlow()

    private val _createdEquipmentId = MutableSharedFlow<String>(
        extraBufferCapacity = 1,
    )
    val createdEquipmentId: SharedFlow<String> = _createdEquipmentId.asSharedFlow()

    fun onQueryChange(q: String) {
        _query.value = q
    }

    fun onBrandSelected(id: String?) {
        _selectedBrandId.value = id
    }

    fun onTypeSelected(id: String?) {
        _selectedTypeId.value = id
    }

    fun requestSelectEquipment(
        row: CatalogEquipmentSearchRow,
        interventionId: String,
        unitId: String,
        parentEquipmentId: String? = null,
        existingEquipmentId: String? = null,
    ) {
        if (_isCreating.value) return
        _pendingSelection.value = PendingCatalogSelection(
            row = row,
            interventionId = interventionId,
            unitId = unitId,
            parentEquipmentId = parentEquipmentId,
            existingEquipmentId = existingEquipmentId,
            selectedEnergyId = row.equipment.energyId,
        )
    }

    fun updatePendingEnergyId(energyId: String) {
        _pendingSelection.update { it?.copy(selectedEnergyId = energyId) }
    }

    fun dismissPendingSelection() {
        _pendingSelection.value = null
    }

    fun confirmPendingSelection() {
        val pending = _pendingSelection.value ?: return
        _pendingSelection.value = null
        selectEquipment(
            row = pending.row,
            interventionId = pending.interventionId,
            unitId = pending.unitId,
            parentEquipmentId = pending.parentEquipmentId,
            existingEquipmentId = pending.existingEquipmentId,
            installationEnergyId = pending.selectedEnergyId,
        )
    }

    private fun selectEquipment(
        row: CatalogEquipmentSearchRow,
        interventionId: String,
        unitId: String,
        parentEquipmentId: String? = null,
        existingEquipmentId: String? = null,
        installationEnergyId: String,
    ) {
        if (_isCreating.value) return
        viewModelScope.launch {
            _isCreating.value = true
            try {
                val nextMobileOrder = (
                    equipmentDao.getMaxOrderForUnit(unitId)?.takeIf { it >= 101 } ?: 100
                ) + 1
                val newId = java.util.UUID.randomUUID().toString()
                val brand = catalogSyncRepository.getNomenclatureById(row.equipment.brandId)
                val type = catalogSyncRepository.getNomenclatureById(row.equipment.equipmentTypeId)
                val typeCode = type?.code?.trim()?.uppercase().orEmpty()
                val catalogEnergy = catalogSyncRepository.getNomenclatureById(row.equipment.energyId)
                val installationEnergy =
                    if (typeCode == "BRULEUR") {
                        null
                    } else {
                        catalogSyncRepository.getNomenclatureById(installationEnergyId)
                            ?: catalogEnergy
                    }

                if (existingEquipmentId != null) {
                    val oldEq = equipmentDao.getEquipmentsByInterventionOnce(interventionId)
                        .find { it.id == existingEquipmentId }
                    if (oldEq != null) {
                        equipmentDao.markAsReplaced(oldEq.interventionId, oldEq.order)
                    }

                    val entity = EquipmentEntity(
                        interventionId = interventionId,
                        order = nextMobileOrder,
                        id = newId,
                        unitId = unitId,
                        brand = brand?.label,
                        model = row.equipment.model,
                        typeCode = type?.code,
                        energyCode = installationEnergy?.code,
                        serialNumber = null,
                        installDate = null,
                        isPrimary = false,
                        equipmentCatalogId = row.equipment.id,
                        catalogBrandId = row.equipment.brandId,
                        parentEquipmentId = parentEquipmentId,
                        powerKw = row.equipment.powerKw?.let { p ->
                            if (p % 1.0 == 0.0) p.toInt().toString() else p.toString()
                        },
                        evacuationMode = null,
                    )
                    equipmentDao.insertAll(listOf(entity))

                    val payload = mapOf(
                        "interventionId" to interventionId,
                        "unitId" to unitId,
                        "order" to nextMobileOrder,
                        "oldEquipmentId" to existingEquipmentId,
                        "model" to row.equipment.model,
                        "brand" to (brand?.label ?: ""),
                        "brandCode" to (brand?.code ?: ""),
                        "typeCode" to (type?.code ?: ""),
                        "energyLabel" to (installationEnergy?.label ?: ""),
                        "energyCode" to (installationEnergy?.code ?: ""),
                        "isPrimary" to false,
                        "equipmentCatalogId" to row.equipment.id,
                        "catalogBrandId" to row.equipment.brandId,
                        "catalogEquipmentTypeId" to row.equipment.equipmentTypeId,
                        "catalogEnergyId" to row.equipment.energyId,
                        "reason" to "replacement",
                    )
                    val op = PendingOperationEntity(
                        id = newId,
                        type = "REPLACE_EQUIPMENT",
                        payload = Gson().toJson(payload),
                        occurredAt = java.time.Instant.now().toString(),
                        interventionId = interventionId,
                        status = "pending",
                        createdAt = java.time.Instant.now().toString(),
                    )
                    pendingOperationDao.insert(op)
                } else {
                    val entity = EquipmentEntity(
                        interventionId = interventionId,
                        order = nextMobileOrder,
                        id = newId,
                        unitId = unitId,
                        brand = brand?.label,
                        model = row.equipment.model,
                        typeCode = type?.code,
                        energyCode = installationEnergy?.code,
                        serialNumber = null,
                        installDate = null,
                        isPrimary = false,
                        equipmentCatalogId = row.equipment.id,
                        catalogBrandId = row.equipment.brandId,
                        parentEquipmentId = parentEquipmentId,
                        powerKw = row.equipment.powerKw?.let { p ->
                            if (p % 1.0 == 0.0) p.toInt().toString() else p.toString()
                        },
                        evacuationMode = null,
                    )
                    equipmentDao.insertAll(listOf(entity))

                    val payload = mapOf(
                        "interventionId" to interventionId,
                        "unitId" to unitId,
                        "order" to nextMobileOrder,
                        "model" to row.equipment.model,
                        "brand" to (brand?.label ?: ""),
                        "brandCode" to (brand?.code ?: ""),
                        "typeCode" to (type?.code ?: ""),
                        "energyLabel" to (installationEnergy?.label ?: ""),
                        "energyCode" to (installationEnergy?.code ?: ""),
                        "isPrimary" to false,
                        "equipmentCatalogId" to row.equipment.id,
                        "catalogBrandId" to row.equipment.brandId,
                        "catalogEquipmentTypeId" to row.equipment.equipmentTypeId,
                        "catalogEnergyId" to row.equipment.energyId,
                        "parentEquipmentId" to parentEquipmentId,
                    )
                    val op = PendingOperationEntity(
                        id = newId,
                        type = "CREATE_EQUIPMENT",
                        payload = Gson().toJson(payload),
                        occurredAt = java.time.Instant.now().toString(),
                        interventionId = interventionId,
                        status = "pending",
                        createdAt = java.time.Instant.now().toString(),
                    )
                    pendingOperationDao.insert(op)
                }

                _createdEquipmentId.emit(newId)
            } finally {
                _isCreating.value = false
            }
        }
    }
}

data class PendingCatalogSelection(
    val row: CatalogEquipmentSearchRow,
    val interventionId: String,
    val unitId: String,
    val parentEquipmentId: String?,
    val existingEquipmentId: String?,
    val selectedEnergyId: String,
)
