package re.melchior.saviomobile.ui.screen.intervention.pacfiche

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import re.melchior.saviomobile.data.remote.api.InterventionPdfApi
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@HiltViewModel
class PacFichePdfViewModel @Inject constructor(
    private val interventionPdfApi: InterventionPdfApi,
    @ApplicationContext private val appContext: Context,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val interventionId: String = checkNotNull(savedStateHandle["interventionId"])
    private val equipmentOrder: Int = checkNotNull(savedStateHandle["equipmentOrder"])

    sealed class UiState {
        object Loading : UiState()
        data class Success(val file: File) : UiState()
        data class Error(val message: String) : UiState()
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        download()
    }

    private fun download() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val body = withContext(Dispatchers.IO) {
                    interventionPdfApi.downloadPacMeasuresPdf(interventionId, equipmentOrder)
                }
                val outDir = File(appContext.filesDir, "pac_fiches").apply { mkdirs() }
                val outFile = File(outDir, "fiche-pac-${interventionId}-${equipmentOrder}.pdf")
                withContext(Dispatchers.IO) {
                    body.use { responseBody ->
                        FileOutputStream(outFile).use { fos ->
                            responseBody.byteStream().use { it.copyTo(fos) }
                        }
                    }
                }
                if (!outFile.exists() || outFile.length() == 0L) {
                    _uiState.value = UiState.Error("Fichier PDF vide ou introuvable")
                    return@launch
                }
                _uiState.value = UiState.Success(outFile)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(
                    e.message ?: "Impossible de télécharger la fiche",
                )
            }
        }
    }
}
