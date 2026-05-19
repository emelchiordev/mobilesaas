package re.melchior.saviomobile.ui.screen.invoice

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import re.melchior.saviomobile.data.local.dao.SettingsDao
import re.melchior.saviomobile.data.local.entity.InvoiceEntity
import re.melchior.saviomobile.data.local.entity.InvoiceLineEntity
import re.melchior.saviomobile.data.repository.InvoiceRepository
import re.melchior.saviomobile.data.repository.SyncRepository
import re.melchior.saviomobile.ui.screen.intervention.cloture.DrawPoint
import re.melchior.saviomobile.ui.util.encodeSignaturePointsToBase64
import re.melchior.saviomobile.ui.util.saveSignatureBase64ToFile
import re.melchior.saviomobile.worker.SyncWorker
import javax.inject.Inject

data class DevisSignatureUiState(
    val invoice: InvoiceEntity? = null,
    val lines: List<InvoiceLineEntity> = emptyList(),
    val step: Int = 1,
    val isLoading: Boolean = false,
    val isCompleted: Boolean = false,
    val errorMessage: String? = null,
    val hasDevisSignature: Boolean = false,
    val hamonImmediateRequested: Boolean = false,
    val hasHamonSignature: Boolean = false,
)

@HiltViewModel
class DevisSignatureViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val invoiceRepository: InvoiceRepository,
    private val syncRepository: SyncRepository,
    private val settingsDao: SettingsDao,
    private val workManager: WorkManager,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val interventionId: String = checkNotNull(savedStateHandle["interventionId"])

    private val _uiState = MutableStateFlow(DevisSignatureUiState())
    val uiState: StateFlow<DevisSignatureUiState> = _uiState.asStateFlow()

    private val _devisPoints = MutableStateFlow<List<DrawPoint>>(emptyList())
    val devisPoints: StateFlow<List<DrawPoint>> = _devisPoints.asStateFlow()

    private val _hamonPoints = MutableStateFlow<List<DrawPoint>>(emptyList())
    val hamonPoints: StateFlow<List<DrawPoint>> = _hamonPoints.asStateFlow()

    private var devisCanvasWidth = 0
    private var devisCanvasHeight = 0
    private var hamonCanvasWidth = 0
    private var hamonCanvasHeight = 0
    private var devisSignatureBase64: String? = null
    private var linesJob: Job? = null

    init {
        viewModelScope.launch {
            invoiceRepository.observeInvoiceForIntervention(interventionId).collect { inv ->
                if (inv == null) {
                    linesJob?.cancel()
                    linesJob = null
                    _uiState.update {
                        it.copy(
                            invoice = null,
                            lines = emptyList(),
                            errorMessage = "Aucune facture pour cette intervention. Revenez à l'écran Facturation.",
                        )
                    }
                } else {
                    _uiState.update { it.copy(invoice = inv, errorMessage = null) }
                    observeLines(inv.id)
                }
            }
        }
    }

    private fun observeLines(invoiceId: String) {
        linesJob?.cancel()
        linesJob =
            viewModelScope.launch {
                invoiceRepository.getLinesForInvoice(invoiceId).collect { list ->
                    val billable =
                        list.filter {
                            it.billingType == "billable" &&
                                !it.isTextBlock &&
                                it.type != "subtotal" &&
                                it.type != "text_block"
                        }
                    _uiState.update { it.copy(lines = billable) }
                }
            }
    }

    fun onDevisCanvasSizeChanged(width: Int, height: Int) {
        devisCanvasWidth = width
        devisCanvasHeight = height
    }

    fun onHamonCanvasSizeChanged(width: Int, height: Int) {
        hamonCanvasWidth = width
        hamonCanvasHeight = height
    }

    fun addDevisPoint(point: DrawPoint) {
        _devisPoints.value = _devisPoints.value + point
        _uiState.update { it.copy(hasDevisSignature = true) }
    }

    fun clearDevisSignature() {
        _devisPoints.value = emptyList()
        _uiState.update { it.copy(hasDevisSignature = false) }
    }

    fun addHamonPoint(point: DrawPoint) {
        _hamonPoints.value = _hamonPoints.value + point
        _uiState.update { it.copy(hasHamonSignature = true) }
    }

    fun clearHamonSignature() {
        _hamonPoints.value = emptyList()
        _uiState.update { it.copy(hasHamonSignature = false) }
    }

    fun setHamonImmediateRequested(requested: Boolean) {
        _uiState.update {
            it.copy(
                hamonImmediateRequested = requested,
                hasHamonSignature = if (!requested) false else it.hasHamonSignature,
            )
        }
        if (!requested) {
            _hamonPoints.value = emptyList()
        }
    }

    fun continueToHamonStep() {
        val state = _uiState.value
        if (!state.hasDevisSignature || state.isLoading) return
        val base64 =
            encodeSignaturePointsToBase64(
                _devisPoints.value,
                devisCanvasWidth,
                devisCanvasHeight,
            )
        if (base64.isNullOrBlank()) {
            _uiState.update { it.copy(errorMessage = "Signature invalide") }
            return
        }
        devisSignatureBase64 = base64
        _uiState.update { it.copy(step = 2, errorMessage = null) }
    }

    fun goBackToDevisStep() {
        _uiState.update { it.copy(step = 1, errorMessage = null) }
    }

    fun finalizeAcceptance() {
        val state = _uiState.value
        if (state.isLoading || state.isCompleted) return
        if (devisSignatureBase64.isNullOrBlank()) {
            _uiState.update { it.copy(errorMessage = "Signature du devis requise") }
            return
        }
        val hamonRequested = state.hamonImmediateRequested
        val hamonBase64 =
            if (hamonRequested) {
                encodeSignaturePointsToBase64(
                    _hamonPoints.value,
                    hamonCanvasWidth,
                    hamonCanvasHeight,
                )
            } else {
                null
            }
        if (hamonRequested && hamonBase64.isNullOrBlank()) {
            _uiState.update { it.copy(errorMessage = "Signature Hamon requise") }
            return
        }
        if (state.invoice == null) {
            _uiState.update {
                it.copy(errorMessage = "Facture introuvable. Revenez à l'écran Facturation.")
            }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val intervention = syncRepository.getInterventionByIdOnce(interventionId)
            if (intervention == null) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Intervention introuvable")
                }
                return@launch
            }
            val freshInvoice = invoiceRepository.resolveInvoiceForIntervention(interventionId)
            if (freshInvoice == null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Facture introuvable. Revenez à l'écran Facturation.",
                    )
                }
                return@launch
            }
            val invoiceId = freshInvoice.id
            val devisPreview =
                saveSignatureBase64ToFile(
                    appContext.filesDir,
                    devisSignatureBase64!!,
                    "sig_devis_$invoiceId.png",
                )
            val hamonPreview =
                if (hamonRequested && hamonBase64 != null) {
                    saveSignatureBase64ToFile(
                        appContext.filesDir,
                        hamonBase64,
                        "sig_hamon_$invoiceId.png",
                    )
                } else {
                    null
                }
            val techId = settingsDao.getSettingsOnce()?.technicianId.orEmpty()
            val result =
                invoiceRepository.acceptDevis(
                    interventionId = interventionId,
                    unitId = intervention.unitId,
                    technicianId = techId,
                    devisSignatureBase64 = devisSignatureBase64!!,
                    hamonRequested = hamonRequested,
                    hamonSignatureBase64 = hamonBase64,
                    devisSignaturePreviewUrl = devisPreview,
                    hamonSignaturePreviewUrl = hamonPreview,
                    preferredInvoiceId = invoiceId,
                )
            result.onSuccess {
                SyncWorker.enqueueNow(workManager)
                _uiState.update { it.copy(isLoading = false, isCompleted = true) }
            }.onFailure { e ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Erreur lors de la signature",
                    )
                }
            }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    override fun onCleared() {
        super.onCleared()
        linesJob?.cancel()
    }
}
