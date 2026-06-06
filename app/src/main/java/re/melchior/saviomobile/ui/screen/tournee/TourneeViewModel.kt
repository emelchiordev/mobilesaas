package re.melchior.saviomobile.ui.screen.tournee

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import re.melchior.saviomobile.data.local.entity.InterventionEntity
import re.melchior.saviomobile.data.local.entity.PendingInterventionEntity
import re.melchior.saviomobile.data.repository.CatalogSyncRepository
import re.melchior.saviomobile.data.repository.TenantArticleSyncRepository
import re.melchior.saviomobile.data.repository.PendingInterventionRepository
import re.melchior.saviomobile.data.repository.MobileSyncOrchestrator
import re.melchior.saviomobile.data.repository.SyncRepository
import re.melchior.saviomobile.data.repository.SyncResult
import android.util.Log
import java.time.LocalDate
import javax.inject.Inject
import retrofit2.HttpException

data class TourneeUiState(
    val isLoading: Boolean = false,
    val isSyncing: Boolean = false,
    val isDatePullRefreshing: Boolean = false,
    val isCatalogSyncing: Boolean = false,
    val errorMessage: String? = null,
    val selectedDate: LocalDate = LocalDate.now(),
    val pendingSyncCount: Int = 0
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TourneeViewModel @Inject constructor(
    private val syncRepository: SyncRepository,
    private val catalogSyncRepository: CatalogSyncRepository,
    private val tenantArticleSyncRepository: TenantArticleSyncRepository,
    private val mobileSyncOrchestrator: MobileSyncOrchestrator,
    private val pendingInterventionRepository: PendingInterventionRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TourneeUiState())
    val uiState: StateFlow<TourneeUiState> = _uiState.asStateFlow()

    private val _resumeCandidate = MutableStateFlow<InterventionEntity?>(null)
    val resumeCandidate: StateFlow<InterventionEntity?> = _resumeCandidate.asStateFlow()

    private val lastSeenSyncStatus = mutableMapOf<String, String>()

    // Flow réactif sur la date sélectionnée
    // flatMapLatest annule automatiquement le collect précédent
    // quand la date change — plus de collectors en concurrence
    val interventions: StateFlow<List<InterventionEntity>> = _uiState
        .flatMapLatest { state ->
            syncRepository.getInterventionsByDate(state.selectedDate)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val pendingSyncCount: StateFlow<Int> = syncRepository
        .getPendingSyncCount()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val pendingOfflineInterventionCount: StateFlow<Int> = pendingInterventionRepository
        .countPending()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0,
        )

    val pendingCreatingForDate: StateFlow<List<PendingInterventionEntity>> = _uiState
        .flatMapLatest { state ->
            pendingInterventionRepository.observePendingForDate(state.selectedDate)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList(),
        )

    init {
        viewModelScope.launch {
            _resumeCandidate.value = syncRepository.getInProgressIntervention()
        }
        viewModelScope.launch {
            interventions.collect { list ->
                list.forEach { entity ->
                    val prev = lastSeenSyncStatus[entity.id]
                    if (prev != entity.syncStatus) {
                        if (entity.syncStatus in CONFLICT_SYNC_STATUSES) {
                            logSyncConflictReplaced(entity)
                        }
                    }
                    lastSeenSyncStatus[entity.id] = entity.syncStatus
                }
            }
        }
        pull()
    }

    private fun logSyncConflictReplaced(entity: InterventionEntity) {
        val timestamp =
            entity.updatedAt?.takeIf { it.isNotBlank() }
                ?: entity.startedAt?.takeIf { it.isNotBlank() }
                ?: entity.completedAt?.takeIf { it.isNotBlank() }
                ?: entity.scheduledAt
        Log.w(
            "SyncConflict",
            "Intervention ${entity.id} : modifications du $timestamp remplacées par version serveur v${entity.version}",
        )
    }

    fun ignoreResumeCandidate() {
        viewModelScope.launch {
            val entity = _resumeCandidate.value ?: return@launch
            syncRepository.abandonInterventionLocally(entity.id)
            _resumeCandidate.value = null
        }
    }

    fun resumeIntervention(onNavigate: (String) -> Unit) {
        val id = _resumeCandidate.value?.id ?: return
        _resumeCandidate.value = null
        onNavigate(id)
    }

    private suspend fun refreshResumeCandidate() {
        _resumeCandidate.value = syncRepository.getInProgressIntervention()
    }

    fun pull(force: Boolean = false, fromDateChange: Boolean = false) {
        viewModelScope.launch {
            _uiState.update {
                if (fromDateChange) {
                    it.copy(isDatePullRefreshing = true, errorMessage = null)
                } else {
                    it.copy(isSyncing = true, errorMessage = null)
                }
            }

            val selectedDate = _uiState.value.selectedDate
            val runResult =
                mobileSyncOrchestrator.runFullSync(
                    pullDate = selectedDate,
                    pullForce = force || fromDateChange,
                )

            when (val result = runResult.pullResult) {
                is SyncResult.Success, null -> {
                    _uiState.update { it.copy(isSyncing = false, isDatePullRefreshing = false) }
                    refreshResumeCandidate()
                }

                is SyncResult.Error -> {
                    val hasLocalData = hasLocalDataForDate(selectedDate)
                    _uiState.update {
                        it.copy(
                            isSyncing = false,
                            isDatePullRefreshing = false,
                            errorMessage = if (hasLocalData) null else result.message,
                        )
                    }
                }
            }
        }
    }

    private suspend fun hasLocalDataForDate(date: LocalDate): Boolean =
        syncRepository.hasCachedInterventionsForDate(date) ||
            pendingInterventionRepository.hasVisiblePendingForDate(date)

    fun selectDate(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date) }
        pull(fromDateChange = true)
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private companion object {
        val CONFLICT_SYNC_STATUSES = setOf("CONFLICT_IMMUTABLE", "CONFLICT_VERSION")

        fun catalogSyncErrorMessage(e: Exception): String = when {
            e is HttpException && e.code() == 401 ->
                "Clé catalogue refusée (HTTP 401). Rebuilder l’app release avec BAN_API_KEY " +
                    "identique à API_KEY sur ban.melchior.re (fichier local.properties)."
            else ->
                "Catalogue BAN : ${e.message ?: "échec de synchronisation"}"
        }
    }

    fun syncCatalog() {
        viewModelScope.launch {
            _uiState.update { it.copy(isCatalogSyncing = true, errorMessage = null) }
            try {
                Log.d("CatalogSync", "Démarrage sync catalogue (full)...")
                val result = catalogSyncRepository.sync(forceFull = true)
                tenantArticleSyncRepository.sync(force = true)
                Log.d(
                    "CatalogSync",
                    "Sync OK — nomenclature=${result.nomenclatureCount}, équipements=${result.equipmentCount}, full=${result.fullSync}",
                )
                val message = when {
                    result.equipmentCount == 0 && result.fullSync ->
                        "Catalogue synchronisé mais aucun appareil reçu — vérifiez l’API BAN (URL / clé x-api-key)."
                    result.equipmentCount == 0 ->
                        "Catalogue à jour (aucun nouvel appareil)."
                    else ->
                        "Catalogue : ${result.equipmentCount} appareil(s) synchronisé(s)."
                }
                _uiState.update { it.copy(errorMessage = message) }
            } catch (e: Exception) {
                Log.e("CatalogSync", "Erreur sync catalogue: ${e.message}", e)
                _uiState.update {
                    it.copy(errorMessage = catalogSyncErrorMessage(e))
                }
            } finally {
                _uiState.update { it.copy(isCatalogSyncing = false) }
            }
        }
    }
}
