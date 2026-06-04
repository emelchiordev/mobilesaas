package re.melchior.saviomobile.data.repository

import android.content.Context
import re.melchior.saviomobile.data.plate.PlateOcrProcessor
import re.melchior.saviomobile.data.remote.api.EquipmentApi
import re.melchior.saviomobile.data.remote.dto.ScanPlateRequestDto
import re.melchior.saviomobile.data.remote.dto.ScanPlateResultDto
import re.melchior.saviomobile.ui.utils.NetworkUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

sealed class PlateScanOutcome {
    data class Parsed(val result: ScanPlateResultDto, val rawOcrText: String) : PlateScanOutcome()
    data class OcrFallback(val rawOcrText: String, val offline: Boolean) : PlateScanOutcome()
    data object EmptyOcr : PlateScanOutcome()
}

@Singleton
class PlateScanRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val plateOcrProcessor: PlateOcrProcessor,
    private val equipmentApi: EquipmentApi,
) {
    suspend fun processPlatePhoto(file: File): PlateScanOutcome {
        val ocrText = plateOcrProcessor.recognizeText(file).trim()
        if (ocrText.length < 3) {
            return PlateScanOutcome.EmptyOcr
        }

        if (!NetworkUtils.isOnline(context)) {
            return PlateScanOutcome.OcrFallback(rawOcrText = ocrText, offline = true)
        }

        return try {
            val result = equipmentApi.scanPlate(ScanPlateRequestDto(ocrText = ocrText))
            PlateScanOutcome.Parsed(result = result, rawOcrText = ocrText)
        } catch (_: Exception) {
            PlateScanOutcome.OcrFallback(rawOcrText = ocrText, offline = false)
        }
    }
}
