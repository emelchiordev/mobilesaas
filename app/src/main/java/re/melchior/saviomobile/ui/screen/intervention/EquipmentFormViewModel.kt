package re.melchior.saviomobile.ui.screen.intervention

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import re.melchior.saviomobile.data.local.dao.EquipmentDao
import re.melchior.saviomobile.data.local.dao.PendingOperationDao
import re.melchior.saviomobile.data.local.entity.EquipmentEntity
import re.melchior.saviomobile.data.local.entity.PendingOperationEntity
import re.melchior.saviomobile.data.repository.CatalogSyncRepository
import javax.inject.Inject

@HiltViewModel
class EquipmentFormViewModel @Inject constructor(
    private val catalogSyncRepository: CatalogSyncRepository,
    private val pendingOperationDao: PendingOperationDao,
    private val equipmentDao: EquipmentDao,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _uiState = MutableStateFlow(EquipmentFormUiState())
    val uiState: StateFlow<EquipmentFormUiState> = _uiState.asStateFlow()

    private val _saved = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val saved: SharedFlow<Unit> = _saved.asSharedFlow()

    init {
        savedStateHandle.get<String>("catalogEquipmentId")
            ?.takeIf { it.isNotBlank() }
            ?.let { loadFromCatalog(it) }
    }

    fun loadFromCatalog(catalogEquipmentId: String) {
        viewModelScope.launch {
            val eq = catalogSyncRepository.getEquipmentById(catalogEquipmentId) ?: return@launch
            val brand = eq.brandId.let { catalogSyncRepository.getNomenclatureById(it) }
            val type = eq.equipmentTypeId.let { catalogSyncRepository.getNomenclatureById(it) }
            val energy = eq.energyId.let { catalogSyncRepository.getNomenclatureById(it) }
            _uiState.update {
                it.copy(
                    model = eq.model,
                    brand = brand?.label ?: "",
                    brandCode = brand?.code ?: "",
                    typeCode = type?.code ?: "",
                    energyLabel = energy?.label ?: "",
                    energyCode = energy?.code ?: "",
                    equipmentCatalogId = catalogEquipmentId,
                    catalogBrandId = eq.brandId,
                    catalogEquipmentTypeId = eq.equipmentTypeId,
                    catalogEnergyId = eq.energyId,
                )
            }
        }
    }

    fun updateModel(v: String) = _uiState.update { it.copy(model = v) }
    fun updateBrand(v: String) = _uiState.update { it.copy(brand = v) }
    fun updateTypeCode(v: String) = _uiState.update { it.copy(typeCode = v) }
    fun updateEnergyLabel(v: String) = _uiState.update { it.copy(energyLabel = v) }
    fun updateSerialNumber(v: String) = _uiState.update { it.copy(serialNumber = v) }
    fun updateNotes(v: String) = _uiState.update { it.copy(notes = v) }
    fun updateIsPrimary(v: Boolean) = _uiState.update { it.copy(isPrimary = v) }
    fun updateReplacementReason(v: String) = _uiState.update { it.copy(replacementReason = v) }

    fun save(
        interventionId: String,
        unitId: String,
        existingEquipmentId: String?,
        parentEquipmentId: String?,
    ) {
        viewModelScope.launch {
            val state = _uiState.value
            val now = java.time.Instant.now().toString()
            val opId = java.util.UUID.randomUUID().toString()

            val type = if (existingEquipmentId != null) "REPLACE_EQUIPMENT" else "CREATE_EQUIPMENT"
            val nextMobileOrder = (
                equipmentDao.getMaxOrderForUnit(unitId)?.takeIf { it >= 101 } ?: 100
            ) + 1
            val payload = buildMap<String, Any?> {
                put("unitId", unitId)
                put("order", nextMobileOrder)
                put("model", state.model)
                put("brand", state.brand)
                put("brandCode", state.brandCode)
                put("typeCode", state.typeCode)
                put("energyLabel", state.energyLabel)
                put("energyCode", state.energyCode)
                put("serialNumber", state.serialNumber.ifBlank { null })
                put("notes", state.notes.ifBlank { null })
                put("isPrimary", state.isPrimary)
                put("equipmentCatalogId", state.equipmentCatalogId)
                put("catalogBrandId", state.catalogBrandId)
                put("catalogEquipmentTypeId", state.catalogEquipmentTypeId)
                put("catalogEnergyId", state.catalogEnergyId)
                if (existingEquipmentId == null) {
                    put("parentEquipmentId", parentEquipmentId)
                }
                if (existingEquipmentId != null) {
                    put("oldEquipmentId", existingEquipmentId)
                    put("reason", state.replacementReason)
                }
            }

            val op = PendingOperationEntity(
                id = opId,
                type = type,
                payload = Gson().toJson(payload),
                occurredAt = now,
                interventionId = interventionId,
                status = "pending",
                createdAt = now,
            )
            pendingOperationDao.insert(op)

            val localEq = EquipmentEntity(
                interventionId = interventionId,
                order = nextMobileOrder,
                id = opId,
                unitId = unitId,
                brand = state.brand,
                model = state.model,
                typeCode = state.typeCode,
                energyCode = state.energyCode,
                serialNumber = state.serialNumber.ifBlank { null },
                installDate = null,
                isPrimary = state.isPrimary,
                equipmentCatalogId = state.equipmentCatalogId,
                catalogBrandId = state.catalogBrandId,
                parentEquipmentId = if (existingEquipmentId == null) parentEquipmentId else null,
                powerKw = null,
                evacuationMode = null,
            )
            equipmentDao.insertAll(listOf(localEq))

            _saved.emit(Unit)
        }
    }
}

data class EquipmentFormUiState(
    val model: String = "",
    val brand: String = "",
    val brandCode: String = "",
    val typeCode: String = "",
    val energyLabel: String = "",
    val energyCode: String = "",
    val serialNumber: String = "",
    val notes: String = "",
    val isPrimary: Boolean = false,
    val replacementReason: String = "replacement",
    val equipmentCatalogId: String? = null,
    val catalogBrandId: String? = null,
    val catalogEquipmentTypeId: String? = null,
    val catalogEnergyId: String? = null,
)
