package re.melchior.saviomobile.ui.screen.intervention

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import re.melchior.saviomobile.data.repository.PlateScanOutcome
import re.melchior.saviomobile.data.repository.PlateScanRepository
import re.melchior.saviomobile.ui.navigation.ScanPlateNavPayload
import java.io.File
import javax.inject.Inject

data class PlateScanUiState(
    val isProcessing: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class PlateScanViewModel @Inject constructor(
    private val plateScanRepository: PlateScanRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlateScanUiState())
    val uiState: StateFlow<PlateScanUiState> = _uiState.asStateFlow()

    fun processPhoto(
        file: File,
        onComplete: (ScanPlateNavPayload, String?) -> Unit,
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, errorMessage = null) }
            when (val outcome = plateScanRepository.processPlatePhoto(file)) {
                is PlateScanOutcome.Parsed -> {
                    onComplete(
                        ScanPlateNavPayload(
                            parsed = outcome.result,
                            rawOcrText = outcome.rawOcrText,
                        ),
                        null,
                    )
                }
                is PlateScanOutcome.OcrFallback -> {
                    val message = if (outcome.offline) {
                        "Analyse automatique indisponible hors ligne"
                    } else {
                        "Analyse automatique indisponible — texte OCR conservé"
                    }
                    onComplete(
                        ScanPlateNavPayload(
                            rawOcrText = outcome.rawOcrText,
                            ocrFallback = true,
                            offline = outcome.offline,
                        ),
                        message,
                    )
                }
                PlateScanOutcome.EmptyOcr -> {
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            errorMessage = "Texte illisible, réessayez",
                        )
                    }
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
