package re.savio.mobile.ui.screen.intervention.cerfa

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import re.savio.mobile.data.local.CerfaPdfService
import re.savio.mobile.data.local.dao.ColdMeasureDao
import re.savio.mobile.data.local.dao.EquipmentDao
import re.savio.mobile.data.local.dao.InterventionDao
import re.savio.mobile.data.local.dao.SettingsDao

@HiltViewModel
class CerfaViewModel @Inject constructor(
    private val cerfaPdfService: CerfaPdfService,
    private val coldMeasureDao: ColdMeasureDao,
    private val equipmentDao: EquipmentDao,
    private val interventionDao: InterventionDao,
    private val settingsDao: SettingsDao,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val interventionId: String =
        savedStateHandle["interventionId"] ?: ""
    private val equipmentId: String =
        savedStateHandle["equipmentId"] ?: ""

    sealed class UiState {
        object Loading : UiState()
        data class Success(val file: File) : UiState()
        data class Error(val message: String) : UiState()
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        generate()
    }

    fun generate() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val measure = coldMeasureDao.getByInterventionAndEquipment(
                    interventionId,
                    equipmentId,
                )
                val equipment = equipmentDao.getEquipmentByServerIdOnce(equipmentId)
                val intervention = interventionDao.getInterventionByIdOnce(interventionId)
                val settings = settingsDao.getSettingsOnce()

                if (measure == null || equipment == null || intervention == null) {
                    _uiState.value = UiState.Error("Données introuvables")
                    return@launch
                }

                val technicienNom = listOfNotNull(
                    settings?.technicianFirstName?.trim()?.takeIf { it.isNotEmpty() },
                    settings?.technicianLastName?.trim()?.takeIf { it.isNotEmpty() },
                ).joinToString(" ")

                val result = cerfaPdfService.generate(
                    measure,
                    intervention,
                    equipment,
                    technicienNom = technicienNom,
                    attestationCapacite = settings?.refrigerantCapacityAttestationNumber.orEmpty(),
                )

                result.fold(
                    onSuccess = { file ->
                        _uiState.value = UiState.Success(file)
                    },
                    onFailure = { e ->
                        _uiState.value = UiState.Error(
                            e.message ?: "Erreur génération PDF",
                        )
                    },
                )
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Erreur")
            }
        }
    }
}
