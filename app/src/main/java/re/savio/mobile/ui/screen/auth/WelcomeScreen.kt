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
import re.savio.mobile.ui.theme.SavioAuthTheme
import re.savio.mobile.ui.theme.SavioDimens
import re.savio.mobile.ui.theme.SavioPalette
import re.savio.mobile.ui.theme.SavioWelcomeBackground
import re.savio.mobile.ui.theme.SavioWelcomeStatusBarEffect

@Composable
fun WelcomeScreen(
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
) {
    SavioWelcomeStatusBarEffect()
    SavioAuthTheme {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(SavioWelcomeBackground)
                    .padding(horizontal = SavioDimens.SpaceXL),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "SAVIO",
                style = MaterialTheme.typography.displaySmall,
                color = SavioPalette.AuthAccent,
                fontWeight = FontWeight.Bold,
                letterSpacing = 4.sp,
            )
            Spacer(modifier = Modifier.height(SavioDimens.SpaceSM))
            Text(
                text = "Terrain. Simple. Efficace.",
                style = MaterialTheme.typography.bodyLarge,
                color = SavioPalette.AuthOnMuted,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(56.dp))

            Button(
                onClick = onRegisterClick,
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
                    text = "Créer un compte",
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Spacer(modifier = Modifier.height(SavioDimens.SpaceMD))
            OutlinedButton(
                onClick = onLoginClick,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(SavioDimens.AppButtonHeight),
                colors =
                    ButtonDefaults.outlinedButtonColors(
                        contentColor = SavioPalette.AuthOnBackground,
                    ),
                border =
                    androidx.compose.foundation.BorderStroke(
                        SavioDimens.BorderMedium,
                        SavioPalette.AuthAccent.copy(alpha = 0.85f),
                    ),
            ) {
                Text(
                    text = "Se connecter",
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}
