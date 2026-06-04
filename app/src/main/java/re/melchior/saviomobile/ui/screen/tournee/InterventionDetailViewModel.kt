package re.melchior.saviomobile.ui.screen.intervention

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import re.melchior.saviomobile.data.local.entity.EquipmentEntity
import re.melchior.saviomobile.data.local.entity.InterventionEntity
import re.melchior.saviomobile.data.local.entity.InterventionHistoryEntity
import re.melchior.saviomobile.data.remote.api.DocumentApi
import re.melchior.saviomobile.data.repository.InvoiceRepository
import re.melchior.saviomobile.data.repository.MobileSyncOrchestrator
import re.melchior.saviomobile.data.repository.SyncRepository
import java.time.LocalDate
import javax.inject.Inject

data class InterventionDetailUiState(
    val intervention: InterventionEntity? = null,
    val equipments: List<EquipmentEntity> = emptyList(),
    val history: List<InterventionHistoryEntity> = emptyList(),
    val historyPhotoUrls: Map<String, List<String>> = emptyMap(),
    val isLoading: Boolean = false,
    val isSyncRetrying: Boolean = false,
    val pendingInvoiceHamonIssue: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class InterventionDetailViewModel @Inject constructor(
    private val syncRepository: SyncRepository,
    private val invoiceRepository: InvoiceRepository,
    private val mobileSyncOrchestrator: MobileSyncOrchestrator,
    private val documentApi: DocumentApi,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val interventionId: String = checkNotNull(savedStateHandle["interventionId"])

    private val _uiState = MutableStateFlow(InterventionDetailUiState())
    val uiState: StateFlow<InterventionDetailUiState> = _uiState.asStateFlow()

    init {
        loadIntervention()
        loadEquipments()
    }

    private fun loadIntervention() {
        viewModelScope.launch {
            syncRepository.getInterventionById(interventionId)
                .collect { intervention ->
                    _uiState.update { it.copy(intervention = intervention) }
                    intervention?.let {
                        loadHistory(it.unitId)
                        refreshInvoiceSyncState()
                    }
                }
        }
    }

    private suspend fun refreshInvoiceSyncState() {
        val invoice = invoiceRepository.resolveInvoiceForIntervention(interventionId)
        val hamonIssue =
            invoice != null && invoiceRepository.isLocalHamonSignatureMissing(invoice)
        _uiState.update { it.copy(pendingInvoiceHamonIssue = hamonIssue) }
    }

    fun retrySync() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncRetrying = true, errorMessage = null) }
            try {
                mobileSyncOrchestrator.runFullSync(pullDate = LocalDate.now(), pullForce = true)
                refreshInvoiceSyncState()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = e.message ?: "Échec de la synchronisation")
                }
            } finally {
                _uiState.update { it.copy(isSyncRetrying = false) }
            }
        }
    }

    private fun loadEquipments() {
        viewModelScope.launch {
            syncRepository.getEquipmentsByIntervention(interventionId)
                .collect { equipments ->
                    _uiState.update { it.copy(equipments = equipments) }
                }
        }
    }

    private fun loadHistory(unitId: String) {
        viewModelScope.launch {
            val history = syncRepository.getHistoryForUnit(unitId)
            _uiState.update { it.copy(history = history) }
            loadHistoryPhotoUrls(history)
        }
    }

    private fun loadHistoryPhotoUrls(history: List<InterventionHistoryEntity>) {
        viewModelScope.launch {
            val urlMap = mutableMapOf<String, List<String>>()
            history.forEach { item ->
                if (!item.photoKeys.isNullOrBlank()) {
                    try {
                        val keys = com.google.gson.Gson()
                            .fromJson(item.photoKeys, Array<String>::class.java)
                            ?: return@forEach
                        val urls = keys.mapNotNull { key ->
                            try {
                                documentApi.getSignedUrl(key).url
                            } catch (e: Exception) {
                                null
                            }
                        }
                        if (urls.isNotEmpty()) urlMap[item.id] = urls
                    } catch (e: Exception) {
                    }
                }
            }
            _uiState.update { it.copy(historyPhotoUrls = urlMap) }
        }
    }

    fun startIntervention() {
        viewModelScope.launch {
            syncRepository.startIntervention(interventionId)
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
