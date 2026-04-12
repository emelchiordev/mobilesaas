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
import re.melchior.saviomobile.data.remote.api.TourneeApi
import re.melchior.saviomobile.data.remote.dto.InterventionTypeDto
import re.melchior.saviomobile.data.remote.dto.stableKey
import re.melchior.saviomobile.data.repository.SyncRepository
import javax.inject.Inject

data class ClotureRapportUiState(
    val intervention: InterventionEntity? = null,
    val report: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val closeTypes: List<InterventionTypeDto> = emptyList(),
    val closeTypesLoading: Boolean = true,
    val closeTypesError: String? = null,
    val selectedCloseTypes: List<InterventionTypeDto> = emptyList()
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
}

@HiltViewModel
class ClotureRapportViewModel @Inject constructor(
    private val syncRepository: SyncRepository,
    private val tourneeApi: TourneeApi,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val interventionId: String = checkNotNull(savedStateHandle["interventionId"])

    private val _uiState = MutableStateFlow(ClotureRapportUiState())
    val uiState: StateFlow<ClotureRapportUiState> = _uiState.asStateFlow()

    init {
        loadIntervention()
        loadCloseTypes()
    }

    private fun loadIntervention() {
        viewModelScope.launch {
            syncRepository.getInterventionById(interventionId)
                .collect { intervention ->
                    _uiState.update {
                        it.copy(
                            intervention = intervention,
                            report = intervention?.report ?: it.report
                        )
                    }
                    tryInitCloseTypeSelection()
                }
        }
    }

    private fun loadCloseTypes() {
        viewModelScope.launch {
            try {
                val types = tourneeApi.getInterventionTypesForClose(showOnClose = true)
                _uiState.update {
                    it.copy(
                        closeTypes = types,
                        closeTypesLoading = false,
                        closeTypesError = null
                    )
                }
                tryInitCloseTypeSelection()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        closeTypesLoading = false,
                        closeTypesError = e.message ?: "Impossible de charger les types d'intervention"
                    )
                }
            }
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
    }

    fun onReportChange(text: String) {
        _uiState.update { it.copy(report = text) }
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
    types: List<InterventionTypeDto>
): InterventionTypeDto {
    intervention.interventionTypeId?.let { id ->
        types.find { it.id == id }?.let { return it }
    }
    types.find { it.code == intervention.typeCode }?.let { return it }
    return types.first()
}
