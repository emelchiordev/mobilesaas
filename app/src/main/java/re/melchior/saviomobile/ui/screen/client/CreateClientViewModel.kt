package re.melchior.saviomobile.ui.screen.client

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest
import re.melchior.saviomobile.data.local.dao.ReferentielDao
import re.melchior.saviomobile.data.local.entity.PendingClientEntity
import re.melchior.saviomobile.data.remote.dto.CreateClientLogementDto
import re.melchior.saviomobile.data.remote.dto.CreateClientOccupancyDto
import re.melchior.saviomobile.data.remote.dto.CreateTenantClientRequestDto
import re.melchior.saviomobile.data.repository.BanAddressPick
import re.melchior.saviomobile.data.repository.BanAddressSearchRepository
import re.melchior.saviomobile.data.repository.ClientsRepository
import re.melchior.saviomobile.data.repository.CreateClientResult
import re.melchior.saviomobile.data.repository.PendingClientRepository
import re.melchior.saviomobile.ui.utils.NetworkUtils
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

enum class ClientAddressEntryMode {
    BAN,
    MANUAL,
}

data class CreateClientUiState(
    val addressEntryMode: ClientAddressEntryMode = ClientAddressEntryMode.BAN,
    val civilityOptions: List<CivilityOptionUi> = emptyList(),
    val unitTypeOptions: List<UnitTypeOptionUi> = emptyList(),
    val selectedCivilityCode: String? = null,
    val selectedUnitTypeCode: String? = null,
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
    val floor: String = "",
    val banSuggestions: List<BanAddressPick> = emptyList(),
    val banLoading: Boolean = false,
    val banSearchError: Boolean = false,
    val isBanAddressSelected: Boolean = false,
    val isSubmitting: Boolean = false,
    val fieldErrors: Map<String, String> = emptyMap(),
    val submitError: String? = null,
) {
    val selectedUnitType: UnitTypeOptionUi?
        get() = unitTypeOptions.firstOrNull { it.code == selectedUnitTypeCode }

    val showsFloorField: Boolean
        get() = selectedUnitType?.showsFloorField() == true
}

sealed interface CreateClientEvent {
    data class Created(
        val customerId: String,
        val unitId: String,
        val displayName: String,
        val addressLine: String,
    ) : CreateClientEvent

    data object SavedOffline : CreateClientEvent
}

@OptIn(FlowPreview::class)
@HiltViewModel
class CreateClientViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val banAddressSearchRepository: BanAddressSearchRepository,
    private val clientsRepository: ClientsRepository,
    private val pendingClientRepository: PendingClientRepository,
    private val referentielDao: ReferentielDao,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateClientUiState())
    val uiState: StateFlow<CreateClientUiState> = _uiState.asStateFlow()

    private val _addressSearchInput = MutableStateFlow("")
    private val _events = Channel<CreateClientEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var isSubmitting = false

    init {
        viewModelScope.launch {
            combine(
                referentielDao.getCivilityOptions(),
                referentielDao.getUnitTypes(),
            ) { civilities, unitTypes ->
                val civList =
                    civilities.map { it.toUi() }
                        .ifEmpty { ClientReferentialDefaults.civilities.map { it.toUi() } }
                val unitList =
                    unitTypes.map { it.toUi() }
                        .ifEmpty { ClientReferentialDefaults.unitTypes.map { it.toUi() } }
                civList to unitList
            }.collect { (civList, unitList) ->
                _uiState.update { state ->
                    val civCode =
                        state.selectedCivilityCode?.takeIf { code ->
                            civList.any { it.code == code }
                        } ?: civList.firstOrNull()?.code
                    val unitCode =
                        state.selectedUnitTypeCode?.takeIf { code ->
                            unitList.any { it.code == code }
                        } ?: unitList.firstOrNull { it.code == "house" }?.code
                            ?: unitList.firstOrNull()?.code
                    state.copy(
                        civilityOptions = civList,
                        unitTypeOptions = unitList,
                        selectedCivilityCode = civCode,
                        selectedUnitTypeCode = unitCode,
                    )
                }
            }
        }

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
        _uiState.update { current ->
            if (current.isBanAddressSelected && value == current.addressSearchText) {
                return@update current
            }
            val userEditedSelection =
                current.isBanAddressSelected && value != current.addressSearchText
            current.copy(
                addressSearchText = value,
                isBanAddressSelected = false,
                streetResolved = if (userEditedSelection) "" else current.streetResolved,
                postalCode = if (userEditedSelection) "" else current.postalCode,
                city = if (userEditedSelection) "" else current.city,
                latitude = if (userEditedSelection) null else current.latitude,
                longitude = if (userEditedSelection) null else current.longitude,
                submitError = null,
                banSearchError = false,
                fieldErrors = current.fieldErrors - "address",
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
                streetResolved =
                    when {
                        it.streetResolved.isNotBlank() -> it.streetResolved
                        hint.isNotEmpty() -> hint
                        else -> ""
                    },
                postalCode = it.postalCode,
                city =
                    it.city.takeIf { c -> c.isNotBlank() && c != "—" }.orEmpty(),
                latitude = it.latitude,
                longitude = it.longitude,
                isBanAddressSelected = false,
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
                isBanAddressSelected = false,
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
        val streetLine = pick.street.ifBlank { pick.label }
        Log.d(
            "BAN",
            "onSelectBanSuggestion label=${pick.label} street=$streetLine " +
                "codePostal=${pick.postalCode} ville=${pick.city} lat=${pick.latitude} lon=${pick.longitude}",
        )
        _uiState.update {
            it.copy(
                addressSearchText = pick.label,
                streetResolved = streetLine,
                postalCode = pick.postalCode,
                city = pick.city,
                latitude = pick.latitude,
                longitude = pick.longitude,
                isBanAddressSelected = true,
                banSuggestions = emptyList(),
                banLoading = false,
                banSearchError = false,
                submitError = null,
                fieldErrors = it.fieldErrors - "address",
            )
        }
        _addressSearchInput.value = ""
    }

    fun onCivilityChange(code: String) {
        _uiState.update { it.copy(selectedCivilityCode = code, submitError = null) }
    }

    fun onUnitTypeChange(code: String) {
        _uiState.update { state ->
            val unit = state.unitTypeOptions.firstOrNull { it.code == code }
            state.copy(
                selectedUnitTypeCode = code,
                floor = if (unit?.showsFloorField() == true) state.floor else "",
                submitError = null,
            )
        }
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

    fun onFloorChange(v: String) {
        _uiState.update { it.copy(floor = v, submitError = null) }
    }

    fun resetForm() {
        isSubmitting = false
        _addressSearchInput.value = ""
        val civs = _uiState.value.civilityOptions
        val units = _uiState.value.unitTypeOptions
        _uiState.value =
            CreateClientUiState(
                civilityOptions = civs,
                unitTypeOptions = units,
                selectedCivilityCode = civs.firstOrNull()?.code,
                selectedUnitTypeCode =
                    units.firstOrNull { it.code == "house" }?.code ?: units.firstOrNull()?.code,
            )
    }

    fun submit() {
        if (isSubmitting) return

        val s = _uiState.value
        val unitType = s.selectedUnitType
        val civilityCode = s.selectedCivilityCode
        Log.d(
            "BAN",
            "submit validation: mode=${s.addressEntryMode} isBanAddressSelected=${s.isBanAddressSelected} " +
                "street=${s.streetResolved} cp=${s.postalCode} city=${s.city}",
        )
        val errors = mutableMapOf<String, String>()
        if (civilityCode.isNullOrBlank()) errors["civility"] = "Sélectionnez une civilité"
        if (unitType == null) errors["unitType"] = "Sélectionnez un type de logement"
        if (s.firstName.isBlank()) errors["firstName"] = "Prénom requis"
        if (s.lastName.isBlank()) errors["lastName"] = "Nom requis"
        val street = s.streetResolved.trim()
        when (s.addressEntryMode) {
            ClientAddressEntryMode.BAN -> {
                if (!s.isBanAddressSelected) {
                    errors["address"] =
                        "Sélectionnez une adresse dans les suggestions BAN ou saisissez-la manuellement."
                } else if (
                    street.isBlank() || s.postalCode.isBlank() || s.city.isBlank() || s.city.trim() == "—"
                ) {
                    errors["address"] = "Adresse incomplète : sélectionnez de nouveau une suggestion."
                }
            }
            ClientAddressEntryMode.MANUAL -> {
                if (street.isBlank() || s.postalCode.isBlank() || s.city.isBlank() || s.city.trim() == "—") {
                    errors["address"] = "Renseignez le numéro et la voie, le code postal et la ville."
                }
            }
        }

        if (errors.isNotEmpty()) {
            _uiState.update { it.copy(fieldErrors = errors, submitError = null) }
            return
        }

        isSubmitting = true
        _uiState.update { it.copy(isSubmitting = true, submitError = null, fieldErrors = emptyMap()) }

        viewModelScope.launch {
            try {
                val resolvedUnitType = requireNotNull(unitType)
                val resolvedCivility = requireNotNull(civilityCode)
                Log.d(
                    TAG,
                    "POST /api/clients — logement: street=\"$street\" postal=\"${s.postalCode.trim()}\" " +
                        "city=\"${s.city.trim()}\" unitType=${resolvedUnitType.code} civility=$resolvedCivility",
                )
                val floorValue =
                    if (s.showsFloorField) {
                        s.floor.trim().takeIf { it.isNotEmpty() }
                    } else {
                        null
                    }
                val logement =
                    CreateClientLogementDto(
                        street = street,
                        postalCode = s.postalCode.trim(),
                        city = s.city.trim(),
                        latitude = s.latitude,
                        longitude = s.longitude,
                        addressLine2 = s.addressComplement.trim().takeIf { it.isNotEmpty() },
                        floor = floorValue,
                        unitType = resolvedUnitType.code,
                        unitCategory = resolvedUnitType.category,
                    )
                val body =
                    CreateTenantClientRequestDto(
                        firstName = s.firstName.trim(),
                        lastName = s.lastName.trim(),
                        civility = resolvedCivility,
                        phone = s.phone.trim().takeIf { it.isNotEmpty() },
                        email = s.email.trim().takeIf { it.isNotEmpty() },
                        logement = logement,
                        occupancy =
                            CreateClientOccupancyDto(
                                role = "tenant",
                                startDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
                            ),
                    )
                if (!NetworkUtils.isOnline(appContext)) {
                    val localId = UUID.randomUUID().toString()
                    pendingClientRepository.insert(
                        PendingClientEntity(
                            localId = localId,
                            firstName = s.firstName.trim(),
                            lastName = s.lastName.trim(),
                            civility = resolvedCivility,
                            phone = s.phone.trim().takeIf { it.isNotEmpty() },
                            email = s.email.trim().takeIf { it.isNotEmpty() },
                            address = street,
                            addressComplement = s.addressComplement.trim().takeIf { it.isNotEmpty() },
                            city = s.city.trim(),
                            zipCode = s.postalCode.trim(),
                            lat = s.latitude,
                            lng = s.longitude,
                            unitType = resolvedUnitType.code,
                            unitCategory = resolvedUnitType.category,
                            floor = floorValue,
                        ),
                    )
                    Log.d(TAG, "Client en file d’attente (offline) localId=$localId")
                    _events.send(CreateClientEvent.SavedOffline)
                    return@launch
                }

                when (val r = clientsRepository.createClient(body)) {
                    is CreateClientResult.Success -> {
                        val resp = r.response
                        val displayName =
                            listOf(s.firstName.trim(), s.lastName.trim())
                                .filter { it.isNotEmpty() }
                                .joinToString(" ")
                                .ifBlank {
                                    listOfNotNull(resp.firstName, resp.lastName)
                                        .joinToString(" ")
                                        .trim()
                                }
                        val streetLine = street.trim()
                        val addressLine =
                            listOf(streetLine, "${s.postalCode.trim()} ${s.city.trim()}".trim())
                                .filter { it.isNotBlank() }
                                .joinToString(", ")
                        _events.send(
                            CreateClientEvent.Created(
                                customerId = resp.resolvedCustomerId(),
                                unitId = resp.unitId,
                                displayName = displayName,
                                addressLine = addressLine,
                            ),
                        )
                    }

                    is CreateClientResult.Error -> {
                        _uiState.update { it.copy(submitError = r.message) }
                    }
                }
            } finally {
                isSubmitting = false
                _uiState.update { it.copy(isSubmitting = false) }
            }
        }
    }

    private companion object {
        const val TAG = "CreateClientVM"
    }
}
