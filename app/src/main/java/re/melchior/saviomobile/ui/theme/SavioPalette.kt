package re.melchior.saviomobile.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Tokens couleur de la marque (Compose Color).
 * Ne pas confondre avec [SavioColors] dans [Theme.kt] (ColorScheme Material3).
 */
object SavioPalette {
    val Primary = Color(0xFF1E6DB5)
    val PrimaryLight = Color(0xFFDDEAF8)
    val PrimaryDark = Color(0xFF155090)

    val TextPrimary = Color(0xFF1A1A2E)
    val TextSecondary = Color(0xFF6B7280)
    val TextHint = Color(0xFFB0BEC5)

    val BackgroundPage = Color(0xFFF3F4F6)
    val BackgroundCard = Color(0xFFFFFFFF)
    val BackgroundCardAlt = Color(0xFFF8F9FA)

    val BorderDefault = Color(0xFFE5E7EB)

    val Success = Color(0xFF1B5E20)
    val SuccessLight = Color(0xFFE8F5E9)
    val Error = Color(0xFFE53935)
    val ErrorLight = Color(0xFFFFEBEE)
    val Warning = Color(0xFFE65100)
    val WarningLight = Color(0xFFFFF3E0)
    val Info = Color(0xFF1E6DB5)
    val InfoLight = Color(0xFFDDEAF8)

    val White = Color(0xFFFFFFFF)
    val Black = Color(0xFF000000)

    val SkeletonBase = Color(0xFFE8EDF2)
    val SkeletonHighlight = Color(0xFFF5F7FA)

    val SnackbarError = Color(0xFFB71C1C)
    val SnackbarWarning = Color(0xFFE65100)
}
