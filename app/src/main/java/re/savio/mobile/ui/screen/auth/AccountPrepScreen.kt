package re.savio.mobile.ui.screen.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import re.savio.mobile.ui.theme.SavioAuthTheme
import re.savio.mobile.ui.theme.SavioPalette

@Composable
fun AccountPrepScreen(
    onGoMain: () -> Unit,
    onStayOnOutgoingMain: (message: String) -> Unit,
    onReauthWelcome: (message: String) -> Unit,
    viewModel: AccountPrepViewModel = hiltViewModel(),
) {
    val ui by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.start()
    }

    LaunchedEffect(ui.navigation) {
        when (val nav = ui.navigation) {
            is AccountPrepNav.GoMain -> {
                viewModel.consumeNavigation()
                onGoMain()
            }
            is AccountPrepNav.StayOnOutgoingMain -> {
                viewModel.consumeNavigation()
                onStayOnOutgoingMain(nav.message)
            }
            is AccountPrepNav.ReauthWelcome -> {
                viewModel.consumeNavigation()
                onReauthWelcome(nav.message)
            }
            null -> Unit
        }
    }

    if (ui.showOverrideConfirmDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissOverrideConfirm() },
            title = { Text("Confirmer la perte de données") },
            text = {
                Text(
                    "Les données non synchronisées seront perdues définitivement. " +
                        "Cette action est irréversible.",
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmOverrideAndContinue() },
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                        ),
                ) {
                    Text("Perdre les données et continuer")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissOverrideConfirm() }) {
                    Text("Annuler")
                }
            },
        )
    }

    SavioAuthTheme {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(SavioPalette.AuthBackground)
                    .padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
            ) {
                Text(
                    text = "SAVIO",
                    style = MaterialTheme.typography.headlineMedium,
                    color = SavioPalette.AuthAccent,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(24.dp))

                when {
                    ui.flushWarningMessage != null -> {
                        Text(
                            text = "Synchronisation requise",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = ui.flushWarningMessage!!,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        ui.flushUnsynced?.let { summary ->
                            if (summary.interventionLikeCount > 0 || summary.totalItems > 0) {
                                Spacer(modifier = Modifier.height(12.dp))
                                val detail =
                                    buildList {
                                        if (summary.interventionLikeCount > 0) {
                                            add(
                                                "${summary.interventionLikeCount} intervention(s)",
                                            )
                                        }
                                        if (summary.pendingPhotos + summary.pendingQuotePhotos > 0) {
                                            add(
                                                "${summary.pendingPhotos + summary.pendingQuotePhotos} photo(s)",
                                            )
                                        }
                                        if (summary.dirtyForms > 0) {
                                            add("${summary.dirtyForms} formulaire(s)")
                                        }
                                    }.joinToString(" · ")
                                if (detail.isNotBlank()) {
                                    Text(
                                        text = detail,
                                        style = MaterialTheme.typography.bodyMedium,
                                        textAlign = TextAlign.Center,
                                        color = SavioPalette.AuthOnMuted,
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(28.dp))
                        Button(
                            onClick = { viewModel.retryFlush() },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("Réessayer la synchronisation")
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = { viewModel.requestOverrideConfirm() },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("Continuer quand même")
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        TextButton(onClick = { viewModel.cancelFlushFailure() }) {
                            Text(
                                if (ui.flushIsLogout) {
                                    "Rester connecté"
                                } else {
                                    "Annuler le changement de compte"
                                },
                            )
                        }
                    }
                    ui.pullOfflineMessage != null -> {
                        Text(
                            text = ui.pullOfflineMessage!!,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { viewModel.retryPull() },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("Réessayer")
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = { viewModel.continueOfflineToMain() },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("Continuer hors ligne")
                        }
                    }
                    ui.errorMessage != null && !ui.running -> {
                        Text(
                            text = ui.errorMessage!!,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.error,
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(onClick = { viewModel.start() }) {
                            Text("Réessayer")
                        }
                    }
                    else -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(40.dp),
                            color = SavioPalette.AuthAccent,
                            strokeWidth = 3.dp,
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = ui.statusLabel,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                }
            }
        }
    }
}
