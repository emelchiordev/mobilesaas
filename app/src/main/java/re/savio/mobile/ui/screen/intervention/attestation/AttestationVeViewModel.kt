package re.savio.mobile.ui.screen.intervention.attestation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import re.savio.mobile.data.local.AttestationVeBruleurLinked
import re.savio.mobile.data.local.AttestationVeBruleurResolver
import re.savio.mobile.data.local.AttestationVeControlPoints
import re.savio.mobile.data.local.dao.CatalogEquipmentDao
import re.savio.mobile.data.local.dao.EquipmentDao
import re.savio.mobile.data.local.entity.AttestationVeEntity
import re.savio.mobile.data.repository.AttestationVeRepository
import re.savio.mobile.ui.util.contentFingerprint
import re.savio.mobile.ui.util.hasMeaningfulData

@HiltViewModel
class AttestationVeViewModel @Inject constructor(
    private val repository: AttestationVeRepository,
    private val equipmentDao: EquipmentDao,
    private val catalogEquipmentDao: CatalogEquipmentDao,
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

    private val _controlPoints =
        MutableStateFlow(AttestationVeControlPoints.getPointsForType(type))
    val controlPoints: StateFlow<List<AttestationVeControlPoints.ControlPoint>> =
        _controlPoints.asStateFlow()

    private val _installationControlPoints =
        MutableStateFlow(
            AttestationVeControlPoints.getInstallationPointsForType(type),
        )
    val installationControlPoints:
        StateFlow<List<AttestationVeControlPoints.ControlPoint>> =
        _installationControlPoints.asStateFlow()

    private val _energyCode = MutableStateFlow<String?>(null)
    val energyCode: StateFlow<String?> = _energyCode.asStateFlow()

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

    private var initialAttestation: AttestationVeEntity? = null
    private var initialPoints: Map<String, String> = emptyMap()
    private var persistedInRoom = false

    init {
        viewModelScope.launch {
            loadExisting()
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
            equipmentDao.getEquipmentsByIntervention(interventionId).collect { list ->
                val equipment = list.find { it.order == equipmentOrder }
                _evacuationMode.value = equipment?.evacuationMode
                _energyCode.value = equipment?.energyCode
                val energy = equipment?.energyCode
                val typeCode = equipment?.typeCode
                _controlPoints.value =
                    AttestationVeControlPoints.getPointsForType(
                        type,
                        energy,
                        typeCode,
                    )
                _installationControlPoints.value =
                    AttestationVeControlPoints.getInstallationPointsForType(
                        type,
                        energy,
                        typeCode,
                    )
            }
        }
    }

    private suspend fun loadExisting() {
        val existing = repository.get(interventionId, equipmentOrder, type)
        persistedInRoom = existing != null

        var entity = existing ?: repository.newDraft(interventionId, equipmentOrder, type)

        if (entity.bruleurMarque == null && entity.bruleurEquipmentOrder == null) {
            entity = prefillBruleurInMemory(entity)
        }

        val loadedPoints =
            if (persistedInRoom) {
                repository.getPointsOnce(interventionId, equipmentOrder, type)
                    .associate { it.cle to it.resultat }
            } else {
                emptyMap()
            }

        _attestation.value = entity
        _points.value = loadedPoints
        initialAttestation = entity.contentFingerprint()
        initialPoints = loadedPoints.toMap()

        if (persistedInRoom) {
            repository.getFlow(interventionId, equipmentOrder, type).collect { updated ->
                if (updated != null && updated.contentFingerprint() == initialAttestation) {
                    _attestation.value = updated
                }
            }
        }
    }

    private suspend fun prefillBruleurInMemory(
        entity: AttestationVeEntity,
    ): AttestationVeEntity {
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
        } ?: return entity
        val powerKw = bruleur.equipmentCatalogId?.let { cid ->
            catalogEquipmentDao.getById(cid)?.powerKw
        }
        return entity.copy(
            bruleurEquipmentOrder = bruleur.order,
            bruleurMarque = bruleur.brand,
            bruleurModele = bruleur.model,
            bruleurSerialNumber = bruleur.serialNumber,
            bruleurCommissioningDate = bruleur.installDate,
            bruleurPuissanceKw = powerKw,
        )
    }

    fun hasChanges(): Boolean {
        val current = _attestation.value ?: return false
        val attestationChanged =
            current.contentFingerprint() != initialAttestation?.contentFingerprint()
        return attestationChanged || _points.value != initialPoints
    }

    fun saveIfChanged() {
        if (!hasChanges()) return
        viewModelScope.launch { persistToRoom() }
    }

    fun updateField(key: String, value: String) {
        val current = _attestation.value ?: return
        val updated = when (key) {
            "appareilMesure" ->
                current.copy(appareilMesure = value)
            "appareilMesureTension" ->
                current.copy(appareilMesureTension = value)
            "appareilMesureGenerateur" ->
                current.copy(appareilMesureGenerateur = value)
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
            saveIfChanged()
        }
    }

    /** Sauvegarde explicite (bouton ou action utilisateur). */
    fun save() {
        val current = _attestation.value ?: return
        if (!hasChanges() && !current.hasMeaningfulData(_points.value)) return
        viewModelScope.launch { persistToRoom() }
    }

    private suspend fun persistToRoom() {
        val current = _attestation.value ?: return
        if (!current.hasMeaningfulData(_points.value) && !persistedInRoom) return

        repository.save(current)
        val attestationId = current.id
        val allKeys = (initialPoints.keys + _points.value.keys).toSet()
        for (cle in allKeys) {
            repository.savePoint(
                interventionId = interventionId,
                equipmentOrder = equipmentOrder,
                type = type,
                attestationId = attestationId,
                cle = cle,
                resultat = _points.value[cle].orEmpty(),
            )
        }
        initialAttestation = current.contentFingerprint()
        initialPoints = _points.value.toMap()
        persistedInRoom = true
    }

    fun togglePoint(cle: String, resultat: String) {
        val current = _points.value[cle] ?: ""
        val newResultat = if (current == resultat) "" else resultat
        _points.value = _points.value.toMutableMap().apply {
            if (newResultat.isEmpty()) {
                remove(cle)
            } else {
                put(cle, newResultat)
            }
        }
        scheduleAutoSave()
    }

    override fun onCleared() {
        saveJob?.cancel()
        super.onCleared()
    }
}
