package re.savio.mobile.ui.screen.intervention

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import re.savio.mobile.data.local.dao.SettingsDao
import re.savio.mobile.data.local.entity.InterventionEntity
import re.savio.mobile.data.local.entity.InterventionHistoryEntity
import re.savio.mobile.data.remote.api.CustomerApi
import re.savio.mobile.data.remote.api.DocumentApi
import re.savio.mobile.data.remote.api.InvoiceApi
import re.savio.mobile.data.repository.ClientFinancialRepository
import re.savio.mobile.data.repository.PendingUpdateRepository
import re.savio.mobile.data.repository.SyncRepository
import re.savio.mobile.data.repository.UnitVeRepository
import re.savio.mobile.ui.component.ClientFinancialRowUi
import re.savio.mobile.ui.component.toRowUi
import re.savio.mobile.ui.screen.intervention.cloture.ContractSummary
import re.savio.mobile.ui.screen.intervention.cloture.LastVeSummary
import re.savio.mobile.ui.screen.intervention.cloture.NextVeDisplay
import re.savio.mobile.ui.screen.intervention.cloture.VeCoverageSummary
import re.savio.mobile.ui.screen.intervention.cloture.formatVeCoverageLabel
import re.savio.mobile.ui.utils.NetworkUtils
import re.savio.mobile.util.UserProfile
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

data class ClientDetailUiState(
    val customerId: String = "",
    val unitId: String = "",
    val displayName: String = "",
    val addressLine: String = "",
    val intervention: InterventionEntity? = null,
    val isLoading: Boolean = true,
    val history: List<InterventionHistoryEntity> = emptyList(),
    val historyPhotoUrls: Map<String, List<String>> = emptyMap(),
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val savedSuccess: Boolean = false,
    val errorMessage: String? = null,
    val emailError: String? = null,
    val phone: String = "",
    val email: String = "",
    val notes: String = "",
    val floor: String = "",
    val doorCode: String = "",
    val addressLine2: String = "",
    val updatesRequireValidation: Boolean = false,
    val contextInterventionId: String = "",
    val contractInfo: ContractSummary? = null,
    val lastVe: LastVeSummary? = null,
    val nextVe: NextVeDisplay? = null,
    val coverage: VeCoverageSummary? = null,
    val showOfflineFinancialSection: Boolean = false,
    val showOnlineFinancialList: Boolean = false,
    val financialQuotes: List<ClientFinancialRowUi> = emptyList(),
    val financialInvoices: List<ClientFinancialRowUi> = emptyList(),
    val financialSummarySyncedAt: Long? = null,
    val onlineFinancialTab: Int = 0,
    val onlineFinancialItems: List<ClientFinancialRowUi> = emptyList(),
    val onlineFinancialLoading: Boolean = false,
    val onlineFinancialLoadingMore: Boolean = false,
    val onlineFinancialHasMore: Boolean = false,
    val onlineFinancialOffline: Boolean = false,
    val pdfMessage: String? = null,
    val isPdfLoading: Boolean = false,
) {
    val titleName: String
        get() {
            intervention?.let { inv ->
                val fromIntervention =
                    "${inv.customerFirstName.orEmpty()} ${inv.customerLastName.orEmpty()}".trim()
                if (fromIntervention.isNotBlank()) return fromIntervention
            }
            return displayName.ifBlank { "Fiche client" }
        }

    val resolvedUnitId: String
        get() = intervention?.unitId?.takeIf { it.isNotBlank() } ?: unitId

    val canShowContent: Boolean
        get() = intervention != null || displayName.isNotBlank()

    val showNewInterventionCta: Boolean
        get() =
            contextInterventionId.isBlank() &&
                resolvedUnitId.isNotBlank() &&
                !isEditing

    val showVeSummary: Boolean
        get() =
            contractInfo != null ||
                lastVe != null ||
                nextVe != null ||
                formatVeCoverageLabel(coverage) != null
}

@HiltViewModel
class ClientDetailViewModel @Inject constructor(
    private val syncRepository: SyncRepository,
    private val unitVeRepository: UnitVeRepository,
    private val pendingUpdateRepository: PendingUpdateRepository,
    private val settingsDao: SettingsDao,
    private val customerApi: CustomerApi,
    private val documentApi: DocumentApi,
    private val clientFinancialRepository: ClientFinancialRepository,
    private val invoiceApi: InvoiceApi,
    @ApplicationContext private val appContext: Context,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val customerId: String = checkNotNull(savedStateHandle["customerId"])
    private val navUnitId: String = savedStateHandle.navArg("unitId").orEmpty()
    private val navDisplayName: String = savedStateHandle.navArg("displayName").orEmpty()
    private val navAddressLine: String = savedStateHandle.navArg("addressLine").orEmpty()
    private val contextInterventionId: String =
        savedStateHandle.navArg("contextInterventionId").orEmpty()

    private var onlineFinancialPage = 1
    private var onlineFinancialTotal = 0

    private val _uiState = MutableStateFlow(
        ClientDetailUiState(
            customerId = customerId,
            unitId = navUnitId,
            displayName = navDisplayName,
            addressLine = navAddressLine,
            contextInterventionId = contextInterventionId,
            showOfflineFinancialSection = contextInterventionId.isNotBlank(),
        ),
    )
    val uiState: StateFlow<ClientDetailUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val settings = settingsDao.getSettingsOnce()
            val profile = settings?.profile ?: UserProfile.ARTISAN_SOLO
            val showOnlineFinancialList =
                contextInterventionId.isBlank() && profile == UserProfile.ARTISAN_SOLO
            _uiState.update {
                it.copy(
                    updatesRequireValidation = settings?.updatesRequireValidation ?: false,
                    isLoading = true,
                    showOnlineFinancialList = showOnlineFinancialList,
                )
            }

            if (_uiState.value.showOfflineFinancialSection) {
                launch {
                    clientFinancialRepository.observeByClientId(customerId).collect { rows ->
                        _uiState.update { state ->
                            state.copy(
                                financialQuotes = rows
                                    .filter { it.documentType == "devis" }
                                    .map { it.toRowUi() },
                                financialInvoices = rows
                                    .filter { it.documentType == "facture" }
                                    .map { it.toRowUi() },
                                financialSummarySyncedAt = rows.maxOfOrNull { it.syncedAt },
                            )
                        }
                    }
                }
                if (NetworkUtils.isOnline(appContext)) {
                    launch {
                        runCatching { clientFinancialRepository.refreshSummary(customerId) }
                    }
                }
            }

            if (showOnlineFinancialList) {
                loadOnlineFinancialDocuments(reset = true)
            }

            if (navUnitId.isNotBlank()) {
                val history = syncRepository.getHistoryForUnit(navUnitId)
                _uiState.update { it.copy(history = history) }
                loadHistoryPhotoUrls(history)
                loadVeContext(navUnitId, interventionHint = null)
            }

            try {
                val customer = customerApi.getCustomer(customerId)
                val customerOverride = pendingUpdateRepository.getLatestCustomerOverride(customerId)
                _uiState.update { state ->
                    state.copy(
                        displayName = state.displayName.ifBlank {
                            listOfNotNull(customer.firstName, customer.lastName)
                                .joinToString(" ")
                                .trim()
                        },
                        phone = customerOverride?.get("phone") as? String
                            ?: customer.phone.orEmpty(),
                        email = customerOverride?.get("email") as? String
                            ?: customer.email.orEmpty(),
                        notes = customerOverride?.get("notes") as? String
                            ?: customer.notes.orEmpty(),
                    )
                }
            } catch (_: Exception) {
            }

            launch {
                syncRepository.getInterventionByCustomerId(customerId).collect { intervention ->
                    if (intervention == null) {
                        _uiState.update { it.copy(isLoading = false) }
                        if (navUnitId.isNotBlank()) {
                            loadVeContext(navUnitId, interventionHint = null)
                        }
                        return@collect
                    }
                    applyInterventionData(intervention)
                }
            }
        }
    }

    fun onOnlineFinancialTabSelected(tab: Int) {
        if (_uiState.value.onlineFinancialTab == tab) return
        _uiState.update { it.copy(onlineFinancialTab = tab) }
        loadOnlineFinancialDocuments(reset = true)
    }

    fun loadMoreOnlineFinancialDocuments() {
        if (_uiState.value.onlineFinancialLoadingMore || !_uiState.value.onlineFinancialHasMore) return
        loadOnlineFinancialDocuments(reset = false)
    }

    private fun loadOnlineFinancialDocuments(reset: Boolean) {
        viewModelScope.launch {
            if (!NetworkUtils.isOnline(appContext)) {
                _uiState.update {
                    it.copy(
                        onlineFinancialOffline = true,
                        onlineFinancialLoading = false,
                        onlineFinancialLoadingMore = false,
                    )
                }
                return@launch
            }
            if (reset) {
                onlineFinancialPage = 1
                onlineFinancialTotal = 0
                _uiState.update {
                    it.copy(
                        onlineFinancialOffline = false,
                        onlineFinancialLoading = true,
                        onlineFinancialItems = emptyList(),
                        onlineFinancialHasMore = false,
                    )
                }
            } else {
                _uiState.update { it.copy(onlineFinancialLoadingMore = true) }
            }
            try {
                val type = if (_uiState.value.onlineFinancialTab == 0) "devis" else "facture"
                val page = if (reset) 1 else onlineFinancialPage + 1
                val response = clientFinancialRepository.fetchDocumentsPage(
                    clientId = customerId,
                    type = type,
                    page = page,
                    pageSize = 25,
                )
                onlineFinancialPage = page
                onlineFinancialTotal = response.total
                val newItems = response.items.map { it.toRowUi() }
                _uiState.update { state ->
                    val merged = if (reset) newItems else state.onlineFinancialItems + newItems
                    state.copy(
                        onlineFinancialItems = merged,
                        onlineFinancialLoading = false,
                        onlineFinancialLoadingMore = false,
                        onlineFinancialHasMore = merged.size < response.total,
                        onlineFinancialOffline = false,
                    )
                }
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        onlineFinancialLoading = false,
                        onlineFinancialLoadingMore = false,
                        onlineFinancialOffline = true,
                    )
                }
            }
        }
    }

    fun openFinancialDocumentPdf(context: Context, item: ClientFinancialRowUi) {
        if (!NetworkUtils.isOnline(context)) {
            _uiState.update { it.copy(pdfMessage = "PDF disponible en ligne uniquement") }
            return
        }
        if (_uiState.value.isPdfLoading) return
        viewModelScope.launch {
            _uiState.update { it.copy(isPdfLoading = true, pdfMessage = null) }
            try {
                val body = withContext(Dispatchers.IO) {
                    if (item.documentType == "devis") {
                        invoiceApi.downloadQuotePdf(item.documentId)
                    } else {
                        invoiceApi.downloadInvoicePdf(item.documentId)
                    }
                }
                val outFile = withContext(Dispatchers.IO) {
                    val outDir = File(context.filesDir, "client_documents").apply { mkdirs() }
                    val prefix = if (item.documentType == "devis") "devis" else "facture"
                    val file = File(outDir, "$prefix-${item.documentId}.pdf")
                    body.use { responseBody ->
                        FileOutputStream(file).use { fos ->
                            responseBody.byteStream().use { it.copyTo(fos) }
                        }
                    }
                    file
                }
                if (!outFile.exists() || outFile.length() == 0L) {
                    _uiState.update { it.copy(pdfMessage = "Fichier PDF vide ou introuvable") }
                    return@launch
                }
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    outFile,
                )
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/pdf")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    if (context !is Activity) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                try {
                    context.startActivity(intent)
                } catch (_: ActivityNotFoundException) {
                    _uiState.update { it.copy(pdfMessage = "Aucun lecteur PDF installé") }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        pdfMessage = e.message?.let { msg -> "Erreur PDF : $msg" }
                            ?: "Erreur PDF",
                    )
                }
            } finally {
                _uiState.update { it.copy(isPdfLoading = false) }
            }
        }
    }

    fun dismissPdfMessage() = _uiState.update { it.copy(pdfMessage = null) }

    private suspend fun loadVeContext(
        unitId: String,
        interventionHint: InterventionEntity?,
    ) {
        val context = unitVeRepository.getVeContextForUnit(
            unitId = unitId,
            interventionHint = interventionHint,
        )
        _uiState.update {
            it.copy(
                contractInfo = context.contractInfo,
                lastVe = context.lastVe,
                nextVe = context.nextVe,
                coverage = context.coverage,
            )
        }
    }

    private suspend fun applyInterventionData(intervention: InterventionEntity) {
        val customerOverride = pendingUpdateRepository.getLatestCustomerOverride(customerId)
        val unitOverride = pendingUpdateRepository.getLatestUnitOverride(intervention.unitId)
        val effectiveUnitId = navUnitId.ifBlank { intervention.unitId }

        if (effectiveUnitId.isNotBlank() && effectiveUnitId != _uiState.value.unitId) {
            val history = syncRepository.getHistoryForUnit(effectiveUnitId)
            _uiState.update { it.copy(history = history, unitId = effectiveUnitId) }
            loadHistoryPhotoUrls(history)
        }

        _uiState.update { state ->
            state.copy(
                intervention = intervention,
                unitId = effectiveUnitId.ifBlank { intervention.unitId },
                displayName = state.displayName.ifBlank {
                    "${intervention.customerFirstName.orEmpty()} ${intervention.customerLastName.orEmpty()}".trim()
                },
                addressLine = state.addressLine.ifBlank {
                    listOf(
                        intervention.unitStreet,
                        "${intervention.unitPostalCode} ${intervention.unitCity}".trim(),
                    ).filter { it.isNotBlank() }.joinToString(", ")
                },
                phone = customerOverride?.get("phone") as? String
                    ?: intervention.customerPhone.orEmpty(),
                email = customerOverride?.get("email") as? String
                    ?: intervention.customerEmail.orEmpty(),
                notes = customerOverride?.get("notes") as? String ?: state.notes,
                floor = unitOverride?.get("floor") as? String
                    ?: intervention.unitFloor.orEmpty(),
                doorCode = unitOverride?.get("doorCode") as? String
                    ?: intervention.unitDoorCode.orEmpty(),
                addressLine2 = unitOverride?.get("addressLine2") as? String
                    ?: intervention.unitAddressLine2.orEmpty(),
                isLoading = false,
            )
        }
        loadVeContext(
            unitId = effectiveUnitId.ifBlank { intervention.unitId },
            interventionHint = intervention,
        )
    }

    private fun loadHistoryPhotoUrls(history: List<InterventionHistoryEntity>) {
        viewModelScope.launch {
            val urlMap = mutableMapOf<String, List<String>>()
            history.forEach { item ->
                if (item.photoKeys.isNullOrBlank()) return@forEach
                try {
                    val keys = com.google.gson.Gson()
                        .fromJson(item.photoKeys, Array<String>::class.java)
                        ?: return@forEach
                    val urls = keys.mapNotNull { key ->
                        try {
                            documentApi.getSignedUrl(key).url
                        } catch (_: Exception) {
                            null
                        }
                    }
                    if (urls.isNotEmpty()) urlMap[item.id] = urls
                } catch (_: Exception) {
                }
            }
            _uiState.update { it.copy(historyPhotoUrls = urlMap) }
        }
    }

    fun startEditing() {
        _uiState.update { it.copy(isEditing = true) }
    }

    fun cancelEditing() {
        _uiState.update { it.copy(isEditing = false) }
        loadData()
    }

    fun onPhoneChange(value: String) = _uiState.update { it.copy(phone = value) }
    fun onEmailChange(value: String) = _uiState.update { it.copy(email = value, emailError = null) }
    fun onNotesChange(value: String) = _uiState.update { it.copy(notes = value) }
    fun onFloorChange(value: String) = _uiState.update { it.copy(floor = value) }
    fun onDoorCodeChange(value: String) = _uiState.update { it.copy(doorCode = value) }
    fun onAddressLine2Change(value: String) = _uiState.update { it.copy(addressLine2 = value) }

    fun saveChanges() {
        val state = _uiState.value
        val unitId = state.resolvedUnitId
        if (unitId.isBlank()) return

        val emailTrim = state.email.trim()
        if (emailTrim.isNotEmpty() &&
            !android.util.Patterns.EMAIL_ADDRESS.matcher(emailTrim).matches()
        ) {
            _uiState.update { it.copy(emailError = "Adresse e-mail invalide") }
            return
        }
        _uiState.update { it.copy(emailError = null) }

        val baselinePhone = state.intervention?.customerPhone.orEmpty()
        val baselineEmail = state.intervention?.customerEmail.orEmpty()
        val baselineFloor = state.intervention?.unitFloor.orEmpty()
        val baselineDoorCode = state.intervention?.unitDoorCode.orEmpty()
        val baselineAddressLine2 = state.intervention?.unitAddressLine2.orEmpty()

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            try {
                pendingUpdateRepository.updateCustomer(
                    customerId = customerId,
                    phone = state.phone.takeIf { it != baselinePhone },
                    email = state.email.takeIf { it != baselineEmail },
                    notes = state.notes.takeIf { it.isNotBlank() },
                )

                pendingUpdateRepository.updateUnitAccess(
                    unitId = unitId,
                    floor = state.floor.takeIf { it != baselineFloor },
                    doorCode = state.doorCode.takeIf { it != baselineDoorCode },
                    addressLine2 = state.addressLine2.takeIf { it != baselineAddressLine2 },
                )

                _uiState.update {
                    it.copy(
                        isSaving = false,
                        isEditing = false,
                        savedSuccess = true,
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = e.message ?: "Erreur lors de la sauvegarde",
                    )
                }
            }
        }
    }

    fun dismissError() = _uiState.update { it.copy(errorMessage = null) }
    fun dismissSuccess() = _uiState.update { it.copy(savedSuccess = false) }

    private companion object {
        fun SavedStateHandle.navArg(key: String): String? =
            get<String>(key)?.takeIf { it.isNotBlank() }
    }
}
