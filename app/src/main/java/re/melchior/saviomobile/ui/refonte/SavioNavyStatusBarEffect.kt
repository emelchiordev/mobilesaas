package re.melchior.saviomobile.ui.refonte

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import re.melchior.saviomobile.ui.theme.SavioRefonte

/**
 * Force status bar navy + icônes claires, navigation bar blanche + icônes sombres.
 * SideEffect pour repasser après SavioTheme / ModalBottomSheet qui réécrivent les couleurs.
 */
@Composable
fun SavioNavyStatusBarEffect() {
    val view = LocalView.current
    if (view.isInEditMode) return

    DisposableEffect(Unit) {
        val window = (view.context as? Activity)?.window
        val controller = window?.let { WindowCompat.getInsetsController(it, view) }
        val previousStatusColor = window?.statusBarColor
        val previousNavColor = window?.navigationBarColor
        val previousLightStatus = controller?.isAppearanceLightStatusBars
        val previousLightNav = controller?.isAppearanceLightNavigationBars

        onDispose {
            if (previousStatusColor != null) {
                window?.statusBarColor = previousStatusColor
            }
            if (previousNavColor != null) {
                window?.navigationBarColor = previousNavColor
            }
            if (previousLightStatus != null) {
                controller?.isAppearanceLightStatusBars = previousLightStatus
            }
            if (previousLightNav != null) {
                controller?.isAppearanceLightNavigationBars = previousLightNav
            }
        }
    }

    SideEffect {
        val window = (view.context as? Activity)?.window ?: return@SideEffect
        val controller = WindowCompat.getInsetsController(window, view)
        window.statusBarColor = SavioRefonte.Navy.toArgb()
        window.navigationBarColor = Color.White.toArgb()
        controller.isAppearanceLightStatusBars = false
        controller.isAppearanceLightNavigationBars = true
    }
}
