package re.melchior.saviomobile.ui.utils

import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

enum class SavioWindowSize { COMPACT, EXPANDED }

@Composable
fun rememberSavioWindowSize(windowSizeClass: WindowSizeClass): SavioWindowSize {
    return remember(windowSizeClass.widthSizeClass) {
        if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded) {
            SavioWindowSize.EXPANDED
        } else {
            SavioWindowSize.COMPACT
        }
    }
}
