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

    fun selectEquipment(
        row: CatalogEquipmentSearchRow,
        interventionId: String,
        unitId: String,
        parentEquipmentId: String? = null,
        existingEquipmentId: String? = null,
    ) {
        if (_isCreating.value) return
        viewModelScope.launch {
            _isCreating.value = true
            try {
                val newId = java.util.UUID.randomUUID().toString()
                val brand = catalogSyncRepository.getNomenclatureById(row.equipment.brandId)
                val type = catalogSyncRepository.getNomenclatureById(row.equipment.equipmentTypeId)
                val energy = catalogSyncRepository.getNomenclatureById(row.equipment.energyId)

                if (existingEquipmentId != null) {
                    equipmentDao.markAsReplaced(existingEquipmentId, interventionId)

                    val entity = EquipmentEntity(
                        id = newId,
                        interventionId = interventionId,
                        brand = brand?.label,
                        model = row.equipment.model,
                        typeCode = type?.code,
                        energyCode = energy?.code,
                        serialNumber = null,
                        installDate = null,
                        isPrimary = false,
                        equipmentCatalogId = row.equipment.id,
                        parentEquipmentId = parentEquipmentId,
                    )
                    equipmentDao.insertAll(listOf(entity))

                    val payload = mapOf(
                        "unitId" to unitId,
                        "oldEquipmentId" to existingEquipmentId,
                        "model" to row.equipment.model,
                        "brand" to (brand?.label ?: ""),
                        "brandCode" to (brand?.code ?: ""),
                        "typeCode" to (type?.code ?: ""),
                        "energyLabel" to (energy?.label ?: ""),
                        "energyCode" to (energy?.code ?: ""),
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
                        id = newId,
                        interventionId = interventionId,
                        brand = brand?.label,
                        model = row.equipment.model,
                        typeCode = type?.code,
                        energyCode = energy?.code,
                        serialNumber = null,
                        installDate = null,
                        isPrimary = false,
                        equipmentCatalogId = row.equipment.id,
                        parentEquipmentId = parentEquipmentId,
                    )
                    equipmentDao.insertAll(listOf(entity))

                    val payload = mapOf(
                        "unitId" to unitId,
                        "model" to row.equipment.model,
                        "brand" to (brand?.label ?: ""),
                        "brandCode" to (brand?.code ?: ""),
                        "typeCode" to (type?.code ?: ""),
                        "energyLabel" to (energy?.label ?: ""),
                        "energyCode" to (energy?.code ?: ""),
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
