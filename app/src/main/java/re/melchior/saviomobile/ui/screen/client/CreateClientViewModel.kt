package re.melchior.saviomobile.ui.screen.client

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest
import re.melchior.saviomobile.data.remote.dto.CreateClientLogementDto
import re.melchior.saviomobile.data.remote.dto.CreateClientOccupancyDto
import re.melchior.saviomobile.data.remote.dto.CreateTenantClientRequestDto
import re.melchior.saviomobile.data.repository.BanAddressPick
import re.melchior.saviomobile.data.repository.BanAddressSearchRepository
import re.melchior.saviomobile.data.repository.ClientsRepository
import re.melchior.saviomobile.data.repository.CreateClientResult
import java.time.LocalDate
import java.time.format.DateTimeFormatter

enum class ClientCivilityUi(val apiValue: String, val label: String) {
    M("M", "M."),
    MME("Mme", "Mme"),
}

enum class HousingKindUi(val unitType: String, val unitCategory: String?, val label: String) {
    MAISON("house", "individual", "Maison individuelle"),
    APPARTEMENT("apartment", "individual", "Appartement"),
    IMMEUBLE_COLLECTIF("building", "collective", "Immeuble collectif"),
}

enum class ClientAddressEntryMode {
    BAN,
    MANUAL,
}

data class CreateClientUiState(
    val addressEntryMode: ClientAddressEntryMode = ClientAddressEntryMode.BAN,
    val civility: ClientCivilityUi = ClientCivilityUi.M,
    val firstName: String = "",
    val lastName: String = "",
    val phone: String = "",
    val email: String = "",
    val addressSearchText: String = "",
    val streetResolved: String = "",
    val postalCode: String = "",
    val city: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val addressComplement: String = "",
    val housingKind: HousingKindUi = HousingKindUi.MAISON,
    val floor: String = "",
    val banSuggestions: List<BanAddressPick> = emptyList(),
    val banLoading: Boolean = false,
    val banSearchError: Boolean = false,
    val isSubmitting: Boolean = false,
    val fieldErrors: Map<String, String> = emptyMap(),
    val submitError: String? = null,
)

sealed interface CreateClientEvent {
    data object Created : CreateClientEvent
}

@OptIn(FlowPreview::class)
@HiltViewModel
class CreateClientViewModel @Inject constructor(
    private val banAddressSearchRepository: BanAddressSearchRepository,
    private val clientsRepository: ClientsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateClientUiState())
    val uiState: StateFlow<CreateClientUiState> = _uiState.asStateFlow()

    private val _addressSearchInput = MutableStateFlow("")
    private val _events = Channel<CreateClientEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            _addressSearchInput
                .debounce(300)
                .distinctUntilChanged()
                .collectLatest { q ->
                    val trimmed = q.trim()
                    if (trimmed.length < 3) {
                        _uiState.update {
                            it.copy(
                                banSuggestions = emptyList(),
                                banLoading = false,
                                banSearchError = false,
                            )
                        }
                        return@collectLatest
                    }
                    if (_uiState.value.addressEntryMode != ClientAddressEntryMode.BAN) {
                        return@collectLatest
                    }
                    _uiState.update { it.copy(banLoading = true, banSearchError = false) }
                    val result = banAddressSearchRepository.searchResult(trimmed)
                    result.fold(
                        onSuccess = { list ->
                            _uiState.update {
                                it.copy(
                                    banSuggestions = list,
                                    banLoading = false,
                                    banSearchError = false,
                                )
                            }
                        },
                        onFailure = {
                            _uiState.update {
                                it.copy(
                                    banSuggestions = emptyList(),
                                    banLoading = false,
                                    banSearchError = true,
                                )
                            }
                        },
                    )
                }
        }
    }

    fun onAddressSearchTextChange(value: String) {
        _uiState.update {
            it.copy(
                addressSearchText = value,
                streetResolved = "",
                postalCode = "",
                city = "",
                latitude = null,
                longitude = null,
                submitError = null,
                banSearchError = false,
                fieldErrors = it.fieldErrors - "address",
            )
        }
        _addressSearchInput.value = value
    }

    fun enterManualAddressMode() {
        val s = _uiState.value
        val hint = s.addressSearchText.trim()
        _uiState.update {
            it.copy(
                addressEntryMode = ClientAddressEntryMode.MANUAL,
                addressSearchText = "",
                banSuggestions = emptyList(),
                banLoading = false,
                banSearchError = false,
                streetResolved = if (it.streetResolved.isBlank() && hint.isNotEmpty()) hint else it.streetResolved,
                postalCode = it.postalCode,
                city = if (it.city == "—") "" else it.city,
                latitude = null,
                longitude = null,
                submitError = null,
                fieldErrors = it.fieldErrors - "address",
            )
        }
        _addressSearchInput.value = ""
    }

    fun returnToBanSearchMode() {
        _uiState.update {
            it.copy(
                addressEntryMode = ClientAddressEntryMode.BAN,
                streetResolved = "",
                postalCode = "",
                city = "",
                latitude = null,
                longitude = null,
                banSuggestions = emptyList(),
                banSearchError = false,
                submitError = null,
                fieldErrors = it.fieldErrors - "address",
            )
        }
        _addressSearchInput.value = ""
    }

    fun onManualStreetChange(v: String) {
        _uiState.update {
            it.copy(
                streetResolved = v,
                latitude = null,
                longitude = null,
                submitError = null,
                fieldErrors = it.fieldErrors - "address",
            )
        }
    }

    fun onManualPostalChange(v: String) {
        _uiState.update {
            it.copy(postalCode = v, submitError = null, fieldErrors = it.fieldErrors - "address")
        }
    }

    fun onManualCityChange(v: String) {
        _uiState.update {
            it.copy(city = v, submitError = null, fieldErrors = it.fieldErrors - "address")
        }
    }

    fun onSelectBanSuggestion(pick: BanAddressPick) {
        _uiState.update {
            it.copy(
                addressSearchText = pick.label,
                streetResolved = pick.street,
                postalCode = pick.postalCode,
                city = pick.city,
                latitude = pick.latitude,
                longitude = pick.longitude,
                banSuggestions = emptyList(),
                banSearchError = false,
                submitError = null,
                fieldErrors = it.fieldErrors - "address",
            )
        }
        _addressSearchInput.value = ""
    }

    fun onCivilityChange(c: ClientCivilityUi) {
        _uiState.update { it.copy(civility = c, submitError = null) }
    }

    fun onFirstNameChange(v: String) {
        _uiState.update { it.copy(firstName = v, submitError = null, fieldErrors = it.fieldErrors - "firstName") }
    }

    fun onLastNameChange(v: String) {
        _uiState.update { it.copy(lastName = v, submitError = null, fieldErrors = it.fieldErrors - "lastName") }
    }

    fun onPhoneChange(v: String) {
        _uiState.update { it.copy(phone = v, submitError = null) }
    }

    fun onEmailChange(v: String) {
        _uiState.update { it.copy(email = v, submitError = null) }
    }

    fun onAddressComplementChange(v: String) {
        _uiState.update { it.copy(addressComplement = v, submitError = null) }
    }

    fun onHousingKindChange(k: HousingKindUi) {
        _uiState.update {
            it.copy(
                housingKind = k,
                floor = if (k != HousingKindUi.APPARTEMENT) "" else it.floor,
                submitError = null,
            )
        }
    }

    fun onFloorChange(v: String) {
        _uiState.update { it.copy(floor = v, submitError = null) }
    }

    fun submit() {
        val s = _uiState.value
        val errors = mutableMapOf<String, String>()
        if (s.firstName.isBlank()) errors["firstName"] = "Prénom requis"
        if (s.lastName.isBlank()) errors["lastName"] = "Nom requis"
        val street = s.streetResolved.trim()
        if (street.isBlank() || s.postalCode.isBlank() || s.city.isBlank() || s.city.trim() == "—") {
            errors["address"] =
                if (s.addressEntryMode == ClientAddressEntryMode.MANUAL) {
                    "Renseignez le numéro et la voie, le code postal et la ville."
                } else {
                    "Sélectionnez une adresse dans les suggestions BAN ou saisissez-la manuellement."
                }
        }

        if (errors.isNotEmpty()) {
            _uiState.update { it.copy(fieldErrors = errors, submitError = null) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, submitError = null, fieldErrors = emptyMap()) }
            val logement =
                CreateClientLogementDto(
                    street = street,
                    postalCode = s.postalCode.trim(),
                    city = s.city.trim(),
                    latitude = s.latitude,
                    longitude = s.longitude,
                    addressLine2 = s.addressComplement.trim().takeIf { it.isNotEmpty() },
                    floor =
                        if (s.housingKind == HousingKindUi.APPARTEMENT) {
                            s.floor.trim().takeIf { it.isNotEmpty() }
                        } else {
                            null
                        },
                    unitType = s.housingKind.unitType,
                    unitCategory = s.housingKind.unitCategory,
                )
            val body =
                CreateTenantClientRequestDto(
                    firstName = s.firstName.trim(),
                    lastName = s.lastName.trim(),
                    civility = s.civility.apiValue,
                    phone = s.phone.trim().takeIf { it.isNotEmpty() },
                    email = s.email.trim().takeIf { it.isNotEmpty() },
                    logement = logement,
                    occupancy =
                        CreateClientOccupancyDto(
                            role = "tenant",
                            startDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
                        ),
                )
            when (val r = clientsRepository.createClient(body)) {
                is CreateClientResult.Success -> {
                    _uiState.update { it.copy(isSubmitting = false) }
                    _events.send(CreateClientEvent.Created)
                }

                is CreateClientResult.Error -> {
                    _uiState.update { it.copy(isSubmitting = false, submitError = r.message) }
                }
            }
        }
    }
}
