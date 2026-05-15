package re.melchior.saviomobile.ui.screen.client

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import re.melchior.saviomobile.data.repository.BanAddressPick
import re.melchior.saviomobile.ui.component.SavioSnackbarHost
import re.melchior.saviomobile.ui.theme.SavioPalette

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateClientScreen(
    onBack: () -> Unit,
    viewModel: CreateClientViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { ev ->
            when (ev) {
                CreateClientEvent.Created -> {
                    snackbarHostState.showSnackbar(
                        message = "Client créé ✅",
                        duration = SnackbarDuration.Short,
                    )
                    onBack()
                }
            }
        }
    }

    Scaffold(
        containerColor = SavioPalette.BackgroundPage,
        snackbarHost = { SavioSnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Nouveau client",
                        color = SavioPalette.TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, enabled = !uiState.isSubmitting) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour",
                            tint = SavioPalette.TextPrimary,
                        )
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = SavioPalette.BackgroundPage,
                        titleContentColor = SavioPalette.TextPrimary,
                        navigationIconContentColor = SavioPalette.TextPrimary,
                    ),
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
                            containerColor = SavioPalette.SurfaceCard,
                        ),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            "Identité",
                            style = MaterialTheme.typography.titleSmall,
                            color = SavioPalette.TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            "Civilité",
                            color = SavioPalette.TextPrimary,
                            style = MaterialTheme.typography.bodySmall,
                        )
                        SingleChoiceSegmentedButtonRow {
                            ClientCivilityUi.entries.forEachIndexed { index, c ->
                                SegmentedButton(
                                    selected = uiState.civility == c,
                                    onClick = { viewModel.onCivilityChange(c) },
                                    enabled = !uiState.isSubmitting,
                                    shape =
                                        SegmentedButtonDefaults.itemShape(
                                            index = index,
                                            count = ClientCivilityUi.entries.size,
                                        ),
                                    colors =
                                        SegmentedButtonDefaults.colors(
                                            activeContainerColor = SavioPalette.Accent,
                                            activeContentColor = SavioPalette.OnAccent,
                                            inactiveContainerColor = SavioPalette.SurfaceElevated,
                                            inactiveBorderColor = SavioPalette.BorderFieldPro,
                                            inactiveContentColor = SavioPalette.TextPrimary,
                                        ),
                                ) {
                                    Text(c.label, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                        OutlinedTextField(
                            value = uiState.firstName,
                            onValueChange = viewModel::onFirstNameChange,
                            label = { Text("Prénom *", color = SavioPalette.TextPrimary) },
                            singleLine = true,
                            isError = uiState.fieldErrors.containsKey("firstName"),
                            supportingText = { uiState.fieldErrors["firstName"]?.let { Text(it) } },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isSubmitting,
                            colors = createClientFieldColors(),
                        )
                        OutlinedTextField(
                            value = uiState.lastName,
                            onValueChange = viewModel::onLastNameChange,
                            label = { Text("Nom *", color = SavioPalette.TextPrimary) },
                            singleLine = true,
                            isError = uiState.fieldErrors.containsKey("lastName"),
                            supportingText = { uiState.fieldErrors["lastName"]?.let { Text(it) } },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isSubmitting,
                            colors = createClientFieldColors(),
                        )
                        OutlinedTextField(
                            value = uiState.phone,
                            onValueChange = viewModel::onPhoneChange,
                            label = { Text("Téléphone", color = SavioPalette.TextPrimary) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isSubmitting,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            colors = createClientFieldColors(),
                        )
                        OutlinedTextField(
                            value = uiState.email,
                            onValueChange = viewModel::onEmailChange,
                            label = { Text("Email", color = SavioPalette.TextPrimary) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isSubmitting,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            colors = createClientFieldColors(),
                        )
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = SavioPalette.SurfaceCard),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            "Adresse",
                            style = MaterialTheme.typography.titleSmall,
                            color = SavioPalette.TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                        )
                        when (uiState.addressEntryMode) {
                            ClientAddressEntryMode.BAN -> {
                                OutlinedTextField(
                                    value = uiState.addressSearchText,
                                    onValueChange = viewModel::onAddressSearchTextChange,
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("Adresse * (BAN)", color = SavioPalette.TextPrimary) },
                                    placeholder = {
                                        Text(
                                            "Tapez au moins 3 caractères…",
                                            color = SavioPalette.TextHint,
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
                                    colors = createClientFieldColors(),
                                )
                                BanSuggestionsDropdown(
                                    suggestions = uiState.banSuggestions,
                                    onPick = viewModel::onSelectBanSuggestion,
                                )
                                val showManualLink =
                                    uiState.addressSearchText.trim().length >= 3 &&
                                        !uiState.banLoading &&
                                        (uiState.banSearchError || uiState.banSuggestions.isEmpty())
                                if (showManualLink) {
                                    Text(
                                        text = "Saisir l'adresse manuellement",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SavioPalette.TextHint,
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
                                    color = SavioPalette.TextSecondary,
                                )
                                OutlinedTextField(
                                    value = uiState.streetResolved,
                                    onValueChange = viewModel::onManualStreetChange,
                                    label = { Text("Numéro et voie *", color = SavioPalette.TextPrimary) },
                                    singleLine = false,
                                    maxLines = 2,
                                    modifier = Modifier.fillMaxWidth(),
                                    isError = uiState.fieldErrors.containsKey("address"),
                                    supportingText = { uiState.fieldErrors["address"]?.let { Text(it) } },
                                    enabled = !uiState.isSubmitting,
                                    colors = createClientFieldColors(),
                                )
                                OutlinedTextField(
                                    value = uiState.postalCode,
                                    onValueChange = viewModel::onManualPostalChange,
                                    label = { Text("Code postal *", color = SavioPalette.TextPrimary) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                                    enabled = !uiState.isSubmitting,
                                    colors = createClientFieldColors(),
                                )
                                OutlinedTextField(
                                    value = uiState.city,
                                    onValueChange = viewModel::onManualCityChange,
                                    label = { Text("Ville *", color = SavioPalette.TextPrimary) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = !uiState.isSubmitting,
                                    colors = createClientFieldColors(),
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
                            label = { Text("Complément d'adresse", color = SavioPalette.TextPrimary) },
                            singleLine = false,
                            maxLines = 3,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isSubmitting,
                            colors = createClientFieldColors(),
                        )
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = SavioPalette.SurfaceCard),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            "Logement",
                            style = MaterialTheme.typography.titleSmall,
                            color = SavioPalette.TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            "Type de logement",
                            color = SavioPalette.TextPrimary,
                            style = MaterialTheme.typography.bodySmall,
                        )
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            HousingKindUi.entries.forEach { k ->
                                FilterChip(
                                    selected = uiState.housingKind == k,
                                    onClick = { viewModel.onHousingKindChange(k) },
                                    enabled = !uiState.isSubmitting,
                                    label = { Text(k.label, maxLines = 2) },
                                    colors =
                                        FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = SavioPalette.Accent,
                                            selectedLabelColor = SavioPalette.OnAccent,
                                            containerColor = SavioPalette.SurfaceElevated,
                                            labelColor = SavioPalette.TextPrimary,
                                        ),
                                )
                            }
                        }
                        if (uiState.housingKind == HousingKindUi.APPARTEMENT) {
                            OutlinedTextField(
                                value = uiState.floor,
                                onValueChange = viewModel::onFloorChange,
                                label = { Text("Étage", color = SavioPalette.TextPrimary) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !uiState.isSubmitting,
                                colors = createClientFieldColors(),
                            )
                        }
                    }
                }

                uiState.submitError?.let { err ->
                    Text(err, color = SavioPalette.Error, style = MaterialTheme.typography.bodyMedium)
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            Button(
                onClick = viewModel::submit,
                enabled = !uiState.isSubmitting,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = SavioPalette.Accent,
                        contentColor = SavioPalette.OnAccent,
                        disabledContainerColor = SavioPalette.SurfaceElevated,
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
private fun BanSuggestionsDropdown(
    suggestions: List<BanAddressPick>,
    onPick: (BanAddressPick) -> Unit,
) {
    if (suggestions.isEmpty()) return
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .border(1.dp, SavioPalette.BorderFieldPro, RoundedCornerShape(8.dp))
                .background(SavioPalette.SurfaceCard, RoundedCornerShape(8.dp)),
    ) {
        suggestions.take(10).forEach { pick ->
            Surface(
                onClick = { onPick(pick) },
                color = SavioPalette.SurfaceCard,
            ) {
                Text(
                    text = pick.label,
                    color = SavioPalette.TextPrimary,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun createClientFieldColors() =
    OutlinedTextFieldDefaults.colors(
        focusedTextColor = SavioPalette.TextPrimary,
        unfocusedTextColor = SavioPalette.TextPrimary,
        focusedLabelColor = SavioPalette.TextPrimary,
        unfocusedLabelColor = SavioPalette.TextPrimary,
        focusedBorderColor = SavioPalette.Accent,
        unfocusedBorderColor = SavioPalette.BorderFieldPro,
        cursorColor = SavioPalette.Accent,
        focusedContainerColor = SavioPalette.SurfaceCard,
        unfocusedContainerColor = SavioPalette.SurfaceCard,
        errorBorderColor = SavioPalette.Error,
        errorLabelColor = SavioPalette.Error,
        errorSupportingTextColor = SavioPalette.Error,
    )
