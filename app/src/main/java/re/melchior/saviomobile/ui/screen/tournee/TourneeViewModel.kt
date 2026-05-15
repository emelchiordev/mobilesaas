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
import re.melchior.saviomobile.data.repository.CatalogSyncRepository
import re.melchior.saviomobile.data.repository.InvoiceRepository
import re.melchior.saviomobile.data.repository.PendingInterventionRepository
import re.melchior.saviomobile.data.repository.PhotoSyncRepository
import re.melchior.saviomobile.data.repository.SyncRepository
import re.melchior.saviomobile.data.repository.SyncResult
import java.time.LocalDate
import javax.inject.Inject

data class TourneeUiState(
    val isLoading: Boolean = false,
    val isSyncing: Boolean = false,
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
    private val invoiceRepository: InvoiceRepository,
    private val photoSyncRepository: PhotoSyncRepository,
    private val pendingInterventionRepository: PendingInterventionRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TourneeUiState())
    val uiState: StateFlow<TourneeUiState> = _uiState.asStateFlow()

    private val _resumeCandidate = MutableStateFlow<InterventionEntity?>(null)
    val resumeCandidate: StateFlow<InterventionEntity?> = _resumeCandidate.asStateFlow()

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

    init {
        viewModelScope.launch {
            _resumeCandidate.value = syncRepository.getInProgressIntervention()
        }
        pull()
    }

    fun ignoreResumeCandidate() {
        viewModelScope.launch {
            val entity = _resumeCandidate.value ?: return@launch
            syncRepository.abandonInterventionLocally(entity.id)
            invoiceRepository.deleteDraftByIntervention(entity.id)
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

    fun pull(force: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true, errorMessage = null) }

            // Upload photos en attente immédiatement — fire and forget
            launch {
                try {
                    photoSyncRepository.uploadPendingPhotos()
                    photoSyncRepository.deletePendingPhotos()
                    photoSyncRepository.uploadPendingSignatures()
                    android.util.Log.d("TourneeVM", "Photos sync terminée")
                } catch (e: Exception) {
                    android.util.Log.w("TourneeVM", "Photos sync error: ${e.message}")
                }
            }

            // Pull interventions
            when (val result = syncRepository.pull(_uiState.value.selectedDate, force)) {
                is SyncResult.Success -> {
                    _uiState.update { it.copy(isSyncing = false) }
                    refreshResumeCandidate()
                }

                is SyncResult.Error -> {
                    _uiState.update {
                        it.copy(isSyncing = false, errorMessage = result.message)
                    }
                }
            }
        }
    }

    fun selectDate(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date) }
        pull()
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun syncCatalog() {
        viewModelScope.launch {
            _uiState.update { it.copy(isCatalogSyncing = true) }
            try {
                android.util.Log.d("CatalogSync", "Démarrage sync catalogue...")
                catalogSyncRepository.sync()
                android.util.Log.d("CatalogSync", "Sync catalogue terminée avec succès")
            } catch (e: Exception) {
                android.util.Log.e("CatalogSync", "Erreur sync catalogue: ${e.message}", e)
            } finally {
                _uiState.update { it.copy(isCatalogSyncing = false) }
            }
        }
    }
}
