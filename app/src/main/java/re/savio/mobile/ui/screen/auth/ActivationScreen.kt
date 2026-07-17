package re.savio.mobile.ui.screen.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import re.savio.mobile.ui.theme.SavioAuthTheme
import re.savio.mobile.ui.theme.SavioDimens
import re.savio.mobile.ui.theme.SavioPalette

@Composable
fun ActivationScreen(
    authViewModel: AuthViewModel,
    onActivationAuthenticated: () -> Unit,
    onNeedsAccountPrep: () -> Unit,
    onActivationChooseSociete: () -> Unit,
    onResendEmail: () -> Unit,
    viewModel: ActivationViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState) {
        when (val s = uiState) {
            ActivationUiState.Authenticated -> onActivationAuthenticated()
            ActivationUiState.NeedsAccountPrep -> onNeedsAccountPrep()
            is ActivationUiState.ChooseSociete -> {
                authViewModel.importSocietesForSelection(s.societes)
                onActivationChooseSociete()
            }
            else -> Unit
        }
    }

    SavioAuthTheme {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(SavioPalette.AuthBackground),
        ) {
            when (val state = uiState) {
                ActivationUiState.Verifying -> {
                    Column(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .padding(horizontal = SavioDimens.SpaceXL),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp),
                            color = SavioPalette.AuthAccent,
                            strokeWidth = 3.dp,
                        )
                        Spacer(modifier = Modifier.height(SavioDimens.SpaceLG))
                        Text(
                            text = "Activation de votre compte…",
                            style = MaterialTheme.typography.titleMedium,
                            color = SavioPalette.AuthOnBackground,
                            textAlign = TextAlign.Center,
                        )
                    }
                }

                is ActivationUiState.Error -> {
                    Column(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .padding(horizontal = SavioDimens.SpaceXL)
                                .verticalScroll(rememberScrollState())
                                .imePadding(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyLarge,
                            color = SavioPalette.AuthOnBackground,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(modifier = Modifier.height(SavioDimens.SpaceXL))
                        Button(
                            onClick = onResendEmail,
                            colors =
                                ButtonDefaults.buttonColors(
                                    containerColor = SavioPalette.AuthAccent,
                                    contentColor = SavioPalette.OnAccent,
                                ),
                        ) {
                            Text("Renvoyer un email")
                        }
                        Spacer(modifier = Modifier.height(SavioDimens.SpaceMD))
                        TextButton(onClick = viewModel::verify) {
                            Text(
                                text = "Réessayer",
                                color = SavioPalette.AuthAccent,
                            )
                        }
                    }
                }

                ActivationUiState.Authenticated,
                ActivationUiState.NeedsAccountPrep,
                is ActivationUiState.ChooseSociete -> {
                    Column(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .padding(horizontal = SavioDimens.SpaceXL),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(40.dp),
                            color = SavioPalette.AuthAccent,
                            strokeWidth = 3.dp,
                        )
                        Spacer(modifier = Modifier.height(SavioDimens.SpaceLG))
                        Text(
                            text = "Connexion à SAVIO…",
                            style = MaterialTheme.typography.bodyLarge,
                            color = SavioPalette.AuthOnMuted,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}
