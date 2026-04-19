package re.melchior.saviomobile.ui.screen.intervention

import android.app.DatePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

private val TopBarBlue = Color(0xFF185FA5)
private val IdentityAvatarBg = Color(0xFFECF0FF)
private val IdentityIconTint = Color(0xFF3949AB)

private val BadgePacBg = Color(0xFFECF0FF)
private val BadgePacFg = Color(0xFF3949AB)
private val BadgeGazBg = Color(0xFFE3F2FD)
private val BadgeGazFg = Color(0xFF1565C0)
private val BadgeClimBg = Color(0xFFECF9FF)
private val BadgeClimFg = Color(0xFF0288D1)
private val BadgeKwBg = Color(0xFFEAF3DE)
private val BadgeKwFg = Color(0xFF27500A)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquipementDetailScreen(
    onBack: () -> Unit,
    onCerfaClick: (interventionId: String, equipmentId: String) -> Unit = { _, _ -> },
    onAttestationClick: (interventionId: String, equipmentId: String) -> Unit = { _, _ -> },
    onReplaceClick: (interventionId: String, equipmentId: String) -> Unit = { _, _ -> },
    viewModel: EquipementDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val catalogEquipment by viewModel.catalogEquipment.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.navigateBack.collect {
            onBack()
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text("Supprimer l'appareil")
            },
            text = {
                Text(
                    "Cet appareil sera supprimé de la fiche client. " +
                        "Cette action sera synchronisée au prochain envoi.",
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteEquipment()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error,
                    ),
                ) {
                    Text("Supprimer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Annuler")
                }
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TopBarBlue,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White,
                ),
                title = {
                    Text(
                        text = uiState.equipment?.let {
                            listOfNotNull(it.brand, it.model).joinToString(" ")
                        } ?: "Équipement",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour",
                            tint = Color.White,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Supprimer l'appareil",
                            tint = Color.White,
                        )
                    }
                },
            )
        },
    ) { padding ->
        when {
            uiState.equipment == null -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
            else -> {
                val equipment = uiState.equipment!!
                val changeMode by viewModel.changeMode.collectAsStateWithLifecycle()
                val catalogQuery by viewModel.catalogQuery.collectAsStateWithLifecycle()
                val catalogResults by viewModel.catalogResults.collectAsStateWithLifecycle()

                var showSerialDialog by remember(equipment.id) { mutableStateOf(false) }
                var serialDraft by remember(equipment.id) { mutableStateOf(equipment.serialNumber.orEmpty()) }
                LaunchedEffect(equipment.serialNumber) {
                    serialDraft = equipment.serialNumber.orEmpty()
                }

                val documentType = remember(equipment.typeCode, equipment.energyCode) {
                    when {
                        equipment.typeCode in listOf("CHAUDIERE") &&
                            equipment.energyCode in listOf("GAZ NAT", "GAZ PROP", "FIOUL") -> "attestation"
                        equipment.typeCode in listOf("PAC", "PAC A/E", "PAC A/A", "CLIMATISEUR") -> "cerfa"
                        else -> null
                    }
                }

                if (showSerialDialog) {
                    AlertDialog(
                        onDismissRequest = { showSerialDialog = false },
                        title = { Text("N° série") },
                        text = {
                            OutlinedTextField(
                                value = serialDraft,
                                onValueChange = { serialDraft = it },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        },
                        confirmButton = {
                            IconButton(
                                onClick = {
                                    viewModel.saveSerialNumber(serialDraft)
                                    showSerialDialog = false
                                },
                            ) {
                                Icon(Icons.Filled.Check, contentDescription = "Valider")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showSerialDialog = false }) {
                                Text("Annuler")
                            }
                        },
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    // SECTION 1 — Identité
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Surface(
                                    modifier = Modifier.size(40.dp),
                                    shape = CircleShape,
                                    color = IdentityAvatarBg,
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Filled.Build,
                                            contentDescription = null,
                                            tint = IdentityIconTint,
                                            modifier = Modifier.size(22.dp),
                                        )
                                    }
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Marque · Modèle",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Text(
                                        text = listOfNotNull(equipment.brand, equipment.model)
                                            .joinToString(" · ")
                                            .ifBlank { "—" },
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                }
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                equipment.typeCode?.let { code ->
                                    TypeEnergyBadge(text = code, isType = true)
                                }
                                equipment.energyCode?.let { code ->
                                    TypeEnergyBadge(text = code, isType = false)
                                }
                                catalogEquipment?.powerKw?.let { kw ->
                                    KwBadge(kw = kw)
                                }
                            }
                        }
                    }

                    // SECTION 2 — Données terrain
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                    ) {
                        Column {
                            FieldTerrainRow(
                                leadingIcon = {
                                    Icon(
                                        Icons.Filled.Edit,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                },
                                label = "N° série",
                                valueText = equipment.serialNumber?.takeIf { it.isNotBlank() } ?: "—",
                                trailingSquareIcon = Icons.Filled.Edit,
                                onTrailingClick = {
                                    serialDraft = equipment.serialNumber.orEmpty()
                                    showSerialDialog = true
                                },
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = MaterialTheme.colorScheme.outlineVariant,
                            )
                            FieldTerrainRow(
                                leadingIcon = {
                                    Icon(
                                        Icons.Filled.CalendarToday,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                },
                                label = "Mise en service",
                                valueText = formatCommissioningDate(equipment.installDate),
                                trailingSquareIcon = Icons.Filled.CalendarToday,
                                onTrailingClick = {
                                    val cal = Calendar.getInstance()
                                    equipment.installDate?.take(10)?.let { s ->
                                        try {
                                            val ld = LocalDate.parse(s)
                                            cal.set(ld.year, ld.monthValue - 1, ld.dayOfMonth)
                                        } catch (_: Exception) {
                                        }
                                    }
                                    DatePickerDialog(
                                        context,
                                        { _, y, m, d ->
                                            val iso = String.format(
                                                Locale.US,
                                                "%04d-%02d-%02d",
                                                y,
                                                m + 1,
                                                d,
                                            )
                                            viewModel.saveCommissioningDate(iso)
                                        },
                                        cal.get(Calendar.YEAR),
                                        cal.get(Calendar.MONTH),
                                        cal.get(Calendar.DAY_OF_MONTH),
                                    ).show()
                                },
                            )
                        }
                    }

                    // SECTION 3 — Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.setChangeMode(true) },
                            modifier = Modifier.weight(1f),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.SwapVert,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Corriger")
                        }
                        OutlinedButton(
                            onClick = {
                                onReplaceClick(equipment.interventionId, equipment.id)
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error,
                            ),
                            border = BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.error,
                            ),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.SwapHoriz,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Remplacer")
                        }
                    }

                    // SECTION 4 — Catalogue (change mode)
                    if (changeMode) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f),
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        "Choisir dans le catalogue",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold,
                                    )
                                    TextButton(onClick = { viewModel.setChangeMode(false) }) {
                                        Text(
                                            "Annuler",
                                            style = MaterialTheme.typography.labelSmall,
                                        )
                                    }
                                }
                                OutlinedTextField(
                                    value = catalogQuery,
                                    onValueChange = { viewModel.onCatalogQueryChange(it) },
                                    placeholder = {
                                        Text(
                                            "Rechercher…",
                                            style = MaterialTheme.typography.bodySmall,
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Filled.Search,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp),
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                )
                                if (catalogResults.isNotEmpty()) {
                                    LazyColumn(
                                        modifier = Modifier.heightIn(max = 200.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp),
                                    ) {
                                        items(catalogResults, key = { it.equipment.id }) { row ->
                                            Surface(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 2.dp),
                                                shape = RoundedCornerShape(8.dp),
                                                color = MaterialTheme.colorScheme.surfaceContainerLow,
                                                onClick = { viewModel.applyNewCatalogEquipment(row) },
                                            ) {
                                                Column(modifier = Modifier.padding(10.dp)) {
                                                    Text(
                                                        row.equipment.model,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = FontWeight.SemiBold,
                                                    )
                                                    Text(
                                                        listOfNotNull(
                                                            row.brandLabel,
                                                            row.typeLabel,
                                                            row.energyLabel,
                                                        ).joinToString(" · "),
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    )
                                                }
                                            }
                                        }
                                    }
                                } else if (catalogQuery.length >= 2) {
                                    Text(
                                        "Aucun résultat",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }

                    // SECTION 5 — Document CERFA / Attestation
                    documentType?.let { docType ->
                        val avatarBg = if (docType == "attestation") {
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
                        } else {
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                        }
                        val avatarIconTint = if (docType == "attestation") {
                            MaterialTheme.colorScheme.tertiary
                        } else {
                            MaterialTheme.colorScheme.secondary
                        }
                        val title = if (docType == "attestation") {
                            "Attestation d'entretien"
                        } else {
                            "CERFA fluides frigorigènes"
                        }
                        Surface(
                            onClick = {
                                val interventionId = equipment.interventionId
                                if (docType == "cerfa") {
                                    onCerfaClick(interventionId, equipment.id)
                                } else {
                                    onAttestationClick(interventionId, equipment.id)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Surface(
                                        modifier = Modifier.fillMaxSize(),
                                        shape = CircleShape,
                                        color = avatarBg,
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                Icons.Filled.Description,
                                                contentDescription = null,
                                                tint = avatarIconTint,
                                                modifier = Modifier.size(20.dp),
                                            )
                                        }
                                    }
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        title,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                    Text(
                                        "Remplir ou consulter",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                Icon(
                                    Icons.Filled.ChevronRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

private fun formatCommissioningDate(installDate: String?): String {
    if (installDate.isNullOrBlank()) return "—"
    return try {
        val d = LocalDate.parse(installDate.take(10))
        d.format(DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.FRENCH))
    } catch (_: Exception) {
        installDate.take(10)
    }
}

@Composable
private fun TypeEnergyBadge(text: String, isType: Boolean) {
    val upper = text.uppercase(Locale.FRANCE)
    val neutralBg = MaterialTheme.colorScheme.surfaceContainerHighest
    val neutralFg = MaterialTheme.colorScheme.onSurfaceVariant
    val (bg, fg) = when {
        isType && upper.contains("PAC") -> BadgePacBg to BadgePacFg
        isType && (upper.contains("CLIM") || upper.contains("CLIMAT")) -> BadgeClimBg to BadgeClimFg
        !isType && upper.contains("GAZ NAT") -> BadgeGazBg to BadgeGazFg
        else -> neutralBg to neutralFg
    }
    BadgeChip(background = bg, content = fg, label = text)
}

@Composable
private fun KwBadge(kw: Double) {
    BadgeChip(
        background = BadgeKwBg,
        content = BadgeKwFg,
        label = "${if (kw % 1.0 == 0.0) kw.toInt() else kw} kW",
    )
}

@Composable
private fun BadgeChip(background: Color, content: Color, label: String) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = background,
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = content,
        )
    }
}

@Composable
private fun FieldTerrainRow(
    leadingIcon: @Composable () -> Unit,
    label: String,
    valueText: String,
    trailingSquareIcon: ImageVector,
    onTrailingClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f),
        ) {
            leadingIcon()
            Text(
                text = label,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = valueText,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Surface(
                onClick = onTrailingClick,
                modifier = Modifier.size(28.dp),
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        trailingSquareIcon,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
