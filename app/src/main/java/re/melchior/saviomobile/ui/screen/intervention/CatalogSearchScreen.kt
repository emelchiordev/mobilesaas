package re.melchior.saviomobile.ui.screen.intervention

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import re.melchior.saviomobile.ui.theme.SavioPalette
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import re.melchior.saviomobile.data.local.entity.CatalogEquipmentSearchRow
import re.melchior.saviomobile.ui.component.BrandLogo

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
    val isCreating by viewModel.isCreating.collectAsStateWithLifecycle()

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
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground,
                        navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                    ),
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
                Text(
                    text = "Aucun résultat — essayez la saisie manuelle",
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
                                    viewModel.selectEquipment(
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
                val (iconRes, iconColor, _) = when {
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
                        .background(SavioPalette.SurfaceCard)
                        .border(
                            width = 0.5.dp,
                            color = SavioPalette.BorderDefault,
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
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp),
                    ) {
                        Text(
                            text = row.brandLabel ?: "—",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        if (!row.energyLabel.isNullOrBlank()) {
                            Text(
                                text = "·",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            val energyUpper = row.energyLabel.uppercase()
                            val (energyBg, energyFg) = when {
                                energyUpper.contains("GAZ") ->
                                    Pair(Color(0xFFE3F2FD), Color(0xFF1565C0))
                                energyUpper.contains("CLIM") ->
                                    Pair(Color(0xFFE8F5E9), Color(0xFF2E7D32))
                                energyUpper.contains("PAC") ->
                                    Pair(Color(0xFFEDE7F6), Color(0xFF4527A0))
                                energyUpper.contains("FIOUL") ->
                                    Pair(Color(0xFFFFF3E0), Color(0xFFE65100))
                                energyUpper.contains("ELEC") ->
                                    Pair(Color(0xFFFFFDE7), Color(0xFFF9A825))
                                else ->
                                    Pair(
                                        MaterialTheme.colorScheme.surfaceContainerHigh,
                                        MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                            }
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = energyBg,
                            ) {
                                Text(
                                    text = row.energyLabel,
                                    modifier = Modifier.padding(
                                        horizontal = 6.dp,
                                        vertical = 2.dp,
                                    ),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = energyFg,
                                    fontWeight = FontWeight.Medium,
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
