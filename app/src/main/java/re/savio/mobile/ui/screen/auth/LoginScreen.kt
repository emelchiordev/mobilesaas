package re.savio.mobile.ui.screen.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import re.savio.mobile.ui.component.SavioSnackbarHost
import re.savio.mobile.ui.theme.SavioAuthTheme
import re.savio.mobile.ui.theme.SavioDimens
import re.savio.mobile.ui.theme.SavioPalette

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNeedsAccountPrep: () -> Unit,
    onChooseSociete: () -> Unit,
    onBack: () -> Unit,
    onNavigateRegister: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) onLoginSuccess()
    }

    LaunchedEffect(uiState.needsAccountPrep) {
        if (uiState.needsAccountPrep) {
            viewModel.consumeNeedsAccountPrep()
            onNeedsAccountPrep()
        }
    }

    LaunchedEffect(uiState.societesToChoose) {
        if (uiState.societesToChoose.isNotEmpty()) onChooseSociete()
    }

    LaunchedEffect(uiState.errorMessage) {
        val msg = uiState.errorMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        viewModel.clearErrorMessage()
    }

    SavioAuthTheme {
        Scaffold(
            containerColor = SavioPalette.AuthBackground,
            snackbarHost = { SavioSnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = { Text("Connexion", color = SavioPalette.AuthOnBackground) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Retour",
                                tint = SavioPalette.AuthOnBackground,
                            )
                        }
                    },
                    colors =
                        TopAppBarDefaults.topAppBarColors(
                            containerColor = SavioPalette.AuthBackground,
                        ),
                )
            },
        ) { padding ->
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = SavioDimens.SpaceXL)
                        .imePadding(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "SAVIO",
                    style = MaterialTheme.typography.headlineMedium,
                    color = SavioPalette.AuthAccent,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(SavioDimens.SpaceSM))
                Text(
                    text = "Accédez à votre tournée terrain.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SavioPalette.AuthOnMuted,
                )
                Spacer(modifier = Modifier.height(SavioDimens.SpaceXL))

                OutlinedTextField(
                    value = uiState.email,
                    onValueChange = viewModel::onEmailChange,
                    label = { Text("Email") },
                    singleLine = true,
                    keyboardOptions =
                        KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next,
                        ),
                    keyboardActions =
                        KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) },
                        ),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading,
                    colors = authFieldColorsLogin(),
                )

                Spacer(modifier = Modifier.height(SavioDimens.SpaceLG))

                OutlinedTextField(
                    value = uiState.password,
                    onValueChange = viewModel::onPasswordChange,
                    label = { Text("Mot de passe") },
                    singleLine = true,
                    visualTransformation =
                        if (passwordVisible) VisualTransformation.None
                        else PasswordVisualTransformation(),
                    keyboardOptions =
                        KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done,
                        ),
                    keyboardActions =
                        KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                viewModel.login()
                            },
                        ),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector =
                                    if (passwordVisible) Icons.Filled.VisibilityOff
                                    else Icons.Filled.Visibility,
                                contentDescription =
                                    if (passwordVisible) "Masquer le mot de passe"
                                    else "Afficher le mot de passe",
                                tint = SavioPalette.AuthOnMuted,
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading,
                    colors = authFieldColorsLogin(),
                )

                Spacer(modifier = Modifier.height(SavioDimens.SpaceXL))

                Button(
                    onClick = {
                        focusManager.clearFocus()
                        viewModel.login()
                    },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(SavioDimens.AppButtonHeight),
                    enabled = !uiState.isLoading,
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = SavioPalette.AuthAccent,
                            contentColor = SavioPalette.OnAccent,
                            disabledContainerColor = SavioPalette.AuthOnMuted.copy(alpha = 0.35f),
                        ),
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = SavioPalette.OnAccent,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text(
                            text = "Se connecter",
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(SavioDimens.SpaceMD))

                TextButton(
                    onClick = onNavigateRegister,
                    enabled = !uiState.isLoading,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = "Créer un compte",
                        color = SavioPalette.AuthAccent,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }
}

@Composable
private fun authFieldColorsLogin() =
    androidx.compose.material3.OutlinedTextFieldDefaults.colors(
        focusedTextColor = SavioPalette.AuthOnBackground,
        unfocusedTextColor = SavioPalette.AuthOnBackground,
        focusedLabelColor = SavioPalette.AuthAccent,
        unfocusedLabelColor = SavioPalette.AuthOnMuted,
        focusedBorderColor = SavioPalette.AuthAccent,
        unfocusedBorderColor = SavioPalette.AuthOnMuted.copy(alpha = 0.4f),
        cursorColor = SavioPalette.AuthAccent,
        errorBorderColor = SavioPalette.Error,
        errorLabelColor = SavioPalette.Error,
        errorSupportingTextColor = SavioPalette.Error,
    )
