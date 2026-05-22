package re.melchior.saviomobile.ui.screen.intervention.cloture

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import re.melchior.saviomobile.data.local.dao.SettingsDao
import re.melchior.saviomobile.data.local.entity.InterventionEntity
import re.melchior.saviomobile.data.remote.api.TourneeApi
import re.melchior.saviomobile.data.remote.dto.InterventionTypeDto
import re.melchior.saviomobile.data.remote.dto.stableKey
import re.melchior.saviomobile.data.repository.InvoiceRepository
import re.melchior.saviomobile.data.repository.PushRepository
import re.melchior.saviomobile.data.repository.SyncRepository
import re.melchior.saviomobile.worker.SyncWorker
import androidx.work.WorkManager
import java.io.File
import java.io.FileOutputStream
import java.time.Instant
import javax.inject.Inject

private const val ROUTE_NO_PRESELECT = "_"
private const val SAVIO_PUSH_LOG = "SavioPush"

data class ClotureSignatureUiState(
    val intervention: InterventionEntity? = null,
    val hasTechSignature: Boolean = false,
    val hasClientSignature: Boolean = false,
    val isLoading: Boolean = false,
    val isCompleted: Boolean = false,
    val isPendingValidation: Boolean = false,
    val errorMessage: String? = null,
    val closeTypes: List<InterventionTypeDto> = emptyList(),
    val closeTypesLoading: Boolean = true,
    val closeTypesError: String? = null,
    val selectedCloseTypes: List<InterventionTypeDto> = emptyList()
) {
    val isAbsent: Boolean get() = selectedCloseTypes.any { it.code == "ABS" }

    val showClientSignature: Boolean
        get() = selectedCloseTypes.any { it.requireClientSignature }

    val showReport: Boolean get() = selectedCloseTypes.any { it.requireReport }

    val isVeChanged: Boolean
        get() = intervention?.typeCode == "VE"
            && selectedCloseTypes.isNotEmpty()
            && selectedCloseTypes.none { it.isVeType }

    val canComplete: Boolean
        get() {
            if (closeTypesLoading || closeTypesError != null) return false
            if (selectedCloseTypes.isEmpty() || closeTypes.isEmpty()) return false
            if (!hasTechSignature) return false
            if (!showClientSignature) return true
            return hasClientSignature
        }
}

@HiltViewModel
class ClotureSignatureViewModel @Inject constructor(
    private val syncRepository: SyncRepository,
    private val invoiceRepository: InvoiceRepository,
    private val pushRepository: PushRepository,
    private val workManager: WorkManager,
    private val tourneeApi: TourneeApi,
    private val settingsDao: SettingsDao,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val interventionId: String = checkNotNull(savedStateHandle["interventionId"])
    private val routePreselectedKeys: String =
        savedStateHandle.get<String>("preselectedActualTypeKeys") ?: ROUTE_NO_PRESELECT

    private val _uiState = MutableStateFlow(ClotureSignatureUiState())
    val uiState: StateFlow<ClotureSignatureUiState> = _uiState.asStateFlow()

    private val _techPoints = MutableStateFlow<List<DrawPoint>>(emptyList())
    val techPoints: StateFlow<List<DrawPoint>> = _techPoints.asStateFlow()

    private val _clientPoints = MutableStateFlow<List<DrawPoint>>(emptyList())
    val clientPoints: StateFlow<List<DrawPoint>> = _clientPoints.asStateFlow()

    init {
        loadIntervention()
        loadCloseTypes()
    }

    private fun loadIntervention() {
        viewModelScope.launch {
            syncRepository.getInterventionById(interventionId)
                .collect { intervention ->
                    _uiState.update { it.copy(intervention = intervention) }
                    tryInitCloseTypeSelection()
                }
        }
    }

    private fun loadCloseTypes() {
        viewModelScope.launch {
            try {
                val types = tourneeApi.getInterventionTypesForClose(showOnClose = true)
                _uiState.update {
                    it.copy(
                        closeTypes = types,
                        closeTypesLoading = false,
                        closeTypesError = null
                    )
                }
                tryInitCloseTypeSelection()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        closeTypesLoading = false,
                        closeTypesError = e.message ?: "Impossible de charger les types d'intervention"
                    )
                }
            }
        }
    }

    private fun tryInitCloseTypeSelection() {
        if (_uiState.value.selectedCloseTypes.isNotEmpty()) return
        val inv = _uiState.value.intervention ?: return
        val types = _uiState.value.closeTypes
        if (types.isEmpty()) return

        val raw = routePreselectedKeys
        if (raw.isNotBlank() && raw != ROUTE_NO_PRESELECT) {
            val keys = raw.split('\u001F').filter { it.isNotEmpty() }
            val resolved = keys.mapNotNull { k ->
                types.find { it.stableKey() == k || it.id == k }
            }
            if (resolved.isNotEmpty()) {
                _uiState.update { it.copy(selectedCloseTypes = resolved) }
                return
            }
        }
        _uiState.update { it.copy(selectedCloseTypes = listOf(resolveDefaultSingle(inv, types))) }
    }

    fun toggleCloseType(type: InterventionTypeDto) {
        _uiState.update { state ->
            val next =
                if (state.selectedCloseTypes.any { it.stableKey() == type.stableKey() }) {
                    state.selectedCloseTypes.filter { it.stableKey() != type.stableKey() }
                } else {
                    state.selectedCloseTypes + type
                }
            val stillNeedsClient = next.any { it.requireClientSignature }
            if (!stillNeedsClient) {
                _clientPoints.value = emptyList()
            }
            state.copy(
                selectedCloseTypes = next,
                hasClientSignature = if (stillNeedsClient) state.hasClientSignature else false
            )
        }
    }

    fun addTechPoint(point: DrawPoint) {
        _techPoints.update { it + point }
        _uiState.update { it.copy(hasTechSignature = true) }
    }

    fun addClientPoint(point: DrawPoint) {
        if (!_uiState.value.showClientSignature) return
        _clientPoints.update { it + point }
        _uiState.update { it.copy(hasClientSignature = true) }
    }

    fun clearTechSignature() {
        _techPoints.update { emptyList() }
        _uiState.update { it.copy(hasTechSignature = false) }
    }

    fun clearClientSignature() {
        _clientPoints.update { emptyList() }
        _uiState.update { it.copy(hasClientSignature = false) }
    }

    fun completeIntervention(
        filesDir: File,
        techCanvasWidth: Int,
        techCanvasHeight: Int,
        clientCanvasWidth: Int,
        clientCanvasHeight: Int
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                android.util.Log.i(SAVIO_PUSH_LOG, "clôture: début interventionId=$interventionId")
                val state = _uiState.value
                val selectedTypes = state.selectedCloseTypes
                if (selectedTypes.isEmpty()) {
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = "Sélectionnez au moins un type réel")
                    }
                    return@launch
                }

                if (!state.showReport) {
                    syncRepository.saveReport(interventionId, "")
                }

                val sigDir = File(filesDir, "signatures").apply { mkdirs() }

                val techSigFile = File(sigDir, "sig_tech_${interventionId}.png")
                saveBitmap(
                    points = _techPoints.value,
                    width = techCanvasWidth,
                    height = techCanvasHeight,
                    file = techSigFile
                )

                val clientSigPath: String? = if (!state.showClientSignature) {
                    null
                } else {
                    val clientSigFile = File(sigDir, "sig_client_${interventionId}.png")
                    saveBitmap(
                        points = _clientPoints.value,
                        width = clientCanvasWidth,
                        height = clientCanvasHeight,
                        file = clientSigFile
                    )
                    clientSigFile.absolutePath
                }

                val now = Instant.now().toString()
                syncRepository.completeIntervention(
                    interventionId = interventionId,
                    completedAt = now,
                    signaturePath = clientSigPath,
                    techSignaturePath = techSigFile.absolutePath,
                    selectedTypes = selectedTypes
                )

                val intervention = syncRepository.getInterventionByIdOnce(interventionId)
                val techId = settingsDao.getSettingsOnce()?.technicianId.orEmpty()
                if (intervention != null) {
                    invoiceRepository.prepareInvoicePushForClosure(
                        interventionId = interventionId,
                        unitId = intervention.unitId,
                        technicianId = techId,
                    )
                }

                android.util.Log.i(SAVIO_PUSH_LOG, "clôture: DB à jour → appel push()")
                val pushResult = pushRepository.push()
                android.util.Log.i(SAVIO_PUSH_LOG, "clôture: push() retourne $pushResult")

                SyncWorker.enqueueNow(workManager)
                android.util.Log.i(
                    SAVIO_PUSH_LOG,
                    "clôture: SyncWorker enqueued (upload photos/signatures hors ViewModel)",
                )

                val requiresValidation = settingsDao.getSettingsOnce()?.updatesRequireValidation ?: false
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isCompleted = true,
                        isPendingValidation = requiresValidation,
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e(SAVIO_PUSH_LOG, "clôture échouée avant/après push: ${e.message}", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Erreur lors de la clôture"
                    )
                }
            }
        }
    }

    private fun saveBitmap(
        points: List<DrawPoint>,
        width: Int,
        height: Int,
        file: File
    ) {
        require(width > 0 && height > 0) { "Dimensions de signature invalides" }
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(android.graphics.Color.WHITE)

        val paint = Paint().apply {
            color = android.graphics.Color.BLACK
            strokeWidth = 4f
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            isAntiAlias = true
        }

        val path = Path()
        points.forEach { point ->
            if (point.isStart) path.moveTo(point.x, point.y)
            else path.lineTo(point.x, point.y)
        }
        canvas.drawPath(path, paint)

        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}

private fun resolveDefaultSingle(
    intervention: InterventionEntity,
    types: List<InterventionTypeDto>
): InterventionTypeDto {
    intervention.interventionTypeId?.let { id ->
        types.find { it.id == id }?.let { return it }
    }
    types.find { it.code == intervention.typeCode }?.let { return it }
    return types.first()
}

data class DrawPoint(
    val x: Float,
    val y: Float,
    val isStart: Boolean = false
)
