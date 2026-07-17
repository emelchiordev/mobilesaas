package re.savio.mobile.ui.utils

import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration

enum class SavioWindowSize { COMPACT, TABLET }

@Composable
fun rememberSavioWindowSize(windowSizeClass: WindowSizeClass): SavioWindowSize {
    return remember(windowSizeClass.widthSizeClass) {
        when (windowSizeClass.widthSizeClass) {
            WindowWidthSizeClass.Compact -> SavioWindowSize.COMPACT
            else -> SavioWindowSize.TABLET
        }
    }
}

@Composable
fun rememberIsSavioTablet(windowSizeClass: WindowSizeClass? = null): Boolean {
    if (windowSizeClass != null) {
        return rememberSavioWindowSize(windowSizeClass) == SavioWindowSize.TABLET
    }
    val configuration = LocalConfiguration.current
    return remember(configuration.screenWidthDp) {
        configuration.screenWidthDp >= 600
    }
}
