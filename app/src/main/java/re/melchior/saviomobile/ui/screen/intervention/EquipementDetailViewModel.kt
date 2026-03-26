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
import re.melchior.saviomobile.data.repository.SyncRepository
import javax.inject.Inject

data class EquipementDetailUiState(
    val equipment: EquipmentEntity? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class EquipementDetailViewModel @Inject constructor(
    private val syncRepository: SyncRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val equipmentId: String = checkNotNull(savedStateHandle["equipmentId"])

    private val _uiState = MutableStateFlow(EquipementDetailUiState())
    val uiState: StateFlow<EquipementDetailUiState> = _uiState.asStateFlow()

    init {
        loadEquipment()
    }

    private fun loadEquipment() {
        viewModelScope.launch {
            syncRepository.getEquipmentById(equipmentId)
                .collect { equipment ->
                    _uiState.update { it.copy(equipment = equipment) }
                }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}