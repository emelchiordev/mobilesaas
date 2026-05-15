package re.melchior.saviomobile.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/** Fond page + cartes Field Pro (Material3 roles de base). */
private val SavioM3Background = Color(0xFF12151C)
private val SavioM3Surface = Color(0xFF1C2030)
private val SavioM3OnSurface = Color(0xFFFFFFFF)

/**
 * Field Pro / SAVIO — schéma Material3 **toujours dark** (ignorer le thème système).
 *
 * Les [androidx.compose.material3.Card] lisent aussi [surfaceContainerLow] etc. : ces rôles
 * restent sur des teintes sombres (#12151C / #1C2030), jamais une surface claire par défaut.
 */
internal val SavioDarkColorScheme =
    darkColorScheme(
        primary = SavioPalette.Accent,
        onPrimary = SavioPalette.OnAccent,
        primaryContainer = SavioPalette.AccentPressed,
        onPrimaryContainer = SavioPalette.TextPrimary,
        secondary = SavioPalette.SurfaceElevated,
        onSecondary = SavioPalette.TextPrimary,
        secondaryContainer = SavioPalette.SurfaceElevated,
        onSecondaryContainer = SavioPalette.TextSecondary,
        tertiary = SavioPalette.Accent,
        onTertiary = SavioPalette.OnAccent,
        tertiaryContainer = SavioPalette.SurfaceElevated,
        onTertiaryContainer = SavioPalette.Accent,
        error = SavioPalette.Error,
        onError = SavioPalette.TextPrimary,
        errorContainer = SavioPalette.ErrorContainer,
        onErrorContainer = Color(0xFFFFDAD6),
        background = SavioM3Background,
        onBackground = SavioM3OnSurface,
        surface = SavioM3Surface,
        onSurface = SavioM3OnSurface,
        surfaceVariant = SavioM3Surface,
        onSurfaceVariant = SavioM3OnSurface,
        outline = SavioPalette.BorderDefault,
        outlineVariant = Color(0xFF2A3040),
        scrim = Color(0x99000000),
        inverseSurface = SavioPalette.TextPrimary,
        inverseOnSurface = SavioPalette.BackgroundPage,
        inversePrimary = SavioPalette.Accent,
        surfaceDim = SavioPalette.BackgroundPage,
        surfaceBright = SavioPalette.SurfaceElevated,
        surfaceContainerLowest = SavioPalette.BackgroundPage,
        surfaceContainerLow = SavioPalette.SurfaceCard,
        surfaceContainer = SavioPalette.SurfaceCard,
        surfaceContainerHigh = SavioPalette.SurfaceElevated,
        surfaceContainerHighest = SavioPalette.SurfaceCard,
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
    /** Pas de branche light / dynamic : tablette en mode sombre produit uniquement. */
    val colorScheme = SavioDarkColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            window.statusBarColor = SavioPalette.BackgroundPage.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content,
    )
}
