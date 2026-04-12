package re.melchior.saviomobile.ui.screen.invoice

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
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
import re.melchior.saviomobile.data.repository.InvoiceRepository
import re.melchior.saviomobile.data.repository.SyncRepository
import javax.inject.Inject

@HiltViewModel
class InvoiceViewModel @Inject constructor(
    private val invoiceRepository: InvoiceRepository,
    private val syncRepository: SyncRepository,
    private val settingsDao: SettingsDao,
    savedStateHandle: SavedStateHandle
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

    private val _requireValidation = MutableStateFlow(false)
    val requireValidation: StateFlow<Boolean> = _requireValidation.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var linesCollectionJob: Job? = null
    private var paymentsJob: Job? = null

    init {
        viewModelScope.launch {
            val settings = settingsDao.getSettingsOnce()
            _requireValidation.value = settings?.requireInvoiceValidation ?: false
        }
    }

    fun loadInvoice(interventionId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            linesCollectionJob?.cancel()
            val inv = invoiceRepository.getInvoiceForIntervention(interventionId)
            _invoice.value = inv
            if (inv != null) {
                linesCollectionJob = launch {
                    invoiceRepository.getLinesForInvoice(inv.id).collect { list ->
                        _lines.value = list
                    }
                }
            } else {
                _lines.value = emptyList()
                _payments.value = emptyList()
                paymentsJob?.cancel()
            }
            _isLoading.value = false
        }
    }

    fun loadPayments(invoiceId: String) {
        paymentsJob?.cancel()
        paymentsJob = viewModelScope.launch {
            invoiceRepository.getPaymentsForInvoice(invoiceId).collect { list ->
                _payments.value = list
            }
        }
    }

    fun addPayment(
        amount: Double,
        paymentMethodCode: String,
    ) {
        val invoiceId = _invoice.value?.id ?: return
        viewModelScope.launch {
            invoiceRepository.addPayment(
                invoiceId = invoiceId,
                amount = amount,
                paymentMethodCode = paymentMethodCode,
                paidAt = java.time.Instant.now().toString(),
            )
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
            val inv = invoiceRepository.createInvoice(
                interventionId = interventionId,
                unitId = intervention.unitId,
                technicianId = techId
            )
            _invoice.value = inv
            linesCollectionJob?.cancel()
            linesCollectionJob = launch {
                invoiceRepository.getLinesForInvoice(inv.id).collect { list ->
                    _lines.value = list
                }
            }
        }
    }

    fun addLineFromCatalogue(item: PrestationDto) {
        val invoiceId = _invoice.value?.id ?: return
        viewModelScope.launch {
            invoiceRepository.addLine(
                invoiceId = invoiceId,
                reference = item.reference,
                label = item.label,
                quantity = 1.0,
                unitPriceHt = item.unitPriceHt ?: 0.0,
                vatRate = item.vatRate,
                billingType = "billable"
            )
            _invoice.value = invoiceRepository.getInvoiceById(invoiceId)
        }
    }

    fun addFreeLine(
        label: String,
        quantity: Double,
        unitPriceHt: Double,
        vatRate: Double
    ) {
        val invoiceId = _invoice.value?.id ?: return
        viewModelScope.launch {
            invoiceRepository.addLine(
                invoiceId = invoiceId,
                reference = null,
                label = label,
                quantity = quantity,
                unitPriceHt = unitPriceHt,
                vatRate = vatRate
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

    fun searchCatalogue(query: String) {
        viewModelScope.launch {
            if (query.length < 2) {
                _searchResults.value = emptyList()
                return@launch
            }
            try {
                val results = invoiceRepository.searchRef(query)
                _searchResults.value = results.prestations + results.pieces
            } catch (e: Exception) {
                _searchResults.value = emptyList()
            }
        }
    }

    fun submitInvoice(validateDirectly: Boolean) {
        val inv = _invoice.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val intervention = syncRepository.getInterventionByIdOnce(interventionId)
            if (intervention == null) {
                _error.value = "Erreur lors de la soumission"
                _isLoading.value = false
                return@launch
            }
            val techId = settingsDao.getSettingsOnce()?.technicianId.orEmpty()
            val result = invoiceRepository.submitInvoice(
                invoiceId = inv.id,
                interventionId = inv.interventionId ?: interventionId,
                unitId = intervention.unitId,
                technicianId = techId,
                validateDirectly = validateDirectly
            )
            result.onSuccess {
                _invoice.value = _invoice.value?.copy(
                    status = if (validateDirectly) "validated"
                    else "pending_validation"
                )
            }
            result.onFailure {
                _error.value = "Erreur lors de la soumission"
            }
            _isLoading.value = false
        }
    }

    fun dismissError() {
        _error.value = null
    }
}
