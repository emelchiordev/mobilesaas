package re.melchior.saviomobile.ui.refonte

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import re.melchior.saviomobile.ui.theme.SavioRefonte

@Composable
fun SavioNavyStatusBarEffect() {
    val view = LocalView.current
    if (view.isInEditMode) return

    DisposableEffect(Unit) {
        val window = (view.context as? Activity)?.window
        val controller = window?.let { WindowCompat.getInsetsController(it, view) }
        val previousColor = window?.statusBarColor
        val previousLight = controller?.isAppearanceLightStatusBars

        window?.statusBarColor = SavioRefonte.Navy.toArgb()
        controller?.isAppearanceLightStatusBars = false

        onDispose {
            if (previousColor != null) {
                window?.statusBarColor = previousColor
            }
            if (previousLight != null) {
                controller?.isAppearanceLightStatusBars = previousLight
            }
        }
    }
}
