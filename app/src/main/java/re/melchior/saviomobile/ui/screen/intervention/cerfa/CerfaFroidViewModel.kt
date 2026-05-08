package re.melchior.saviomobile.ui.screen.intervention.cerfa

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
import re.melchior.saviomobile.data.local.entity.ColdMeasureEntity
import re.melchior.saviomobile.data.repository.ColdMeasureRepository

val FLUIDES_FRIGORIGENES = listOf(
    "R32", "R410A", "R134a", "R22", "R407C", "R404A",
    "R600a", "R290", "R744", "R1234yf", "R1234ze",
)

@HiltViewModel
class CerfaFroidViewModel @Inject constructor(
    private val repository: ColdMeasureRepository,
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

    init {
        loadExisting()
    }

    private fun loadExisting() {
        viewModelScope.launch {
            _hasPersistedData.value = repository.existsPersisted(
                interventionId,
                equipmentId,
            )
            val existing = repository.getOrCreate(interventionId, equipmentId)
            _state.value = existing
        }
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
                "FLUIDERC" -> current.copy(fluiderc = value)
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
            updated.copy(
                fluidrein = if (rein == 0.0) "" else formatFluid(rein),
                fluidrecup = if (recup == 0.0) "" else formatFluid(recup),
            )
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
        viewModelScope.launch {
            _isSaving.value = true
            repository.save(_state.value)
            _hasPersistedData.value = true
            _isSaving.value = false
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch { repository.save(_state.value) }
    }
}
