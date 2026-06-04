package re.melchior.saviomobile.ui.screen.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import re.melchior.saviomobile.data.local.database.TokenDataStore
import re.melchior.saviomobile.ui.navigation.Screen
import re.melchior.saviomobile.ui.theme.SavioAuthTheme
import re.melchior.saviomobile.ui.theme.SavioPalette

@Composable
fun SplashScreen(
    tokenDataStore: TokenDataStore,
    onNavigate: (route: String) -> Unit,
) {
    SavioAuthTheme {
        LaunchedEffect(Unit) {
            delay(320)
            val token = tokenDataStore.getAccessToken()
            val slug = tokenDataStore.getSocieteSlugStored()
            val onboardingDone = tokenDataStore.isOnboardingCompletedFirst()
            val route =
                when {
                    token.isNullOrBlank() -> Screen.Welcome.route
                    slug.isNullOrBlank() -> Screen.Welcome.route
                    !onboardingDone -> Screen.Onboarding.route
                    else -> Screen.Main.route
                }
            onNavigate(route)
        }

        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(SavioPalette.AuthBackground),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "SAVIO",
                    style = MaterialTheme.typography.headlineLarge,
                    color = SavioPalette.AuthAccent,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 3.sp,
                )
                Spacer(modifier = Modifier.height(24.dp))
                CircularProgressIndicator(
                    modifier = Modifier.size(36.dp),
                    color = SavioPalette.AuthAccent,
                    strokeWidth = 3.dp,
                )
            }
        }
    }
}
