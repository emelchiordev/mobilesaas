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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import re.melchior.saviomobile.data.local.entity.EquipmentEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquipementDetailScreen(
    onBack: () -> Unit,
    viewModel: EquipementDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.equipment?.let {
                            listOfNotNull(it.brand, it.model).joinToString(" ")
                        } ?: "Équipement"
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { padding ->
        when {
            uiState.equipment == null -> {
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
                val equipment = uiState.equipment!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Infos générales
                    InfoCard(equipment = equipment)

                    // Document requis selon le type
                    DocumentRequis(equipment = equipment)

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun InfoCard(equipment: EquipmentEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Informations",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            InfoRow(label = "Marque", value = equipment.brand)
            InfoRow(label = "Modèle", value = equipment.model)
            InfoRow(label = "Type", value = equipment.typeCode)
            InfoRow(label = "Énergie", value = equipment.energyCode)
            InfoRow(label = "N° série", value = equipment.serialNumber)
            InfoRow(
                label = "Installation",
                value = equipment.installDate?.substring(0, 10)
            )
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String?) {
    if (value.isNullOrBlank()) return
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
    HorizontalDivider(modifier = Modifier.padding(top = 4.dp))
}

@Composable
private fun DocumentRequis(equipment: EquipmentEntity) {
    val documentType = when {
        equipment.typeCode in listOf("boiler") &&
                equipment.energyCode in listOf("gas", "oil") ->
            "attestation"
        equipment.typeCode in listOf("heat_pump", "ac") ->
            "cerfa"
        else -> null
    }

    documentType ?: return

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (documentType) {
                "attestation" -> MaterialTheme.colorScheme.tertiaryContainer
                else -> MaterialTheme.colorScheme.secondaryContainer
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = when (documentType) {
                    "attestation" -> "Attestation d'entretien requise"
                    else -> "CERFA fluides frigorigènes requis"
                },
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = when (documentType) {
                    "attestation" -> MaterialTheme.colorScheme.onTertiaryContainer
                    else -> MaterialTheme.colorScheme.onSecondaryContainer
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = when (documentType) {
                    "attestation" ->
                        "Pression circuit, température, indice de fumée, CO fumées, points de contrôle."
                    else ->
                        "Type de fluide, quantités récupérées/rechargées, attestation d'intervention."
                },
                style = MaterialTheme.typography.bodySmall,
                color = when (documentType) {
                    "attestation" -> MaterialTheme.colorScheme.onTertiaryContainer
                    else -> MaterialTheme.colorScheme.onSecondaryContainer
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "→ Remplir le document (à venir)",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = when (documentType) {
                    "attestation" -> MaterialTheme.colorScheme.onTertiaryContainer
                    else -> MaterialTheme.colorScheme.onSecondaryContainer
                }
            )
        }
    }
}