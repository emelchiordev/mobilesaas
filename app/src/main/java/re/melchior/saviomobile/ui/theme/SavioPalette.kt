package re.melchior.saviomobile.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Tokens couleur SAVIO — alignés WelcomeScreen (fond sombre + accent ambre).
 * [Primary] / [PrimaryDark] restent des alias rétrocompatibles vers l’accent orange.
 */
object SavioPalette {
    val BackgroundPage = Color(0xFF12151C)
    val SurfaceCard = Color(0xFF1C2030)
    val SurfaceElevated = Color(0xFF252A38)

    val Accent = Color(0xFFF5A623)
    val AccentPressed = Color(0xFFD4891A)
    val OnAccent = Color(0xFF000000)

    val TextPrimary = Color(0xFFFFFFFF)
    val TextSecondary = Color(0xFF8A8F9E)
    val TextHint = Color(0xFF6B7080)

    /** Alias historique : accent orange (pas le bleu ancien). */
    val Primary: Color get() = Accent

    val PrimaryDark: Color get() = AccentPressed

    /** Pastilles / chips sur fond sombre (teinte ambre très sombre, opaque). */
    val PrimaryLight: Color get() = Color(0xFF332A18)

    val BackgroundCard: Color get() = SurfaceCard
    val BackgroundCardAlt: Color get() = SurfaceElevated

    val BorderDefault = Color(0xFF323848)

    val BorderFieldPro = Color(0xFF2A3050)

    val Success = Color(0xFF81C784)
    val SuccessDark = Color(0xFF1E3A24)
    val Error = Color(0xFFFF6B6B)
    val ErrorContainer = Color(0xFF4A1818)
    val Warning = Color(0xFFF5A623)
    val WarningTintBg = Color(0xFF3D3319)
    val Info: Color get() = Accent
    val InfoTintBg = Color(0xFF2A2618)

    /** Alias badges / surfaces teintées (rétrocompat). */
    val WarningLight: Color get() = WarningTintBg
    val SuccessLight: Color get() = SuccessDark
    val ErrorLight: Color get() = ErrorContainer
    val InfoLight: Color get() = InfoTintBg

    val White = Color(0xFFFFFFFF)
    val Black = Color(0xFF000000)

    val SkeletonBase = Color(0xFF252A38)
    val SkeletonHighlight = Color(0xFF323848)

    val SnackbarError = Color(0xFFB71C1C)
    val SnackbarWarning = Color(0xFFE65100)

    /** Écrans auth : mêmes jetons que le thème global. */
    val AuthBackground = BackgroundPage
    val AuthSurface = SurfaceCard
    val AuthAccent = Accent
    val AuthAccentDark = AccentPressed
    val AuthOnBackground = TextPrimary
    val AuthOnMuted = TextSecondary
}
