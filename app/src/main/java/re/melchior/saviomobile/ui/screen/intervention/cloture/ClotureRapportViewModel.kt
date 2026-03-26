package re.melchior.saviomobile.ui.screen.intervention.cloture

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import re.melchior.saviomobile.data.local.entity.InterventionEntity
import re.melchior.saviomobile.data.repository.SyncRepository
import javax.inject.Inject

data class ClotureRapportUiState(
    val intervention: InterventionEntity? = null,
    val report: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val canProceed: Boolean = false
)

@HiltViewModel
class ClotureRapportViewModel @Inject constructor(
    private val syncRepository: SyncRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val interventionId: String = checkNotNull(savedStateHandle["interventionId"])

    private val _uiState = MutableStateFlow(ClotureRapportUiState())
    val uiState: StateFlow<ClotureRapportUiState> = _uiState.asStateFlow()

    init {
        loadIntervention()
    }

    private fun loadIntervention() {
        viewModelScope.launch {
            syncRepository.getInterventionById(interventionId)
                .collect { intervention ->
                    _uiState.update {
                        it.copy(
                            intervention = intervention,
                            // Pré-remplir si rapport déjà saisi
                            report = intervention?.report ?: it.report
                        )
                    }
                }
        }
    }

    fun onReportChange(text: String) {
        _uiState.update {
            it.copy(
                report = text,
                canProceed = text.isNotBlank()
            )
        }
    }

    suspend fun saveReport(): Boolean {
        val report = _uiState.value.report
        if (report.isBlank()) return false
        return try {
            syncRepository.saveReport(interventionId, report)
            true
        } catch (e: Exception) {
            _uiState.update { it.copy(errorMessage = e.message) }
            false
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}