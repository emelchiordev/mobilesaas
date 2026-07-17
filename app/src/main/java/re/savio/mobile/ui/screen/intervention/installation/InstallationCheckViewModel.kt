package re.savio.mobile.ui.screen.intervention.installation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import re.savio.mobile.data.local.entity.InstallationCheckEntity
import re.savio.mobile.data.repository.AnomalyDraftRepository
import re.savio.mobile.data.repository.InstallationCheckRepository
import re.savio.mobile.data.repository.SyncRepository
import re.savio.mobile.util.GAS_PIPE_EXPIRED_ANOMALY_CODE
import re.savio.mobile.util.GasPipeValidityStatus
import re.savio.mobile.util.SavioTimeZone
import re.savio.mobile.util.evaluateGasPipeValidity
import java.time.LocalDate

data class InstallationCheckUiState(
    val entity: InstallationCheckEntity? = null,
    val validityStatus: GasPipeValidityStatus = GasPipeValidityStatus.None,
    val hasGasPipeAnomalyDraft: Boolean = false,
)

@HiltViewModel
class InstallationCheckViewModel @Inject constructor(
    private val repository: InstallationCheckRepository,
    private val anomalyDraftRepository: AnomalyDraftRepository,
    private val syncRepository: SyncRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(InstallationCheckUiState())
    val uiState: StateFlow<InstallationCheckUiState> = _uiState.asStateFlow()

    private var boundInterventionId: String? = null
    private var initialFingerprint: String? = null
    private var saveJob: Job? = null

    fun bindIntervention(interventionId: String) {
        if (interventionId.isBlank() || boundInterventionId == interventionId) return
        boundInterventionId = interventionId
        viewModelScope.launch {
            val existing = repository.get(interventionId)
            val entity = existing ?: repository.newDraft(interventionId)
            initialFingerprint = entity.fingerprint()
            applyEntity(entity)
        }
        viewModelScope.launch {
            anomalyDraftRepository.observeDrafts(interventionId).collect { drafts ->
                val hasDraft =
                    drafts.any { it.anomalyTypeCode == GAS_PIPE_EXPIRED_ANOMALY_CODE }
                _uiState.update { state ->
                    state.copy(hasGasPipeAnomalyDraft = hasDraft)
                }
            }
        }
    }

    fun setTurbidityTested(checked: Boolean) {
        update {
            if (checked) {
                it.copy(turbidityTested = true)
            } else {
                it.copy(
                    turbidityTested = false,
                    turbidityNtu = "",
                    turbidityState = null,
                )
            }
        }
    }

    fun setTurbidityNtu(value: String) {
        update { it.copy(turbidityNtu = value.filter { ch -> ch.isDigit() || ch == '.' || ch == ',' }) }
    }

    fun setTurbidityState(value: String?) {
        update { it.copy(turbidityState = value) }
    }

    fun setGasPipeType(value: String?) {
        update { it.copy(gasPipeType = value) }
    }

    fun setGasPipeValidityDate(value: String) {
        update { it.copy(gasPipeValidityDate = value) }
    }

    fun setGasPipeReplaced(checked: Boolean) {
        update { it.copy(gasPipeReplaced = checked) }
    }

    fun setGasTapCompliant(value: String?) {
        update { it.copy(gasTapCompliant = value) }
    }

    fun setNotes(value: String) {
        update { it.copy(notes = value) }
    }

    fun createGasPipeAnomalyManually() {
        viewModelScope.launch {
            createGasPipeAnomalyDraft()
        }
    }

    private suspend fun createGasPipeAnomalyDraft() {
        val interventionId = boundInterventionId ?: return
        val unitId = syncRepository.getInterventionByIdOnce(interventionId)?.unitId ?: return
        anomalyDraftRepository.ensureGasPipeExpiredDraft(interventionId, unitId)
    }

    private fun update(block: (InstallationCheckEntity) -> InstallationCheckEntity) {
        val current = _uiState.value.entity ?: return
        applyEntity(block(current))
        scheduleAutoSave()
    }

    private fun applyEntity(entity: InstallationCheckEntity) {
        _uiState.update { state ->
            state.copy(
                entity = entity,
                validityStatus =
                    evaluateGasPipeValidity(
                        gasPipeType = entity.gasPipeType,
                        gasPipeValidityDateIso = entity.gasPipeValidityDate,
                        today = LocalDate.now(SavioTimeZone.appZone),
                    ),
            )
        }
    }

    private fun scheduleAutoSave() {
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            delay(2000)
            persistIfChanged()
        }
    }

    private suspend fun persistIfChanged() {
        val current = _uiState.value.entity ?: return
        if (current.fingerprint() == initialFingerprint) return
        if (!current.hasMeaningfulData() && repository.get(current.interventionId) == null) return
        repository.upsertLocal(current)
        initialFingerprint = current.fingerprint()
    }

    override fun onCleared() {
        saveJob?.cancel()
        super.onCleared()
    }

    private fun InstallationCheckEntity.fingerprint(): String =
        listOf(
            turbidityTested.toString(),
            turbidityNtu,
            turbidityState.orEmpty(),
            gasPipeType.orEmpty(),
            gasPipeValidityDate,
            gasPipeReplaced.toString(),
            gasPipeAnomalyDeclinedDate,
            gasTapCompliant.orEmpty(),
            notes,
        ).joinToString("|")

    private fun InstallationCheckEntity.hasMeaningfulData(): Boolean =
        turbidityTested ||
            turbidityNtu.isNotBlank() ||
            !turbidityState.isNullOrBlank() ||
            !gasPipeType.isNullOrBlank() ||
            gasPipeValidityDate.isNotBlank() ||
            gasPipeReplaced ||
            !gasTapCompliant.isNullOrBlank() ||
            notes.isNotBlank()
}
