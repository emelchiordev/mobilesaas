package re.melchior.saviomobile.ui.screen.intervention

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import re.melchior.saviomobile.data.local.dao.EquipmentDao
import re.melchior.saviomobile.data.local.dao.PendingOperationDao
import re.melchior.saviomobile.data.local.entity.CatalogEquipmentEntity
import re.melchior.saviomobile.data.local.entity.CatalogEquipmentSearchRow
import re.melchior.saviomobile.data.local.entity.EquipmentEntity
import re.melchior.saviomobile.data.local.entity.PendingOperationEntity
import re.melchior.saviomobile.data.repository.CatalogSyncRepository
import re.melchior.saviomobile.data.repository.SyncRepository
import javax.inject.Inject

data class EquipementDetailUiState(
    val equipment: EquipmentEntity? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class EquipementDetailViewModel @Inject constructor(
    private val catalogSyncRepository: CatalogSyncRepository,
    private val pendingOperationDao: PendingOperationDao,
    private val equipmentDao: EquipmentDao,
    private val syncRepository: SyncRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val interventionId: String = checkNotNull(savedStateHandle["interventionId"])
    private val equipmentId: String = checkNotNull(savedStateHandle["equipmentId"])
    val currentInterventionId: String get() = interventionId

    private val _uiState = MutableStateFlow(EquipementDetailUiState())
    val uiState: StateFlow<EquipementDetailUiState> = _uiState.asStateFlow()

    private val _catalogEquipment = MutableStateFlow<CatalogEquipmentEntity?>(null)
    val catalogEquipment: StateFlow<CatalogEquipmentEntity?> = _catalogEquipment.asStateFlow()

    private val _interventionUnitId = MutableStateFlow<String?>(null)
    val interventionUnitId: StateFlow<String?> = _interventionUnitId.asStateFlow()

    private val _changeMode = MutableStateFlow(false)
    val changeMode: StateFlow<Boolean> = _changeMode.asStateFlow()

    private val _catalogQuery = MutableStateFlow("")
    val catalogQuery: StateFlow<String> = _catalogQuery.asStateFlow()

    private val _navigateBack = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1,
    )
    val navigateBack: SharedFlow<Unit> = _navigateBack.asSharedFlow()

    val catalogResults = _catalogQuery
        .debounce(300)
        .filter { it.length >= 2 }
        .flatMapLatest { q ->
            catalogSyncRepository.searchEquipment(q, null, null, null)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val energies = catalogSyncRepository.getEnergies()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _pendingCatalogChange = MutableStateFlow<CatalogEquipmentSearchRow?>(null)
    val pendingCatalogChange: StateFlow<CatalogEquipmentSearchRow?> = _pendingCatalogChange.asStateFlow()

    private val _pendingCatalogEnergyId = MutableStateFlow<String?>(null)
    val pendingCatalogEnergyId: StateFlow<String?> = _pendingCatalogEnergyId.asStateFlow()

    val interventionInProgress: StateFlow<Boolean> =
        syncRepository
            .getInterventionById(interventionId)
            .map { it?.status == "in_progress" }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = false,
            )

    /** Tous les équipements de l'intervention (ex. détection PAC hybride). */
    val interventionEquipments: StateFlow<List<EquipmentEntity>> =
        equipmentDao
            .getEquipmentsByIntervention(interventionId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList(),
            )

    /** True si cet équipement est issu d'un CREATE/REPLACE encore en attente de sync (badge « Nouveau »). */
    val isNewEquipment: StateFlow<Boolean> =
        equipmentDao
            .getEquipmentByInterventionAndServerId(interventionId, equipmentId)
            .flatMapLatest { eq ->
                if (eq == null) {
                    flowOf(false)
                } else {
                    pendingOperationDao.getPendingByInterventionId(interventionId).map { ops ->
                        ops.any {
                            it.id == equipmentId &&
                                (
                                    it.type == "CREATE_EQUIPMENT" ||
                                        it.type == "REPLACE_EQUIPMENT"
                                    )
                        }
                    }
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = false,
            )

    init {
        loadEquipment()
    }

    fun setChangeMode(enabled: Boolean) {
        _changeMode.value = enabled
        if (!enabled) {
            _catalogQuery.value = ""
        }
    }

    fun onCatalogQueryChange(q: String) {
        _catalogQuery.value = q
    }

    fun applyNewCatalogEquipment(selected: CatalogEquipmentSearchRow) {
        val typeCode = selected.typeCode?.trim()?.uppercase().orEmpty()
        if (typeCode == "BRULEUR") {
            confirmCatalogChange(selected, selected.equipment.energyId)
            return
        }
        _pendingCatalogChange.value = selected
        _pendingCatalogEnergyId.value = selected.equipment.energyId
    }

    fun dismissPendingCatalogChange() {
        _pendingCatalogChange.value = null
        _pendingCatalogEnergyId.value = null
    }

    fun updatePendingCatalogEnergyId(energyId: String) {
        _pendingCatalogEnergyId.value = energyId
    }

    fun confirmPendingCatalogChange() {
        val selected = _pendingCatalogChange.value ?: return
        val energyId = _pendingCatalogEnergyId.value ?: selected.equipment.energyId
        confirmCatalogChange(selected, energyId)
    }

    private fun confirmCatalogChange(selected: CatalogEquipmentSearchRow, installationEnergyId: String) {
        viewModelScope.launch {
            val eq = _uiState.value.equipment ?: return@launch
            val interventionId = eq.interventionId
            val brand = catalogSyncRepository.getNomenclatureById(selected.equipment.brandId)
            val type = catalogSyncRepository.getNomenclatureById(selected.equipment.equipmentTypeId)
            val typeCode = type?.code?.trim()?.uppercase().orEmpty()
            val catalogEnergy = catalogSyncRepository.getNomenclatureById(selected.equipment.energyId)
            val installationEnergy =
                if (typeCode == "BRULEUR") {
                    null
                } else {
                    catalogSyncRepository.getNomenclatureById(installationEnergyId) ?: catalogEnergy
                }

            val now = java.time.Instant.now().toString()
            val payload = mapOf(
                "equipmentId" to eq.id,
                "equipmentCatalogId" to selected.equipment.id,
                "brand" to (brand?.label ?: ""),
                "brandCode" to (brand?.code ?: ""),
                "model" to selected.equipment.model,
                "typeCode" to (type?.code ?: ""),
                "energyLabel" to (installationEnergy?.label ?: ""),
                "energyCode" to (installationEnergy?.code ?: ""),
                "catalogBrandId" to selected.equipment.brandId,
                "catalogEquipmentTypeId" to selected.equipment.equipmentTypeId,
                "catalogEnergyId" to selected.equipment.energyId,
            )

            val op = PendingOperationEntity(
                id = java.util.UUID.randomUUID().toString(),
                type = "UPDATE_EQUIPMENT",
                payload = Gson().toJson(payload),
                occurredAt = now,
                interventionId = interventionId,
                status = "pending",
                createdAt = now,
            )
            pendingOperationDao.insert(op)

            val updated = eq.copy(
                brand = brand?.label ?: eq.brand,
                model = selected.equipment.model,
                typeCode = type?.code ?: eq.typeCode,
                energyCode = installationEnergy?.code ?: eq.energyCode,
                equipmentCatalogId = selected.equipment.id,
                catalogBrandId = selected.equipment.brandId,
            )
            equipmentDao.insertAll(listOf(updated))

            _catalogEquipment.value = catalogSyncRepository.getEquipmentById(selected.equipment.id)

            _changeMode.value = false
            _catalogQuery.value = ""
            _pendingCatalogChange.value = null
            _pendingCatalogEnergyId.value = null
            _uiState.update { it.copy(equipment = updated) }
        }
    }

    private fun loadEquipment() {
        viewModelScope.launch {
            equipmentDao.getEquipmentByInterventionAndServerId(interventionId, equipmentId)
                .collect { equipment ->
                    _uiState.update { it.copy(equipment = equipment) }
                    viewModelScope.launch {
                        val catalogId = equipment?.equipmentCatalogId
                        _catalogEquipment.value = if (catalogId != null) {
                            catalogSyncRepository.getEquipmentById(catalogId)
                        } else {
                            null
                        }
                        _interventionUnitId.value = equipment?.let {
                            syncRepository.getInterventionByIdOnce(interventionId)?.unitId
                        }
                    }
                }
        }
    }

    fun saveSerialNumber(serial: String) {
        viewModelScope.launch {
            val eq = _uiState.value.equipment ?: return@launch
            val value = serial.trim().ifBlank { null }
            val op = PendingOperationEntity(
                id = java.util.UUID.randomUUID().toString(),
                type = "UPDATE_EQUIPMENT",
                payload = Gson().toJson(
                    mapOf(
                        "equipmentId" to eq.id,
                        "serialNumber" to value,
                    ),
                ),
                occurredAt = java.time.Instant.now().toString(),
                interventionId = eq.interventionId,
                status = "pending",
                createdAt = java.time.Instant.now().toString(),
            )
            pendingOperationDao.insert(op)

            val updated = eq.copy(serialNumber = value)
            equipmentDao.insertAll(listOf(updated))
            _uiState.update { it.copy(equipment = updated) }
        }
    }

    fun saveCommissioningDate(date: String) {
        viewModelScope.launch {
            val eq = _uiState.value.equipment ?: return@launch
            val value = date.trim().ifBlank { null }
            val op = PendingOperationEntity(
                id = java.util.UUID.randomUUID().toString(),
                type = "UPDATE_EQUIPMENT",
                payload = Gson().toJson(
                    mapOf(
                        "equipmentId" to eq.id,
                        "installDate" to value,
                    ),
                ),
                occurredAt = java.time.Instant.now().toString(),
                interventionId = eq.interventionId,
                status = "pending",
                createdAt = java.time.Instant.now().toString(),
            )
            pendingOperationDao.insert(op)

            val updated = eq.copy(installDate = value)
            equipmentDao.insertAll(listOf(updated))
            _uiState.update { it.copy(equipment = updated) }
        }
    }

    fun saveEnergy(energyId: String) {
        viewModelScope.launch {
            val eq = _uiState.value.equipment ?: return@launch
            val nom = catalogSyncRepository.getNomenclatureById(energyId) ?: return@launch
            val op = PendingOperationEntity(
                id = java.util.UUID.randomUUID().toString(),
                type = "UPDATE_EQUIPMENT",
                payload = Gson().toJson(
                    mapOf(
                        "equipmentId" to eq.id,
                        "energyCode" to nom.code,
                        "energyLabel" to nom.label,
                    ),
                ),
                occurredAt = java.time.Instant.now().toString(),
                interventionId = eq.interventionId,
                status = "pending",
                createdAt = java.time.Instant.now().toString(),
            )
            pendingOperationDao.insert(op)

            val updated = eq.copy(energyCode = nom.code)
            equipmentDao.insertAll(listOf(updated))
            _uiState.update { it.copy(equipment = updated) }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun deleteEquipment() {
        viewModelScope.launch {
            val eq = _uiState.value.equipment ?: return@launch
            val unitId = _interventionUnitId.value
                ?: syncRepository.getInterventionByIdOnce(eq.interventionId)?.unitId

            val existingCreate = pendingOperationDao.getByIdAndType(eq.id, "CREATE_EQUIPMENT")

            equipmentDao.deleteByInterventionAndOrder(eq.interventionId, eq.order)

            if (existingCreate != null) {
                pendingOperationDao.deleteById(existingCreate.id)
            } else {
                val op = PendingOperationEntity(
                    id = java.util.UUID.randomUUID().toString(),
                    type = "DELETE_EQUIPMENT",
                    payload = Gson().toJson(
                        mapOf(
                            "equipmentId" to eq.id,
                            "unitId" to unitId,
                        ),
                    ),
                    occurredAt = java.time.Instant.now().toString(),
                    interventionId = eq.interventionId,
                    status = "pending",
                    createdAt = java.time.Instant.now().toString(),
                )
                pendingOperationDao.insert(op)
            }

            _navigateBack.emit(Unit)
        }
    }

    fun openNotice(catalogEquipmentId: String, context: Context) {
        viewModelScope.launch {
            try {
                val url = catalogSyncRepository.getNoticeDownloadUrl(catalogEquipmentId)
                if (!url.isNullOrBlank()) {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    context.startActivity(intent)
                }
            } catch (e: Exception) {
                android.util.Log.e("EquipDetail", "Erreur notice: ${e.message}")
            }
        }
    }

    /** Fusionne `pompe` / `gicleur` dans `equipment.attrs` (sync mobile). */
    fun saveInstallationAttrs(pompeFields: Map<String, String?>, gicleurFields: Map<String, String?>) {
        viewModelScope.launch {
            val eq = _uiState.value.equipment ?: return@launch
            val gson = Gson()
            val root = try {
                JsonParser.parseString(eq.attrsJson ?: "{}").asJsonObject
            } catch (_: Exception) {
                JsonObject()
            }
            fun putNested(name: String, fields: Map<String, String?>) {
                val nested = JsonObject()
                fields.forEach { (k, v) ->
                    val t = v?.trim().orEmpty()
                    if (t.isNotEmpty()) nested.addProperty(k, t)
                }
                if (nested.size() == 0) {
                    root.remove(name)
                } else {
                    root.add(name, nested)
                }
            }
            putNested("pompe", pompeFields)
            putNested("gicleur", gicleurFields)
            val json = gson.toJson(root)
            val payload = JsonObject()
            payload.addProperty("equipmentId", eq.id)
            payload.addProperty("interventionId", eq.interventionId)
            payload.add("attrs", root)
            val op = PendingOperationEntity(
                id = java.util.UUID.randomUUID().toString(),
                type = "UPDATE_EQUIPMENT",
                payload = payload.toString(),
                occurredAt = java.time.Instant.now().toString(),
                interventionId = eq.interventionId,
                status = "pending",
                createdAt = java.time.Instant.now().toString(),
            )
            pendingOperationDao.insert(op)
            val updated = eq.copy(attrsJson = json)
            equipmentDao.insertAll(listOf(updated))
            _uiState.update { it.copy(equipment = updated) }
        }
    }
}
