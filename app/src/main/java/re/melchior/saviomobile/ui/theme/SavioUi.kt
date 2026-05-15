package re.melchior.saviomobile.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Accès raccourci aux tokens [SavioPalette].
 */
object SavioUi {
    val Blue: Color get() = SavioPalette.Accent
    val PageBackground: Color get() = SavioPalette.BackgroundPage
    val CardBorder: Color get() = SavioPalette.BorderDefault
    val ChipBackground: Color get() = SavioPalette.PrimaryLight
    val PlanifListBadgeBg: Color get() = SavioPalette.WarningTintBg
    val PlanifListBadgeFg: Color get() = SavioPalette.Accent
    val DestructiveRed: Color get() = SavioPalette.Error

    val ShimmerBase: Color get() = SavioPalette.SkeletonBase
    val ShimmerHighlight: Color get() = SavioPalette.SkeletonHighlight

    val EmptyStateIconTint: Color get() = SavioPalette.Accent

    val SnackbarErrorBg: Color get() = SavioPalette.SnackbarError
    val SnackbarErrorAction: Color get() = SavioPalette.Accent

    val SnackbarSuccessBg: Color get() = SavioPalette.SuccessDark

    val ActionDisabledGray: Color get() = SavioPalette.TextHint

    val OfflineBannerBg: Color get() = SavioPalette.WarningTintBg
    val OfflineBannerFg: Color get() = SavioPalette.Accent
}
