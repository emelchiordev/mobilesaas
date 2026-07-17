package re.savio.mobile.ui.screen.intervention.cerfa

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import re.savio.mobile.data.local.dao.SettingsDao
import re.savio.mobile.data.local.entity.ColdMeasureEntity
import re.savio.mobile.data.local.objectbox.MeasurementDeviceBox
import re.savio.mobile.data.local.objectbox.RefrigerantContainerBox
import re.savio.mobile.data.local.objectbox.RefrigerantWastePartnerBox
import re.savio.mobile.data.remote.dto.controlDateToIso
import re.savio.mobile.data.remote.dto.formatControlDateFr
import re.savio.mobile.data.remote.dto.formatVilleForCerfa
import re.savio.mobile.data.repository.ColdMeasureRepository
import re.savio.mobile.data.repository.MeasurementDeviceSyncRepository
import re.savio.mobile.data.repository.RefrigerantCapacityAttestationRepository
import re.savio.mobile.data.repository.RefrigerantContainerSyncRepository
import re.savio.mobile.data.repository.RefrigerantWastePartnerSyncRepository
import re.savio.mobile.ui.util.contentFingerprint
import re.savio.mobile.ui.util.hasMeaningfulCerfaData
import re.savio.mobile.util.UserProfile

val FLUIDES_FRIGORIGENES = listOf(
    "R32", "R454B", "R513A", "R1234yf", "R1234ze",
    "R290", "R600a", "R744", "R717",
    "R410A", "R134a", "R407C", "R448A", "R449A",
    "R22", "R404A", "R507A",
)

data class CapacityAttestationUiState(
    val needsCapture: Boolean = false,
    val canEdit: Boolean = false,
    val currentNumber: String = "",
    val isDemo: Boolean = false,
    val saveError: String? = null,
    val isSaving: Boolean = false,
)

@HiltViewModel
class CerfaFroidViewModel @Inject constructor(
    private val repository: ColdMeasureRepository,
    private val containerSyncRepository: RefrigerantContainerSyncRepository,
    private val wastePartnerSyncRepository: RefrigerantWastePartnerSyncRepository,
    private val measurementDeviceSyncRepository: MeasurementDeviceSyncRepository,
    private val capacityAttestationRepository: RefrigerantCapacityAttestationRepository,
    private val settingsDao: SettingsDao,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val interventionId: String = checkNotNull(savedStateHandle["interventionId"])
    private val equipmentId: String = checkNotNull(savedStateHandle["equipmentId"])

    private val _state = MutableStateFlow(
        ColdMeasureEntity(
            id = "",
            interventionId = interventionId,
            equipmentId = equipmentId,
        ),
    )
    val state: StateFlow<ColdMeasureEntity> = _state.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _hasPersistedData = MutableStateFlow(false)
    val hasPersistedData: StateFlow<Boolean> = _hasPersistedData.asStateFlow()

    private val _supplyContainers = MutableStateFlow<List<RefrigerantContainerBox>>(emptyList())
    val supplyContainers: StateFlow<List<RefrigerantContainerBox>> = _supplyContainers.asStateFlow()

    private val _recoveryContainers = MutableStateFlow<List<RefrigerantContainerBox>>(emptyList())
    val recoveryContainers: StateFlow<List<RefrigerantContainerBox>> = _recoveryContainers.asStateFlow()

    private val _containerCreateError = MutableStateFlow<String?>(null)
    val containerCreateError: StateFlow<String?> = _containerCreateError.asStateFlow()

    private val _destinationPartners = MutableStateFlow<List<RefrigerantWastePartnerBox>>(emptyList())
    val destinationPartners: StateFlow<List<RefrigerantWastePartnerBox>> = _destinationPartners.asStateFlow()

    private val _transporterPartners = MutableStateFlow<List<RefrigerantWastePartnerBox>>(emptyList())
    val transporterPartners: StateFlow<List<RefrigerantWastePartnerBox>> = _transporterPartners.asStateFlow()

    private val _partnerCreateError = MutableStateFlow<String?>(null)
    val partnerCreateError: StateFlow<String?> = _partnerCreateError.asStateFlow()

    private val _capacityAttestation = MutableStateFlow(CapacityAttestationUiState())
    val capacityAttestation: StateFlow<CapacityAttestationUiState> = _capacityAttestation.asStateFlow()

    private val _measurementDevices = MutableStateFlow<List<MeasurementDeviceBox>>(emptyList())
    val measurementDevices: StateFlow<List<MeasurementDeviceBox>> = _measurementDevices.asStateFlow()

    private val _canCreateMeasurementDevice = MutableStateFlow(false)
    val canCreateMeasurementDevice: StateFlow<Boolean> = _canCreateMeasurementDevice.asStateFlow()

    private val _measurementDeviceCreateError = MutableStateFlow<String?>(null)
    val measurementDeviceCreateError: StateFlow<String?> = _measurementDeviceCreateError.asStateFlow()

    private var initialState: ColdMeasureEntity? = null

    init {
        loadExisting()
    }

    private fun loadExisting() {
        viewModelScope.launch {
            val existing = repository.get(interventionId, equipmentId)
            _hasPersistedData.value = existing != null
            val loaded = existing ?: repository.newDraft(interventionId, equipmentId)
            _state.value = loaded
            initialState = loaded.contentFingerprint()
            refreshContainers()
            refreshWastePartners()
            reloadMeasurementDeviceList()
            applyRememberedMeasurementDeviceIfEmpty()
            refreshMeasurementDevices()
            refreshCapacityAttestation()
        }
    }

    fun refreshCapacityAttestation() {
        viewModelScope.launch {
            val settings = settingsDao.getSettingsOnce()
            val number = settings?.refrigerantCapacityAttestationNumber.orEmpty()
            val isDemo = settings?.refrigerantCapacityAttestationIsDemo == true
            _capacityAttestation.value =
                CapacityAttestationUiState(
                    needsCapture = capacityAttestationRepository.needsCapture(number, isDemo),
                    canEdit = settings?.profile == UserProfile.ARTISAN_SOLO,
                    currentNumber = number,
                    isDemo = isDemo,
                )
        }
    }

    fun saveCapacityAttestation(rawNumber: String) {
        viewModelScope.launch {
            _capacityAttestation.update { it.copy(isSaving = true, saveError = null) }
            val result = capacityAttestationRepository.saveLocallyAndEnqueue(rawNumber)
            result.fold(
                onSuccess = { refreshCapacityAttestation() },
                onFailure = { e ->
                    _capacityAttestation.update {
                        it.copy(
                            isSaving = false,
                            saveError = e.message ?: "Enregistrement impossible",
                        )
                    }
                },
            )
        }
    }

    fun clearCapacityAttestationError() {
        _capacityAttestation.update { it.copy(saveError = null) }
    }

    fun refreshContainers() {
        viewModelScope.launch {
            containerSyncRepository.syncIfNeeded()
            reloadContainerLists()
        }
    }

    fun refreshWastePartners() {
        viewModelScope.launch {
            wastePartnerSyncRepository.sync(force = true)
            reloadWastePartnerLists()
        }
    }

    fun refreshMeasurementDevices() {
        viewModelScope.launch {
            measurementDeviceSyncRepository.sync(force = true)
            reloadMeasurementDeviceList()
        }
    }

    private suspend fun reloadMeasurementDeviceList() {
        val settings = settingsDao.getSettingsOnce()
        _canCreateMeasurementDevice.value = settings?.profile == UserProfile.ARTISAN_SOLO
        _measurementDevices.value =
            measurementDeviceSyncRepository.listForCerfa(
                technicianId = settings?.technicianId,
                profile = settings?.profile,
            )
    }

    private suspend fun applyRememberedMeasurementDeviceIfEmpty() {
        val current = _state.value
        if (current.detm1.isNotBlank() || current.dett1.isNotBlank() || current.detd1.isNotBlank()) {
            return
        }
        val techId = settingsDao.getSettingsOnce()?.technicianId ?: return
        val lastId = measurementDeviceSyncRepository.getLastCerfaDeviceId(techId) ?: return
        val device = measurementDeviceSyncRepository.findById(lastId) ?: return
        applyMeasurementDeviceFields(device, remember = false)
    }

    private fun applyMeasurementDeviceFields(
        device: MeasurementDeviceBox,
        remember: Boolean,
    ) {
        _state.update {
            it.copy(
                detm1 = device.brand,
                dett1 = device.model,
                detd1 = device.formatControlDateFr(),
            )
        }
        if (remember) {
            viewModelScope.launch {
                measurementDeviceSyncRepository.rememberCerfaDevice(device.id)
            }
        }
    }

    fun selectMeasurementDevice(deviceId: String?) {
        if (deviceId.isNullOrBlank()) return
        val device = measurementDeviceSyncRepository.findById(deviceId) ?: return
        applyMeasurementDeviceFields(device, remember = true)
    }

    fun createMeasurementDeviceOnTheFly(
        brand: String,
        model: String,
        serialNumber: String,
        lastControlDate: String,
        onSuccess: () -> Unit = {},
    ) {
        viewModelScope.launch {
            _measurementDeviceCreateError.value = null
            if (brand.isBlank() || model.isBlank()) {
                _measurementDeviceCreateError.value = "Marque et modèle obligatoires"
                return@launch
            }
            val result =
                measurementDeviceSyncRepository.createOnTheFly(
                    brand = brand,
                    model = model,
                    serialNumber = serialNumber.ifBlank { null },
                    lastControlDate = controlDateToIso(lastControlDate),
                )
            result.fold(
                onSuccess = { device ->
                    reloadMeasurementDeviceList()
                    applyMeasurementDeviceFields(device, remember = true)
                    onSuccess()
                },
                onFailure = {
                    _measurementDeviceCreateError.value =
                        "Création impossible (connexion requise, ARTISAN_SOLO)"
                },
            )
        }
    }

    fun clearMeasurementDeviceCreateError() {
        _measurementDeviceCreateError.value = null
    }

    fun measurementDeviceLabel(device: MeasurementDeviceBox): String {
        val serial = device.serialNumber?.trim().orEmpty()
        return if (serial.isBlank()) {
            "${device.brand} ${device.model}"
        } else {
            "${device.brand} ${device.model} — $serial"
        }
    }

    private suspend fun reloadContainerLists() {
        val techId = settingsDao.getSettingsOnce()?.technicianId
        val fluidFilter = _state.value.frigo.trim().uppercase().takeIf { it.isNotBlank() }
        _supplyContainers.value = containerSyncRepository.listForTechnician(
            technicianId = techId,
            containerKind = "supply",
            fluidType = fluidFilter,
        )
        _recoveryContainers.value = containerSyncRepository.listForTechnician(
            technicianId = techId,
            containerKind = "recovery",
            fluidType = fluidFilter,
        )
    }

    private fun reloadWastePartnerLists() {
        _destinationPartners.value = wastePartnerSyncRepository.listByKind(
            RefrigerantWastePartnerSyncRepository.KIND_DESTINATION,
        )
        _transporterPartners.value = wastePartnerSyncRepository.listByKind(
            RefrigerantWastePartnerSyncRepository.KIND_TRANSPORTER,
        )
    }

    fun selectSupplyContainer(containerId: String?) {
        if (containerId.isNullOrBlank()) {
            _state.update { it.copy(supplyContainerId = "", fluiderc = "") }
            return
        }
        val container = containerSyncRepository.findById(containerId) ?: return
        _state.update {
            it.copy(
                supplyContainerId = containerId,
                fluiderc = container.containerIdentifier,
            )
        }
    }

    fun selectRecoveryContainer(containerId: String?) {
        _state.update { it.copy(recoveryContainerId = containerId.orEmpty()) }
    }

    fun selectDestinationPartner(partnerId: String?) {
        if (partnerId.isNullOrBlank()) return
        val partner = wastePartnerSyncRepository.findById(partnerId) ?: return
        _state.update {
            it.copy(
                nomdec = partner.label,
                adres1dec = partner.addressLine1.orEmpty(),
                adres2dec = partner.addressLine2.orEmpty(),
                villedec = partner.formatVilleForCerfa(),
            )
        }
    }

    fun selectTransporterPartner(partnerId: String?) {
        if (partnerId.isNullOrBlank()) return
        val partner = wastePartnerSyncRepository.findById(partnerId) ?: return
        _state.update {
            it.copy(
                nomtrans = partner.label,
                adres1trans = partner.addressLine1.orEmpty(),
                adres2trans = partner.addressLine2.orEmpty(),
                villetrans = partner.formatVilleForCerfa(),
            )
        }
    }

    fun createContainerOnTheFly(
        identifier: String,
        capacityKg: Double,
        containerKind: String,
        onSuccess: () -> Unit = {},
    ) {
        viewModelScope.launch {
            _containerCreateError.value = null
            val techId = settingsDao.getSettingsOnce()?.technicianId
            if (techId.isNullOrBlank()) {
                _containerCreateError.value = "Technicien non configuré"
                return@launch
            }
            val fluidType = _state.value.frigo.trim().uppercase()
            if (fluidType.isBlank()) {
                _containerCreateError.value = "Sélectionnez d'abord le fluide (onglet Équipement)"
                return@launch
            }
            val result = containerSyncRepository.createOnTheFly(
                identifier = identifier,
                fluidType = fluidType,
                capacityKg = capacityKg,
                containerKind = containerKind,
                technicianId = techId,
            )
            result.fold(
                onSuccess = { container ->
                    reloadContainerLists()
                    if (containerKind == "supply") {
                        selectSupplyContainer(container.id)
                    } else {
                        selectRecoveryContainer(container.id)
                    }
                    onSuccess()
                },
                onFailure = {
                    _containerCreateError.value = "Création impossible (connexion requise)"
                },
            )
        }
    }

    fun createWastePartnerOnTheFly(
        kind: String,
        label: String,
        siret: String,
        addressLine1: String,
        addressLine2: String,
        postalCode: String,
        city: String,
        onSuccess: () -> Unit = {},
    ) {
        viewModelScope.launch {
            _partnerCreateError.value = null
            if (label.isBlank() || siret.isBlank()) {
                _partnerCreateError.value = "Libellé et SIRET obligatoires"
                return@launch
            }
            val result = wastePartnerSyncRepository.createOnTheFly(
                kind = kind,
                label = label,
                siret = siret,
                addressLine1 = addressLine1,
                addressLine2 = addressLine2,
                postalCode = postalCode,
                city = city,
            )
            result.fold(
                onSuccess = { partner ->
                    reloadWastePartnerLists()
                    if (kind == RefrigerantWastePartnerSyncRepository.KIND_DESTINATION) {
                        selectDestinationPartner(partner.id)
                    } else {
                        selectTransporterPartner(partner.id)
                    }
                    onSuccess()
                },
                onFailure = {
                    _partnerCreateError.value = "Création impossible (connexion ou SIRET invalide)"
                },
            )
        }
    }

    fun clearContainerCreateError() {
        _containerCreateError.value = null
    }

    fun clearPartnerCreateError() {
        _partnerCreateError.value = null
    }

    fun partnerLabel(partner: RefrigerantWastePartnerBox): String {
        val ville = partner.formatVilleForCerfa()
        return if (ville.isBlank()) partner.label else "${partner.label} — $ville"
    }

    /** Mode fréquence dérivé des cases FREQA* / FREQS* déjà saisies. */
    fun frequencyMode(): CerfaFrequencyMode = cerfaFrequencyMode(_state.value)

    /**
     * Bascule standard ↔ auto : efface l’autre groupe et mappe la sélection courante si possible.
     */
    fun setFrequencyMode(auto: Boolean) {
        val current = selectedFrequencyMonths(_state.value)
        val mapped =
            if (current == null) {
                null
            } else if (auto) {
                when (current) {
                    3 -> 6
                    6 -> 12
                    12 -> 24
                    else -> current
                }
            } else {
                when (current) {
                    24 -> 12
                    12 -> 6
                    6 -> 3
                    else -> current
                }
            }
        applyFrequencySelection(months = mapped, auto = auto)
    }

    fun selectFrequencyMonths(months: Int, auto: Boolean) {
        applyFrequencySelection(months = months, auto = auto)
    }

    private fun applyFrequencySelection(months: Int?, auto: Boolean) {
        _state.update { current ->
            var next =
                current.copy(
                    freqs1 = "",
                    freqs2 = "",
                    freqs3 = "",
                    freqa1 = "",
                    freqa2 = "",
                    freqa3 = "",
                )
            if (months != null) {
                next =
                    if (auto) {
                        when (months) {
                            24 -> next.copy(freqa1 = "O", freqa2 = "N", freqa3 = "N")
                            12 -> next.copy(freqa1 = "N", freqa2 = "O", freqa3 = "N")
                            6 -> next.copy(freqa1 = "N", freqa2 = "N", freqa3 = "O")
                            else -> next
                        }
                    } else {
                        when (months) {
                            12 -> next.copy(freqs1 = "O", freqs2 = "N", freqs3 = "N")
                            6 -> next.copy(freqs1 = "N", freqs2 = "O", freqs3 = "N")
                            3 -> next.copy(freqs1 = "N", freqs2 = "N", freqs3 = "O")
                            else -> next
                        }
                    }
            }
            next
        }
    }

    fun clearLeakBlock(index: Int) {
        when (index) {
            1 -> {
                update("FUITELOC1", "")
                update("FUITEREP1", "")
            }
            2 -> {
                update("FUITELOC2", "")
                update("FUITEREP2", "")
            }
            3 -> {
                update("FUITELOC3", "")
                update("FUITEREP3", "")
            }
        }
    }

    /** Supprime le bloc n et décale les suivants (2←3). */
    fun removeLeakBlock(index: Int) {
        _state.update { s ->
            when (index) {
                2 ->
                    s.copy(
                        fuiteloc2 = s.fuiteloc3,
                        fuiterep2 = s.fuiterep3,
                        fuiteloc3 = "",
                        fuiterep3 = "",
                    )
                3 -> s.copy(fuiteloc3 = "", fuiterep3 = "")
                else -> s
            }
        }
    }

    fun containerLabel(container: RefrigerantContainerBox): String {
        return "${container.containerIdentifier} — ${container.fluidType} " +
            "(${container.currentKg}/${container.capacityKg} kg)"
    }

    fun hasChanges(): Boolean {
        val initial = initialState ?: return false
        return _state.value.contentFingerprint() != initial
    }

    fun saveIfChanged() {
        if (!hasChanges()) return
        persist()
    }

    fun update(key: String, value: String) {
        _state.update { current ->
            val updated = when (key) {
                "FRIGO" -> current.copy(frigo = value)
                "CHARG" -> current.copy(charg = value)
                "TONNAGE" -> current.copy(tonnage = value)
                "MINTER1" -> current.copy(minter1 = value)
                "MINTER2" -> current.copy(minter2 = value)
                "MINTER3" -> current.copy(minter3 = value)
                "MINTER4" -> current.copy(minter4 = value)
                "MINTER5" -> current.copy(minter5 = value)
                "MINTER6" -> current.copy(minter6 = value)
                "MINTER7" -> current.copy(minter7 = value)
                "MINTER8" -> current.copy(minter8 = value)
                "MINTERAUT" -> current.copy(minteraut = value)
                "OBSERN1" -> current.copy(obsern1 = value)
                "OBSERN2" -> current.copy(obsern2 = value)
                "DETM1" -> current.copy(detm1 = value)
                "DETT1" -> current.copy(dett1 = value)
                "DETD1" -> current.copy(detd1 = value)
                "AUTOFUITE" -> current.copy(autofuite = value)
                "QTEFRI" -> current.copy(qtefri = value)
                "QTEFRI2" -> current.copy(qtefri2 = value)
                "QTEFRI3" -> current.copy(qtefri3 = value)
                "FREQS1" -> current.copy(
                    freqs1 = value,
                    freqs2 = if (value == "O") "N" else current.freqs2,
                    freqs3 = if (value == "O") "N" else current.freqs3,
                )
                "FREQS2" -> current.copy(
                    freqs1 = if (value == "O") "N" else current.freqs1,
                    freqs2 = value,
                    freqs3 = if (value == "O") "N" else current.freqs3,
                )
                "FREQS3" -> current.copy(
                    freqs1 = if (value == "O") "N" else current.freqs1,
                    freqs2 = if (value == "O") "N" else current.freqs2,
                    freqs3 = value,
                )
                "FREQA1" -> current.copy(
                    freqa1 = value,
                    freqa2 = if (value == "O") "N" else current.freqa2,
                    freqa3 = if (value == "O") "N" else current.freqa3,
                )
                "FREQA2" -> current.copy(
                    freqa1 = if (value == "O") "N" else current.freqa1,
                    freqa2 = value,
                    freqa3 = if (value == "O") "N" else current.freqa3,
                )
                "FREQA3" -> current.copy(
                    freqa1 = if (value == "O") "N" else current.freqa1,
                    freqa2 = if (value == "O") "N" else current.freqa2,
                    freqa3 = value,
                )
                "PASFUITE" -> current.copy(pasfuite = value)
                "FUITELOC1" -> current.copy(fuiteloc1 = value)
                "FUITEREP1" -> current.copy(fuiterep1 = value)
                "FUITELOC2" -> current.copy(fuiteloc2 = value)
                "FUITEREP2" -> current.copy(fuiterep2 = value)
                "FUITELOC3" -> current.copy(fuiteloc3 = value)
                "FUITEREP3" -> current.copy(fuiterep3 = value)
                "FLUIDECV" -> current.copy(fluidecv = value)
                "FLUIDECR" -> current.copy(fluidecr = value)
                "FLUIDECRG" -> current.copy(fluidecrg = value)
                "FLUIDERT" -> current.copy(fluidert = value)
                "FLUIDERU" -> current.copy(fluideru = value)
                "UN1078A" -> current.copy(un1078a = value)
                "UN1078B" -> current.copy(un1078b = value)
                "NOMDEC" -> current.copy(nomdec = value)
                "ADRES1DEC" -> current.copy(adres1dec = value)
                "ADRES2DEC" -> current.copy(adres2dec = value)
                "VILLEDEC" -> current.copy(villedec = value)
                "NOMTRANS" -> current.copy(nomtrans = value)
                "ADRES1TRANS" -> current.copy(adres1trans = value)
                "ADRES2TRANS" -> current.copy(adres2trans = value)
                "VILLETRANS" -> current.copy(villetrans = value)
                "FLUIDOBS" -> current.copy(fluidobs = value)
                "FLUIDOBS2" -> current.copy(fluidobs2 = value)
                "BORDEQTE" -> current.copy(bordeqte = value)
                "BORDETRANS" -> current.copy(bordetrans = value)
                "INSTATRAIT" -> current.copy(instatrait = value)
                "CODERD" -> current.copy(coderd = value)
                "QTERECEP" -> current.copy(qterecep = value)
                "FRIGO2" -> current.copy(frigo2 = value)
                "BSFF" -> current.copy(bsff = value)
                "UN3161A" -> current.copy(un3161a = value)
                "UN3161B" -> current.copy(un3161b = value)
                else -> current
            }
            val cv = updated.fluidecv.replace(",", ".").toDoubleOrNull() ?: 0.0
            val cr = updated.fluidecr.replace(",", ".").toDoubleOrNull() ?: 0.0
            val crg = updated.fluidecrg.replace(",", ".").toDoubleOrNull() ?: 0.0
            val rein = cv + cr + crg
            val rt = updated.fluidert.replace(",", ".").toDoubleOrNull() ?: 0.0
            val ru = updated.fluideru.replace(",", ".").toDoubleOrNull() ?: 0.0
            val recup = rt + ru
            val withFluids =
                updated.copy(
                    fluidrein = if (rein == 0.0) "" else formatFluid(rein),
                    fluidrecup = if (recup == 0.0) "" else formatFluid(recup),
                )
            // Tonnage auto dès que fluide + charge connus (saisie manuelle TONNAGE si GWP inconnu).
            if (key == "FRIGO" || key == "CHARG") {
                val auto =
                    FluideGwp.computeTonnageCo2e(withFluids.frigo, withFluids.charg)
                if (auto != null) {
                    withFluids.copy(tonnage = auto)
                } else if (key == "FRIGO" && !FluideGwp.hasKnownGwp(withFluids.frigo)) {
                    withFluids
                } else if (key == "CHARG" && withFluids.charg.isBlank()) {
                    withFluids.copy(tonnage = "")
                } else {
                    withFluids
                }
            } else {
                withFluids
            }
        }
        if (key == "FRIGO") {
            viewModelScope.launch { reloadContainerLists() }
        }
    }

    private fun formatFluid(v: Double): String {
        return if (v == v.toLong().toDouble()) {
            v.toLong().toString()
        } else {
            "%.3f".format(Locale.US, v)
        }
    }

    fun saveLocally() {
        val current = _state.value
        if (!hasChanges() && !current.hasMeaningfulCerfaData()) return
        persist()
    }

    private fun persist() {
        viewModelScope.launch {
            _isSaving.value = true
            val saved = _state.value
            repository.save(saved)
            initialState = saved.contentFingerprint()
            _hasPersistedData.value = true
            _isSaving.value = false
        }
    }
}
