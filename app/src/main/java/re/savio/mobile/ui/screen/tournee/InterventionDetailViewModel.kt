package re.savio.mobile.ui.screen.intervention

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import re.savio.mobile.data.local.entity.EquipmentEntity
import re.savio.mobile.data.local.entity.InterventionEntity
import re.savio.mobile.data.local.entity.InterventionHistoryEntity
import re.savio.mobile.data.local.dao.SettingsDao
import re.savio.mobile.data.remote.api.DocumentApi
import re.savio.mobile.data.repository.FollowUpResolveOutcome
import re.savio.mobile.data.repository.FollowUpResolveRepository
import re.savio.mobile.data.repository.InvoiceRepository
import re.savio.mobile.data.repository.MobileSyncOrchestrator
import re.savio.mobile.data.repository.PlanningUpdateOutcome
import re.savio.mobile.data.repository.PlanningUpdateRepository
import re.savio.mobile.data.repository.SyncRepository
import re.savio.mobile.data.repository.UnitVeRepository
import re.savio.mobile.ui.screen.intervention.cloture.ContractSummary
import re.savio.mobile.ui.screen.intervention.cloture.LastVeSummary
import re.savio.mobile.ui.screen.intervention.cloture.NextVeDisplay
import re.savio.mobile.ui.screen.intervention.cloture.VeCoverageSummary
import re.savio.mobile.ui.screen.intervention.cloture.formatVeCoverageLabel
import re.savio.mobile.util.MobilePlanningPermission
import re.savio.mobile.util.isFollowUpPending
import java.time.LocalDate
import javax.inject.Inject

data class InterventionDetailUiState(
    val intervention: InterventionEntity? = null,
    val equipments: List<EquipmentEntity> = emptyList(),
    val history: List<InterventionHistoryEntity> = emptyList(),
    val historyPhotoUrls: Map<String, List<String>> = emptyMap(),
    val isLoading: Boolean = false,
    val isSyncRetrying: Boolean = false,
    val pendingInvoiceHamonIssue: Boolean = false,
    val planningPermission: MobilePlanningPermission = MobilePlanningPermission.LIMITED_EDIT,
    val blockMobileFollowUpResolve: Boolean = false,
    val isFollowUpResolving: Boolean = false,
    val isPlanningSaving: Boolean = false,
    val errorMessage: String? = null,
    val contractInfo: ContractSummary? = null,
    val lastVe: LastVeSummary? = null,
    val nextVe: NextVeDisplay? = null,
    val coverage: VeCoverageSummary? = null,
) {
    val showVeSummary: Boolean
        get() =
            contractInfo != null ||
                lastVe != null ||
                nextVe != null ||
                formatVeCoverageLabel(coverage) != null
}

@HiltViewModel
class InterventionDetailViewModel @Inject constructor(
    private val syncRepository: SyncRepository,
    private val unitVeRepository: UnitVeRepository,
    private val invoiceRepository: InvoiceRepository,
    private val mobileSyncOrchestrator: MobileSyncOrchestrator,
    private val planningUpdateRepository: PlanningUpdateRepository,
    private val followUpResolveRepository: FollowUpResolveRepository,
    private val settingsDao: SettingsDao,
    private val documentApi: DocumentApi,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val interventionId: String = checkNotNull(savedStateHandle["interventionId"])

    private val _uiState = MutableStateFlow(InterventionDetailUiState())
    val uiState: StateFlow<InterventionDetailUiState> = _uiState.asStateFlow()

    init {
        loadIntervention()
        loadEquipments()
        viewModelScope.launch {
            _uiState.update {
                it.copy(planningPermission = planningUpdateRepository.currentPermission())
            }
            val settings = settingsDao.getSettingsOnce()
            _uiState.update {
                it.copy(blockMobileFollowUpResolve = settings?.blockMobileFollowUpResolve == true)
            }
        }
    }

    private fun loadIntervention() {
        viewModelScope.launch {
            syncRepository.getInterventionById(interventionId)
                .collect { intervention ->
                    _uiState.update { it.copy(intervention = intervention) }
                    intervention?.let {
                        loadHistory(it.unitId)
                        loadVeContext(it)
                        refreshInvoiceSyncState()
                    }
                }
        }
    }

    private fun loadVeContext(intervention: InterventionEntity) {
        viewModelScope.launch {
            val context = unitVeRepository.getVeContextForUnit(
                unitId = intervention.unitId,
                interventionHint = intervention,
            )
            _uiState.update {
                it.copy(
                    contractInfo = context.contractInfo,
                    lastVe = context.lastVe,
                    nextVe = context.nextVe,
                    coverage = context.coverage,
                )
            }
        }
    }

    private suspend fun refreshInvoiceSyncState() {
        val invoice = invoiceRepository.resolveInvoiceForIntervention(interventionId)
        val hamonIssue =
            invoice != null && invoiceRepository.isLocalHamonSignatureMissing(invoice)
        _uiState.update { it.copy(pendingInvoiceHamonIssue = hamonIssue) }
    }

    fun retrySync() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncRetrying = true, errorMessage = null) }
            try {
                mobileSyncOrchestrator.runFullSync(pullDate = LocalDate.now(), pullForce = true)
                refreshInvoiceSyncState()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = e.message ?: "Échec de la synchronisation")
                }
            } finally {
                _uiState.update { it.copy(isSyncRetrying = false) }
            }
        }
    }

    private fun loadEquipments() {
        viewModelScope.launch {
            syncRepository.hydrateInterventionEquipmentsIfEmpty(interventionId)
            syncRepository.getEquipmentsByIntervention(interventionId)
                .collect { equipments ->
                    _uiState.update { it.copy(equipments = equipments) }
                }
        }
    }

    private fun loadHistory(unitId: String) {
        viewModelScope.launch {
            val history = syncRepository.getHistoryForUnit(unitId)
            _uiState.update { it.copy(history = history) }
            loadHistoryPhotoUrls(history)
        }
    }

    private fun loadHistoryPhotoUrls(history: List<InterventionHistoryEntity>) {
        viewModelScope.launch {
            val urlMap = mutableMapOf<String, List<String>>()
            history.forEach { item ->
                if (!item.photoKeys.isNullOrBlank()) {
                    try {
                        val keys = com.google.gson.Gson()
                            .fromJson(item.photoKeys, Array<String>::class.java)
                            ?: return@forEach
                        val urls = keys.mapNotNull { key ->
                            try {
                                documentApi.getSignedUrl(key).url
                            } catch (e: Exception) {
                                null
                            }
                        }
                        if (urls.isNotEmpty()) urlMap[item.id] = urls
                    } catch (e: Exception) {
                    }
                }
            }
            _uiState.update { it.copy(historyPhotoUrls = urlMap) }
        }
    }

    fun startIntervention() {
        viewModelScope.launch {
            try {
                syncRepository.startIntervention(interventionId)
            } catch (e: IllegalStateException) {
                _uiState.update { it.copy(errorMessage = e.message) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = e.message ?: "Impossible de démarrer l'intervention")
                }
            }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun savePlanning(scheduledAt: String, timeSlot: String, isUrgent: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isPlanningSaving = true, errorMessage = null) }
            when (val outcome = planningUpdateRepository.updatePlanning(interventionId, scheduledAt, timeSlot, isUrgent)) {
                PlanningUpdateOutcome.Success -> Unit
                PlanningUpdateOutcome.ReadOnly ->
                    _uiState.update {
                        it.copy(errorMessage = "Planning verrouillé par votre société")
                    }
                is PlanningUpdateOutcome.Error ->
                    _uiState.update { it.copy(errorMessage = outcome.message) }
            }
            _uiState.update { it.copy(isPlanningSaving = false) }
        }
    }

    fun resolveFollowUp() {
        viewModelScope.launch {
            _uiState.update { it.copy(isFollowUpResolving = true, errorMessage = null) }
            when (val outcome = followUpResolveRepository.resolveFollowUp(interventionId)) {
                FollowUpResolveOutcome.Success -> Unit
                FollowUpResolveOutcome.BlockedBySettings ->
                    _uiState.update {
                        it.copy(errorMessage = "Clôture du suivi réservée au bureau")
                    }
                FollowUpResolveOutcome.NotPending ->
                    _uiState.update {
                        it.copy(errorMessage = "Aucun suivi « À revoir » en attente")
                    }
                is FollowUpResolveOutcome.Error ->
                    _uiState.update { it.copy(errorMessage = outcome.message) }
            }
            _uiState.update { it.copy(isFollowUpResolving = false) }
        }
    }

    fun isFollowUpPending(intervention: InterventionEntity?): Boolean =
        intervention?.let {
            isFollowUpPending(it.followUpStatus, it.followUpRequired, it.status)
        } == true
}
