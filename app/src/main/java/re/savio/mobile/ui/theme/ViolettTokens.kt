package re.savio.mobile.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/** Tokens Violett × SAVIO mobile — source : handoff `violett-savio.css`. */
object ViolettTokens {
    val Bg = Color(0xFFF5F6F8)
    val Surface = Color(0xFFFFFFFF)
    val Faint = Color(0xFFF3F5F7)
    val Tint = Color(0xFFEDF4FE)

    val Ink = Color(0xFF222222)
    val Ink2 = Color(0xFF565B63)
    val Muted = Color(0xFF8A9099)
    val FaintInk = Color(0xFFAEB4BD)

    val Line = Color(0xFFEEEEEE)
    val Line2 = Color(0xFFF1F3F5)

    val Accent = Color(0xFF1877D8)
    val AccentInk = Color(0xFF1462B8)
    val Cyan = Color(0xFF1FA8C4)

    val GradStart = Color(0xFF1C6FD6)
    val GradEnd = Color(0xFF22B7CE)
    val GradSoftStart = Color(0xFF4B93EA)
    val GradSoftEnd = Color(0xFF3FD0DE)

    val Nav = Color(0xFF2B2E34)
    val NavInk = Color(0xFF8A8F98)

    val IconDefault = Color(0xFF3A3F47)

    val StatusPlanBg = Color(0xFFEAEFFE)
    val StatusPlanFg = Color(0xFF3B54C4)
    val StatusPlanDot = Color(0xFF6C82E6)

    val StatusLiveBg = Color(0xFFFCE9F1)
    val StatusLiveFg = Color(0xFFC43A7C)
    val StatusLiveDot = Color(0xFFE85D9C)

    val StatusDoneBg = Color(0xFFE1F5F0)
    val StatusDoneFg = Color(0xFF0E8E76)
    val StatusDoneDot = Color(0xFF22BFA0)

    val WarnTagBg = Color(0xFFFCEFF5)
    val WarnTagFg = Color(0xFFC43A7C)

    val CardShadow = Color(0x120C3660)
    val FabShadow = Color(0x6B1C6FD6)

    val PrimaryGradient: Brush
        get() =
            Brush.linearGradient(
                colors = listOf(GradStart, GradEnd),
            )

    val PrimaryGradientSoft: Brush
        get() =
            Brush.linearGradient(
                colors = listOf(GradSoftStart, GradSoftEnd),
            )
}
