package re.melchior.saviomobile.ui.screen.tournee

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import re.melchior.saviomobile.data.local.entity.InterventionEntity
import re.melchior.saviomobile.data.repository.SyncRepository
import re.melchior.saviomobile.data.repository.SyncResult
import re.melchior.saviomobile.worker.SyncWorker
import java.time.LocalDate
import javax.inject.Inject

data class TourneeUiState(
    val isLoading: Boolean = false,
    val isSyncing: Boolean = false,
    val errorMessage: String? = null,
    val selectedDate: LocalDate = LocalDate.now(),
    val pendingSyncCount: Int = 0
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TourneeViewModel @Inject constructor(
    private val syncRepository: SyncRepository,
    private val workManager: WorkManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(TourneeUiState())
    val uiState: StateFlow<TourneeUiState> = _uiState.asStateFlow()

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

    init {
        pull()
    }

    fun pull() {
        viewModelScope.launch {


            _uiState.update { it.copy(isSyncing = true, errorMessage = null) }

            android.util.Log.d("TourneeVM", "pull() démarrage syncRepo.pull")

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(constraints)
                .build()

            workManager.enqueue(syncRequest)

            when (val result = syncRepository.pull(_uiState.value.selectedDate)) {
                is SyncResult.Success -> {
                    android.util.Log.d("TourneeVM", "pull() success")
                    _uiState.update { it.copy(isSyncing = false) }
                }
                is SyncResult.Error -> {
                    android.util.Log.d("TourneeVM", "pull() error: ${result.message}")
                    _uiState.update {
                        it.copy(
                            isSyncing = false,
                            errorMessage = result.message
                        )
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
}