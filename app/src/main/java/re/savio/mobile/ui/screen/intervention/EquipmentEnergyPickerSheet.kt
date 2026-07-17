package re.savio.mobile.ui.screen.intervention

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import re.savio.mobile.data.local.entity.CatalogNomenclatureEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquipmentEnergyPickerSheet(
    title: String,
    modelLabel: String,
    subtitle: String?,
    catalogEnergyHint: String?,
    energies: List<CatalogNomenclatureEntity>,
    selectedEnergyId: String,
    onEnergySelected: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmEnabled: Boolean = selectedEnergyId.isNotBlank(),
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var energyMenuExpanded by remember { mutableStateOf(false) }
    val selectedEnergy = energies.find { it.id == selectedEnergyId }
    val selectedEnergyLabel = selectedEnergy?.label ?: catalogEnergyHint ?: "—"

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = modelLabel,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (!catalogEnergyHint.isNullOrBlank()) {
                Text(
                    text = "Catalogue : $catalogEnergyHint — modifiable si le raccordement diffère.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            ExposedDropdownMenuBox(
                expanded = energyMenuExpanded,
                onExpandedChange = { energyMenuExpanded = !energyMenuExpanded },
            ) {
                OutlinedTextField(
                    value = selectedEnergyLabel,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Énergie raccordée") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = energyMenuExpanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(
                            type = MenuAnchorType.PrimaryNotEditable,
                            enabled = true,
                        ),
                )
                ExposedDropdownMenu(
                    expanded = energyMenuExpanded,
                    onDismissRequest = { energyMenuExpanded = false },
                ) {
                    energies.forEach { energy ->
                        DropdownMenuItem(
                            text = { Text("${energy.label} (${energy.code})") },
                            onClick = {
                                onEnergySelected(energy.id)
                                energyMenuExpanded = false
                            },
                        )
                    }
                }
            }
            Button(
                onClick = onConfirm,
                modifier = Modifier.fillMaxWidth(),
                enabled = confirmEnabled,
            ) {
                Text("Valider")
            }
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Annuler")
            }
        }
    }
}

fun resolveEnergyDisplayLabel(
    energyCode: String?,
    energies: List<CatalogNomenclatureEntity>,
): String {
    val code = energyCode?.trim().orEmpty()
    if (code.isBlank() || code.uppercase() == "INCONNU") return "À renseigner"
    return energies.find { it.code.equals(code, ignoreCase = true) }?.label ?: code.uppercase()
}

fun isEnergyTodo(energyCode: String?): Boolean {
    val code = energyCode?.trim().orEmpty()
    return code.isBlank() || code.uppercase() == "INCONNU"
}

fun resolveEnergyIdForCode(
    energyCode: String?,
    energies: List<CatalogNomenclatureEntity>,
): String {
    val code = energyCode?.trim().orEmpty()
    if (code.isBlank()) return ""
    return energies.find { it.code.equals(code, ignoreCase = true) }?.id.orEmpty()
}
