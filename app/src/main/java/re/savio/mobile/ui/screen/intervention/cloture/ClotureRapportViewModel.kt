package re.savio.mobile.ui.screen.intervention.cloture

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonParser
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import re.savio.mobile.data.local.dao.AttestationVeDao
import re.savio.mobile.data.local.dao.AttestationVePointControleDao
import re.savio.mobile.data.local.dao.ColdMeasureDao
import re.savio.mobile.data.local.dao.EquipmentDao
import re.savio.mobile.data.local.dao.InvoiceDao
import re.savio.mobile.data.local.dao.InvoiceLineDao
import re.savio.mobile.data.local.dao.MeasureDao
import re.savio.mobile.data.local.dao.PacMeasureDao
import re.savio.mobile.data.local.dao.ReferentielDao
import re.savio.mobile.data.local.dao.SettingsDao
import re.savio.mobile.data.local.entity.AnomalyDraftEntity
import re.savio.mobile.data.local.entity.AnomalyTypeEntity
import re.savio.mobile.data.local.entity.AttestationVeEntity
import re.savio.mobile.data.local.entity.EquipmentEntity
import re.savio.mobile.data.local.entity.InterventionEntity
import re.savio.mobile.data.local.entity.InstallationCheckEntity
import re.savio.mobile.data.local.entity.InvoiceLineEntity
import re.savio.mobile.data.local.entity.toDto
import re.savio.mobile.data.remote.api.InterventionApi
import re.savio.mobile.data.remote.dto.GenerateReportRequestDto
import re.savio.mobile.data.remote.dto.InterventionTypeDto
import re.savio.mobile.data.remote.dto.stableKey
import re.savio.mobile.data.repository.AnomalyDraftRepository
import re.savio.mobile.data.repository.ContractRenewalRepository
import re.savio.mobile.data.repository.InvoiceRepository
import re.savio.mobile.data.repository.RenewalEligibility
import re.savio.mobile.observability.SavioSyncSentry
import re.savio.mobile.data.repository.InstallationCheckRepository
import re.savio.mobile.data.repository.SyncRepository
import re.savio.mobile.data.repository.UnitVeRepository
import re.savio.mobile.ui.theme.formatEquipmentTypeLabel
import re.savio.mobile.ui.utils.NetworkUtils
import re.savio.mobile.util.GAS_PIPE_EXPIRED_ANOMALY_CODE
import re.savio.mobile.util.SavioTimeZone
import re.savio.mobile.util.toAttestableInput
import re.savio.mobile.util.formatScheduledAtDateReadable
import re.savio.mobile.util.isGasPipeExpiredWithoutAnomaly
import java.time.LocalDate
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
    val coverage: VeCoverageSummary? = null,
    val consequences: List<ClosureConsequence> = emptyList(),
    val anomalyDrafts: List<AnomalyDraftEntity> = emptyList(),
    val anomalyCatalog: List<AnomalyTypeEntity> = emptyList(),
    val rootEquipments: List<EquipmentEntity> = emptyList(),
    val invoiceLines: List<InvoiceLineEntity> = emptyList(),
    val showAddAnomalySheet: Boolean = false,
    val isGeneratingReport: Boolean = false,
    val reportGenerationStatus: String? = null,
    val reportFallbackUsed: Boolean = false,
    val canGenerateReport: Boolean = false,
    val followUpRequired: Boolean = false,
    val followUpNote: String = "",
    val renewalEligibility: RenewalEligibility? = null,
    val renewalLoading: Boolean = false,
    val renewalError: String? = null,
) {
    val showRenewContractCta: Boolean
        get() =
            shouldShowRenewContractCta(
                renewable = renewalEligibility?.renewable == true,
                hasVeTypeSelected = selectedCloseTypes.any { it.isVeType },
            )

    val isAbsent: Boolean get() = selectedCloseTypes.any { it.code == "ABS" }

    val showClientSignature: Boolean
        get() = selectedCloseTypes.any { it.requireClientSignature }

    val showReport: Boolean get() = selectedCloseTypes.any { it.requireReport }

    val isVeChanged: Boolean
        get() {
            val planned = resolvePlannedType(intervention, closeTypes)
            return isPlannedVeChanged(planned, selectedCloseTypes)
        }

    val canProceed: Boolean
        get() {
            if (closeTypesLoading || closeTypesError != null) return false
            if (selectedCloseTypes.isEmpty() || closeTypes.isEmpty()) return false
            if (blocksClosureForMissingCommissioning(selectedCloseTypes, rootEquipments)) {
                return false
            }
            if (!showReport) return true
            return report.isNotBlank()
        }

    val isReportGenerationBusy: Boolean
        get() =
            isGeneratingReport ||
                reportGenerationStatus in REPORT_GENERATION_BUSY_STATUSES

    val reportGenerateButtonText: String
        get() =
            when {
                isReportGenerationBusy -> "Génération en cours…"
                reportGenerationStatus == "done" -> "Régénérer"
                else -> "Générer le compte rendu"
            }
}

private const val CLOSE_TYPES_SYNC_REQUIRED =
    "Types d'intervention non disponibles. Une synchronisation est requise."

private val REPORT_GENERATION_BUSY_STATUSES = setOf("queued", "processing", "retrying")
private const val REPORT_GENERATION_POLL_MS = 3_000L
private const val REPORT_GENERATION_TIMEOUT_MS = 180_000L
private const val REPORT_GENERATION_ERROR =
    "Génération impossible, saisissez manuellement"
private const val REPORT_GENERATION_OFFLINE =
    "Réseau requis pour cette fonctionnalité"
private const val REPORT_RENEWAL_OFFLINE =
    "Réseau requis pour renouveler le contrat"
private const val REPORT_RENEWAL_ERROR =
    "Impossible de créer la facture de renouvellement"

@HiltViewModel
class ClotureRapportViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val syncRepository: SyncRepository,
    private val unitVeRepository: UnitVeRepository,
    private val contractRenewalRepository: ContractRenewalRepository,
    private val invoiceRepository: InvoiceRepository,
    private val anomalyDraftRepository: AnomalyDraftRepository,
    private val installationCheckRepository: InstallationCheckRepository,
    private val equipmentDao: EquipmentDao,
    private val invoiceDao: InvoiceDao,
    private val invoiceLineDao: InvoiceLineDao,
    private val interventionApi: InterventionApi,
    private val referentielDao: ReferentielDao,
    private val attestationVeDao: AttestationVeDao,
    private val attestationVePointControleDao: AttestationVePointControleDao,
    private val measureDao: MeasureDao,
    private val coldMeasureDao: ColdMeasureDao,
    private val pacMeasureDao: PacMeasureDao,
    private val settingsDao: SettingsDao,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val interventionId: String = checkNotNull(savedStateHandle["interventionId"])

    private val _uiState = MutableStateFlow(ClotureRapportUiState())
    val uiState: StateFlow<ClotureRapportUiState> = _uiState.asStateFlow()

    private val _navigateToInvoice = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val navigateToInvoice: SharedFlow<String> = _navigateToInvoice.asSharedFlow()

    private var updatesRequireValidation: Boolean = false
    private var hasLocalAttestationVe: Boolean = false
    private var hasLocalCerfaFluides: Boolean = false
    private var baseVeCoverage: VeCoverageSummary? = null
    private var latestAttestations: List<AttestationVeEntity> = emptyList()
    private var installationCheck: InstallationCheckEntity? = null
    private var reportTechnicalFactsCount: Int = 0

    init {
        viewModelScope.launch {
            updatesRequireValidation = settingsDao.getSettingsOnce()?.updatesRequireValidation ?: false
            refreshReportGenerationGate()
        }
        viewModelScope.launch {
            attestationVeDao.getByIntervention(interventionId).collect { attestations ->
                latestAttestations = attestations
                hasLocalAttestationVe = attestations.isNotEmpty()
                refreshDerivedState()
                refreshReportGenerationGate()
            }
        }
        viewModelScope.launch {
            coldMeasureDao.observeCountForIntervention(interventionId).collect { count ->
                hasLocalCerfaFluides = count > 0
                refreshDerivedState()
                refreshReportGenerationGate()
            }
        }
        loadIntervention()
        loadCloseTypes()
        loadAnomalyContext()
        loadInvoiceLines()
        viewModelScope.launch {
            installationCheckRepository.observe(interventionId).collect { check ->
                installationCheck = check
                refreshDerivedState()
                refreshReportGenerationGate()
            }
        }
    }

    private fun refreshReportGenerationGate() {
        viewModelScope.launch {
            val state = _uiState.value
            val intervention = state.intervention ?: run {
                _uiState.update { it.copy(canGenerateReport = false) }
                return@launch
            }
            val attestations = attestationVeDao.getByIntervention(interventionId).first()
            val attestationPoints = loadAttestationPointsMap(attestations)
            val measures = measureDao.getByIntervention(interventionId)
            val pacMeasures = pacMeasureDao.observeByInterventionId(interventionId).first()
            val coldCount = coldMeasureDao.countForIntervention(interventionId)
            val assessment =
                assessReportGenerationContent(
                    technicianNotes = intervention.notes,
                    invoiceLines = state.invoiceLines,
                    anomalyDrafts = state.anomalyDrafts,
                    attestations = attestations,
                    attestationPoints = attestationPoints,
                    measures = measures,
                    pacMeasures = pacMeasures,
                    coldMeasureCount = coldCount,
                    installationCheck = installationCheck,
                )
            reportTechnicalFactsCount = assessment.technicalFactsCount
            _uiState.update { it.copy(canGenerateReport = assessment.canGenerate) }
        }
    }

    private fun loadInvoiceLines() {
        viewModelScope.launch {
            val invoice = invoiceDao.getByInterventionId(interventionId) ?: return@launch
            val lines = invoiceLineDao.getByInvoiceIdOnce(invoice.id)
            _uiState.update { it.copy(invoiceLines = lines) }
            refreshReportGenerationGate()
        }
    }

    private fun loadAnomalyContext() {
        viewModelScope.launch {
            anomalyDraftRepository.observeDrafts(interventionId).collect { drafts ->
                _uiState.update { it.copy(anomalyDrafts = drafts) }
                refreshDerivedState()
                refreshReportGenerationGate()
            }
        }
        viewModelScope.launch {
            val catalog = anomalyDraftRepository.getCatalogTypesOnce()
            _uiState.update { it.copy(anomalyCatalog = catalog) }
        }
        viewModelScope.launch {
            equipmentDao.getRootEquipmentsByInterventionOnce(interventionId).let { roots ->
                _uiState.update { it.copy(rootEquipments = roots) }
                refreshClosureCoverage()
                loadRenewalEligibility()
            }
        }
    }

    private fun resolvePrimaryEquipmentId(): String? {
        val roots = _uiState.value.rootEquipments
        if (roots.isEmpty()) return null
        return roots.firstOrNull { it.isPrimary }?.id ?: roots.first().id
    }

    private fun loadRenewalEligibility() {
        viewModelScope.launch {
            if (!NetworkUtils.isOnline(context)) return@launch
            val equipmentId = resolvePrimaryEquipmentId() ?: return@launch
            runCatching {
                contractRenewalRepository.getRenewalEligibility(equipmentId)
            }.onSuccess { eligibility ->
                _uiState.update { it.copy(renewalEligibility = eligibility, renewalError = null) }
            }
        }
    }

    fun renewContractOnSite() {
        viewModelScope.launch {
            val eligibility = _uiState.value.renewalEligibility
            val lineId = eligibility?.contractLineId?.trim().orEmpty()
            if (lineId.isEmpty()) {
                _uiState.update { it.copy(renewalError = "Renouvellement indisponible pour cet appareil.") }
                return@launch
            }
            if (!NetworkUtils.isOnline(context)) {
                _uiState.update { it.copy(renewalError = REPORT_RENEWAL_OFFLINE) }
                return@launch
            }
            _uiState.update { it.copy(renewalLoading = true, renewalError = null) }
            runCatching {
                contractRenewalRepository.createRenewalInvoice(lineId, interventionId)
                invoiceRepository.refreshInvoiceFromServer(interventionId)
            }.onSuccess {
                _uiState.update { it.copy(renewalLoading = false) }
                _navigateToInvoice.emit(interventionId)
            }.onFailure { err ->
                val message =
                    when (err) {
                        is HttpException -> err.message() ?: REPORT_RENEWAL_ERROR
                        else -> err.message ?: REPORT_RENEWAL_ERROR
                    }
                _uiState.update { it.copy(renewalLoading = false, renewalError = message) }
            }
        }
    }

    fun clearRenewalError() {
        _uiState.update { it.copy(renewalError = null) }
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

    fun setAnomalyCorrected(localId: String, corrected: Boolean) {
        viewModelScope.launch {
            anomalyDraftRepository.setCorrected(localId, corrected)
        }
    }

    fun removeAnomalyDraft(localId: String) {
        viewModelScope.launch {
            anomalyDraftRepository.deleteDraft(localId)
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
                            followUpRequired = intervention?.followUpRequired ?: it.followUpRequired,
                            followUpNote = intervention?.followUpNote ?: it.followUpNote,
                        )
                    }
                    if (intervention != null) {
                        loadUnitVeContext(intervention)
                    }
                    tryInitCloseTypeSelection()
                    refreshDerivedState()
                    refreshReportGenerationGate()
                }
        }
    }

    private suspend fun loadUnitVeContext(intervention: InterventionEntity) {
        val context = unitVeRepository.getVeContextForUnit(
            unitId = intervention.unitId,
            interventionHint = intervention,
        )
        baseVeCoverage = context.coverage
        _uiState.update {
            it.copy(
                contractInfo = context.contractInfo,
                lastVe = context.lastVe,
                nextVe = context.nextVe,
            )
        }
        refreshClosureCoverage()
    }

    private fun refreshClosureCoverage() {
        val state = _uiState.value
        val closureIsVe = state.selectedCloseTypes.any { it.isVeType }
        val equipmentInputs = state.rootEquipments.map { it.toAttestableInput() }
        val orderById = state.rootEquipments.associate { it.id to it.order }
        val coverage =
            projectClosureCoverage(
                base = baseVeCoverage,
                closureIsVe = closureIsVe,
                localAttestationEquipmentOrders =
                    latestAttestations.map { it.equipmentOrder }.toSet(),
                unitEquipments = equipmentInputs,
                equipmentOrderForInput = { input -> orderById[input.id] },
            )
        _uiState.update { it.copy(coverage = coverage) }
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
            it.copy(selectedCloseTypes = listOf(resolveDefaultCloseType(inv, types)))
        }
    }

    private fun refreshDerivedState() {
        val state = _uiState.value
        val contract = state.intervention?.let { contractSummaryFrom(it) }
        val hasGasPipeDraft =
            state.anomalyDrafts.any { it.anomalyTypeCode == GAS_PIPE_EXPIRED_ANOMALY_CODE }
        val gasPipeExpiredWithoutAnomaly =
            isGasPipeExpiredWithoutAnomaly(
                check = installationCheck,
                hasGasPipeAnomalyDraft = hasGasPipeDraft,
                today = LocalDate.now(SavioTimeZone.appZone),
            )
        _uiState.update {
            it.copy(
                contractInfo = contract,
                consequences = computeClosureConsequences(
                    selectedTypes = state.selectedCloseTypes,
                    plannedType = resolvePlannedType(state.intervention, state.closeTypes),
                    hasLocalAttestationVe = hasLocalAttestationVe,
                    updatesRequireValidation = updatesRequireValidation,
                    equipmentsMissingCommissioning =
                        countEquipmentsMissingCommissioning(state.rootEquipments),
                    gasPipeExpiredWithoutAnomaly = gasPipeExpiredWithoutAnomaly,
                    hasLocalCerfaFluides = hasLocalCerfaFluides,
                ),
            )
        }
        refreshClosureCoverage()
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

    fun clearReport() {
        _uiState.update { it.copy(report = "") }
    }

    fun onFollowUpRequiredChange(required: Boolean) {
        _uiState.update {
            it.copy(
                followUpRequired = required,
                followUpNote = if (required) it.followUpNote else "",
            )
        }
    }

    fun onFollowUpNoteChange(value: String) {
        _uiState.update { it.copy(followUpNote = value) }
    }

    fun generateReport() {
        viewModelScope.launch {
            if (!NetworkUtils.isOnline(context)) {
                _uiState.update {
                    it.copy(errorMessage = REPORT_GENERATION_OFFLINE)
                }
                return@launch
            }
            val state = _uiState.value
            if (!state.canGenerateReport) {
                _uiState.update {
                    it.copy(
                        errorMessage =
                            "Saisissez des observations, mesures, attestation, pièces, anomalies ou contrôle installation avant de générer le compte rendu.",
                    )
                }
                return@launch
            }
            val intervention = state.intervention ?: return@launch
            SavioSyncSentry.onReportGenerationStarted(interventionId)
            _uiState.update {
                it.copy(
                    isGeneratingReport = true,
                    reportGenerationStatus = "queued",
                    reportFallbackUsed = false,
                    errorMessage = null,
                )
            }
            try {
                val body = buildGenerateReportRequest(state, intervention)
                interventionApi.generateReport(interventionId, body)
                val deadline = System.currentTimeMillis() + REPORT_GENERATION_TIMEOUT_MS
                var finished = false
                while (System.currentTimeMillis() < deadline) {
                    delay(REPORT_GENERATION_POLL_MS)
                    val statusResponse = interventionApi.getReportStatus(interventionId)
                    val status = statusResponse.status?.trim().orEmpty()
                    when (status) {
                        "done" -> {
                            val report = statusResponse.report?.trim().orEmpty()
                            if (report.isBlank()) {
                                throw IllegalStateException(REPORT_GENERATION_ERROR)
                            }
                            _uiState.update {
                                it.copy(
                                    report = report,
                                    reportGenerationStatus = "done",
                                    reportFallbackUsed = statusResponse.fallbackUsed == true,
                                    isGeneratingReport = false,
                                )
                            }
                            SavioSyncSentry.onReportGenerationDone(
                                interventionId = interventionId,
                                fallbackUsed = statusResponse.fallbackUsed == true,
                            )
                            finished = true
                            break
                        }
                        "error" -> throw IllegalStateException(REPORT_GENERATION_ERROR)
                        "queued", "processing", "retrying", "pending" -> {
                            _uiState.update {
                                it.copy(reportGenerationStatus = status)
                            }
                        }
                    }
                }
                if (!finished) {
                    throw IllegalStateException(REPORT_GENERATION_ERROR)
                }
            } catch (e: Exception) {
                val message =
                    if (e is IllegalStateException && e.message == REPORT_GENERATION_ERROR) {
                        REPORT_GENERATION_ERROR
                    } else {
                        humanReadableApiError(e)
                    }
                _uiState.update {
                    it.copy(
                        isGeneratingReport = false,
                        reportGenerationStatus = "error",
                        errorMessage = message,
                    )
                }
                SavioSyncSentry.onReportGenerationFailed(interventionId, message)
            }
        }
    }

    suspend fun saveReport(): Boolean {
        val state = _uiState.value
        val toSave = if (!state.showReport) "" else state.report
        if (state.showReport && toSave.isBlank()) return false
        return try {
            syncRepository.saveReport(interventionId, toSave)
            syncRepository.saveFollowUp(
                interventionId = interventionId,
                required = state.followUpRequired,
                note = state.followUpNote,
            )
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

    private suspend fun buildEstablishedDocuments(): List<String> {
        val docs = mutableListOf<String>()
        val attestations = attestationVeDao.getByIntervention(interventionId).first()
        if (attestations.any { it.type != "ECS" }) {
            docs.add("Attestation d'entretien")
        }
        if (attestations.any { it.type == "ECS" }) {
            docs.add("Contrôle ECS")
        }
        if (hasLocalMeasures()) {
            docs.add("Prises de mesures")
        }
        return docs
    }

    private suspend fun loadAttestationPointsMap(
        attestations: List<AttestationVeEntity>,
    ): Map<AttestationPointsKey, Map<String, String>> =
        buildMap {
            for (att in attestations) {
                val points =
                    attestationVePointControleDao
                        .getByAttestationOnce(
                            att.interventionId,
                            att.equipmentOrder,
                            att.type,
                        ).associate { it.cle to it.resultat }
                put(attestationPointsKey(att.equipmentOrder, att.type), points)
            }
        }

    private suspend fun hasLocalMeasures(): Boolean =
        measureDao.getByIntervention(interventionId).isNotEmpty() ||
            coldMeasureDao.countForIntervention(interventionId) > 0 ||
            pacMeasureDao.countForIntervention(interventionId) > 0

    private suspend fun buildGenerateReportRequest(
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
        val establishedDocuments = buildEstablishedDocuments().ifEmpty { null }
        val attestations = attestationVeDao.getByIntervention(interventionId).first()
        val attestationPoints = loadAttestationPointsMap(attestations)
        val structuredFacts = buildStructuredFactsBlocks(
            equipments = state.rootEquipments,
            attestations = attestations,
            measures = measureDao.getByIntervention(interventionId),
            pacMeasures = pacMeasureDao.observeByInterventionId(interventionId).first(),
            anomalyDrafts = state.anomalyDrafts,
            anomalyCatalog = state.anomalyCatalog,
            invoiceLines = state.invoiceLines,
            installationCheck = installationCheck,
            attestationPoints = attestationPoints,
        ).ifEmpty { null }

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
            interventionDate = formatInterventionDate(intervention),
            establishedDocuments = establishedDocuments,
            structuredFacts = structuredFacts,
            technicalFactsCount = reportTechnicalFactsCount.takeIf { it > 0 },
        )
    }
}

private fun hasText(value: String?): Boolean = !value.isNullOrBlank()

internal fun billableInvoiceLines(lines: List<InvoiceLineEntity>): List<InvoiceLineEntity> =
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

internal fun buildAnomalySummaries(
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
            if (display.draft.corrected) {
                append(" — corrigée sur place")
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

private fun formatInterventionDate(intervention: InterventionEntity): String? {
    val iso = intervention.startedAt?.takeIf { it.isNotBlank() }
        ?: intervention.scheduledAt.takeIf { it.isNotBlank() }
        ?: return null
    val readable = formatScheduledAtDateReadable(iso)
    return readable.takeIf { it != "—" }
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