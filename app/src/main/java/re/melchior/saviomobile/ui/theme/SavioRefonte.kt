package re.melchior.saviomobile.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** Tokens UI refonte SAVIO MOBILITE (handoff Refonte SAVIO.html). */
object SavioRefonte {
    val Navy = Color(0xFF1B4E80)
    val Navy700 = Color(0xFF163F66)
    val Ink = Color(0xFF1C2430)
    val Muted = Color(0xFF8893A2)
    val Line = Color(0xFFE6EBF1)
    val BgPage = Color(0xFFEFF3F7)
    val Tint = Color(0xFFEAF1F8)
    val Link = Color(0xFF1E66B0)
    val OrangeAction = Color(0xFFEC971F)
    val OrangeDark = Color(0xFFD07C05)

    val StatusPlanBg = Color(0xFFE9F0F8)
    val StatusPlanFg = Color(0xFF2C5A8C)
    val StatusPlanDot = Color(0xFF6E94BE)
    val StatusLiveBg = Color(0xFFFCEED7)
    val StatusLiveFg = Color(0xFFB06E08)
    val StatusLiveDot = Color(0xFFF2A33C)
    val StatusDoneBg = Color(0xFFE2F3E9)
    val StatusDoneFg = Color(0xFF1E7A4F)
    val StatusDoneDot = Color(0xFF2E9E6B)

    val NavInactive = Color(0xFF9AA4B2)
    val CardShadow = Color(0x0A142846)
}

@Composable
fun useSavioRefonteUi(): Boolean = true
