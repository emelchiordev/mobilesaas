package re.savio.mobile.ui.screen.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import re.savio.mobile.ui.theme.SavioAuthTheme
import re.savio.mobile.ui.theme.SavioDimens
import re.savio.mobile.ui.theme.SavioPalette

@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    SavioAuthTheme {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(SavioPalette.AuthBackground)
                    .padding(SavioDimens.SpaceXL),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Bienvenue sur SAVIO",
                style = MaterialTheme.typography.headlineSmall,
                color = SavioPalette.AuthOnBackground,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(SavioDimens.SpaceMD))
            Text(
                text =
                    "Étape suivante : nous vous guiderons pour finaliser votre installation " +
                        "(wizard à venir).",
                style = MaterialTheme.typography.bodyMedium,
                color = SavioPalette.AuthOnMuted,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(SavioDimens.SpaceXL))
            Button(
                onClick = { viewModel.completeOnboarding(onFinished) },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(SavioDimens.AppButtonHeight),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = SavioPalette.AuthAccent,
                        contentColor = SavioPalette.OnAccent,
                    ),
            ) {
                Text(
                    text = "Continuer vers ma tournée",
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}
