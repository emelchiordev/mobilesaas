package re.melchior.saviomobile.ui.screen.intervention

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import re.melchior.saviomobile.ui.theme.savioTopAppBarColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquipmentFormScreen(
    interventionId: String,
    unitId: String,
    catalogEquipmentId: String?,
    existingEquipmentId: String?,
    parentEquipmentId: String?,
    onSaved: () -> Unit,
    onBack: () -> Unit,
    viewModel: EquipmentFormViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.saved.collect {
            onSaved()
        }
    }

    val title = if (existingEquipmentId != null) {
        "Remplacer l'appareil"
    } else {
        "Nouvel appareil"
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                colors =
                    savioTopAppBarColors(),
            )
        },
    ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (!catalogEquipmentId.isNullOrBlank()) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = MaterialTheme.shapes.medium,
                    ) {
                        Text(
                            text = "Pré-rempli depuis le catalogue — vous pouvez modifier",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(12.dp),
                        )
                    }
                }

                OutlinedTextField(
                    value = uiState.model,
                    onValueChange = viewModel::updateModel,
                    label = { Text("Modèle *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )

                OutlinedTextField(
                    value = uiState.brand,
                    onValueChange = viewModel::updateBrand,
                    label = { Text("Marque") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )

                OutlinedTextField(
                    value = uiState.typeCode,
                    onValueChange = viewModel::updateTypeCode,
                    label = { Text("Type") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )

                OutlinedTextField(
                    value = uiState.energyLabel,
                    onValueChange = viewModel::updateEnergyLabel,
                    label = { Text("Énergie") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )

                OutlinedTextField(
                    value = uiState.serialNumber,
                    onValueChange = viewModel::updateSerialNumber,
                    label = { Text("Numéro de série") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )

                OutlinedTextField(
                    value = uiState.notes,
                    onValueChange = viewModel::updateNotes,
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("Appareil principal", style = MaterialTheme.typography.bodyLarge)
                    Switch(
                        checked = uiState.isPrimary,
                        onCheckedChange = viewModel::updateIsPrimary,
                    )
                }

                if (existingEquipmentId != null) {
                    Text("Motif", style = MaterialTheme.typography.labelLarge)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = uiState.replacementReason == "replacement",
                            onClick = { viewModel.updateReplacementReason("replacement") },
                        )
                        Text("Remplacement", modifier = Modifier.padding(end = 16.dp))
                        RadioButton(
                            selected = uiState.replacementReason == "correction",
                            onClick = { viewModel.updateReplacementReason("correction") },
                        )
                        Text("Correction")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        viewModel.save(interventionId, unitId, existingEquipmentId, parentEquipmentId)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    enabled = uiState.model.isNotBlank(),
                ) {
                    Text("Enregistrer")
                }
            }
        }
}
