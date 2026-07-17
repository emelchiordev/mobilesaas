package re.savio.mobile.ui.screen.intervention

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import re.savio.mobile.ui.theme.SavioPalette
import re.savio.mobile.ui.theme.savioTopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import re.savio.mobile.data.local.entity.CatalogEquipmentSearchRow
import re.savio.mobile.ui.component.BrandLogo
import re.savio.mobile.ui.theme.formatEquipmentTypeLabel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogSearchScreen(
    interventionId: String,
    unitId: String,
    existingEquipmentId: String?,
    parentEquipmentId: String?,
    onManualEntry: () -> Unit,
    onBack: () -> Unit,
    onEquipmentSelected: (equipmentId: String) -> Unit,
    viewModel: CatalogSearchViewModel = hiltViewModel(),
) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    val selectedBrandId by viewModel.selectedBrandId.collectAsStateWithLifecycle()
    val selectedTypeId by viewModel.selectedTypeId.collectAsStateWithLifecycle()
    val results by viewModel.results.collectAsStateWithLifecycle()
    val brands by viewModel.brands.collectAsStateWithLifecycle(initialValue = emptyList())
    val equipmentTypes by viewModel.equipmentTypes.collectAsStateWithLifecycle(initialValue = emptyList())
    val energies by viewModel.energies.collectAsStateWithLifecycle(initialValue = emptyList())
    val pendingSelection by viewModel.pendingSelection.collectAsStateWithLifecycle()
    val isCreating by viewModel.isCreating.collectAsStateWithLifecycle()

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(Unit) {
        viewModel.createdEquipmentId.collect { newId ->
            onEquipmentSelected(newId)
        }
    }

    var brandMenuExpanded by remember { mutableStateOf(false) }
    var typeMenuExpanded by remember { mutableStateOf(false) }

    val selectedBrandLabel = brands.find { it.id == selectedBrandId }?.label ?: "Toutes les marques"
    val selectedTypeLabel = equipmentTypes.find { it.id == selectedTypeId }?.label ?: "Tous les types"

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Choisir un appareil") },
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
                .padding(horizontal = 16.dp),
        ) {
            TextButton(
                onClick = onManualEntry,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
            ) {
                Text("Saisie manuelle")
            }

            OutlinedTextField(
                value = query,
                onValueChange = viewModel::onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Rechercher") },
                singleLine = true,
            )

            Spacer(modifier = Modifier.padding(4.dp))

            ExposedDropdownMenuBox(
                expanded = brandMenuExpanded,
                onExpandedChange = { brandMenuExpanded = !brandMenuExpanded },
            ) {
                OutlinedTextField(
                    value = selectedBrandLabel,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Marque") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = brandMenuExpanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(
                            type = MenuAnchorType.PrimaryNotEditable,
                            enabled = true,
                        ),
                )
                ExposedDropdownMenu(
                    expanded = brandMenuExpanded,
                    onDismissRequest = { brandMenuExpanded = false },
                ) {
                    DropdownMenuItem(
                        text = { Text("Toutes les marques") },
                        onClick = {
                            viewModel.onBrandSelected(null)
                            brandMenuExpanded = false
                        },
                    )
                    brands.forEach { b ->
                        DropdownMenuItem(
                            text = { Text(b.label) },
                            onClick = {
                                viewModel.onBrandSelected(b.id)
                                brandMenuExpanded = false
                            },
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.padding(4.dp))

            ExposedDropdownMenuBox(
                expanded = typeMenuExpanded,
                onExpandedChange = { typeMenuExpanded = !typeMenuExpanded },
            ) {
                OutlinedTextField(
                    value = selectedTypeLabel,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Type") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeMenuExpanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(
                            type = MenuAnchorType.PrimaryNotEditable,
                            enabled = true,
                        ),
                )
                ExposedDropdownMenu(
                    expanded = typeMenuExpanded,
                    onDismissRequest = { typeMenuExpanded = false },
                ) {
                    DropdownMenuItem(
                        text = { Text("Tous les types") },
                        onClick = {
                            viewModel.onTypeSelected(null)
                            typeMenuExpanded = false
                        },
                    )
                    equipmentTypes.forEach { t ->
                        DropdownMenuItem(
                            text = { Text(t.label) },
                            onClick = {
                                viewModel.onTypeSelected(t.id)
                                typeMenuExpanded = false
                            },
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.padding(8.dp))

            if (results.isEmpty()) {
                val emptyHint = when {
                    brands.isEmpty() && equipmentTypes.isEmpty() ->
                        "Catalogue vide ou non synchronisé. Sur l’écran Tournée, appuyez sur l’icône livre pour mettre à jour le catalogue BAN, puis réessayez."
                    query.isNotBlank() || selectedBrandId != null || selectedTypeId != null ->
                        "Aucun résultat pour ces critères — essayez la saisie manuelle."
                    else ->
                        "Aucun appareil dans le catalogue local — synchronisez depuis la Tournée (icône livre)."
                }
                Text(
                    text = emptyHint,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 24.dp),
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(0.dp),
                ) {
                    items(results, key = { it.equipment.id }) { row ->
                        CatalogEquipmentRow(
                            row = row,
                            onClick = {
                                if (!isCreating) {
                                    viewModel.requestSelectEquipment(
                                        row = row,
                                        interventionId = interventionId,
                                        unitId = unitId,
                                        parentEquipmentId = parentEquipmentId,
                                        existingEquipmentId = existingEquipmentId,
                                    )
                                }
                            },
                        )
                    }
                }
            }
        }
    }

    pendingSelection?.let { pending ->
        val typeCode = pending.row.typeCode?.trim()?.uppercase().orEmpty()
        val isBruleur = typeCode == "BRULEUR"
        val catalogEnergyLabel = pending.row.energyLabel?.takeIf { it.isNotBlank() } ?: "—"
        var energyMenuExpanded by remember(pending.row.equipment.id) { mutableStateOf(false) }
        val selectedEnergy = energies.find { it.id == pending.selectedEnergyId }
        val selectedEnergyLabel = selectedEnergy?.label ?: catalogEnergyLabel

        ModalBottomSheet(
            onDismissRequest = viewModel::dismissPendingSelection,
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
                    text = "Énergie raccordée",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = pending.row.equipment.model,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = listOfNotNull(pending.row.brandLabel, pending.row.typeLabel)
                        .joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (!isBruleur) {
                    Text(
                        text = "Catalogue : $catalogEnergyLabel — modifiable si le raccordement diffère.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
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
                                        viewModel.updatePendingEnergyId(energy.id)
                                        energyMenuExpanded = false
                                    },
                                )
                            }
                        }
                    }
                }
                Button(
                    onClick = viewModel::confirmPendingSelection,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = isBruleur || pending.selectedEnergyId.isNotBlank(),
                ) {
                    Text("Confirmer")
                }
                TextButton(
                    onClick = viewModel::dismissPendingSelection,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Annuler")
                }
            }
        }
    }
}

@Composable
private fun CatalogEquipmentRow(
    row: CatalogEquipmentSearchRow,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClick)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                val typeUpper = row.typeLabel?.uppercase() ?: ""
                val (iconRes, iconColor, typeChipBg) = when {
                    typeUpper.contains("CHAUDIERE") ->
                        Triple(
                            Icons.Filled.LocalFireDepartment,
                            Color(0xFFE53935),
                            Color(0xFFFFECEC),
                        )
                    typeUpper.contains("PAC") ->
                        Triple(
                            Icons.Filled.Air,
                            Color(0xFF3949AB),
                            Color(0xFFECF0FF),
                        )
                    typeUpper.contains("CLIM") ->
                        Triple(
                            Icons.Filled.AcUnit,
                            Color(0xFF0288D1),
                            Color(0xFFECF9FF),
                        )
                    typeUpper.contains("CHAUFFE") || typeUpper.contains("BALLON") ->
                        Triple(
                            Icons.Filled.WaterDrop,
                            Color(0xFFF57C00),
                            Color(0xFFFFF8EC),
                        )
                    typeUpper.contains("VMC") ->
                        Triple(
                            Icons.Filled.Air,
                            Color(0xFF43A047),
                            Color(0xFFECF9EC),
                        )
                    else ->
                        Triple(
                            Icons.Filled.Build,
                            MaterialTheme.colorScheme.onSecondaryContainer,
                            MaterialTheme.colorScheme.secondaryContainer,
                        )
                }

                Box(
                    modifier = Modifier
                        .width(52.dp)
                        .height(34.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(
                            width = 0.5.dp,
                            color = MaterialTheme.colorScheme.outline,
                            shape = RoundedCornerShape(6.dp),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    BrandLogo(
                        brandId = row.equipment.brandId,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(4.dp),
                        fallback = {
                            Icon(
                                imageVector = iconRes,
                                contentDescription = null,
                                tint = iconColor,
                                modifier = Modifier.size(20.dp),
                            )
                        },
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = row.equipment.model,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = row.brandLabel ?: "—",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    val typeLabel =
                        row.typeLabel?.takeIf { it.isNotBlank() }
                            ?: formatEquipmentTypeLabel(row.typeCode).takeIf { it.isNotBlank() }
                    val energyLabel = row.energyLabel?.takeIf { it.isNotBlank() }
                    if (typeLabel != null || energyLabel != null) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 4.dp),
                        ) {
                            if (typeLabel != null) {
                                CatalogMetaChip(
                                    text = typeLabel,
                                    background = typeChipBg,
                                    content = iconColor,
                                )
                            }
                            if (energyLabel != null) {
                                val (energyBg, energyFg) = catalogEnergyChipColors(energyLabel)
                                CatalogMetaChip(
                                    text = energyLabel,
                                    background = energyBg,
                                    content = energyFg,
                                )
                            }
                        }
                    }
                    if (row.equipment.powerKw != null) {
                        Text(
                            text = "${row.equipment.powerKw} kW",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
            }
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.outlineVariant,
            )
        }
    }
}

@Composable
private fun CatalogMetaChip(
    text: String,
    background: Color,
    content: Color,
) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = background,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = content,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private fun catalogEnergyChipColors(energyLabel: String): Pair<Color, Color> {
    val energyUpper = energyLabel.uppercase()
    return when {
        energyUpper.contains("GAZ") ->
            Color(0xFFE3F2FD) to Color(0xFF1565C0)
        energyUpper.contains("CLIM") ->
            Color(0xFFE8F5E9) to Color(0xFF2E7D32)
        energyUpper.contains("PAC") ->
            Color(0xFFEDE7F6) to Color(0xFF4527A0)
        energyUpper.contains("FIOUL") ->
            Color(0xFFFFF3E0) to Color(0xFFE65100)
        energyUpper.contains("ELEC") ->
            Color(0xFFFFFDE7) to Color(0xFFF9A825)
        else ->
            Color(0xFFF5F5F5) to Color(0xFF616161)
    }
}
