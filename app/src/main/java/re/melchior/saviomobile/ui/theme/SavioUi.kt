package re.melchior.saviomobile.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Accès raccourci rétrocompatible aux tokens [SavioPalette].
 * Préférer [SavioPalette] / [SavioType] / [SavioDimens] dans le nouveau code.
 */
object SavioUi {
    val Blue: Color get() = SavioPalette.Primary
    val PageBackground: Color get() = SavioPalette.BackgroundPage
    val CardBorder: Color get() = SavioPalette.BorderDefault
    val ChipBackground: Color get() = SavioPalette.PrimaryLight
    val PlanifListBadgeBg: Color get() = SavioPalette.WarningLight
    val PlanifListBadgeFg: Color get() = SavioPalette.Warning
    val DestructiveRed: Color get() = SavioPalette.Error

    val ShimmerBase: Color get() = SavioPalette.SkeletonBase
    val ShimmerHighlight: Color get() = SavioPalette.SkeletonHighlight

    val EmptyStateIconTint: Color get() = SavioPalette.PrimaryLight

    val SnackbarErrorBg: Color get() = SavioPalette.SnackbarError
    val SnackbarErrorAction: Color get() = SavioPalette.PrimaryLight

    val SnackbarSuccessBg: Color get() = SavioPalette.Success

    val ActionDisabledGray: Color get() = SavioPalette.TextHint

    val OfflineBannerBg: Color get() = SavioPalette.WarningLight
    val OfflineBannerFg: Color get() = SavioPalette.Warning
}
