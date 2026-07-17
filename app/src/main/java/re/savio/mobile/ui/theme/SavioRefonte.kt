package re.savio.mobile.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Alias de compatibilité vers [ViolettTokens].
 * Les écrans refonte consomment ces jetons ; la source de vérité est Violett.
 */
object SavioRefonte {
    val BgPage: Color get() = ViolettTokens.Bg
    val Ink: Color get() = ViolettTokens.Ink
    val Muted: Color get() = ViolettTokens.Muted
    val Line: Color get() = ViolettTokens.Line
    val Tint: Color get() = ViolettTokens.Tint
    val Link: Color get() = ViolettTokens.Accent

    /** Accents interactifs (remplace navy / orange legacy). */
    val Navy: Color get() = ViolettTokens.Accent
    val Navy700: Color get() = ViolettTokens.AccentInk
    val OrangeAction: Color get() = ViolettTokens.Accent
    val OrangeDark: Color get() = ViolettTokens.AccentInk

    val StatusPlanBg: Color get() = ViolettTokens.StatusPlanBg
    val StatusPlanFg: Color get() = ViolettTokens.StatusPlanFg
    val StatusPlanDot: Color get() = ViolettTokens.StatusPlanDot
    val StatusLiveBg: Color get() = ViolettTokens.StatusLiveBg
    val StatusLiveFg: Color get() = ViolettTokens.StatusLiveFg
    val StatusLiveDot: Color get() = ViolettTokens.StatusLiveDot
    val StatusDoneBg: Color get() = ViolettTokens.StatusDoneBg
    val StatusDoneFg: Color get() = ViolettTokens.StatusDoneFg
    val StatusDoneDot: Color get() = ViolettTokens.StatusDoneDot

    val NavInactive: Color get() = ViolettTokens.NavInk
    val NavDark: Color get() = ViolettTokens.Nav
    val CardShadow: Color get() = ViolettTokens.CardShadow

    val PrimaryGradient: Brush get() = ViolettTokens.PrimaryGradient
    val PrimaryGradientSoft: Brush get() = ViolettTokens.PrimaryGradientSoft
}

@Composable
fun useSavioRefonteUi(): Boolean = true

@Composable
fun useViolettUi(): Boolean = useSavioRefonteUi()
