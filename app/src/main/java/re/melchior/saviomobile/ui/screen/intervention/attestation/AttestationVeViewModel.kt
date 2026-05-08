package re.melchior.saviomobile.ui.screen.intervention.attestation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import re.melchior.saviomobile.data.local.AttestationVeBruleurLinked
import re.melchior.saviomobile.data.local.AttestationVeBruleurResolver
import re.melchior.saviomobile.data.local.AttestationVeControlPoints
import re.melchior.saviomobile.data.local.dao.CatalogEquipmentDao
import re.melchior.saviomobile.data.local.dao.EquipmentDao
import re.melchior.saviomobile.data.local.dao.InterventionDao
import re.melchior.saviomobile.data.local.entity.AttestationVeEntity
import re.melchior.saviomobile.data.repository.AttestationVeRepository

@HiltViewModel
class AttestationVeViewModel @Inject constructor(
    private val repository: AttestationVeRepository,
    private val equipmentDao: EquipmentDao,
    private val catalogEquipmentDao: CatalogEquipmentDao,
    private val interventionDao: InterventionDao,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val interventionId: String =
        savedStateHandle["interventionId"] ?: ""
    private val equipmentOrder: Int =
        savedStateHandle["equipmentOrder"] ?: 0
    private val type: String =
        savedStateHandle["type"] ?: ""

    private val _attestation =
        MutableStateFlow<AttestationVeEntity?>(null)
    val attestation: StateFlow<AttestationVeEntity?> =
        _attestation.asStateFlow()

    private val _points =
        MutableStateFlow<Map<String, String>>(emptyMap())
    val points: StateFlow<Map<String, String>> =
        _points.asStateFlow()

    val controlPoints = AttestationVeControlPoints.getPointsForType(type)

    val validatedCount: StateFlow<Int> = _points
        .map { map -> map.values.count { it == "V" } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    val nonValidatedCount: StateFlow<Int> = _points
        .map { map -> map.values.count { it == "N" } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    private val _evacuationMode = MutableStateFlow<String?>(null)
    val evacuationMode: StateFlow<String?> = _evacuationMode.asStateFlow()

    private val _linkedBruleur =
        MutableStateFlow<AttestationVeBruleurLinked?>(null)
    val linkedBruleur: StateFlow<AttestationVeBruleurLinked?> =
        _linkedBruleur.asStateFlow()

    init {
        viewModelScope.launch {
            val inProgress = interventionDao.findFirstInProgress()
            android.util.Log.d(
                "ATTEST_VM",
                "current in_progress=${inProgress?.id} " +
                    "my interventionId=$interventionId",
            )
        }

        viewModelScope.launch {
            var entity = repository.getOrCreate(
                interventionId,
                equipmentOrder,
                type,
            )

            if (entity.isDirty && entity.bruleurMarque == null && entity.bruleurEquipmentOrder == null) {
                val parentEquipment = equipmentDao.getEquipmentByInterventionAndOrder(
                    interventionId,
                    equipmentOrder,
                )
                val equipments =
                    equipmentDao.getEquipmentsByInterventionOnce(interventionId)
                val bruleur = equipments.find { eq ->
                    eq.parentEquipmentId != null &&
                        eq.typeCode?.uppercase() == "BRULEUR" &&
                        parentEquipment?.id == eq.parentEquipmentId
                }
                if (bruleur != null) {
                    val powerKw = bruleur.equipmentCatalogId?.let { cid ->
                        catalogEquipmentDao.getById(cid)?.powerKw
                    }
                    entity = entity.copy(
                        bruleurEquipmentOrder = bruleur.order,
                        bruleurMarque = bruleur.brand,
                        bruleurModele = bruleur.model,
                        bruleurSerialNumber = bruleur.serialNumber,
                        bruleurCommissioningDate = bruleur.installDate,
                        bruleurPuissanceKw = powerKw,
                        isDirty = true,
                    )
                    repository.save(entity)
                }
            }

            _attestation.value = entity

            repository.getFlow(
                interventionId,
                equipmentOrder,
                type,
            ).collect { updated ->
                if (updated != null) {
                    _attestation.value = updated
                }
            }
        }

        viewModelScope.launch {
            combine(
                attestation,
                equipmentDao.getEquipmentsByIntervention(interventionId),
            ) { att, _ ->
                att
            }.collect { att ->
                _linkedBruleur.value = att?.let {
                    AttestationVeBruleurResolver.findLinkedBruleur(
                        it,
                        equipmentDao,
                        catalogEquipmentDao,
                    )
                }
            }
        }

        viewModelScope.launch {
            repository.getPointsFlow(
                interventionId,
                equipmentOrder,
                type,
            ).collect { list ->
                _points.value = list.associate { it.cle to it.resultat }
            }
        }

        viewModelScope.launch {
            equipmentDao.getEquipmentsByIntervention(interventionId).collect { list ->
                _evacuationMode.value = list.find { it.order == equipmentOrder }
                    ?.evacuationMode
            }
        }
    }

    fun updateField(key: String, value: String) {
        val current = _attestation.value ?: return
        val updated = when (key) {
            "appareilMesure" ->
                current.copy(appareilMesure = value)
            "defautsCorriges" ->
                current.copy(defautsCorriges = value)
            "recommandationUsage" ->
                current.copy(recommandationUsage = value)
            "recommandationAmeliorations" ->
                current.copy(recommandationAmeliorations = value)
            "recommandationRemplacement" ->
                current.copy(recommandationRemplacement = value)
            "commentaire" ->
                current.copy(commentaire = value)
            "nomPersonnePresente" ->
                current.copy(nomPersonnePresente = value)
            "remarquesHydraulique" ->
                current.copy(remarquesHydraulique = value)
            "remarquesRegulation" ->
                current.copy(remarquesRegulation = value)
            "remarquesGenerateur" ->
                current.copy(remarquesGenerateur = value)
            "co" -> current.copy(co = value)
            "coConduitPpm" ->
                current.copy(coConduitPpm = value.toDoubleOrNull())
            "tempFumees" -> current.copy(tempFumees = value)
            "tempAmbiante" -> current.copy(tempAmbiante = value)
            "co2Fumees" -> current.copy(co2Fumees = value)
            "o2Fumees" -> current.copy(o2Fumees = value)
            "rendementEvalue" ->
                current.copy(rendementEvalue = value)
            "noxEmissions" ->
                current.copy(noxEmissions = value)
            "classeEnergetique" ->
                current.copy(classeEnergetique = value)
            "indiceNoircissement" ->
                current.copy(indiceNoircissement = value)
            "pressionGicleur" ->
                current.copy(pressionGicleur = value)
            "emissionsPoussieres" ->
                current.copy(emissionsPoussieres = value)
            "emissionsCov" ->
                current.copy(emissionsCov = value)
            "tExterieurChauf" ->
                current.copy(tExterieurChauf = value)
            "tExterieurRefroid" ->
                current.copy(tExterieurRefroid = value)
            "tInterieurChauf" ->
                current.copy(tInterieurChauf = value)
            "tInterieurRefroid" ->
                current.copy(tInterieurRefroid = value)
            "tensionStatique" ->
                current.copy(tensionStatique = value)
            "tensionDynamique" ->
                current.copy(tensionDynamique = value)
            "fluideRef" -> current.copy(fluideRef = value)
            "chargeTotale" ->
                current.copy(chargeTotale = value)
            "pressionBp" -> current.copy(pressionBp = value)
            "pressionHp" -> current.copy(pressionHp = value)
            else -> current
        }
        _attestation.value = updated
    }

    private var saveJob: Job? = null

    fun scheduleAutoSave() {
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            delay(500)
            save()
        }
    }

    fun save() {
        val current = _attestation.value ?: return
        viewModelScope.launch {
            repository.save(current)
        }
    }

    fun togglePoint(cle: String, resultat: String) {
        val attestationId = _attestation.value?.id ?: return
        val current = _points.value[cle] ?: ""

        val newResultat = if (current == resultat) "" else resultat

        _points.value = _points.value.toMutableMap().apply {
            if (newResultat.isEmpty()) {
                remove(cle)
            } else {
                put(cle, newResultat)
            }
        }

        viewModelScope.launch {
            repository.savePoint(
                interventionId = interventionId,
                equipmentOrder = equipmentOrder,
                type = type,
                attestationId = attestationId,
                cle = cle,
                resultat = newResultat,
            )
        }
    }

    override fun onCleared() {
        saveJob?.cancel()
        val current = _attestation.value
        if (current != null) {
            runBlocking(Dispatchers.IO) {
                repository.save(current)
            }
        }
        super.onCleared()
    }
}
