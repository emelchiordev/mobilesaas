package re.melchior.saviomobile.ui.screen.intervention.cloture

import re.melchior.saviomobile.worker.SyncWorker
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import re.melchior.saviomobile.data.local.entity.InterventionEntity
import re.melchior.saviomobile.data.repository.SyncRepository
import java.io.File
import java.io.FileOutputStream
import java.time.Instant
import javax.inject.Inject

data class ClotureSignatureUiState(
    val intervention: InterventionEntity? = null,
    val hasTechSignature: Boolean = false,
    val hasClientSignature: Boolean = false,
    val isLoading: Boolean = false,
    val isCompleted: Boolean = false,
    val errorMessage: String? = null
) {
    val canComplete: Boolean get() = hasTechSignature && hasClientSignature
}

@HiltViewModel
class ClotureSignatureViewModel @Inject constructor(
    private val syncRepository: SyncRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val interventionId: String = checkNotNull(savedStateHandle["interventionId"])

    private val _uiState = MutableStateFlow(ClotureSignatureUiState())
    val uiState: StateFlow<ClotureSignatureUiState> = _uiState.asStateFlow()

    // Points signature technicien
    private val _techPoints = MutableStateFlow<List<DrawPoint>>(emptyList())
    val techPoints: StateFlow<List<DrawPoint>> = _techPoints.asStateFlow()

    // Points signature client
    private val _clientPoints = MutableStateFlow<List<DrawPoint>>(emptyList())
    val clientPoints: StateFlow<List<DrawPoint>> = _clientPoints.asStateFlow()

    init {
        loadIntervention()
    }

    private fun loadIntervention() {
        viewModelScope.launch {
            syncRepository.getInterventionById(interventionId)
                .collect { intervention ->
                    _uiState.update { it.copy(intervention = intervention) }
                }
        }
    }

    fun addTechPoint(point: DrawPoint) {
        _techPoints.update { it + point }
        _uiState.update { it.copy(hasTechSignature = true) }
    }

    fun addClientPoint(point: DrawPoint) {
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
                val sigDir = File(filesDir, "signatures").apply { mkdirs() }

                // Sauvegarder signature technicien
                val techSigFile = File(sigDir, "sig_tech_${interventionId}.png")
                saveBitmap(
                    points = _techPoints.value,
                    width = techCanvasWidth,
                    height = techCanvasHeight,
                    file = techSigFile
                )

                // Sauvegarder signature client
                val clientSigFile = File(sigDir, "sig_client_${interventionId}.png")
                saveBitmap(
                    points = _clientPoints.value,
                    width = clientCanvasWidth,
                    height = clientCanvasHeight,
                    file = clientSigFile
                )

// Clôturer l'intervention
                val now = Instant.now().toString()
                syncRepository.completeIntervention(
                    interventionId = interventionId,
                    completedAt = now,
                    signaturePath = clientSigFile.absolutePath,
                    techSignaturePath = techSigFile.absolutePath
                )

// Déclencher sync immédiat si réseau disponible
//                val constraints = Constraints.Builder()
//                    .setRequiredNetworkType(NetworkType.CONNECTED)
//                    .build()
//
//                val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
//                    .setConstraints(constraints)
//                    .build()
//
//                workManager.enqueue(syncRequest)

                _uiState.update { it.copy(isLoading = false, isCompleted = true) }

            } catch (e: Exception) {
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

data class DrawPoint(
    val x: Float,
    val y: Float,
    val isStart: Boolean = false
)