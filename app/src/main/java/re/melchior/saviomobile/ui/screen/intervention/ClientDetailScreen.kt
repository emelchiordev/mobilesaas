package re.melchior.saviomobile.ui.screen.intervention

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientDetailScreen(
    onBack: () -> Unit,
    viewModel: ClientDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissError()
        }
    }

    LaunchedEffect(uiState.savedSuccess) {
        if (uiState.savedSuccess) {
            val message = if (uiState.updatesRequireValidation)
                "Modifications enregistrées — en attente de validation"
            else
                "Modifications enregistrées"
            snackbarHostState.showSnackbar(message)
            viewModel.dismissSuccess()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = uiState.intervention?.let {
                                "${it.customerFirstName ?: ""} ${it.customerLastName ?: ""}".trim()
                            } ?: "Fiche client"
                        )
                        if (uiState.updatesRequireValidation) {
                            Text(
                                text = "Modifications soumises à validation",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    if (!uiState.isEditing) {
                        IconButton(onClick = viewModel::startEditing) {
                            Icon(
                                Icons.Filled.Edit,
                                contentDescription = "Modifier",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        when {
            uiState.intervention == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            else -> {
                val intervention = uiState.intervention!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Info validation si nécessaire
                    if (uiState.updatesRequireValidation && uiState.isEditing) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Filled.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.padding(4.dp))
                                Text(
                                    text = "Vos modifications seront soumises à validation avant d'être appliquées.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }

                    // Infos fixes (non éditables)
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Identité",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "${intervention.customerFirstName ?: ""} ${intervention.customerLastName ?: ""}".trim(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Coordonnées éditables
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Coordonnées",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            if (uiState.isEditing) {
                                OutlinedTextField(
                                    value = uiState.phone,
                                    onValueChange = viewModel::onPhoneChange,
                                    label = { Text("Téléphone") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = uiState.email,
                                    onValueChange = viewModel::onEmailChange,
                                    label = { Text("Email") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = uiState.notes,
                                    onValueChange = viewModel::onNotesChange,
                                    label = { Text("Notes") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(100.dp),
                                    maxLines = 4
                                )
                            } else {
                                InfoLigne("Téléphone", uiState.phone.ifBlank { "Non renseigné" })
                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                InfoLigne("Email", uiState.email.ifBlank { "Non renseigné" })
                                if (uiState.notes.isNotBlank()) {
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                    InfoLigne("Notes", uiState.notes)
                                }
                            }
                        }
                    }

                    // Accès logement éditable
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Accès logement",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = intervention.unitStreet,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            if (uiState.isEditing) {
                                OutlinedTextField(
                                    value = uiState.addressLine2,
                                    onValueChange = viewModel::onAddressLine2Change,
                                    label = { Text("Complément d'adresse (bât, résidence...)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = uiState.floor,
                                        onValueChange = viewModel::onFloorChange,
                                        label = { Text("Étage") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = uiState.doorCode,
                                        onValueChange = viewModel::onDoorCodeChange,
                                        label = { Text("Code accès") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                }
                            } else {
                                if (uiState.addressLine2.isNotBlank()) {
                                    InfoLigne("Complément", uiState.addressLine2)
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                }
                                InfoLigne(
                                    "Étage",
                                    uiState.floor.ifBlank { "Non renseigné" }
                                )
                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                InfoLigne(
                                    "Code accès",
                                    uiState.doorCode.ifBlank { "Non renseigné" }
                                )
                            }
                        }
                    }

                    // Boutons édition
                    if (uiState.isEditing) {
                        Button(
                            onClick = viewModel::saveChanges,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            enabled = !uiState.isSaving
                        ) {
                            if (uiState.isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = if (uiState.updatesRequireValidation)
                                        "Soumettre les modifications"
                                    else
                                        "Enregistrer",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = viewModel::cancelEditing,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Annuler")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun InfoLigne(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}