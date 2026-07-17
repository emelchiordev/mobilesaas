package re.savio.mobile.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Tokens UI adaptés au thème système (light / dark).
 * L’accent app métier suit [ViolettTokens.Accent] ; l’ambre reste sur l’auth Field Pro.
 */
object SavioUi {
    /** Navigation / liens — bleu Violett. */
    val Blue: Color
        @Composable get() = MaterialTheme.colorScheme.primary

    /** Accents métier (icônes, liens secondaires). */
    val BusinessAccent: Color get() = ViolettTokens.Accent

    val PlanifListBadgeFg: Color get() = SavioInterventionColors.PlanifFg
    val EmptyStateIconTint: Color get() = ViolettTokens.Accent
    val SnackbarErrorAction: Color get() = ViolettTokens.Accent
    val OfflineBannerFg: Color get() = ViolettTokens.Accent

    val PageBackground: Color
        @Composable get() = MaterialTheme.colorScheme.background

    val CardBorder: Color
        @Composable get() = MaterialTheme.colorScheme.outline

    val ChipBackground: Color
        @Composable get() = MaterialTheme.colorScheme.surfaceVariant

    val PlanifListBadgeBg: Color
        @Composable get() =
            if (isSystemInDarkTheme()) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                SavioInterventionColors.PlanifBg
            }

    val DestructiveRed: Color
        @Composable get() = MaterialTheme.colorScheme.error

    val ShimmerBase: Color
        @Composable get() = MaterialTheme.colorScheme.surfaceVariant

    val ShimmerHighlight: Color
        @Composable get() = MaterialTheme.colorScheme.outlineVariant

    val SnackbarErrorBg: Color
        @Composable get() = MaterialTheme.colorScheme.errorContainer

    val SnackbarSuccessBg: Color
        @Composable get() = MaterialTheme.colorScheme.secondaryContainer

    val ActionDisabledGray: Color
        @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)

    val OfflineBannerBg: Color
        @Composable get() = MaterialTheme.colorScheme.primaryContainer

    /** Fond badge / bandeau « en cours » — teinte ambre adaptée au thème. */
    val StatusTintBg: Color
        @Composable get() = MaterialTheme.colorScheme.primaryContainer

    /** Fond badge succès (terminée). */
    val StatusSuccessBg: Color
        @Composable get() = MaterialTheme.colorScheme.secondaryContainer

    val StatusSuccessFg: Color
        @Composable get() = SavioPalette.Success

    val SurfaceMuted: Color
        @Composable get() = MaterialTheme.colorScheme.surfaceVariant

    val OnSurfaceMuted: Color
        @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
}
