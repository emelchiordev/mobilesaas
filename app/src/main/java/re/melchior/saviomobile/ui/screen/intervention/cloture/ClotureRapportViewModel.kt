package re.melchior.saviomobile.ui.screen.intervention.cloture

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonParser
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import re.melchior.saviomobile.data.local.dao.AttestationVeDao
import re.melchior.saviomobile.data.local.dao.EquipmentDao
import re.melchior.saviomobile.data.local.dao.InvoiceDao
import re.melchior.saviomobile.data.local.dao.InvoiceLineDao
import re.melchior.saviomobile.data.local.dao.ReferentielDao
import re.melchior.saviomobile.data.local.dao.SettingsDao
import re.melchior.saviomobile.data.local.entity.AnomalyDraftEntity
import re.melchior.saviomobile.data.local.entity.AnomalyTypeEntity
import re.melchior.saviomobile.data.local.entity.EquipmentEntity
import re.melchior.saviomobile.data.local.entity.InterventionEntity
import re.melchior.saviomobile.data.local.entity.InvoiceLineEntity
import re.melchior.saviomobile.data.local.entity.toDto
import re.melchior.saviomobile.data.remote.api.InterventionApi
import re.melchior.saviomobile.data.remote.dto.GenerateReportRequestDto
import re.melchior.saviomobile.data.remote.dto.InterventionTypeDto
import re.melchior.saviomobile.data.remote.dto.stableKey
import re.melchior.saviomobile.data.repository.AnomalyDraftRepository
import re.melchior.saviomobile.data.repository.SyncRepository
import re.melchior.saviomobile.ui.theme.formatEquipmentTypeLabel
import re.melchior.saviomobile.ui.utils.NetworkUtils
import retrofit2.HttpException
import javax.inject.Inject

data class ClotureRapportUiState(
    val intervention: InterventionEntity? = null,
    val report: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val closeTypes: List<InterventionTypeDto> = emptyList(),
    val closeTypesLoading: Boolean = true,
    val closeTypesError: String? = null,
    val selectedCloseTypes: List<InterventionTypeDto> = emptyList(),
    val contractInfo: ContractSummary? = null,
    val lastVe: LastVeSummary? = null,
    val nextVe: NextVeDisplay? = null,
    val consequences: List<ClosureConsequence> = emptyList(),
    val anomalyDrafts: List<AnomalyDraftEntity> = emptyList(),
    val anomalyCatalog: List<AnomalyTypeEntity> = emptyList(),
    val rootEquipments: List<EquipmentEntity> = emptyList(),
    val invoiceLines: List<InvoiceLineEntity> = emptyList(),
    val showAddAnomalySheet: Boolean = false,
    val isGeneratingReport: Boolean = false,
) {
    val isAbsent: Boolean get() = selectedCloseTypes.any { it.code == "ABS" }

    val showClientSignature: Boolean
        get() = selectedCloseTypes.any { it.requireClientSignature }

    val showReport: Boolean get() = selectedCloseTypes.any { it.requireReport }

    val isVeChanged: Boolean
        get() = intervention?.typeCode == "VE"
            && selectedCloseTypes.isNotEmpty()
            && selectedCloseTypes.none { it.isVeType }

    val canProceed: Boolean
        get() {
            if (closeTypesLoading || closeTypesError != null) return false
            if (selectedCloseTypes.isEmpty() || closeTypes.isEmpty()) return false
            if (!showReport) return true
            return report.isNotBlank()
        }

    val canGenerateReport: Boolean
        get() {
            if (intervention == null) return false
            if (hasText(intervention.notes)) return true
            if (billableInvoiceLines(invoiceLines).isNotEmpty()) return true
            if (anomalyDrafts.isNotEmpty()) return true
            if (rootEquipments.isNotEmpty()) return true
            return false
        }
}

private const val CLOSE_TYPES_SYNC_REQUIRED =
    "Types d'intervention non disponibles. Une synchronisation est requise."

@HiltViewModel
class ClotureRapportViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val syncRepository: SyncRepository,
    private val anomalyDraftRepository: AnomalyDraftRepository,
    private val equipmentDao: EquipmentDao,
    private val invoiceDao: InvoiceDao,
    private val invoiceLineDao: InvoiceLineDao,
    private val interventionApi: InterventionApi,
    private val referentielDao: ReferentielDao,
    private val attestationVeDao: AttestationVeDao,
    private val settingsDao: SettingsDao,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val interventionId: String = checkNotNull(savedStateHandle["interventionId"])

    private val _uiState = MutableStateFlow(ClotureRapportUiState())
    val uiState: StateFlow<ClotureRapportUiState> = _uiState.asStateFlow()

    private var updatesRequireValidation: Boolean = false
    private var hasLocalAttestationVe: Boolean = false

    init {
        viewModelScope.launch {
            updatesRequireValidation = settingsDao.getSettingsOnce()?.updatesRequireValidation ?: false
            hasLocalAttestationVe = attestationVeDao.countForIntervention(interventionId) > 0
            refreshDerivedState()
        }
        loadIntervention()
        loadCloseTypes()
        loadAnomalyContext()
        loadInvoiceLines()
    }

    private fun loadInvoiceLines() {
        viewModelScope.launch {
            val invoice = invoiceDao.getByInterventionId(interventionId) ?: return@launch
            val lines = invoiceLineDao.getByInvoiceIdOnce(invoice.id)
            _uiState.update { it.copy(invoiceLines = lines) }
        }
    }

    private fun loadAnomalyContext() {
        viewModelScope.launch {
            anomalyDraftRepository.observeDrafts(interventionId).collect { drafts ->
                _uiState.update { it.copy(anomalyDrafts = drafts) }
            }
        }
        viewModelScope.launch {
            val catalog = anomalyDraftRepository.getCatalogTypesOnce()
            _uiState.update { it.copy(anomalyCatalog = catalog) }
        }
        viewModelScope.launch {
            equipmentDao.getRootEquipmentsByInterventionOnce(interventionId).let { roots ->
                _uiState.update { it.copy(rootEquipments = roots) }
            }
        }
    }

    fun openAddAnomalySheet() {
        _uiState.update { it.copy(showAddAnomalySheet = true) }
    }

    fun dismissAddAnomalySheet() {
        _uiState.update { it.copy(showAddAnomalySheet = false) }
    }

    fun addAnomalyDraft(
        scope: String,
        equipmentId: String?,
        anomalyTypeCode: String?,
        customDescription: String?,
        action: String?,
    ) {
        viewModelScope.launch {
            val unitId = _uiState.value.intervention?.unitId ?: return@launch
            anomalyDraftRepository.addDraft(
                interventionId = interventionId,
                unitId = unitId,
                equipmentId = equipmentId,
                scope = scope,
                anomalyTypeCode = anomalyTypeCode,
                customDescription = customDescription,
                action = action,
            )
        }
    }

    private fun loadIntervention() {
        viewModelScope.launch {
            syncRepository.getInterventionById(interventionId)
                .collect { intervention ->
                    _uiState.update {
                        it.copy(
                            intervention = intervention,
                            report = intervention?.report ?: it.report,
                        )
                    }
                    if (intervention != null) {
                        loadUnitVeContext(intervention)
                    }
                    tryInitCloseTypeSelection()
                    refreshDerivedState()
                }
        }
    }

    private suspend fun loadUnitVeContext(intervention: InterventionEntity) {
        val lastHistory = syncRepository.getLastCompletedVeForUnit(intervention.unitId)
        val lastVe = lastVeSummaryFrom(lastHistory)
        val contract = contractSummaryFrom(intervention)
        val nextDate = computeNextVeDate(lastVe, contract?.renewalDate)
        val nextVe = nextDate?.let { nextVeDisplay(it) }
        _uiState.update {
            it.copy(
                contractInfo = contract,
                lastVe = lastVe,
                nextVe = nextVe,
            )
        }
    }

    private fun loadCloseTypes() {
        viewModelScope.launch {
            val types = referentielDao.getCloseTypesOnce().map { it.toDto() }
            _uiState.update {
                it.copy(
                    closeTypes = types,
                    closeTypesLoading = false,
                    closeTypesError =
                        if (types.isEmpty()) CLOSE_TYPES_SYNC_REQUIRED else null,
                )
            }
            tryInitCloseTypeSelection()
            refreshDerivedState()
        }
    }

    private fun tryInitCloseTypeSelection() {
        if (_uiState.value.selectedCloseTypes.isNotEmpty()) return
        val inv = _uiState.value.intervention ?: return
        val types = _uiState.value.closeTypes
        if (types.isEmpty()) return
        _uiState.update {
            it.copy(selectedCloseTypes = listOf(resolveDefaultSingle(inv, types)))
        }
    }

    private fun refreshDerivedState() {
        val state = _uiState.value
        val contract = state.intervention?.let { contractSummaryFrom(it) }
        _uiState.update {
            it.copy(
                contractInfo = contract,
                consequences = computeClosureConsequences(
                    selectedTypes = state.selectedCloseTypes,
                    plannedTypeCode = state.intervention?.typeCode,
                    hasLocalAttestationVe = hasLocalAttestationVe,
                    updatesRequireValidation = updatesRequireValidation,
                ),
            )
        }
    }

    fun toggleCloseType(type: InterventionTypeDto) {
        _uiState.update { state ->
            val next =
                if (state.selectedCloseTypes.any { it.stableKey() == type.stableKey() }) {
                    state.selectedCloseTypes.filter { it.stableKey() != type.stableKey() }
                } else {
                    state.selectedCloseTypes + type
                }
            val report =
                if (next.any { it.code == "ABS" }) "" else state.report
            state.copy(selectedCloseTypes = next, report = report)
        }
        refreshDerivedState()
    }

    fun onReportChange(text: String) {
        _uiState.update { it.copy(report = text) }
    }

    fun generateReport() {
        viewModelScope.launch {
            if (!NetworkUtils.isOnline(context)) {
                _uiState.update {
                    it.copy(errorMessage = "Connexion requise pour générer le compte rendu")
                }
                return@launch
            }
            val state = _uiState.value
            if (!state.canGenerateReport) {
                _uiState.update {
                    it.copy(
                        errorMessage =
                            "Saisissez des observations, pièces ou anomalies avant de générer le compte rendu.",
                    )
                }
                return@launch
            }
            val intervention = state.intervention ?: return@launch
            _uiState.update { it.copy(isGeneratingReport = true, errorMessage = null) }
            try {
                val body = buildGenerateReportRequest(state, intervention)
                val response = interventionApi.generateReport(interventionId, body)
                _uiState.update {
                    it.copy(report = response.report.trim(), isGeneratingReport = false)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isGeneratingReport = false,
                        errorMessage = humanReadableApiError(e),
                    )
                }
            }
        }
    }

    suspend fun saveReport(): Boolean {
        val state = _uiState.value
        val toSave = if (!state.showReport) "" else state.report
        if (state.showReport && toSave.isBlank()) return false
        return try {
            syncRepository.saveReport(interventionId, toSave)
            true
        } catch (e: Exception) {
            _uiState.update { it.copy(errorMessage = e.message) }
            false
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun preselectedKeysForNavigation(): String {
        val keys = _uiState.value.selectedCloseTypes.joinToString("\u001F") { it.stableKey() }
        return if (keys.isEmpty()) "_" else keys
    }
}

private fun resolveDefaultSingle(
    intervention: InterventionEntity,
    types: List<InterventionTypeDto>,
): InterventionTypeDto {
    intervention.interventionTypeId?.let { id ->
        types.find { it.id == id }?.let { return it }
    }
    types.find { it.code == intervention.typeCode }?.let { return it }
    return types.first()
}

private fun hasText(value: String?): Boolean = !value.isNullOrBlank()

private fun billableInvoiceLines(lines: List<InvoiceLineEntity>): List<InvoiceLineEntity> =
    lines.filter { !it.isTextBlock }

private fun formatInvoiceLine(line: InvoiceLineEntity): String {
    val label = line.label.trim()
    return if (line.quantity != 1.0) {
        val qty =
            if (line.quantity % 1.0 == 0.0) line.quantity.toInt().toString()
            else line.quantity.toString()
        "${qty}× $label"
    } else {
        label
    }
}

private fun formatEquipment(eq: EquipmentEntity): String {
    val brandModel = listOfNotNull(eq.brand, eq.model)
        .joinToString(" ")
        .trim()
    if (brandModel.isNotEmpty()) return brandModel
    return formatEquipmentTypeLabel(eq.typeCode).ifBlank { "Équipement" }
}

private fun buildAnomalySummaries(
    drafts: List<AnomalyDraftEntity>,
    catalog: List<AnomalyTypeEntity>,
): List<String> {
    return buildAnomalyDraftDisplays(drafts, catalog).map { display ->
        buildString {
            append(display.label)
            append(" (")
            append(display.levelTag)
            append(')')
            display.draft.action?.trim()?.takeIf { it.isNotEmpty() }?.let { action ->
                append(" — ")
                append(action)
            }
        }
    }
}

private fun buildLastVisitSummary(lastVe: LastVeSummary?): String? {
    lastVe ?: return null
    val datePart = "Dernière VE le ${formatClosureDate(lastVe.completedAt)}"
    val tech = lastVe.technicianFirstName?.trim()?.takeIf { it.isNotEmpty() }
    return if (tech != null) "$datePart par $tech" else datePart
}

private fun buildGenerateReportRequest(
    state: ClotureRapportUiState,
    intervention: InterventionEntity,
): GenerateReportRequestDto {
    val customerName = listOfNotNull(
        intervention.customerFirstName?.trim()?.takeIf { it.isNotEmpty() },
        intervention.customerLastName?.trim()?.takeIf { it.isNotEmpty() },
    ).joinToString(" ").ifBlank { null }

    val address = buildString {
        append(intervention.unitStreet.trim())
        append(", ")
        append(intervention.unitPostalCode.trim())
        append(' ')
        append(intervention.unitCity.trim())
    }.trim().trim(',').ifBlank { null }

    val actualLabels = state.selectedCloseTypes.map { it.label }.ifEmpty { null }
    val equipments = state.rootEquipments.map(::formatEquipment).ifEmpty { null }
    val invoiceLineLabels = billableInvoiceLines(state.invoiceLines).map(::formatInvoiceLine)
    val anomalySummaries = buildAnomalySummaries(state.anomalyDrafts, state.anomalyCatalog)
        .ifEmpty { null }

    return GenerateReportRequestDto(
        plannedTypeLabel = intervention.typeLabel,
        actualTypeLabels = actualLabels,
        customerName = customerName,
        address = address,
        equipments = equipments,
        technicianObservations = intervention.notes?.trim()?.takeIf { it.isNotEmpty() },
        invoiceLines = invoiceLineLabels.ifEmpty { null },
        anomalySummaries = anomalySummaries,
        lastVisitSummary = buildLastVisitSummary(state.lastVe),
    )
}

private fun humanReadableApiError(e: Throwable): String {
    if (e is HttpException) {
        runCatching {
            val raw = e.response()?.errorBody()?.string().orEmpty()
            if (raw.isBlank()) return@runCatching
            val root = JsonParser.parseString(raw).asJsonObject
            val msgEl = root.get("message") ?: return@runCatching
            when {
                msgEl.isJsonPrimitive ->
                    msgEl.asString.trim().takeIf { it.isNotEmpty() }?.let { return it }

                msgEl.isJsonArray -> {
                    val joined =
                        msgEl.asJsonArray.joinToString(" ") { je ->
                            je.asJsonPrimitive.asString.trim()
                        }
                    if (joined.isNotBlank()) return joined
                }
            }
        }
        return "Erreur ${e.code()}"
    }
    return e.message ?: "Erreur réseau"
}
