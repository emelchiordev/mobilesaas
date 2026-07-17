package re.savio.mobile.ui

import android.content.Intent
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import re.savio.mobile.data.account.AccountSwitchCoordinator
import re.savio.mobile.data.local.database.TokenDataStore
import re.savio.mobile.data.remote.interceptor.AuthEventBus
import re.savio.mobile.ui.navigation.AppNavigation
import re.savio.mobile.ui.refonte.SavioNavyStatusBarEffect
import re.savio.mobile.ui.theme.SavioTheme

@Composable
fun SavioApp(
    windowSizeClass: WindowSizeClass,
    tokenDataStore: TokenDataStore,
    accountSwitchCoordinator: AccountSwitchCoordinator,
    authEventBus: AuthEventBus,
    deepLinkIntent: Intent?,
    onConsumeDeepLinkIntent: () -> Unit,
) {
    SavioTheme {
        SavioNavyStatusBarEffect()
        AppNavigation(
            tokenDataStore = tokenDataStore,
            accountSwitchCoordinator = accountSwitchCoordinator,
            authEventBus = authEventBus,
            windowSizeClass = windowSizeClass,
            deepLinkIntent = deepLinkIntent,
            onConsumeDeepLinkIntent = onConsumeDeepLinkIntent,
        )
    }
}
