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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import re.melchior.saviomobile.data.local.dao.SettingsDao
import re.melchior.saviomobile.data.local.entity.InvoiceEntity
import re.melchior.saviomobile.data.local.entity.InvoiceLineEntity
import re.melchior.saviomobile.data.local.entity.InvoicePaymentEntity
import re.melchior.saviomobile.data.remote.dto.PrestationDto
import re.melchior.saviomobile.data.remote.dto.toPrestationDto
import re.melchior.saviomobile.data.repository.InvoiceRepository
import re.melchior.saviomobile.data.repository.SyncRepository
import re.melchior.saviomobile.data.repository.TenantArticleSyncRepository
import re.melchior.saviomobile.ui.utils.NetworkUtils
import re.melchior.saviomobile.worker.SyncWorker
import javax.inject.Inject

@HiltViewModel
class InvoiceViewModel @Inject constructor(
    private val invoiceRepository: InvoiceRepository,
    private val syncRepository: SyncRepository,
    private val tenantArticleSyncRepository: TenantArticleSyncRepository,
    private val settingsDao: SettingsDao,
    private val workManager: WorkManager,
    @ApplicationContext private val appContext: Context,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val interventionId: String = checkNotNull(savedStateHandle["interventionId"])

    private val _invoice = MutableStateFlow<InvoiceEntity?>(null)
    val invoice: StateFlow<InvoiceEntity?> = _invoice.asStateFlow()

    private val _lines = MutableStateFlow<List<InvoiceLineEntity>>(emptyList())
    val lines: StateFlow<List<InvoiceLineEntity>> = _lines.asStateFlow()

    private val _payments = MutableStateFlow<List<InvoicePaymentEntity>>(emptyList())
    val payments: StateFlow<List<InvoicePaymentEntity>> = _payments.asStateFlow()

    val remainingAmount: StateFlow<Double> = combine(_invoice, _payments) { inv, pays ->
        val totalPaid = pays.sumOf { it.amount }
        (inv?.totalTtc ?: 0.0) - totalPaid
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0.0)

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _searchResults = MutableStateFlow<List<PrestationDto>>(emptyList())
    val searchResults: StateFlow<List<PrestationDto>> = _searchResults.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _requireInvoiceValidation = MutableStateFlow(false)
    val requireInvoiceValidation: StateFlow<Boolean> = _requireInvoiceValidation.asStateFlow()

    private val _showEmitConfirmDialog = MutableStateFlow(false)
    val showEmitConfirmDialog: StateFlow<Boolean> = _showEmitConfirmDialog.asStateFlow()

    private val _showEmitAdjustmentsSheet = MutableStateFlow(false)
    val showEmitAdjustmentsSheet: StateFlow<Boolean> = _showEmitAdjustmentsSheet.asStateFlow()

    private val _emitSuccessMessage = MutableStateFlow<String?>(null)
    val emitSuccessMessage: StateFlow<String?> = _emitSuccessMessage.asStateFlow()

    private val _paymentSuccessMessage = MutableStateFlow<String?>(null)
    val paymentSuccessMessage: StateFlow<String?> = _paymentSuccessMessage.asStateFlow()

    private val _showMarkPaidConfirmDialog = MutableStateFlow(false)
    val showMarkPaidConfirmDialog: StateFlow<Boolean> = _showMarkPaidConfirmDialog.asStateFlow()

    private var linesCollectionJob: Job? = null
    private var paymentsJob: Job? = null
    private var billingJob: Job? = null

    fun loadInvoice(interventionId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _requireInvoiceValidation.value =
                settingsDao.getSettingsOnce()?.requireInvoiceValidation == true
            invoiceRepository.refreshInvoiceFromServer(interventionId)
            billingJob?.cancel()
            billingJob =
                launch {
                    invoiceRepository.observeBillingForIntervention(interventionId).collect { summary ->
                        val inv = summary.invoice
                        val previousId = _invoice.value?.id
                        _invoice.value = inv
                        if (inv != null) {
                            if (linesCollectionJob == null || previousId != inv.id) {
                                linesCollectionJob?.cancel()
                                linesCollectionJob =
                                    launch {
                                        invoiceRepository.getLinesForInvoice(inv.id).collect { list ->
                                            _lines.value = list
                                        }
                                    }
                            }
                            loadPayments(inv.id)
                        } else {
                            linesCollectionJob?.cancel()
                            linesCollectionJob = null
                            _lines.value = emptyList()
                            _payments.value = emptyList()
                        }
                    }
                }
            _isLoading.value = false
        }
    }

    fun loadPayments(invoiceId: String) {
        paymentsJob?.cancel()
        paymentsJob =
            viewModelScope.launch {
                invoiceRepository.getPaymentsForInvoice(invoiceId).collect { list ->
                    _payments.value = list
                }
            }
    }

    fun addPayment(amount: Double, paymentMethodCode: String) {
        val inv = _invoice.value ?: return
        val invoiceId = inv.id
        viewModelScope.launch {
            invoiceRepository.addPayment(
                invoiceId = invoiceId,
                amount = amount,
                paymentMethodCode = paymentMethodCode,
                paidAt = java.time.Instant.now().toString(),
            )
            val totalPaid = _payments.value.sumOf { it.amount } + amount
            val remaining = inv.totalTtc - totalPaid
            if (inv.mobileStatus() == InvoiceMobileStatus.INVOICED && remaining <= 0) {
                markInvoicePaidInternal(invoiceId, showSuccessSnackbar = true)
            }
        }
    }

    fun removePayment(paymentId: String) {
        viewModelScope.launch {
            invoiceRepository.removePayment(paymentId)
        }
    }

    fun createInvoice() {
        viewModelScope.launch {
            val intervention = syncRepository.getInterventionByIdOnce(interventionId) ?: return@launch
            val techId = settingsDao.getSettingsOnce()?.technicianId.orEmpty()
            val inv =
                invoiceRepository.createInvoice(
                    interventionId = interventionId,
                    unitId = intervention.unitId,
                    technicianId = techId,
                )
            _invoice.value = inv
            linesCollectionJob?.cancel()
            linesCollectionJob =
                launch {
                    invoiceRepository.getLinesForInvoice(inv.id).collect { list ->
                        _lines.value = list
                    }
                }
        }
    }

    fun addLineFromCatalogue(
        reference: String,
        label: String,
        quantity: Double,
        unitPriceHt: Double,
        vatRate: Double,
        billingType: String,
    ) {
        val invoiceId = _invoice.value?.id ?: return
        viewModelScope.launch {
            invoiceRepository.addLine(
                invoiceId = invoiceId,
                reference = reference,
                label = label,
                quantity = quantity,
                unitPriceHt = unitPriceHt,
                vatRate = vatRate,
                billingType = billingType,
            )
            _invoice.value = invoiceRepository.getInvoiceById(invoiceId)
        }
    }

    fun addFreeLine(
        reference: String?,
        label: String,
        quantity: Double,
        unitPriceHt: Double,
        vatRate: Double,
        billingType: String,
    ) {
        val invoiceId = _invoice.value?.id ?: return
        viewModelScope.launch {
            invoiceRepository.addLine(
                invoiceId = invoiceId,
                reference = reference?.trim()?.takeIf { it.isNotEmpty() },
                label = label,
                quantity = quantity,
                unitPriceHt = unitPriceHt,
                vatRate = vatRate,
                billingType = billingType,
            )
            _invoice.value = invoiceRepository.getInvoiceById(invoiceId)
        }
    }

    fun removeLine(lineId: String) {
        val invoiceId = _invoice.value?.id ?: return
        viewModelScope.launch {
            invoiceRepository.removeLine(invoiceId, lineId)
            _invoice.value = invoiceRepository.getInvoiceById(invoiceId)
        }
    }

    fun addTextBlockLine(text: String) {
        val invoiceId = _invoice.value?.id ?: return
        if (text.isBlank()) return
        viewModelScope.launch {
            invoiceRepository.addTextBlockLine(invoiceId, text)
        }
    }

    fun moveLine(fromIndex: Int, toIndex: Int) {
        val invoiceId = _invoice.value?.id ?: return
        viewModelScope.launch {
            invoiceRepository.moveLine(invoiceId, fromIndex, toIndex)
        }
    }

    fun requestMarkAsPaid() {
        val inv = _invoice.value ?: return
        if (inv.mobileStatus() != InvoiceMobileStatus.INVOICED) return
        val remaining = remainingAmount.value
        if (remaining <= 0) return
        _showMarkPaidConfirmDialog.value = true
    }

    fun dismissMarkPaidConfirmDialog() {
        _showMarkPaidConfirmDialog.value = false
    }

    fun confirmMarkAsPaid() {
        val invoiceId = _invoice.value?.id ?: return
        _showMarkPaidConfirmDialog.value = false
        viewModelScope.launch {
            markInvoicePaidInternal(invoiceId, showSuccessSnackbar = false)
        }
    }

    fun dismissPaymentSuccessMessage() {
        _paymentSuccessMessage.value = null
    }

    private suspend fun markInvoicePaidInternal(invoiceId: String, showSuccessSnackbar: Boolean) {
        _isLoading.value = true
        _error.value = null
        invoiceRepository.markInvoicePaid(invoiceId)
            .onSuccess {
                _invoice.value = invoiceRepository.getInvoiceById(invoiceId)
                SyncWorker.enqueueNow(workManager)
                if (showSuccessSnackbar) {
                    _paymentSuccessMessage.value = "Facture soldée"
                }
            }
            .onFailure {
                _error.value = "Erreur lors du marquage payée"
            }
        _isLoading.value = false
    }

    fun searchCatalogue(query: String) {
        viewModelScope.launch {
            if (query.length < 2) {
                _searchResults.value = emptyList()
                return@launch
            }
            val localArticles =
                tenantArticleSyncRepository.search(query).map { it.toPrestationDto() }
            _searchResults.value =
                if (NetworkUtils.isOnline(appContext)) {
                    try {
                        val results = invoiceRepository.searchRef(query)
                        mergeCatalogueHits(
                            results.prestations + results.pieces + results.articles,
                            localArticles,
                        )
                    } catch (_: Exception) {
                        localArticles
                    }
                } else {
                    localArticles
                }
        }
    }

    private fun mergeCatalogueHits(
        remote: List<PrestationDto>,
        local: List<PrestationDto>,
    ): List<PrestationDto> {
        if (local.isEmpty()) return remote
        val seen = remote.map { "${it.type}:${it.reference}" }.toMutableSet()
        val merged = remote.toMutableList()
        for (item in local) {
            val key = "${item.type}:${item.reference}"
            if (key !in seen) {
                seen.add(key)
                merged.add(item)
            }
        }
        return merged
    }

    fun dismissError() {
        _error.value = null
    }

    fun dismissEmitSuccessMessage() {
        _emitSuccessMessage.value = null
    }

    fun onEmitInvoiceClicked() {
        _showEmitConfirmDialog.value = true
    }

    fun dismissEmitConfirmDialog() {
        _showEmitConfirmDialog.value = false
    }

    fun confirmEmitDialog() {
        _showEmitConfirmDialog.value = false
        _showEmitAdjustmentsSheet.value = true
    }

    fun dismissEmitAdjustmentsSheet() {
        _showEmitAdjustmentsSheet.value = false
    }

    fun emitInvoice() {
        val invoiceId = _invoice.value?.id ?: return
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            invoiceRepository.emitInvoice(invoiceId)
                .onSuccess {
                    _showEmitAdjustmentsSheet.value = false
                    _invoice.value = invoiceRepository.getInvoiceById(invoiceId)
                    SyncWorker.enqueueNow(workManager)
                }
                .onFailure {
                    _error.value = it.message ?: "Erreur lors de l'émission"
                }
            _isLoading.value = false
        }
    }

    fun submitForValidation() {
        val invoiceId = _invoice.value?.id ?: return
        viewModelScope.launch {
            val intervention =
                syncRepository.getInterventionByIdOnce(interventionId) ?: run {
                    _error.value = "Intervention introuvable"
                    return@launch
                }
            val techId = settingsDao.getSettingsOnce()?.technicianId.orEmpty()
            _isLoading.value = true
            _error.value = null
            invoiceRepository.submitForValidation(
                invoiceId = invoiceId,
                interventionId = interventionId,
                unitId = intervention.unitId,
                technicianId = techId,
            )
                .onSuccess {
                    _invoice.value = invoiceRepository.getInvoiceById(invoiceId)
                    _emitSuccessMessage.value =
                        "Facture soumise au responsable pour validation"
                    SyncWorker.enqueueNow(workManager)
                }
                .onFailure {
                    _error.value = it.message ?: "Erreur lors de la soumission"
                }
            _isLoading.value = false
        }
    }

    override fun onCleared() {
        super.onCleared()
        linesCollectionJob?.cancel()
        paymentsJob?.cancel()
        billingJob?.cancel()
    }
}
