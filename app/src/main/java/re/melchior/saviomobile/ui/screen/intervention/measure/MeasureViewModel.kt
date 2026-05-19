package re.melchior.saviomobile.ui.screen.intervention.measure

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
import re.melchior.saviomobile.data.local.entity.MeasureEntity
import re.melchior.saviomobile.data.repository.MeasureRepository
import javax.inject.Inject

data class MeasureUiState(
    val interventionId: String = "",
    val equipmentOrder: Int = 0,

    val co: String = "",
    val coamb: String = "",
    val co2: String = "",
    val o2: String = "",
    val tair: String = "",
    val temfu: String = "",
    val rend: String = "",
    val nox: String = "",
    val eta: String = "",

    val thpa: String = "",
    val no: String = "",
    val no2: String = "",
    val o2ven: String = "",
    val condilu: String = "",
    val tgaz: String = "",
    val ta: String = "",

    val pregas: String = "",
    val prega: String = "",
    val pregn: String = "",
    val pregm: String = "",
    val puisgaz: String = "",
    val debga: String = "",

    val temec: String = "",
    val temef: String = "",
    val delta: String = "",
    val debio: String = "",

    val debfuel: String = "",
    val prefp: String = "",
    val puisfuel: String = "",
    val pulve: String = "",

    val spot: String = "",
    val testdsc: String = "",
    val remplacond: String = "",
    val templagigleur: String = "",
    val remplapoly: String = "",
    val etaventil: String = "",
    val ctranode: String = "",
    val ctrextvmc: String = "",

    val suie1: String = "",
    val suie2: String = "",
    val suie3: String = "",
    val residhuil: String = "",
    val opaci: String = "",
    val ionis: String = "",
    val pgevg: String = "",
    val pgepg: String = "",
    val depre: String = "",
    val depr2: String = "",
    val gican: String = "",
    val gicle: String = "",
    val pabs: String = "",
    val perte: String = "",
    val ppm: String = "",

    val obser: String = "",

    val isLoading: Boolean = false,
    val isDirty: Boolean = false,
)

@HiltViewModel
class MeasureViewModel @Inject constructor(
    private val measureRepository: MeasureRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val interventionId: String =
        savedStateHandle.get<String>("interventionId") ?: ""
    private val equipmentOrder: Int =
        savedStateHandle.get<Int>("equipmentOrder") ?: 0

    private val _uiState = MutableStateFlow(MeasureUiState())
    val uiState: StateFlow<MeasureUiState> = _uiState.asStateFlow()

    private var initialState: MeasureUiState? = null
    private var saveJob: Job? = null

    init {
        loadMeasure()
    }

    private fun loadMeasure() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val measure = measureRepository.getMeasure(
                interventionId,
                equipmentOrder,
            )
            if (measure != null) {
                val loaded = MeasureUiState(
                        interventionId = interventionId,
                        equipmentOrder = equipmentOrder,
                        co = measure.co?.toString() ?: "",
                        coamb = measure.coamb?.toString() ?: "",
                        co2 = measure.co2?.toString() ?: "",
                        o2 = measure.o2?.toString() ?: "",
                        tair = measure.tair?.toString() ?: "",
                        temfu = measure.temfu?.toString() ?: "",
                        rend = measure.rend?.toString() ?: "",
                        nox = measure.nox?.toString() ?: "",
                        eta = measure.eta?.toString() ?: "",
                        thpa = measure.thpa?.toString() ?: "",
                        no = measure.no?.toString() ?: "",
                        no2 = measure.no2?.toString() ?: "",
                        o2ven = measure.o2ven?.toString() ?: "",
                        condilu = measure.condilu?.toString() ?: "",
                        tgaz = measure.tgaz?.toString() ?: "",
                        ta = measure.ta?.toString() ?: "",
                        pregas = measure.pregas?.toString() ?: "",
                        prega = measure.prega?.toString() ?: "",
                        pregn = measure.pregn?.toString() ?: "",
                        pregm = measure.pregm?.toString() ?: "",
                        puisgaz = measure.puisgaz?.toString() ?: "",
                        debga = measure.debga?.toString() ?: "",
                        temec = measure.temec?.toString() ?: "",
                        temef = measure.temef?.toString() ?: "",
                        delta = measure.delta?.toString() ?: "",
                        debio = measure.debio?.toString() ?: "",
                        debfuel = measure.debfuel?.toString() ?: "",
                        prefp = measure.prefp?.toString() ?: "",
                        puisfuel = measure.puisfuel?.toString() ?: "",
                        pulve = measure.pulve?.toString() ?: "",
                        spot = measure.spot?.toString() ?: "",
                        testdsc = measure.testdsc ?: "",
                        remplacond = measure.remplacond ?: "",
                        templagigleur = measure.templagigleur ?: "",
                        remplapoly = measure.remplapoly ?: "",
                        etaventil = measure.etaventil ?: "",
                        ctranode = measure.ctranode ?: "",
                        ctrextvmc = measure.ctrextvmc ?: "",
                        suie1 = measure.suie1?.toString() ?: "",
                        suie2 = measure.suie2?.toString() ?: "",
                        suie3 = measure.suie3?.toString() ?: "",
                        residhuil = measure.residhuil?.toString() ?: "",
                        opaci = measure.opaci?.toString() ?: "",
                        ionis = measure.ionis?.toString() ?: "",
                        pgevg = measure.pgevg?.toString() ?: "",
                        pgepg = measure.pgepg?.toString() ?: "",
                        depre = measure.depre?.toString() ?: "",
                        depr2 = measure.depr2?.toString() ?: "",
                        gican = measure.gican?.toString() ?: "",
                        gicle = measure.gicle?.toString() ?: "",
                        pabs = measure.pabs?.toString() ?: "",
                        perte = measure.perte?.toString() ?: "",
                        ppm = measure.ppm?.toString() ?: "",
                        obser = measure.obser ?: "",
                        isLoading = false,
                        isDirty = false,
                    )
                _uiState.value = loaded
                initialState = loaded.copy(isDirty = false)
            } else {
                val empty = MeasureUiState(
                    interventionId = interventionId,
                    equipmentOrder = equipmentOrder,
                    isLoading = false,
                    isDirty = false,
                )
                _uiState.value = empty
                initialState = empty
            }
        }
    }

    fun hasChanges(): Boolean {
        val initial = initialState ?: return false
        return _uiState.value.toComparableSnapshot() != initial.toComparableSnapshot()
    }

    fun saveIfChanged() {
        if (!hasChanges()) return
        save()
    }

    private fun MeasureUiState.toComparableSnapshot(): MeasureUiState =
        copy(isLoading = false, isDirty = false)

    fun updateField(key: String, value: String) {
        _uiState.update { state ->
            val normalized = normalizeNumeric(value)
            when (key) {
                "co" -> state.copy(co = normalized, isDirty = true)
                "coamb" -> state.copy(coamb = normalized, isDirty = true)
                "co2" -> state.copy(co2 = normalized, isDirty = true)
                "o2" -> state.copy(o2 = normalized, isDirty = true)
                "tair" -> state.copy(tair = normalized, isDirty = true)
                "temfu" -> state.copy(temfu = normalized, isDirty = true)
                "rend" -> state.copy(rend = normalized, isDirty = true)
                "nox" -> state.copy(nox = normalized, isDirty = true)
                "eta" -> state.copy(eta = normalized, isDirty = true)
                "thpa" -> state.copy(thpa = normalized, isDirty = true)
                "no" -> state.copy(no = normalized, isDirty = true)
                "no2" -> state.copy(no2 = normalized, isDirty = true)
                "o2ven" -> state.copy(o2ven = normalized, isDirty = true)
                "condilu" -> state.copy(condilu = normalized, isDirty = true)
                "tgaz" -> state.copy(tgaz = normalized, isDirty = true)
                "ta" -> state.copy(ta = normalized, isDirty = true)
                "pregas" -> state.copy(pregas = normalized, isDirty = true)
                "prega" -> state.copy(prega = normalized, isDirty = true)
                "pregn" -> state.copy(pregn = normalized, isDirty = true)
                "pregm" -> state.copy(pregm = normalized, isDirty = true)
                "puisgaz" -> state.copy(puisgaz = normalized, isDirty = true)
                "debga" -> state.copy(debga = normalized, isDirty = true)
                "temec" -> state.copy(temec = normalized, isDirty = true)
                "temef" -> state.copy(temef = normalized, isDirty = true)
                "delta" -> state.copy(delta = normalized, isDirty = true)
                "debio" -> state.copy(debio = normalized, isDirty = true)
                "debfuel" -> state.copy(debfuel = normalized, isDirty = true)
                "prefp" -> state.copy(prefp = normalized, isDirty = true)
                "puisfuel" -> state.copy(puisfuel = normalized, isDirty = true)
                "pulve" -> state.copy(pulve = normalized, isDirty = true)
                "spot" -> state.copy(spot = normalized, isDirty = true)
                "testdsc" -> state.copy(testdsc = value, isDirty = true)
                "remplacond" -> state.copy(remplacond = value, isDirty = true)
                "templagigleur" -> state.copy(templagigleur = value, isDirty = true)
                "remplapoly" -> state.copy(remplapoly = value, isDirty = true)
                "etaventil" -> state.copy(etaventil = value, isDirty = true)
                "ctranode" -> state.copy(ctranode = value, isDirty = true)
                "ctrextvmc" -> state.copy(ctrextvmc = value, isDirty = true)
                "suie1" -> state.copy(suie1 = normalized, isDirty = true)
                "suie2" -> state.copy(suie2 = normalized, isDirty = true)
                "suie3" -> state.copy(suie3 = normalized, isDirty = true)
                "residhuil" -> state.copy(residhuil = normalized, isDirty = true)
                "opaci" -> state.copy(opaci = normalized, isDirty = true)
                "ionis" -> state.copy(ionis = normalized, isDirty = true)
                "pgevg" -> state.copy(pgevg = normalized, isDirty = true)
                "pgepg" -> state.copy(pgepg = normalized, isDirty = true)
                "depre" -> state.copy(depre = normalized, isDirty = true)
                "depr2" -> state.copy(depr2 = normalized, isDirty = true)
                "gican" -> state.copy(gican = normalized, isDirty = true)
                "gicle" -> state.copy(gicle = normalized, isDirty = true)
                "pabs" -> state.copy(pabs = normalized, isDirty = true)
                "perte" -> state.copy(perte = normalized, isDirty = true)
                "ppm" -> state.copy(ppm = normalized, isDirty = true)
                "obser" -> state.copy(obser = value, isDirty = true)
                else -> state
            }
        }
        autoSave()
    }

    private fun autoSave() {
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            delay(500)
            saveIfChanged()
        }
    }

    fun save() {
        val state = _uiState.value
        if (!hasChanges() && !state.hasAnyFieldFilled()) return
        viewModelScope.launch {
            measureRepository.saveMeasure(state.toEntity())
            val saved = state.copy(isDirty = false)
            _uiState.value = saved
            initialState = saved.toComparableSnapshot()
        }
    }

    private fun MeasureUiState.hasAnyFieldFilled(): Boolean =
        listOf(
            co, coamb, co2, o2, tair, temfu, rend, nox, eta, thpa, no, no2, o2ven,
            condilu, tgaz, ta, pregas, prega, pregn, pregm, puisgaz, debga, temec,
            temef, delta, debio, debfuel, prefp, puisfuel, pulve, spot, testdsc,
            remplacond, templagigleur, remplapoly, etaventil, ctranode, ctrextvmc,
            suie1, suie2, suie3, residhuil, opaci, ionis, pgevg, pgepg, depre, depr2,
            gican, gicle, pabs, perte, ppm, obser,
        ).any { it.isNotBlank() }

    private fun MeasureUiState.toEntity(): MeasureEntity =
        MeasureEntity(
            interventionId = interventionId,
            equipmentOrder = equipmentOrder,
            co = co.toDoubleOrNull(),
            coamb = coamb.toDoubleOrNull(),
            co2 = co2.toDoubleOrNull(),
            o2 = o2.toDoubleOrNull(),
            tair = tair.toIntOrNull(),
            temfu = temfu.toDoubleOrNull(),
            rend = rend.toDoubleOrNull(),
            nox = nox.toDoubleOrNull(),
            eta = eta.toDoubleOrNull(),
            thpa = thpa.toDoubleOrNull(),
            no = no.toDoubleOrNull(),
            no2 = no2.toDoubleOrNull(),
            o2ven = o2ven.toDoubleOrNull(),
            condilu = condilu.toDoubleOrNull(),
            tgaz = tgaz.toIntOrNull(),
            ta = ta.toDoubleOrNull(),
            pregas = pregas.toDoubleOrNull(),
            prega = prega.toDoubleOrNull(),
            pregn = pregn.toDoubleOrNull(),
            pregm = pregm.toDoubleOrNull(),
            puisgaz = puisgaz.toDoubleOrNull(),
            debga = debga.toDoubleOrNull(),
            temec = temec.toDoubleOrNull(),
            temef = temef.toDoubleOrNull(),
            delta = delta.toDoubleOrNull(),
            debio = debio.toDoubleOrNull(),
            debfuel = debfuel.toDoubleOrNull(),
            prefp = prefp.toDoubleOrNull(),
            puisfuel = puisfuel.toDoubleOrNull(),
            pulve = pulve.toDoubleOrNull(),
            spot = spot.toIntOrNull(),
            testdsc = testdsc.ifBlank { null },
            remplacond = remplacond.ifBlank { null },
            templagigleur = templagigleur.ifBlank { null },
            remplapoly = remplapoly.ifBlank { null },
            etaventil = etaventil.ifBlank { null },
            ctranode = ctranode.ifBlank { null },
            ctrextvmc = ctrextvmc.ifBlank { null },
            suie1 = suie1.toIntOrNull(),
            suie2 = suie2.toIntOrNull(),
            suie3 = suie3.toIntOrNull(),
            residhuil = residhuil.toIntOrNull(),
            opaci = opaci.toDoubleOrNull(),
            ionis = ionis.toDoubleOrNull(),
            pgevg = pgevg.toDoubleOrNull(),
            pgepg = pgepg.toDoubleOrNull(),
            depre = depre.toDoubleOrNull(),
            depr2 = depr2.toDoubleOrNull(),
            gican = gican.toDoubleOrNull(),
            gicle = gicle.toDoubleOrNull(),
            pabs = pabs.toDoubleOrNull(),
            perte = perte.toDoubleOrNull(),
            ppm = ppm.toIntOrNull(),
            obser = obser.ifBlank { null },
            updatedAt = java.time.Instant.now().toString(),
            isDirty = true,
        )

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
        super.onCleared()
    }
}
