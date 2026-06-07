package re.melchior.saviomobile.ui.screen.client

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.foundation.text.KeyboardOptions
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import re.melchior.saviomobile.data.repository.BanAddressPick
import re.melchior.saviomobile.ui.component.SavioSnackbarHost
import re.melchior.saviomobile.ui.theme.SavioPalette
import re.melchior.saviomobile.ui.theme.savioFieldColors
import re.melchior.saviomobile.ui.theme.savioTopAppBarColors

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateClientScreen(
    onBack: () -> Unit,
    onNavigateToCreateIntervention: (
        customerId: String,
        unitId: String,
        displayName: String,
        addressLine: String,
    ) -> Unit,
    viewModel: CreateClientViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var createdClientSheet by remember { mutableStateOf<CreateClientEvent.Created?>(null) }

    val keyboardController = LocalSoftwareKeyboardController.current
    val density = LocalDensity.current

    LaunchedEffect(Unit) {
        viewModel.events.collect { ev ->
            when (ev) {
                is CreateClientEvent.Created -> {
                    snackbarHostState.showSnackbar(
                        message = "Client créé ✅",
                        duration = SnackbarDuration.Short,
                    )
                    createdClientSheet = ev
                }

                CreateClientEvent.SavedOffline -> {
                    snackbarHostState.showSnackbar(
                        message = "Client sauvegardé ☁️",
                        duration = SnackbarDuration.Short,
                    )
                    viewModel.resetForm()
                    onBack()
                }
            }
        }
    }

    createdClientSheet?.let { created ->
        ClientCreatedBottomSheet(
            displayName = created.displayName,
            onCreateIntervention = {
                createdClientSheet = null
                viewModel.resetForm()
                onNavigateToCreateIntervention(
                    created.customerId,
                    created.unitId,
                    created.displayName,
                    created.addressLine,
                )
            },
            onDismiss = {
                createdClientSheet = null
                viewModel.resetForm()
                onBack()
            },
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SavioSnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Nouveau client",
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, enabled = !uiState.isSubmitting) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour",
                        )
                    }
                },
                colors =
                    savioTopAppBarColors(),
            )
        },
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .imePadding(),
        ) {
            Column(
                modifier =
                    Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Card(
                    colors =
                        CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                        ),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            "Identité",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold,
                        )
                        CivilityDropdownField(
                            options = uiState.civilityOptions,
                            selectedCode = uiState.selectedCivilityCode,
                            onSelect = viewModel::onCivilityChange,
                            enabled = !uiState.isSubmitting,
                            isError = uiState.fieldErrors.containsKey("civility"),
                            supportingText = uiState.fieldErrors["civility"],
                            modifier = Modifier.fillMaxWidth(),
                        )
                        OutlinedTextField(
                            value = uiState.firstName,
                            onValueChange = viewModel::onFirstNameChange,
                            label = { Text("Prénom *", color = MaterialTheme.colorScheme.onSurface) },
                            singleLine = true,
                            isError = uiState.fieldErrors.containsKey("firstName"),
                            supportingText = { uiState.fieldErrors["firstName"]?.let { Text(it) } },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isSubmitting,
                            colors = savioFieldColors(),
                        )
                        OutlinedTextField(
                            value = uiState.lastName,
                            onValueChange = viewModel::onLastNameChange,
                            label = { Text("Nom *", color = MaterialTheme.colorScheme.onSurface) },
                            singleLine = true,
                            isError = uiState.fieldErrors.containsKey("lastName"),
                            supportingText = { uiState.fieldErrors["lastName"]?.let { Text(it) } },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isSubmitting,
                            colors = savioFieldColors(),
                        )
                        OutlinedTextField(
                            value = uiState.phone,
                            onValueChange = viewModel::onPhoneChange,
                            label = { Text("Téléphone", color = MaterialTheme.colorScheme.onSurface) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isSubmitting,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            colors = savioFieldColors(),
                        )
                        OutlinedTextField(
                            value = uiState.email,
                            onValueChange = viewModel::onEmailChange,
                            label = { Text("Email", color = MaterialTheme.colorScheme.onSurface) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isSubmitting,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            colors = savioFieldColors(),
                        )
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            "Adresse",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold,
                        )
                        when (uiState.addressEntryMode) {
                            ClientAddressEntryMode.BAN -> {
                                var banFieldHeightPx by remember { mutableIntStateOf(0) }
                                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                                    OutlinedTextField(
                                        value = uiState.addressSearchText,
                                        onValueChange = viewModel::onAddressSearchTextChange,
                                        modifier =
                                            Modifier
                                                .fillMaxWidth()
                                                .onGloballyPositioned {
                                                    banFieldHeightPx = it.size.height
                                                },
                                        label = { Text("Adresse * (BAN)", color = MaterialTheme.colorScheme.onSurface) },
                                        placeholder = {
                                            Text(
                                                "Tapez au moins 3 caractères…",
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        },
                                        trailingIcon = {
                                            if (uiState.banLoading) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(22.dp),
                                                    color = SavioPalette.Accent,
                                                    strokeWidth = 2.dp,
                                                )
                                            }
                                        },
                                        isError = uiState.fieldErrors.containsKey("address"),
                                        supportingText = { uiState.fieldErrors["address"]?.let { Text(it) } },
                                        enabled = !uiState.isSubmitting,
                                        colors = savioFieldColors(),
                                    )
                                    BanSuggestionsPopupAboveField(
                                        suggestions = uiState.banSuggestions,
                                        onPick = { pick ->
                                            keyboardController?.hide()
                                            viewModel.onSelectBanSuggestion(pick)
                                        },
                                        popupYOffsetPx =
                                            -banFieldHeightPx - with(density) { 8.dp.roundToPx() },
                                        suggestionWidth = maxWidth,
                                    )
                                }
                                val showManualLink =
                                    uiState.addressSearchText.trim().length >= 3 &&
                                        !uiState.banLoading &&
                                        (uiState.banSearchError || uiState.banSuggestions.isEmpty())
                                if (showManualLink) {
                                    Text(
                                        text = "Saisir l'adresse manuellement",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textDecoration = TextDecoration.Underline,
                                        modifier =
                                            Modifier
                                                .clickable(enabled = !uiState.isSubmitting) {
                                                    viewModel.enterManualAddressMode()
                                                }
                                                .padding(top = 4.dp),
                                    )
                                }
                            }
                            ClientAddressEntryMode.MANUAL -> {
                                Text(
                                    "Saisie manuelle (sans géolocalisation)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                OutlinedTextField(
                                    value = uiState.streetResolved,
                                    onValueChange = viewModel::onManualStreetChange,
                                    label = { Text("Numéro et voie *", color = MaterialTheme.colorScheme.onSurface) },
                                    singleLine = false,
                                    maxLines = 2,
                                    modifier = Modifier.fillMaxWidth(),
                                    isError = uiState.fieldErrors.containsKey("address"),
                                    supportingText = { uiState.fieldErrors["address"]?.let { Text(it) } },
                                    enabled = !uiState.isSubmitting,
                                    colors = savioFieldColors(),
                                )
                                OutlinedTextField(
                                    value = uiState.postalCode,
                                    onValueChange = viewModel::onManualPostalChange,
                                    label = { Text("Code postal *", color = MaterialTheme.colorScheme.onSurface) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                                    enabled = !uiState.isSubmitting,
                                    colors = savioFieldColors(),
                                )
                                OutlinedTextField(
                                    value = uiState.city,
                                    onValueChange = viewModel::onManualCityChange,
                                    label = { Text("Ville *", color = MaterialTheme.colorScheme.onSurface) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = !uiState.isSubmitting,
                                    colors = savioFieldColors(),
                                )
                                TextButton(
                                    onClick = viewModel::returnToBanSearchMode,
                                    enabled = !uiState.isSubmitting,
                                    modifier = Modifier.padding(top = 4.dp),
                                ) {
                                    Text(
                                        "Rechercher quand même",
                                        color = SavioPalette.Accent,
                                        style = MaterialTheme.typography.bodySmall,
                                    )
                                }
                            }
                        }
                        OutlinedTextField(
                            value = uiState.addressComplement,
                            onValueChange = viewModel::onAddressComplementChange,
                            label = { Text("Complément d'adresse", color = MaterialTheme.colorScheme.onSurface) },
                            singleLine = false,
                            maxLines = 3,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isSubmitting,
                            colors = savioFieldColors(),
                        )
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            "Logement",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold,
                        )
                        UnitTypeDropdownField(
                            options = uiState.unitTypeOptions,
                            selectedCode = uiState.selectedUnitTypeCode,
                            onSelect = viewModel::onUnitTypeChange,
                            enabled = !uiState.isSubmitting,
                            isError = uiState.fieldErrors.containsKey("unitType"),
                            supportingText = uiState.fieldErrors["unitType"],
                            modifier = Modifier.fillMaxWidth(),
                        )
                        if (uiState.showsFloorField) {
                            OutlinedTextField(
                                value = uiState.floor,
                                onValueChange = viewModel::onFloorChange,
                                label = { Text("Étage", color = MaterialTheme.colorScheme.onSurface) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !uiState.isSubmitting,
                                colors = savioFieldColors(),
                            )
                        }
                    }
                }

                uiState.submitError?.let { err ->
                    Text(err, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            Button(
                onClick = viewModel::submit,
                enabled = !uiState.isSubmitting && createdClientSheet == null,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = SavioPalette.Accent,
                        contentColor = SavioPalette.OnAccent,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    ),
                contentPadding = PaddingValues(vertical = 14.dp),
            ) {
                if (uiState.isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = SavioPalette.OnAccent,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text("Créer le client", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun BanSuggestionsPopupAboveField(
    suggestions: List<BanAddressPick>,
    onPick: (BanAddressPick) -> Unit,
    popupYOffsetPx: Int,
    suggestionWidth: Dp,
) {
    if (suggestions.isEmpty()) return
    val scrollState = rememberScrollState()
    Popup(
        alignment = Alignment.BottomStart,
        offset = IntOffset(0, popupYOffsetPx),
        properties =
            PopupProperties(
                focusable = false,
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
            ),
    ) {
        Column(
            modifier =
                Modifier
                    .width(suggestionWidth)
                    .heightIn(max = 240.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                    .verticalScroll(scrollState),
        ) {
            suggestions.take(10).forEach { pick ->
                Surface(
                    onClick = { onPick(pick) },
                    color = MaterialTheme.colorScheme.surface,
                ) {
                    Text(
                        text = pick.label,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}
