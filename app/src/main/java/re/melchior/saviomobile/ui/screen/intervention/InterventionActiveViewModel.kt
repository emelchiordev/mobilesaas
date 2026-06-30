package re.melchior.saviomobile.ui.screen.intervention

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import re.melchior.saviomobile.data.local.dao.PendingOperationDao
import re.melchior.saviomobile.data.local.dao.SettingsDao
import re.melchior.saviomobile.data.local.entity.EquipmentEntity
import re.melchior.saviomobile.data.local.entity.InterventionEntity
import re.melchior.saviomobile.data.local.entity.InvoiceEntity
import re.melchior.saviomobile.data.repository.InvoiceRepository
import re.melchior.saviomobile.data.repository.SyncRepository
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class InterventionActiveUiState(
    val intervention: InterventionEntity? = null,
    val equipments: List<EquipmentEntity> = emptyList(),
    val elapsedSeconds: Long = 0L,
    val startTimeLabel: String = "",
    val showQuitDialog: Boolean = false,
    val isLoading: Boolean = false,
    val invoice: InvoiceEntity? = null,
    val invoiceLineCount: Int = 0,
)

@HiltViewModel
class InterventionActiveViewModel @Inject constructor(
    private val syncRepository: SyncRepository,
    private val invoiceRepository: InvoiceRepository,
    private val settingsDao: SettingsDao,
    private val pendingOperationDao: PendingOperationDao,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val interventionId: String = checkNotNull(savedStateHandle["interventionId"])

    private val _uiState = MutableStateFlow(InterventionActiveUiState())
    val uiState: StateFlow<InterventionActiveUiState> = _uiState.asStateFlow()

    /** IDs d’équipements créés pendant l’intervention (ajout pur ou nouvel appareil après remplacement). */
    val newEquipmentIds: StateFlow<Set<String>> =
        pendingOperationDao
            .getPendingByInterventionId(interventionId)
            .map { ops ->
                ops
                    .filter {
                        it.type == "CREATE_EQUIPMENT" ||
                            it.type == "REPLACE_EQUIPMENT"
                    }
                    .map { it.id }
                    .toSet()
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptySet(),
            )

    private val _navigateToInvoice = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val navigateToInvoice: SharedFlow<String> = _navigateToInvoice.asSharedFlow()

    private val _navigateBackToPlanning = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val navigateBackToPlanning: SharedFlow<Unit> = _navigateBackToPlanning.asSharedFlow()

    private var chronoJob: Job? = null

    init {
        loadIntervention()
        loadEquipments()
        observeBilling()
    }

    private fun observeBilling() {
        viewModelScope.launch {
            invoiceRepository
                .observeBillingForIntervention(interventionId)
                .collect { summary ->
                    _uiState.update {
                        it.copy(
                            invoice = summary.invoice,
                            invoiceLineCount = summary.lineCount,
                        )
                    }
                }
        }
        viewModelScope.launch {
            invoiceRepository.refreshInvoiceFromServer(interventionId)
        }
    }

    private fun loadIntervention() {
        viewModelScope.launch {
            syncRepository.getInterventionById(interventionId)
                .collect { intervention ->
                    intervention?.let {
                        _uiState.update { state ->
                            state.copy(intervention = it)
                        }
                        it.startedAt?.let { startedAt ->
                            startChrono(startedAt)
                        }
                    } ?: _uiState.update {
                        it.copy(intervention = null, invoice = null, invoiceLineCount = 0)
                    }
                }
        }
    }

    private fun loadEquipments() {
        viewModelScope.launch {
            syncRepository.hydrateInterventionEquipmentsIfEmpty(interventionId)
            syncRepository.getEquipmentsByIntervention(interventionId)
                .collect { equipments ->
                    _uiState.update { it.copy(equipments = equipments) }
                }
        }
    }

    private fun startChrono(startedAt: String) {
        // Calculer le label heure de début
        val startInstant = Instant.parse(startedAt)
        val startLocal = startInstant.atZone(ZoneId.systemDefault()).toLocalTime()
        val startLabel = startLocal.format(DateTimeFormatter.ofPattern("HH'h'mm"))
        _uiState.update { it.copy(startTimeLabel = startLabel) }

        // Calculer le temps écoulé depuis startedAt
        val startEpoch = startInstant.epochSecond

        // Annuler le job précédent si existe
        chronoJob?.cancel()
        chronoJob = viewModelScope.launch {
            while (true) {
                val now = Instant.now().epochSecond
                val elapsed = now - startEpoch
                _uiState.update { it.copy(elapsedSeconds = elapsed) }
                delay(1000L)
            }
        }
    }

    fun onCloseClick() {
        _uiState.update { it.copy(showQuitDialog = true) }
    }

    fun onQuitDismissed() {
        _uiState.update { it.copy(showQuitDialog = false) }
    }

    fun onQuitConfirmed() {
        viewModelScope.launch {
            chronoJob?.cancel()
            syncRepository.abandonInterventionLocally(interventionId)
            _uiState.update { it.copy(showQuitDialog = false) }
            _navigateBackToPlanning.emit(Unit)
        }
    }

    fun createAndNavigateToInvoice(
        interventionId: String,
        unitId: String,
        technicianId: String,
    ) {
        viewModelScope.launch {
            val tech = technicianId.ifBlank {
                settingsDao.getSettingsOnce()?.technicianId.orEmpty()
            }
            val invoice = invoiceRepository.createInvoice(
                interventionId = interventionId,
                unitId = unitId,
                technicianId = tech,
            )
            _uiState.update { it.copy(invoice = invoice) }
            _navigateToInvoice.emit(interventionId)
        }
    }

    override fun onCleared() {
        super.onCleared()
        chronoJob?.cancel()
    }
}

// Extension utilitaire pour formater le temps écoulé
fun Long.toElapsedLabel(): String {
    val hours = this / 3600
    val minutes = (this % 3600) / 60
    val seconds = this % 60
    return if (hours > 0) {
        "%02d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%02d:%02d".format(minutes, seconds)
    }
}