package re.savio.mobile.ui.navigation

import re.savio.mobile.data.remote.dto.ScanPlateResultDto

object ScanPlateNav {
    const val RESULT_JSON = "scanPlateResultJson"
    const val SNACKBAR = "scanPlateSnackbar"
}

data class ScanPlateNavPayload(
    val parsed: ScanPlateResultDto? = null,
    val rawOcrText: String? = null,
    val ocrFallback: Boolean = false,
    val offline: Boolean = false,
)
