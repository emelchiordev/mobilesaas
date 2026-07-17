package re.savio.mobile.ui.screen.client

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import re.savio.mobile.ui.theme.savioFieldColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CivilityDropdownField(
    options: List<CivilityOptionUi>,
    selectedCode: String?,
    onSelect: (String) -> Unit,
    enabled: Boolean,
    isError: Boolean,
    supportingText: String?,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = options.firstOrNull { it.code == selectedCode }?.label ?: "Sélectionner…"
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled) expanded = it },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            label = { Text("Civilité *") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier =
                Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
            enabled = enabled,
            isError = isError,
            supportingText = supportingText?.let { { Text(it) } },
            colors = savioFieldColors(),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label) },
                    onClick = {
                        onSelect(option.code)
                        expanded = false
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitTypeDropdownField(
    options: List<UnitTypeOptionUi>,
    selectedCode: String?,
    onSelect: (String) -> Unit,
    enabled: Boolean,
    isError: Boolean,
    supportingText: String?,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = options.firstOrNull { it.code == selectedCode }?.label ?: "Sélectionner…"
    val individual = options.filter { it.category == "individual" }
    val collective = options.filter { it.category == "collective" }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled) expanded = it },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            label = { Text("Type de logement *") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier =
                Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
            enabled = enabled,
            isError = isError,
            supportingText = supportingText?.let { { Text(it) } },
            colors = savioFieldColors(),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            if (individual.isNotEmpty()) {
                DropdownMenuItem(
                    text = {
                        Text(
                            "Individuel",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    onClick = {},
                    enabled = false,
                )
                individual.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.label) },
                        onClick = {
                            onSelect(option.code)
                            expanded = false
                        },
                    )
                }
            }
            if (collective.isNotEmpty()) {
                DropdownMenuItem(
                    text = {
                        Text(
                            "Collectif",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    onClick = {},
                    enabled = false,
                )
                collective.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.label) },
                        onClick = {
                            onSelect(option.code)
                            expanded = false
                        },
                    )
                }
            }
        }
    }
}
