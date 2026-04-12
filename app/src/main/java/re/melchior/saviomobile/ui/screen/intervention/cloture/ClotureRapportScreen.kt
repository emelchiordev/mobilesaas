package re.melchior.saviomobile.ui.screen.intervention.cloture

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import re.melchior.saviomobile.data.remote.dto.InterventionTypeDto
import re.melchior.saviomobile.data.remote.dto.stableKey

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ClotureRapportScreen(
    onBack: () -> Unit,
    onNext: (interventionId: String, preselectedActualTypeKeys: String) -> Unit,
    viewModel: ClotureRapportViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Clôture — étape 1 / 2",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Compte-rendu",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Barre de progression étape 1/2
            LinearProgressIndicator(
                progress = { 0.5f },
                modifier = Modifier.fillMaxWidth()
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Infos intervention
                uiState.intervention?.let { intervention ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${intervention.customerFirstName ?: ""} " +
                                        "${intervention.customerLastName ?: ""}".trim()
                                            .ifEmpty { "Client non renseigné" },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${intervention.unitStreet}, ${intervention.unitCity}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = intervention.typeLabel,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                if (uiState.closeTypesLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                } else if (uiState.closeTypesError != null) {
                    Text(
                        text = uiState.closeTypesError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    ActualTypeChips(
                        types = uiState.closeTypes,
                        selectedTypes = uiState.selectedCloseTypes,
                        onToggle = viewModel::toggleCloseType
                    )
                }

                if (uiState.isVeChanged) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFFFFEBEE)
                    ) {
                        Text(
                            text = "Attention : cette intervention était planifiée comme une " +
                                    "Visite d'entretien. Changer le type annulera la couverture VE du contrat.",
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFB71C1C)
                        )
                    }
                }

                if (!uiState.showReport) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFFE3F2FD)
                    ) {
                        Text(
                            text = "Aucun compte-rendu requis pour les types sélectionnés.",
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                if (uiState.isAbsent) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFFE3F2FD)
                    ) {
                        Text(
                            text = "Absence : le compte-rendu et la signature client peuvent être non requis selon les types.",
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                if (uiState.showReport) {
                    OutlinedTextField(
                        value = uiState.report,
                        onValueChange = viewModel::onReportChange,
                        label = { Text("Compte-rendu d'intervention") },
                        placeholder = {
                            Text(
                                text = "Décrivez les opérations effectuées, " +
                                        "les observations, les pièces remplacées...",
                                style = MaterialTheme.typography.bodySmall
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp),
                        maxLines = Int.MAX_VALUE,
                        supportingText = {
                            Text(
                                text = "${uiState.report.length} caractères",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Bouton suivant
                Button(
                    onClick = {
                        scope.launch {
                            val saved = viewModel.saveReport()
                            if (saved) {
                                uiState.intervention?.id?.let { id ->
                                    onNext(id, viewModel.preselectedKeysForNavigation())
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    enabled = uiState.canProceed && !uiState.isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Suivant — Signature →",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ActualTypeChips(
    types: List<InterventionTypeDto>,
    selectedTypes: List<InterventionTypeDto>,
    onToggle: (InterventionTypeDto) -> Unit
) {
    Column {
        Text(
            text = "Type(s) réel(s) de l'intervention",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            types.forEach { type ->
                val isSelected = selectedTypes.any {
                    it.stableKey() == type.stableKey()
                }
                val typeColor = remember(type.color) {
                    try {
                        type.color?.let {
                            Color(android.graphics.Color.parseColor(it))
                        }
                    } catch (e: Exception) {
                        null
                    }
                } ?: MaterialTheme.colorScheme.primary

                FilterChip(
                    selected = isSelected,
                    onClick = { onToggle(type) },
                    label = { Text(type.label, fontSize = 13.sp) },
                    leadingIcon = if (isSelected) {
                        {
                            Icon(
                                Icons.Filled.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    } else {
                        null
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = typeColor.copy(alpha = 0.15f),
                        selectedLabelColor = typeColor,
                        selectedLeadingIconColor = typeColor
                    )
                )
            }
        }

        if (selectedTypes.isEmpty()) {
            Text(
                text = "Sélectionnez au moins un type",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}