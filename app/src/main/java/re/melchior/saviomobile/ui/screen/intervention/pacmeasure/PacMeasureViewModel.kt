package re.melchior.saviomobile.ui.screen.intervention.pacmeasure

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import re.melchior.saviomobile.data.local.entity.PacMeasureEntity
import re.melchior.saviomobile.data.repository.PacMeasureRepository
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class PacMeasureViewModel @Inject constructor(
    private val pacMeasureRepository: PacMeasureRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val interventionId: String =
        savedStateHandle.get<String>("interventionId") ?: ""
    private val equipmentOrder: Int =
        savedStateHandle.get<Int>("equipmentOrder") ?: 0

    private val _state = MutableStateFlow(
        PacMeasureEntity(interventionId = interventionId, equipmentOrder = equipmentOrder),
    )
    val state: StateFlow<PacMeasureEntity> = _state.asStateFlow()

    private var isDirty = false
    private var saveJob: Job? = null

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            val existing = pacMeasureRepository.getByInterventionAndOrder(
                interventionId,
                equipmentOrder,
            )
            if (existing != null) {
                _state.value = existing
            }
        }
    }

    fun updateField(key: String, value: String) {
        _state.update { cur ->
            val next = when (key) {
                "pacVentilation" -> cur.copy(pacVentilation = value)
                "pacNetail" -> cur.copy(pacNetail = value)
                "pacVerail" -> cur.copy(pacVerail = value)
                "pacFiltre" -> cur.copy(pacFiltre = value)
                "pacFuite" -> cur.copy(pacFuite = value)
                "pacEvac" -> cur.copy(pacEvac = value)
                "pacPression1" -> cur.copy(pacPression1 = value)
                "pacPression2" -> cur.copy(pacPression2 = normalizeNumeric(value))
                "pacGlycol1" -> cur.copy(pacGlycol1 = value)
                "pacGlycol2" -> cur.copy(pacGlycol2 = normalizeNumeric(value))
                "pacTenStat" -> cur.copy(pacTenStat = normalizeNumeric(value))
                "pacTenDyna" -> cur.copy(pacTenDyna = normalizeNumeric(value))
                "pacIntensite" -> cur.copy(pacIntensite = normalizeNumeric(value))
                "pacResserage1" -> cur.copy(pacResserage1 = value)
                "pacResserage2" -> cur.copy(pacResserage2 = value)
                "pacInterieure" -> cur.copy(pacInterieure = normalizeNumeric(value))
                "pacExterieure" -> cur.copy(pacExterieure = normalizeNumeric(value))
                "pacDepart" -> cur.copy(pacDepart = normalizeNumeric(value))
                "pacRetour" -> cur.copy(pacRetour = normalizeNumeric(value))
                "pacDeltaT" -> cur.copy(pacDeltaT = value)
                "pacHiver" -> cur.copy(pacHiver = normalizeNumeric(value))
                "pacAppoint" -> cur.copy(pacAppoint = normalizeNumeric(value))
                "pacConfort" -> cur.copy(pacConfort = normalizeNumeric(value))
                "pacNonChauf" -> cur.copy(pacNonChauf = normalizeNumeric(value))
                "pacEcsConsigne" -> cur.copy(pacEcsConsigne = normalizeNumeric(value))
                "pacEcs" -> cur.copy(pacEcs = normalizeNumeric(value))
                "pacManometreBp" -> cur.copy(pacManometreBp = normalizeNumeric(value))
                "pacManometreHp" -> cur.copy(pacManometreHp = normalizeNumeric(value))
                "pacDegivrage" -> cur.copy(pacDegivrage = value)
                "pacInversion" -> cur.copy(pacInversion = value)
                "pacHFonct" -> cur.copy(pacHFonct = normalizeNumeric(value))
                "pacHComp1" -> cur.copy(pacHComp1 = normalizeNumeric(value))
                "pacHVenti" -> cur.copy(pacHVenti = normalizeNumeric(value))
                "pacNbDemarr" -> cur.copy(pacNbDemarr = normalizeNumeric(value))
                "pacHAppoint1" -> cur.copy(pacHAppoint1 = normalizeNumeric(value))
                "pacHAppoint2" -> cur.copy(pacHAppoint2 = normalizeNumeric(value))
                "pacAlarme1" -> cur.copy(pacAlarme1 = value.take(20))
                "pacAlarme2" -> cur.copy(pacAlarme2 = value.take(20))
                "pacBlocage1" -> cur.copy(pacBlocage1 = value.take(20))
                "pacBlocage2" -> cur.copy(pacBlocage2 = value.take(20))
                "pacReleve" -> cur.copy(pacReleve = normalizeNumeric(value))
                "pacRem1" -> cur.copy(pacRem1 = value)
                else -> cur
            }
            val depart = next.pacDepart.toDoubleOrNull()
            val retour = next.pacRetour.toDoubleOrNull()
            val deltaT = if (depart != null && retour != null) {
                String.format(Locale.US, "%.2f", depart - retour)
            } else {
                next.pacDeltaT
            }
            next.copy(pacDeltaT = deltaT)
        }
        isDirty = true
        scheduleSave()
    }

    private fun scheduleSave() {
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            delay(500)
            save()
        }
    }

    fun save() {
        if (!isDirty) return
        val s = _state.value
        viewModelScope.launch {
            pacMeasureRepository.upsert(s)
            isDirty = false
        }
    }

    private fun normalizeNumeric(value: String): String {
        if (value.isEmpty()) return ""
        val normalized = value.replace(',', '.')
        return if (normalized.matches(Regex("-?[0-9]*\\.?[0-9]*"))) {
            normalized
        } else {
            ""
        }
    }

    override fun onCleared() {
        saveJob?.cancel()
        if (isDirty && interventionId.isNotEmpty()) {
            runBlocking {
                pacMeasureRepository.upsert(_state.value)
            }
        }
        super.onCleared()
    }
}
