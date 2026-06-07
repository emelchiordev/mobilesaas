package re.melchior.saviomobile.ui.screen.intervention.create

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import re.melchior.saviomobile.data.local.dao.ReferentielDao
import re.melchior.saviomobile.data.local.dao.SettingsDao
import re.melchior.saviomobile.data.local.entity.InterventionTypeEntity
import re.melchior.saviomobile.data.remote.dto.CreateInterventionRequestDto
import re.melchior.saviomobile.data.remote.dto.CustomerSearchRowDto
import re.melchior.saviomobile.data.repository.CreateInterventionOutcome
import re.melchior.saviomobile.data.repository.CustomerSearchOutcome
import re.melchior.saviomobile.data.repository.InterventionCreateRepository
import re.melchior.saviomobile.data.repository.PlanningUpdateRepository
import re.melchior.saviomobile.util.InterventionTimeSlot
import re.melchior.saviomobile.util.MobilePlanningPermission
import java.time.LocalDate
import javax.inject.Inject

data class SelectedCustomerUi(
    val unitId: String,
    val customerId: String? = null,
    val displayName: String,
    val addressLine: String,
)

data class CreateInterventionUiState(
    val customerSearchQuery: String = "",
    val customerSearchResults: List<CustomerSearchRowDto> = emptyList(),
    val customerSearchLoading: Boolean = false,
    val customerSearchError: String? = null,
    val selectedCustomer: SelectedCustomerUi? = null,
    val interventionTypes: List<InterventionTypeEntity> = emptyList(),
    val selectedTypeCode: String? = null,
    val timeSlot: InterventionTimeSlot = InterventionTimeSlot.MATIN,
    val isUrgent: Boolean = false,
    val planningDateText: String = defaultPlanningDateText(),
    val planningTimeText: String = "",
    val planningPermission: MobilePlanningPermission = MobilePlanningPermission.LIMITED_EDIT,
    val notes: String = "",
    val isSubmitting: Boolean = false,
    val submitError: String? = null,
) {
    val previewScheduledAtMillis: Long
        get() {
            val date = runCatching { LocalDate.parse(planningDateText) }.getOrDefault(LocalDate.now())
            return buildPlanningScheduledAtMillis(date, timeSlot, planningTimeText)
        }
}

sealed interface CreateInterventionEvent {
    data class Created(val scheduledAtMillis: Long) : CreateInterventionEvent
    data object NavigateOffline : CreateInterventionEvent
}

@OptIn(FlowPreview::class)
@HiltViewModel
class CreateInterventionViewModel @Inject constructor(
    private val interventionCreateRepository: InterventionCreateRepository,
    private val planningUpdateRepository: PlanningUpdateRepository,
    private val referentielDao: ReferentielDao,
    private val settingsDao: SettingsDao,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        CreateInterventionUiState(
            selectedCustomer = preselectedCustomerFromNavArgs(savedStateHandle),
        ),
    )
    val uiState: StateFlow<CreateInterventionUiState> = _uiState.asStateFlow()

    val interventionTypes: StateFlow<List<InterventionTypeEntity>> =
        referentielDao.getCreateInterventionTypes()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _customerSearchInput = MutableStateFlow("")
    private val _events = Channel<CreateInterventionEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            _uiState.update {
                it.copy(planningPermission = planningUpdateRepository.currentPermission())
            }
        }

        viewModelScope.launch {
            interventionTypes.collect { types ->
                _uiState.update { state ->
                    state.copy(
                        interventionTypes = types,
                        selectedTypeCode = state.selectedTypeCode
                            ?: types.firstOrNull()?.code,
                    )
                }
            }
        }

        viewModelScope.launch {
            _customerSearchInput
                .debounce(300)
                .distinctUntilChanged()
                .collectLatest { q ->
                    val trimmed = q.trim()
                    if (trimmed.length < 2) {
                        _uiState.update {
                            it.copy(
                                customerSearchResults = emptyList(),
                                customerSearchLoading = false,
                                customerSearchError = null,
                            )
                        }
                        return@collectLatest
                    }
                    if (_uiState.value.selectedCustomer != null) return@collectLatest

                    _uiState.update {
                        it.copy(customerSearchLoading = true, customerSearchError = null)
                    }
                    when (val outcome = interventionCreateRepository.searchCustomers(trimmed)) {
                        is CustomerSearchOutcome.Success ->
                            _uiState.update {
                                it.copy(
                                    customerSearchResults = outcome.items,
                                    customerSearchLoading = false,
                                    customerSearchError = null,
                                )
                            }

                        is CustomerSearchOutcome.NetworkError ->
                            _uiState.update {
                                it.copy(
                                    customerSearchResults = emptyList(),
                                    customerSearchLoading = false,
                                    customerSearchError = "Hors ligne",
                                )
                            }

                        is CustomerSearchOutcome.Error ->
                            _uiState.update {
                                it.copy(
                                    customerSearchResults = emptyList(),
                                    customerSearchLoading = false,
                                    customerSearchError = outcome.message,
                                )
                            }
                    }
                }
        }
    }

    fun onCustomerSearchChange(value: String) {
        _uiState.update { it.copy(customerSearchQuery = value) }
        _customerSearchInput.value = value
    }

    fun selectCustomer(row: CustomerSearchRowDto) {
        _uiState.update {
            it.copy(
                selectedCustomer = SelectedCustomerUi(
                    unitId = row.unitId,
                    customerId = row.customerId,
                    displayName = row.resolvedDisplayName(),
                    addressLine = row.formattedAddress(),
                ),
                customerSearchQuery = "",
                customerSearchResults = emptyList(),
                customerSearchLoading = false,
                customerSearchError = null,
            )
        }
        _customerSearchInput.value = ""
    }

    fun clearSelectedCustomer() {
        _uiState.update { it.copy(selectedCustomer = null) }
    }

    fun selectType(code: String) {
        _uiState.update { it.copy(selectedTypeCode = code) }
    }

    fun onTimeSlotChange(timeSlot: InterventionTimeSlot) {
        _uiState.update { it.copy(timeSlot = timeSlot) }
    }

    fun onPlanningTimeChange(value: String) {
        _uiState.update { it.copy(planningTimeText = value) }
    }

    fun onPlanningDateChange(value: String) {
        _uiState.update { it.copy(planningDateText = value) }
    }

    fun onIsUrgentChange(value: Boolean) {
        _uiState.update { it.copy(isUrgent = value) }
    }

    fun onNotesChange(value: String) {
        _uiState.update { it.copy(notes = value) }
    }

    fun create() {
        val state = _uiState.value
        val customer = state.selectedCustomer
        val typeCode = state.selectedTypeCode
        when {
            customer == null -> {
                _uiState.update { it.copy(submitError = "Sélectionnez un client") }
                return
            }
            typeCode.isNullOrBlank() -> {
                _uiState.update { it.copy(submitError = "Sélectionnez un type d'intervention") }
                return
            }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, submitError = null) }
            val technicianId = settingsDao.getSettingsOnce()?.technicianId
            val notesTrimmed = state.notes.trim().takeIf { it.isNotEmpty() }
            val scheduledAtMillis = if (state.planningPermission == MobilePlanningPermission.READ_ONLY) {
                buildPlanningScheduledAtMillis(LocalDate.now(), InterventionTimeSlot.MATIN, "")
            } else {
                state.previewScheduledAtMillis
            }
            val timeSlot = if (state.planningPermission == MobilePlanningPermission.READ_ONLY) {
                InterventionTimeSlot.MATIN
            } else {
                state.timeSlot
            }
            val isUrgent = state.planningPermission != MobilePlanningPermission.READ_ONLY && state.isUrgent
            val body = CreateInterventionRequestDto(
                unitId = customer.unitId,
                technicianId = technicianId,
                interventionTypeId = typeCode,
                scheduledAt = scheduledAtToIso(scheduledAtMillis),
                timeSlot = timeSlot.wire,
                isUrgent = isUrgent,
                notes = notesTrimmed,
            )
            when (val outcome = interventionCreateRepository.createIntervention(body)) {
                is CreateInterventionOutcome.Success -> {
                    _uiState.update { it.copy(isSubmitting = false) }
                    _events.send(CreateInterventionEvent.Created(scheduledAtMillis))
                }

                is CreateInterventionOutcome.NetworkError -> {
                    _uiState.update { it.copy(isSubmitting = false) }
                    _events.send(CreateInterventionEvent.NavigateOffline)
                }

                is CreateInterventionOutcome.ServerError -> {
                    _uiState.update {
                        it.copy(isSubmitting = false, submitError = outcome.message)
                    }
                }
            }
        }
    }

    private companion object {
        fun preselectedCustomerFromNavArgs(savedStateHandle: SavedStateHandle): SelectedCustomerUi? {
            val unitId = savedStateHandle.navArg("unitId") ?: return null
            return SelectedCustomerUi(
                unitId = unitId,
                customerId = savedStateHandle.navArg("customerId"),
                displayName = savedStateHandle.navArg("displayName").orEmpty(),
                addressLine = savedStateHandle.navArg("addressLine").orEmpty(),
            )
        }

        private fun SavedStateHandle.navArg(key: String): String? =
            get<String>(key)?.takeIf { it.isNotBlank() }?.let(Uri::decode)
    }
}
