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
import re.melchior.saviomobile.data.local.entity.InterventionHistoryEntity
import re.melchior.saviomobile.data.remote.api.CustomerApi
import re.melchior.saviomobile.data.remote.api.DocumentApi
import re.melchior.saviomobile.data.repository.PendingUpdateRepository
import re.melchior.saviomobile.data.repository.SyncRepository
import javax.inject.Inject

data class ClientDetailUiState(
    val customerId: String = "",
    val unitId: String = "",
    val displayName: String = "",
    val addressLine: String = "",
    val intervention: InterventionEntity? = null,
    val isLoading: Boolean = true,
    val history: List<InterventionHistoryEntity> = emptyList(),
    val historyPhotoUrls: Map<String, List<String>> = emptyMap(),
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val savedSuccess: Boolean = false,
    val errorMessage: String? = null,
    val emailError: String? = null,
    val phone: String = "",
    val email: String = "",
    val notes: String = "",
    val floor: String = "",
    val doorCode: String = "",
    val addressLine2: String = "",
    val updatesRequireValidation: Boolean = false,
    val contextInterventionId: String = "",
) {
    val titleName: String
        get() {
            intervention?.let { inv ->
                val fromIntervention =
                    "${inv.customerFirstName.orEmpty()} ${inv.customerLastName.orEmpty()}".trim()
                if (fromIntervention.isNotBlank()) return fromIntervention
            }
            return displayName.ifBlank { "Fiche client" }
        }

    val resolvedUnitId: String
        get() = intervention?.unitId?.takeIf { it.isNotBlank() } ?: unitId

    val canShowContent: Boolean
        get() = intervention != null || displayName.isNotBlank()

    val showNewInterventionCta: Boolean
        get() =
            contextInterventionId.isBlank() &&
                resolvedUnitId.isNotBlank() &&
                !isEditing
}

@HiltViewModel
class ClientDetailViewModel @Inject constructor(
    private val syncRepository: SyncRepository,
    private val pendingUpdateRepository: PendingUpdateRepository,
    private val settingsDao: SettingsDao,
    private val customerApi: CustomerApi,
    private val documentApi: DocumentApi,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val customerId: String = checkNotNull(savedStateHandle["customerId"])
    private val navUnitId: String = savedStateHandle.navArg("unitId").orEmpty()
    private val navDisplayName: String = savedStateHandle.navArg("displayName").orEmpty()
    private val navAddressLine: String = savedStateHandle.navArg("addressLine").orEmpty()
    private val contextInterventionId: String =
        savedStateHandle.navArg("contextInterventionId").orEmpty()

    private val _uiState = MutableStateFlow(
        ClientDetailUiState(
            customerId = customerId,
            unitId = navUnitId,
            displayName = navDisplayName,
            addressLine = navAddressLine,
            contextInterventionId = contextInterventionId,
        ),
    )
    val uiState: StateFlow<ClientDetailUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val settings = settingsDao.getSettingsOnce()
            _uiState.update {
                it.copy(
                    updatesRequireValidation = settings?.updatesRequireValidation ?: false,
                    isLoading = true,
                )
            }

            if (navUnitId.isNotBlank()) {
                val history = syncRepository.getHistoryForUnit(navUnitId)
                _uiState.update { it.copy(history = history) }
                loadHistoryPhotoUrls(history)
            }

            try {
                val customer = customerApi.getCustomer(customerId)
                val customerOverride = pendingUpdateRepository.getLatestCustomerOverride(customerId)
                _uiState.update { state ->
                    state.copy(
                        displayName = state.displayName.ifBlank {
                            listOfNotNull(customer.firstName, customer.lastName)
                                .joinToString(" ")
                                .trim()
                        },
                        phone = customerOverride?.get("phone") as? String
                            ?: customer.phone.orEmpty(),
                        email = customerOverride?.get("email") as? String
                            ?: customer.email.orEmpty(),
                        notes = customerOverride?.get("notes") as? String
                            ?: customer.notes.orEmpty(),
                    )
                }
            } catch (_: Exception) {
                // Données de navigation ou intervention locale suffisent hors ligne.
            }

            launch {
                syncRepository.getInterventionByCustomerId(customerId).collect { intervention ->
                    if (intervention == null) {
                        _uiState.update { it.copy(isLoading = false) }
                        return@collect
                    }
                    applyInterventionData(intervention)
                }
            }
        }
    }

    private suspend fun applyInterventionData(intervention: InterventionEntity) {
        val customerOverride = pendingUpdateRepository.getLatestCustomerOverride(customerId)
        val unitOverride = pendingUpdateRepository.getLatestUnitOverride(intervention.unitId)
        val effectiveUnitId = navUnitId.ifBlank { intervention.unitId }

        if (effectiveUnitId.isNotBlank() && effectiveUnitId != _uiState.value.unitId) {
            val history = syncRepository.getHistoryForUnit(effectiveUnitId)
            _uiState.update { it.copy(history = history, unitId = effectiveUnitId) }
            loadHistoryPhotoUrls(history)
        }

        _uiState.update { state ->
            state.copy(
                intervention = intervention,
                unitId = effectiveUnitId.ifBlank { intervention.unitId },
                displayName = state.displayName.ifBlank {
                    "${intervention.customerFirstName.orEmpty()} ${intervention.customerLastName.orEmpty()}".trim()
                },
                addressLine = state.addressLine.ifBlank {
                    listOf(
                        intervention.unitStreet,
                        "${intervention.unitPostalCode} ${intervention.unitCity}".trim(),
                    ).filter { it.isNotBlank() }.joinToString(", ")
                },
                phone = customerOverride?.get("phone") as? String
                    ?: intervention.customerPhone.orEmpty(),
                email = customerOverride?.get("email") as? String
                    ?: intervention.customerEmail.orEmpty(),
                notes = customerOverride?.get("notes") as? String ?: state.notes,
                floor = unitOverride?.get("floor") as? String
                    ?: intervention.unitFloor.orEmpty(),
                doorCode = unitOverride?.get("doorCode") as? String
                    ?: intervention.unitDoorCode.orEmpty(),
                addressLine2 = unitOverride?.get("addressLine2") as? String
                    ?: intervention.unitAddressLine2.orEmpty(),
                isLoading = false,
            )
        }
    }

    private fun loadHistoryPhotoUrls(history: List<InterventionHistoryEntity>) {
        viewModelScope.launch {
            val urlMap = mutableMapOf<String, List<String>>()
            history.forEach { item ->
                if (item.photoKeys.isNullOrBlank()) return@forEach
                try {
                    val keys = com.google.gson.Gson()
                        .fromJson(item.photoKeys, Array<String>::class.java)
                        ?: return@forEach
                    val urls = keys.mapNotNull { key ->
                        try {
                            documentApi.getSignedUrl(key).url
                        } catch (_: Exception) {
                            null
                        }
                    }
                    if (urls.isNotEmpty()) urlMap[item.id] = urls
                } catch (_: Exception) {
                }
            }
            _uiState.update { it.copy(historyPhotoUrls = urlMap) }
        }
    }

    fun startEditing() {
        _uiState.update { it.copy(isEditing = true) }
    }

    fun cancelEditing() {
        _uiState.update { it.copy(isEditing = false) }
        loadData()
    }

    fun onPhoneChange(value: String) = _uiState.update { it.copy(phone = value) }
    fun onEmailChange(value: String) = _uiState.update { it.copy(email = value, emailError = null) }
    fun onNotesChange(value: String) = _uiState.update { it.copy(notes = value) }
    fun onFloorChange(value: String) = _uiState.update { it.copy(floor = value) }
    fun onDoorCodeChange(value: String) = _uiState.update { it.copy(doorCode = value) }
    fun onAddressLine2Change(value: String) = _uiState.update { it.copy(addressLine2 = value) }

    fun saveChanges() {
        val state = _uiState.value
        val unitId = state.resolvedUnitId
        if (unitId.isBlank()) return

        val emailTrim = state.email.trim()
        if (emailTrim.isNotEmpty() &&
            !android.util.Patterns.EMAIL_ADDRESS.matcher(emailTrim).matches()
        ) {
            _uiState.update { it.copy(emailError = "Adresse e-mail invalide") }
            return
        }
        _uiState.update { it.copy(emailError = null) }

        val baselinePhone = state.intervention?.customerPhone.orEmpty()
        val baselineEmail = state.intervention?.customerEmail.orEmpty()
        val baselineFloor = state.intervention?.unitFloor.orEmpty()
        val baselineDoorCode = state.intervention?.unitDoorCode.orEmpty()
        val baselineAddressLine2 = state.intervention?.unitAddressLine2.orEmpty()

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            try {
                pendingUpdateRepository.updateCustomer(
                    customerId = customerId,
                    phone = state.phone.takeIf { it != baselinePhone },
                    email = state.email.takeIf { it != baselineEmail },
                    notes = state.notes.takeIf { it.isNotBlank() },
                )

                pendingUpdateRepository.updateUnitAccess(
                    unitId = unitId,
                    floor = state.floor.takeIf { it != baselineFloor },
                    doorCode = state.doorCode.takeIf { it != baselineDoorCode },
                    addressLine2 = state.addressLine2.takeIf { it != baselineAddressLine2 },
                )

                _uiState.update {
                    it.copy(
                        isSaving = false,
                        isEditing = false,
                        savedSuccess = true,
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = e.message ?: "Erreur lors de la sauvegarde",
                    )
                }
            }
        }
    }

    fun dismissError() = _uiState.update { it.copy(errorMessage = null) }
    fun dismissSuccess() = _uiState.update { it.copy(savedSuccess = false) }

    private companion object {
        fun SavedStateHandle.navArg(key: String): String? =
            get<String>(key)?.takeIf { it.isNotBlank() }
    }
}
