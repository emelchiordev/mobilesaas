package re.savio.mobile.ui.screen.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.collectLatest
import re.savio.mobile.ui.component.SavioSnackbarHost
import re.savio.mobile.ui.theme.SavioAuthTheme
import re.savio.mobile.ui.theme.SavioDimens
import re.savio.mobile.ui.theme.SavioPalette
import re.savio.mobile.ui.theme.SavioType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onBack: () -> Unit,
    authViewModel: AuthViewModel,
    onNavigateOnboarding: () -> Unit,
    onNavigateSelectSociete: () -> Unit,
    onNavigateAccountPrep: () -> Unit = {},
    onNavigateToLogin: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.navEvents.collectLatest { ev ->
            when (ev) {
                RegisterNavEvent.Onboarding -> onNavigateOnboarding()
                RegisterNavEvent.AccountPrep -> onNavigateAccountPrep()
                is RegisterNavEvent.ChooseSociete -> {
                    authViewModel.importSocietesForSelection(ev.societes)
                    onNavigateSelectSociete()
                }
            }
        }
    }

    LaunchedEffect(uiState.errorMessage, uiState.pendingVerificationEmail) {
        val msg = uiState.errorMessage ?: return@LaunchedEffect
        if (uiState.pendingVerificationEmail != null) return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
    }

    SavioAuthTheme {
        if (uiState.pendingVerificationEmail != null) {
            RegisterPendingVerificationContent(
                email = uiState.pendingVerificationEmail!!,
                isResending = uiState.isResendingVerificationEmail,
                resendFeedback = uiState.resendVerificationFeedback,
                resendError = uiState.resendVerificationError,
                onActivatedAccountClick = onNavigateToLogin,
                onResendClick = viewModel::resendVerificationEmail,
                onBackClick = onBack,
            )
        } else {
        Scaffold(
            containerColor = SavioPalette.AuthBackground,
            snackbarHost = { SavioSnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = { Text("Inscription", color = SavioPalette.AuthOnBackground) },
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
                        .imePadding()
                        .verticalScroll(rememberScrollState()),
            ) {
                Spacer(modifier = Modifier.height(SavioDimens.SpaceLG))
                Text(
                    text = "Créez votre espace",
                    style = MaterialTheme.typography.headlineSmall,
                    color = SavioPalette.AuthOnBackground,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(modifier = Modifier.height(SavioDimens.SpaceSM))
                Text(
                    text = "Vous rejoignez SAVIO en quelques secondes.",
                    style = SavioType.BodySmall,
                    color = SavioPalette.AuthOnMuted,
                )
                Spacer(modifier = Modifier.height(SavioDimens.SpaceXL))

                OutlinedTextField(
                    value = uiState.contactFullName,
                    onValueChange = viewModel::onContactFullNameChange,
                    label = { Text("Votre prénom et nom") },
                    placeholder = { Text("Optionnel — utilisé pour votre fiche technicien") },
                    singleLine = false,
                    maxLines = 2,
                    isError = uiState.contactFullNameError != null,
                    supportingText = { uiState.contactFullNameError?.let { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading,
                    keyboardOptions =
                        KeyboardOptions(
                            capitalization = KeyboardCapitalization.Characters,
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next,
                        ),
                    keyboardActions =
                        KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) },
                        ),
                    colors = authFieldColors(),
                )
                Spacer(modifier = Modifier.height(SavioDimens.SpaceLG))

                OutlinedTextField(
                    value = uiState.companyName,
                    onValueChange = viewModel::onCompanyNameChange,
                    label = { Text("Nom de la société") },
                    singleLine = true,
                    isError = uiState.companyNameError != null,
                    supportingText = { uiState.companyNameError?.let { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading,
                    keyboardOptions =
                        KeyboardOptions(
                            capitalization = KeyboardCapitalization.Characters,
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next,
                        ),
                    keyboardActions =
                        KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) },
                        ),
                    colors = authFieldColors(),
                )
                Spacer(modifier = Modifier.height(SavioDimens.SpaceLG))

                OutlinedTextField(
                    value = uiState.email,
                    onValueChange = viewModel::onEmailChange,
                    label = { Text("Email") },
                    singleLine = true,
                    isError = uiState.emailError != null,
                    supportingText = { uiState.emailError?.let { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading,
                    keyboardOptions =
                        KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next,
                        ),
                    keyboardActions =
                        KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) },
                        ),
                    colors = authFieldColors(),
                )
                Spacer(modifier = Modifier.height(SavioDimens.SpaceLG))

                OutlinedTextField(
                    value = uiState.password,
                    onValueChange = viewModel::onPasswordChange,
                    label = { Text("Mot de passe") },
                    singleLine = true,
                    isError = uiState.passwordError != null,
                    supportingText = { uiState.passwordError?.let { Text(it) } },
                    visualTransformation =
                        if (passwordVisible) VisualTransformation.None
                        else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector =
                                    if (passwordVisible) Icons.Filled.VisibilityOff
                                    else Icons.Filled.Visibility,
                                contentDescription = null,
                                tint = SavioPalette.AuthOnMuted,
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading,
                    keyboardOptions =
                        KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Next,
                        ),
                    keyboardActions =
                        KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) },
                        ),
                    colors = authFieldColors(),
                )
                Spacer(modifier = Modifier.height(SavioDimens.SpaceLG))

                OutlinedTextField(
                    value = uiState.confirmPassword,
                    onValueChange = viewModel::onConfirmPasswordChange,
                    label = { Text("Confirmer le mot de passe") },
                    singleLine = true,
                    isError = uiState.confirmPasswordError != null,
                    supportingText = { uiState.confirmPasswordError?.let { Text(it) } },
                    visualTransformation =
                        if (confirmVisible) VisualTransformation.None
                        else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { confirmVisible = !confirmVisible }) {
                            Icon(
                                imageVector =
                                    if (confirmVisible) Icons.Filled.VisibilityOff
                                    else Icons.Filled.Visibility,
                                contentDescription = null,
                                tint = SavioPalette.AuthOnMuted,
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading,
                    keyboardOptions =
                        KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done,
                        ),
                    keyboardActions =
                        KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                viewModel.submit()
                            },
                        ),
                    colors = authFieldColors(),
                )

                Spacer(modifier = Modifier.height(SavioDimens.SpaceXL))

                Button(
                    onClick = {
                        focusManager.clearFocus()
                        viewModel.submit()
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
                            text = "Créer mon compte",
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(SavioDimens.SpaceLG))

                TextButton(
                    onClick = onBack,
                    enabled = !uiState.isLoading,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = "Déjà un compte ? Se connecter",
                        color = SavioPalette.AuthAccent,
                    )
                }
                Spacer(modifier = Modifier.height(SavioDimens.SpaceXL))
            }
        }
        }
    }
}

@Composable
private fun authFieldColors() =
    OutlinedTextFieldDefaults.colors(
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
