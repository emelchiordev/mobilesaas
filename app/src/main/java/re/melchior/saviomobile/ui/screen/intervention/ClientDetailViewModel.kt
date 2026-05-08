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
import re.melchior.saviomobile.data.local.dao.SettingsDao
import re.melchior.saviomobile.data.local.entity.InterventionEntity
import re.melchior.saviomobile.data.repository.PendingUpdateRepository
import re.melchior.saviomobile.data.repository.SyncRepository
import javax.inject.Inject

data class ClientDetailUiState(
    val intervention: InterventionEntity? = null,
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val savedSuccess: Boolean = false,
    val errorMessage: String? = null,
    val emailError: String? = null,
    // Champs éditables
    val phone: String = "",
    val email: String = "",
    val notes: String = "",
    val floor: String = "",
    val doorCode: String = "",
    val addressLine2: String = "",
    // Indique si des modifs sont en attente de validation
    val updatesRequireValidation: Boolean = false
)

@HiltViewModel
class ClientDetailViewModel @Inject constructor(
    private val syncRepository: SyncRepository,
    private val pendingUpdateRepository: PendingUpdateRepository,
    private val settingsDao: SettingsDao,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val customerId: String = checkNotNull(savedStateHandle["customerId"])

    private val _uiState = MutableStateFlow(ClientDetailUiState())
    val uiState: StateFlow<ClientDetailUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            // Charger les settings pour savoir si validation requise
            val settings = settingsDao.getSettingsOnce()
            _uiState.update {
                it.copy(
                    updatesRequireValidation = settings?.updatesRequireValidation ?: false
                )
            }

            // Chercher l'intervention liée à ce client
            syncRepository.getInterventionByCustomerId(customerId)
                .collect { intervention ->
                    intervention?.let { inv ->
                        // Appliquer les overrides locaux si présents
                        val customerOverride = pendingUpdateRepository
                            .getLatestCustomerOverride(customerId)
                        val unitOverride = inv.unitId.let { uid ->
                            pendingUpdateRepository.getLatestUnitOverride(uid)
                        }

                        _uiState.update { state ->
                            state.copy(
                                intervention = inv,
                                phone = customerOverride?.get("phone") as? String
                                    ?: inv.customerPhone ?: "",
                                email = customerOverride?.get("email") as? String
                                    ?: inv.customerEmail ?: "",
                                notes = customerOverride?.get("notes") as? String ?: "",
                                floor = unitOverride?.get("floor") as? String
                                    ?: inv.unitFloor ?: "",
                                doorCode = unitOverride?.get("doorCode") as? String
                                    ?: inv.unitDoorCode ?: "",
                                addressLine2 = unitOverride?.get("addressLine2") as? String
                                    ?: inv.unitAddressLine2 ?: ""
                            )
                        }
                    }
                }
        }
    }

    fun startEditing() {
        _uiState.update { it.copy(isEditing = true) }
    }

    fun cancelEditing() {
        _uiState.update { it.copy(isEditing = false) }
        loadData() // Recharger les valeurs originales
    }

    fun onPhoneChange(value: String) = _uiState.update { it.copy(phone = value) }
    fun onEmailChange(value: String) = _uiState.update { it.copy(email = value, emailError = null) }
    fun onNotesChange(value: String) = _uiState.update { it.copy(notes = value) }
    fun onFloorChange(value: String) = _uiState.update { it.copy(floor = value) }
    fun onDoorCodeChange(value: String) = _uiState.update { it.copy(doorCode = value) }
    fun onAddressLine2Change(value: String) = _uiState.update { it.copy(addressLine2 = value) }

    fun saveChanges() {
        val state = _uiState.value
        val intervention = state.intervention ?: return

        val emailTrim = state.email.trim()
        if (emailTrim.isNotEmpty() &&
            !android.util.Patterns.EMAIL_ADDRESS.matcher(emailTrim).matches()
        ) {
            _uiState.update { it.copy(emailError = "Adresse e-mail invalide") }
            return
        }
        _uiState.update { it.copy(emailError = null) }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            try {
                // Sauvegarder modifications client
                pendingUpdateRepository.updateCustomer(
                    customerId = customerId,
                    phone = state.phone.takeIf { it != (intervention.customerPhone ?: "") },
                    email = state.email.takeIf { it != (intervention.customerEmail ?: "") },
                    notes = state.notes.takeIf { it.isNotBlank() }
                )

                // Sauvegarder modifications accès logement
                pendingUpdateRepository.updateUnitAccess(
                    unitId = intervention.unitId,
                    floor = state.floor.takeIf { it != (intervention.unitFloor ?: "") },
                    doorCode = state.doorCode.takeIf { it != (intervention.unitDoorCode ?: "") },
                    addressLine2 = state.addressLine2.takeIf {
                        it != (intervention.unitAddressLine2 ?: "")
                    }
                )

                _uiState.update {
                    it.copy(
                        isSaving = false,
                        isEditing = false,
                        savedSuccess = true
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = e.message ?: "Erreur lors de la sauvegarde"
                    )
                }
            }
        }
    }

    fun dismissError() = _uiState.update { it.copy(errorMessage = null) }
    fun dismissSuccess() = _uiState.update { it.copy(savedSuccess = false) }
}