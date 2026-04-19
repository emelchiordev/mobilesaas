package re.melchior.saviomobile.ui

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import re.melchior.saviomobile.data.local.database.TokenDataStore
import re.melchior.saviomobile.data.remote.interceptor.AuthEventBus
import re.melchior.saviomobile.ui.navigation.AppNavigation
import re.melchior.saviomobile.ui.theme.SavioTheme

@Composable
fun SavioApp(
    windowSizeClass: WindowSizeClass,
    tokenDataStore: TokenDataStore,
    authEventBus: AuthEventBus,
) {
    SavioTheme {
        AppNavigation(
            tokenDataStore = tokenDataStore,
            authEventBus = authEventBus,
            windowSizeClass = windowSizeClass,
        )
    }
}
