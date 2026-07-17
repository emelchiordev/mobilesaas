package re.savio.mobile.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Tokens couleur SAVIO.
 * Accent UI = Violett (bleu) partout, y compris auth / splash / Field Pro.
 */
object SavioPalette {
    val BackgroundPage = Color(0xFF12151C)
    val SurfaceCard = Color(0xFF1C2030)
    val SurfaceElevated = Color(0xFF252A38)

    /** @deprecated Alias historique — pointe vers [Accent]. */
    val BrandOrange: Color get() = Accent
    val BrandOrangePressed: Color get() = AccentPressed
    val OnBrandOrange: Color get() = OnAccent

    /** Accent interactif (handoff Violett). */
    val Accent = ViolettTokens.Accent
    val AccentPressed = ViolettTokens.AccentInk
    val OnAccent = Color.White

    val TextPrimary = Color(0xFFFFFFFF)
    val TextSecondary = Color(0xFF8A8F9E)
    val TextHint = Color(0xFF6B7080)

    val Primary: Color get() = Accent

    val PrimaryDark: Color get() = AccentPressed

    /** Pastilles / chips sur fond sombre Field Pro. */
    val PrimaryLight: Color get() = Color(0xFF1A2A40)

    val BackgroundCard: Color get() = SurfaceCard
    val BackgroundCardAlt: Color get() = SurfaceElevated

    val BorderDefault = Color(0xFF323848)

    val BorderFieldPro = Color(0xFF2A3050)

    val Success = Color(0xFF81C784)
    val SuccessDark = Color(0xFF1E3A24)
    val Error = Color(0xFFFF6B6B)
    val ErrorContainer = Color(0xFF4A1818)
    /** Alerte métier (pas l’accent marque). */
    val Warning = Color(0xFFE8A317)
    val WarningTintBg = Color(0xFF3D3319)
    val Info: Color get() = Accent
    val InfoTintBg = ViolettTokens.Tint.copy(alpha = 0.35f)

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

    val AuthBackground = BackgroundPage
    val AuthSurface = SurfaceCard
    val AuthAccent: Color get() = Accent
    val AuthAccentDark: Color get() = AccentPressed
    val AuthOnBackground = TextPrimary
    val AuthOnMuted = TextSecondary
}
