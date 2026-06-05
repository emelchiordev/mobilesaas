package re.melchior.saviomobile.ui.theme

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
private val SavioDarkPrimary = Color(0xFFF5A623)
private val SavioDarkOnPrimary = Color(0xFF000000)
private val SavioDarkSecondary = Color(0xFF8A8F9E)
private val SavioDarkOutline = Color(0xFF2A3050)
private val SavioDarkError = Color(0xFFCF6679)

private val SavioLightBackground = Color(0xFFEFF3F7)
private val SavioLightOnBackground = Color(0xFF1C2430)
private val SavioLightSurface = Color(0xFFFFFFFF)
private val SavioLightOnSurface = Color(0xFF1C2430)
private val SavioLightSurfaceVariant = Color(0xFFEAF1F8)
private val SavioLightOnSurfaceVariant = Color(0xFF8893A2)
private val SavioLightPrimary = Color(0xFF1B4E80)
private val SavioLightOnPrimary = Color(0xFFFFFFFF)
private val SavioLightPrimaryContainer = Color(0xFFD6E4F5)
private val SavioLightOnPrimaryContainer = Color(0xFF1B4F8A)
private val SavioLightSecondary = Color(0xFFEC971F)
private val SavioLightOnSecondary = Color(0xFF000000)
private val SavioLightSecondaryContainer = Color(0xFFFFE0A0)
private val SavioLightOnSecondaryContainer = Color(0xFF1A1A1A)
private val SavioLightTertiary = Color(0xFF2E6DB4)
private val SavioLightOnTertiary = Color(0xFFFFFFFF)
private val SavioLightOutline = Color(0xFFE6EBF1)
private val SavioLightOutlineVariant = Color(0xFFEDF1F6)
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
    // Refonte SAVIO MOBILITE : shell métier en thème clair (maquette handoff).
    // Welcome / auth gardent leur fond sombre via composants dédiés.
    val darkTheme = false
    val colorScheme = SavioLightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            val statusBarColor =
                if (darkTheme) {
                    SavioDarkBackground
                } else {
                    SavioLightPrimary
                }
            window.statusBarColor = statusBarColor.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

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
