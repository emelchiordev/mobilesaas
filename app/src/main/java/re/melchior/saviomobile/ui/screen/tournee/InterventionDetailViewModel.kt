package re.melchior.saviomobile.ui.screen.intervention

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import re.melchior.saviomobile.data.local.entity.EquipmentEntity
import re.melchior.saviomobile.data.local.entity.InterventionEntity
import re.melchior.saviomobile.data.repository.SyncRepository
import javax.inject.Inject

data class InterventionDetailUiState(
    val intervention: InterventionEntity? = null,
    val equipments: List<EquipmentEntity> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class InterventionDetailViewModel @Inject constructor(
    private val syncRepository: SyncRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val interventionId: String = checkNotNull(savedStateHandle["interventionId"])

    private val _uiState = MutableStateFlow(InterventionDetailUiState())
    val uiState: StateFlow<InterventionDetailUiState> = _uiState.asStateFlow()

    init {
        loadIntervention()
        loadEquipments()
    }

    private fun loadIntervention() {
        viewModelScope.launch {
            syncRepository.getInterventionById(interventionId)
                .collect { intervention ->
                    _uiState.update { it.copy(intervention = intervention) }
                }
        }
    }

    private fun loadEquipments() {
        viewModelScope.launch {
            syncRepository.getEquipmentsByIntervention(interventionId)
                .collect { equipments ->
                    _uiState.update { it.copy(equipments = equipments) }
                }
        }
    }

    fun startIntervention() {
        viewModelScope.launch {
            syncRepository.startIntervention(interventionId)
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}