package re.savio.mobile.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/** Fond Welcome / auth marketing — toujours Field Pro sombre. */
val SavioWelcomeBackground = Color(0xFF12151C)

private val SavioDarkBackground = Color(0xFF12151C)
private val SavioDarkSurface = Color(0xFF1C2030)
private val SavioDarkOnSurface = Color(0xFFFFFFFF)
private val SavioDarkPrimary = ViolettTokens.Accent
private val SavioDarkOnPrimary = Color.White
private val SavioDarkSecondary = Color(0xFF8A8F9E)
private val SavioDarkOutline = Color(0xFF2A3050)
private val SavioDarkError = Color(0xFFCF6679)

private val SavioLightBackground = ViolettTokens.Bg
private val SavioLightOnBackground = ViolettTokens.Ink
private val SavioLightSurface = ViolettTokens.Surface
private val SavioLightOnSurface = ViolettTokens.Ink
private val SavioLightSurfaceVariant = ViolettTokens.Faint
private val SavioLightOnSurfaceVariant = ViolettTokens.Muted
private val SavioLightPrimary = ViolettTokens.Accent
private val SavioLightOnPrimary = Color.White
private val SavioLightPrimaryContainer = ViolettTokens.Tint
private val SavioLightOnPrimaryContainer = ViolettTokens.AccentInk
private val SavioLightSecondary = ViolettTokens.GradEnd
private val SavioLightOnSecondary = Color.White
private val SavioLightSecondaryContainer = ViolettTokens.Tint
private val SavioLightOnSecondaryContainer = ViolettTokens.AccentInk
private val SavioLightTertiary = ViolettTokens.Cyan
private val SavioLightOnTertiary = Color.White
private val SavioLightOutline = ViolettTokens.Line
private val SavioLightOutlineVariant = ViolettTokens.Line2
private val SavioLightError = Color(0xFFB00020)
private val SavioLightOnError = Color(0xFFFFFFFF)

/** Field Pro — thème sombre (préférences système en mode dark). */
internal val SavioDarkColorScheme =
    darkColorScheme(
        primary = SavioDarkPrimary,
        onPrimary = SavioDarkOnPrimary,
        primaryContainer = SavioDarkPrimary,
        onPrimaryContainer = SavioDarkOnPrimary,
        secondary = SavioDarkSecondary,
        onSecondary = SavioDarkOnSurface,
        secondaryContainer = SavioPalette.SurfaceElevated,
        onSecondaryContainer = SavioDarkOnSurface,
        tertiary = SavioDarkPrimary,
        onTertiary = SavioDarkOnPrimary,
        tertiaryContainer = SavioPalette.SurfaceElevated,
        onTertiaryContainer = SavioDarkPrimary,
        error = SavioDarkError,
        onError = SavioDarkOnSurface,
        errorContainer = SavioPalette.ErrorContainer,
        onErrorContainer = Color(0xFFFFDAD6),
        background = SavioDarkBackground,
        onBackground = SavioDarkOnSurface,
        surface = SavioDarkSurface,
        onSurface = SavioDarkOnSurface,
        surfaceVariant = SavioDarkSurface,
        onSurfaceVariant = SavioDarkOnSurface,
        outline = SavioDarkOutline,
        outlineVariant = SavioDarkOutline,
        scrim = Color(0x99000000),
        inverseSurface = SavioDarkOnSurface,
        inverseOnSurface = SavioDarkBackground,
        inversePrimary = SavioDarkPrimary,
        surfaceDim = SavioDarkBackground,
        surfaceBright = SavioPalette.SurfaceElevated,
        surfaceContainerLowest = SavioDarkBackground,
        surfaceContainerLow = SavioDarkSurface,
        surfaceContainer = SavioDarkSurface,
        surfaceContainerHigh = SavioPalette.SurfaceElevated,
        surfaceContainerHighest = SavioDarkSurface,
    )

/** Thème clair professionnel — préférences système en mode light. */
internal val SavioLightColorScheme =
    lightColorScheme(
        primary = SavioLightPrimary,
        onPrimary = SavioLightOnPrimary,
        primaryContainer = SavioLightPrimaryContainer,
        onPrimaryContainer = SavioLightOnPrimaryContainer,
        secondary = SavioLightSecondary,
        onSecondary = SavioLightOnSecondary,
        secondaryContainer = SavioLightSecondaryContainer,
        onSecondaryContainer = SavioLightOnSecondaryContainer,
        tertiary = SavioLightTertiary,
        onTertiary = SavioLightOnTertiary,
        tertiaryContainer = SavioLightPrimaryContainer,
        onTertiaryContainer = SavioLightOnPrimaryContainer,
        error = SavioLightError,
        onError = SavioLightOnError,
        errorContainer = Color(0xFFFFDAD6),
        onErrorContainer = SavioLightError,
        background = SavioLightBackground,
        onBackground = SavioLightOnBackground,
        surface = SavioLightSurface,
        onSurface = SavioLightOnSurface,
        surfaceVariant = SavioLightSurfaceVariant,
        onSurfaceVariant = SavioLightOnSurfaceVariant,
        outline = SavioLightOutline,
        outlineVariant = SavioLightOutlineVariant,
        scrim = Color(0x66000000),
        inverseSurface = SavioLightOnBackground,
        inverseOnSurface = SavioLightSurface,
        inversePrimary = SavioLightPrimary,
        surfaceDim = SavioLightSurfaceVariant,
        surfaceBright = SavioLightSurface,
        surfaceContainerLowest = SavioLightBackground,
        surfaceContainerLow = SavioLightSurface,
        surfaceContainer = SavioLightSurface,
        surfaceContainerHigh = SavioLightSurfaceVariant,
        surfaceContainerHighest = SavioLightSurfaceVariant,
    )

@Immutable
data class ColorFamily(
    val color: Color,
    val onColor: Color,
    val colorContainer: Color,
    val onColorContainer: Color,
)

val unspecified_scheme =
    ColorFamily(
        Color.Unspecified,
        Color.Unspecified,
        Color.Unspecified,
        Color.Unspecified,
    )

@Composable
fun SavioTheme(
    content: @Composable () -> Unit,
) {
    // Violett × SAVIO : shell métier clair (handoff mobile).
    // Welcome / auth gardent leur fond sombre via composants dédiés.
    // Barre système : MainActivity.enableEdgeToEdge + SavioNavyStatusBarEffect (SavioApp).
    val colorScheme = SavioLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content,
    )
}

/** Welcome : barre de statut sombre même si le système est en mode clair. */
@Composable
fun SavioWelcomeStatusBarEffect() {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            window.statusBarColor = SavioWelcomeBackground.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }
}
