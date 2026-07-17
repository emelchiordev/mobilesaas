package re.savio.mobile.ui.screen.intervention

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
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import re.savio.mobile.ui.theme.SavioPalette
import re.savio.mobile.ui.theme.SavioRefonte
import re.savio.mobile.ui.theme.SavioUi
import re.savio.mobile.ui.theme.SavioInterventionTabIndicator
import re.savio.mobile.util.isEcsControlEquipment
import re.savio.mobile.util.isPartOfHybrideAsPac
import re.savio.mobile.util.suggestedAttestationType
import re.savio.mobile.util.toAttestableInput
import re.savio.mobile.ui.component.BrandLogo
import re.savio.mobile.ui.theme.formatEquipmentTypeLabel
import re.savio.mobile.ui.theme.savioTabSelectedColor
import re.savio.mobile.ui.theme.savioTabUnselectedColor
import re.savio.mobile.ui.refonte.SavioEdgeToEdgeScaffoldInsets
import re.savio.mobile.ui.refonte.SavioNavyHeader
import re.savio.mobile.ui.refonte.SavioGhostButton
import re.savio.mobile.ui.refonte.SavioEquipBrandCard
import re.savio.mobile.ui.refonte.SavioRefonteCard
import re.savio.mobile.ui.theme.savioTopAppBarColors
import re.savio.mobile.ui.theme.useSavioRefonteUi
import re.savio.mobile.data.local.entity.EquipmentEntity
import re.savio.mobile.ui.screen.intervention.attestation.AttestationTypePickerSheet
import re.savio.mobile.ui.screen.intervention.attestation.attestationTypeLabel
import re.savio.mobile.ui.screen.intervention.diagnostic.DiagnosticAideSheet
import re.savio.mobile.ui.utils.equipmentIcon
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquipementDetailScreen(
    onBack: () -> Unit,
    onCerfaClick: (interventionId: String, equipmentId: String) -> Unit = { _, _ -> },
    onAttestationVeClick: (
        interventionId: String,
        equipmentOrder: Int,
        type: String,
    ) -> Unit = { _, _, _ -> },
    onMeasureClick: (interventionId: String, equipmentOrder: Int) -> Unit = { _, _ -> },
    onPacMeasureClick: (interventionId: String, equipmentOrder: Int) -> Unit = { _, _ -> },
    onReplaceClick: (interventionId: String, equipmentId: String) -> Unit = { _, _ -> },
    viewModel: EquipementDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val interventionInProgress by viewModel.interventionInProgress.collectAsStateWithLifecycle()
    val catalogEquipment by viewModel.catalogEquipment.collectAsStateWithLifecycle()
    val interventionEquipments by viewModel.interventionEquipments.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showDiagnosticAide by rememberSaveable { mutableStateOf(false) }

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

    val refonte = useSavioRefonteUi()
    val equipmentTitle =
        uiState.equipment?.let {
            listOfNotNull(it.brand, it.model).joinToString(" ")
        } ?: "Équipement"
    Scaffold(
        containerColor =
            if (refonte) MaterialTheme.colorScheme.background else SavioUi.PageBackground,
        contentWindowInsets = if (refonte) SavioEdgeToEdgeScaffoldInsets else androidx.compose.material3.ScaffoldDefaults.contentWindowInsets,
        topBar = {
            if (refonte) {
                SavioNavyHeader(
                    title = equipmentTitle,
                    leading = {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Retour",
                                tint = androidx.compose.ui.graphics.Color.White,
                            )
                        }
                    },
                    actions = {
                        if (uiState.equipment?.typeCode != "replaced") {
                            IconButton(onClick = { showDeleteDialog = true }) {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = "Supprimer l'appareil",
                                    tint = androidx.compose.ui.graphics.Color.White,
                                )
                            }
                        }
                    },
                )
            } else {
                TopAppBar(
                    colors = savioTopAppBarColors(),
                    title = {
                        Text(
                            text = equipmentTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Retour",
                            )
                        }
                    },
                    actions = {
                        if (uiState.equipment?.typeCode != "replaced") {
                            IconButton(onClick = { showDeleteDialog = true }) {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = "Supprimer l'appareil",
                                )
                            }
                        }
                    },
                )
            }
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
                val isReplaced = equipment.typeCode == "replaced"

                if (showDiagnosticAide) {
                    DiagnosticAideSheet(
                        interventionId = viewModel.currentInterventionId,
                        equipment = equipment,
                        onDismiss = { showDiagnosticAide = false },
                    )
                }

                val isBruleur = remember(equipment.typeCode) {
                    equipment.typeCode?.uppercase() == "BRULEUR"
                }
                val changeMode by viewModel.changeMode.collectAsStateWithLifecycle()
                val catalogQuery by viewModel.catalogQuery.collectAsStateWithLifecycle()
                val catalogResults by viewModel.catalogResults.collectAsStateWithLifecycle()
                val isNew by viewModel.isNewEquipment.collectAsStateWithLifecycle()
                val hasCatalogNotice =
                    !equipment.equipmentCatalogId.isNullOrBlank() &&
                        !catalogEquipment?.noticeUrl.isNullOrBlank()

                var showSerialDialog by remember(equipment.id) { mutableStateOf(false) }
                var serialDraft by remember(equipment.id) { mutableStateOf(equipment.serialNumber.orEmpty()) }
                var showEnergySheet by remember(equipment.id) { mutableStateOf(false) }
                var energyDraftId by remember(equipment.id) { mutableStateOf("") }
                val energies by viewModel.energies.collectAsStateWithLifecycle(initialValue = emptyList())
                val pendingCatalogChange by viewModel.pendingCatalogChange.collectAsStateWithLifecycle()
                val pendingCatalogEnergyId by viewModel.pendingCatalogEnergyId.collectAsStateWithLifecycle()
                LaunchedEffect(equipment.serialNumber) {
                    serialDraft = equipment.serialNumber.orEmpty()
                }

                val typeCodeNorm = remember(equipment.typeCode) {
                    (equipment.typeCode ?: "").trim().uppercase()
                }
                val isChaudiereEquipment = typeCodeNorm == "CHAUDIERE"
                val showHybrideStartBadge =
                    isChaudiereEquipment && !equipment.hybridePacEquipmentId.isNullOrBlank()

                val chauffageHybride = remember(interventionEquipments, equipment.id) {
                    interventionEquipments.firstOrNull { eq ->
                        (eq.typeCode ?: "").trim().uppercase() == "CHAUDIERE" &&
                            !eq.hybridePacEquipmentId.isNullOrBlank() &&
                            eq.hybridePacEquipmentId == equipment.id
                    }
                }
                val unitAttestableInputs = remember(interventionEquipments) {
                    interventionEquipments.map { it.toAttestableInput() }
                }
                val equipmentAttestable = remember(equipment) {
                    equipment.toAttestableInput()
                }
                val isPartOfHybrideAsPac =
                    remember(equipmentAttestable, unitAttestableInputs) {
                        isPartOfHybrideAsPac(equipmentAttestable, unitAttestableInputs)
                    }

                val suggestedAttestationType = remember(equipmentAttestable) {
                    suggestedAttestationType(equipmentAttestable)
                }

                var showAttestationPicker by remember(equipment.id) {
                    mutableStateOf(false)
                }

                val isPacOrClim = remember(typeCodeNorm) {
                    typeCodeNorm in setOf("PAC", "PAC A/E", "PAC A/A", "CLIMATISEUR")
                }

                val attestationNavOnPacCard =
                    refonte &&
                    isPacOrClim &&
                    !isReplaced &&
                    !isBruleur &&
                    !isPartOfHybrideAsPac

                val attestationNavSubtitle =
                    if (suggestedAttestationType != null) {
                        "Suggestion : ${attestationTypeLabel(suggestedAttestationType)}"
                    } else {
                        "Choisir le type d'attestation"
                    }

                val showMeasures = remember(equipment.typeCode) {
                    listOf(
                        "CHAUDIERE",
                        "POELE",
                        "INSERT",
                        "CHAUDIERE_BOIS",
                    ).contains(
                        equipment.typeCode
                            ?.trim()
                            ?.uppercase(),
                    )
                }

                val showEcsControl = remember(equipmentAttestable) {
                    isEcsControlEquipment(equipmentAttestable)
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

                if (showEnergySheet && !isBruleur) {
                    EquipmentEnergyPickerSheet(
                        title = "Énergie raccordée",
                        modelLabel = equipment.model?.takeIf { it.isNotBlank() } ?: "—",
                        subtitle =
                            listOfNotNull(
                                equipment.brand?.takeIf { it.isNotBlank() },
                                equipment.typeCode?.takeIf { it.isNotBlank() },
                            ).joinToString(" · ").ifBlank { null },
                        catalogEnergyHint =
                            catalogEquipment?.let { cat ->
                                energies.find { it.id == cat.energyId }?.label
                            },
                        energies = energies,
                        selectedEnergyId = energyDraftId,
                        onEnergySelected = { energyDraftId = it },
                        onConfirm = {
                            if (energyDraftId.isNotBlank()) {
                                viewModel.saveEnergy(energyDraftId)
                            }
                            showEnergySheet = false
                        },
                        onDismiss = { showEnergySheet = false },
                    )
                }

                pendingCatalogChange?.let { pendingRow ->
                    val pendingEnergyId = pendingCatalogEnergyId ?: pendingRow.equipment.energyId
                    EquipmentEnergyPickerSheet(
                        title = "Énergie raccordée",
                        modelLabel = pendingRow.equipment.model,
                        subtitle =
                            listOfNotNull(
                                pendingRow.brandLabel,
                                pendingRow.typeLabel,
                            ).joinToString(" · ").ifBlank { null },
                        catalogEnergyHint = pendingRow.energyLabel,
                        energies = energies,
                        selectedEnergyId = pendingEnergyId,
                        onEnergySelected = viewModel::updatePendingCatalogEnergyId,
                        onConfirm = viewModel::confirmPendingCatalogChange,
                        onDismiss = viewModel::dismissPendingCatalogChange,
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(if (refonte) 15.dp else 16.dp),
                    verticalArrangement = Arrangement.spacedBy(if (refonte) 12.dp else 14.dp),
                ) {
                    // SECTION 1 — Identité
                    if (refonte) {
                        val equipTags =
                            buildList {
                                equipment.typeCode?.let { code ->
                                    add(formatEquipmentTypeLabel(code).ifBlank { code.uppercase() })
                                }
                                if (!isBruleur) {
                                    equipment.energyCode
                                        ?.takeIf {
                                            it.isNotBlank() &&
                                                it.uppercase(Locale.getDefault()) != "INCONNU"
                                        }
                                        ?.let { add(it.uppercase(Locale.getDefault())) }
                                }
                                catalogEquipment?.powerKw?.let { kw ->
                                    add(
                                        "${if (kw % 1.0 == 0.0) kw.toInt() else kw} kW",
                                    )
                                }
                                    ?: equipment.powerKw
                                        ?.replace(',', '.')
                                        ?.toDoubleOrNull()
                                        ?.let { kw ->
                                            add(
                                                "${if (kw % 1.0 == 0.0) kw.toInt() else kw} kW",
                                            )
                                        }
                            }
                        SavioEquipBrandCard(
                            eyebrow = "Marque · Modèle",
                            title =
                                listOfNotNull(equipment.brand, equipment.model)
                                    .joinToString(" · ")
                                    .ifBlank { "—" },
                            tags = equipTags,
                            catalogBrandId = equipment.catalogBrandId,
                            typeCode = equipment.typeCode,
                        )
                    } else {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(0.5.dp, SavioUi.CardBorder),
                        shadowElevation = 0.dp,
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Surface(
                                    modifier = Modifier.size(40.dp),
                                    shape = CircleShape,
                                    color = SavioUi.ChipBackground,
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        BrandLogo(
                                            brandId = equipment.catalogBrandId,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(4.dp),
                                            fallback = {
                                                Icon(
                                                    equipmentIcon(equipment.typeCode),
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .padding(8.dp),
                                                )
                                            },
                                        )
                                    }
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Marque · Modèle",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Text(
                                        text = listOfNotNull(equipment.brand, equipment.model)
                                            .joinToString(" · ")
                                            .ifBlank { "—" },
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                    )
                                    equipment.serialNumber?.takeIf { it.isNotBlank() }?.let { sn ->
                                        Text(
                                            text = "S/N $sn",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                            }
                            HorizontalDivider(
                                thickness = 0.5.dp,
                                color = SavioUi.CardBorder,
                            )
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
                                if (!isBruleur) {
                                    equipment.energyCode
                                        ?.takeIf { it.isNotBlank() && it.uppercase(Locale.getDefault()) != "INCONNU" }
                                        ?.let { code ->
                                            TypeEnergyBadge(text = code, isType = false)
                                        }
                                }
                                catalogEquipment?.powerKw?.let { kw ->
                                    KwBadge(kw = kw)
                                } ?: equipment.powerKw
                                    ?.replace(',', '.')
                                    ?.toDoubleOrNull()
                                    ?.let { kw -> KwBadge(kw = kw) }
                            }
                            if (!isReplaced && !isBruleur && showHybrideStartBadge) {
                                Surface(
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp),
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Bolt,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                            modifier = Modifier.size(16.dp),
                                        )
                                        Text(
                                            text = "Point de départ de l'attestation hybride",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        )
                                    }
                                }
                            }
                        }
                    }
                    }

                    if (!refonte && hasCatalogNotice) {
                        Surface(
                            onClick = {
                                viewModel.openNotice(
                                    checkNotNull(equipment.equipmentCatalogId),
                                    context,
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(0.5.dp, SavioUi.CardBorder),
                            shadowElevation = 0.dp,
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Icon(
                                    Icons.Filled.PictureAsPdf,
                                    contentDescription = null,
                                    tint = SavioUi.BusinessAccent,
                                    modifier = Modifier.size(24.dp),
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "Notice constructeur",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                    )
                                    Text(
                                        "Consulter le PDF",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                Icon(
                                    Icons.Filled.ChevronRight,
                                    contentDescription = null,
                                    tint = SavioUi.BusinessAccent,
                                )
                            }
                        }
                    }

                    if (!refonte && interventionInProgress && !isReplaced) {
                        SavioGhostButton(
                            text = "Aide au diagnostic (Beta)",
                            icon = Icons.Outlined.Search,
                            onClick = { showDiagnosticAide = true },
                        )
                    }

                    val energyUc = (equipment.energyCode ?: "").uppercase(Locale.getDefault())
                    val typeUc = (equipment.typeCode ?: "").uppercase(Locale.getDefault())
                    val hasBruleurChild = interventionEquipments.any {
                        (it.parentEquipmentId ?: "") == equipment.id &&
                            (it.typeCode ?: "").uppercase(Locale.getDefault()) == "BRULEUR"
                    }
                    val showPompeTabA = typeUc == "CHAUDIERE" &&
                        (energyUc.contains("FIOUL") || energyUc.contains("FUEL") ||
                            (energyUc.contains("GAZ") && hasBruleurChild))
                    val showGicleurTabA =
                        typeUc == "CHAUDIERE" && (energyUc.contains("FIOUL") || energyUc.contains("FUEL"))
                    val tabLabelsCh = remember(showPompeTabA, showGicleurTabA) {
                        buildList {
                            add("Appareil")
                            if (showPompeTabA) add("Pompe")
                            if (showGicleurTabA) add("Gicleur")
                        }
                    }
                    var chTabIdx by remember(equipment.id) { mutableIntStateOf(0) }
                    if (tabLabelsCh.size > 1 && !isReplaced) {
                        TabRow(
                            selectedTabIndex = chTabIdx,
                            containerColor = MaterialTheme.colorScheme.background,
                            contentColor = savioTabUnselectedColor(),
                            indicator = { positions -> SavioInterventionTabIndicator(positions, chTabIdx) },
                        ) {
                            tabLabelsCh.forEachIndexed { i, title ->
                                Tab(
                                    selected = chTabIdx == i,
                                    onClick = { chTabIdx = i },
                                    selectedContentColor = savioTabSelectedColor(),
                                    unselectedContentColor = savioTabUnselectedColor(),
                                    text = { Text(title, style = MaterialTheme.typography.labelMedium) },
                                )
                            }
                        }
                    }
                    val chTabLabel = tabLabelsCh.getOrNull(chTabIdx) ?: "Appareil"

                    when (chTabLabel) {
                        "Pompe" -> if (!isReplaced && showPompeTabA) {
                            PompeFioulInstallTab(viewModel = viewModel, equipment = equipment)
                        }

                        "Gicleur" -> if (!isReplaced && showGicleurTabA) {
                            GicleurFioulInstallTab(viewModel = viewModel, equipment = equipment)
                        }

                        else -> {
                    if (isReplaced) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFFECEC),
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Icon(
                                    Icons.Filled.SwapHoriz,
                                    contentDescription = null,
                                    tint = Color(0xFFA32D2D),
                                    modifier = Modifier.size(16.dp),
                                )
                                Text(
                                    "Appareil remplacé — consultation uniquement",
                                    fontSize = 12.sp,
                                    color = Color(0xFFA32D2D),
                                    fontWeight = FontWeight.Medium,
                                )
                            }
                        }
                    }

                    // SECTION 2 — Données terrain
                    if (refonte) {
                        val serialTodo = equipment.serialNumber.isNullOrBlank()
                        val installTodo = equipment.installDate.isNullOrBlank()
                        val energyTodo = isEnergyTodo(equipment.energyCode)
                        val energyDisplayValue = resolveEnergyDisplayLabel(equipment.energyCode, energies)
                        val hybrideChaudiereLabel =
                            remember(chauffageHybride) {
                                chauffageHybride?.let { ch ->
                                    listOfNotNull(
                                        ch.brand?.takeIf { it.isNotBlank() },
                                        ch.model?.takeIf { it.isNotBlank() },
                                    ).joinToString(" ").takeIf { it.isNotBlank() }
                                }
                            }
                        EquipementDetailRefonteContent(
                            isReplaced = isReplaced,
                            isBruleur = isBruleur,
                            isNew = isNew,
                            hasCatalogNotice = hasCatalogNotice,
                            interventionInProgress = interventionInProgress,
                            showMeasures = showMeasures,
                            showEcsControl = showEcsControl,
                            isPacOrClim = isPacOrClim,
                            attestationNavOnPacCard = attestationNavOnPacCard,
                            attestationNavSubtitle = attestationNavSubtitle,
                            isPartOfHybrideAsPac = isPartOfHybrideAsPac,
                            hybrideChaudiereLabel = hybrideChaudiereLabel,
                            serialDisplayValue =
                                equipment.serialNumber?.takeIf { it.isNotBlank() }
                                    ?: "À renseigner",
                            installDisplayValue =
                                if (installTodo) {
                                    "À renseigner"
                                } else {
                                    formatCommissioningDate(equipment.installDate)
                                },
                            serialTodo = serialTodo,
                            installTodo = installTodo,
                            energyDisplayValue = energyDisplayValue,
                            energyTodo = energyTodo,
                            onSerialEditClick = {
                                serialDraft = equipment.serialNumber.orEmpty()
                                showSerialDialog = true
                            },
                            onInstallDateEditClick = {
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
                                        val iso =
                                            String.format(
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
                            onEnergyEditClick = {
                                energyDraftId = resolveEnergyIdForCode(equipment.energyCode, energies)
                                showEnergySheet = true
                            },
                            onNoticeClick = {
                                viewModel.openNotice(
                                    checkNotNull(equipment.equipmentCatalogId),
                                    context,
                                )
                            },
                            onDiagnosticClick = { showDiagnosticAide = true },
                            onMeasureClick = {
                                onMeasureClick(
                                    viewModel.currentInterventionId,
                                    equipment.order ?: 0,
                                )
                            },
                            onEcsControlClick = {
                                onAttestationVeClick(
                                    viewModel.currentInterventionId,
                                    equipment.order ?: 0,
                                    "ECS",
                                )
                            },
                            onPacMeasureClick = {
                                onPacMeasureClick(
                                    viewModel.currentInterventionId,
                                    equipment.order ?: 0,
                                )
                            },
                            onCerfaClick = {
                                onCerfaClick(
                                    viewModel.currentInterventionId,
                                    equipment.id,
                                )
                            },
                            onAttestationClick = { showAttestationPicker = true },
                            onCorrect = { viewModel.setChangeMode(true) },
                            onReplace =
                                if (!isNew) {
                                    {
                                        onReplaceClick(
                                            viewModel.currentInterventionId,
                                            equipment.id,
                                        )
                                    }
                                } else {
                                    null
                                },
                        )
                    } else {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(0.5.dp, SavioUi.CardBorder),
                        shadowElevation = 0.dp,
                    ) {
                        Column {
                            FieldTerrainRow(
                                leadingIcon = {
                                    Icon(
                                        Icons.Filled.Edit,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = SavioUi.BusinessAccent,
                                    )
                                },
                                label = "N° série",
                                valueText = equipment.serialNumber?.takeIf { it.isNotBlank() } ?: "—",
                                trailingSquareIcon = Icons.Filled.Edit,
                                onTrailingClick =
                                    if (!isReplaced) {
                                        {
                                            serialDraft = equipment.serialNumber.orEmpty()
                                            showSerialDialog = true
                                        }
                                    } else {
                                        null
                                    },
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                thickness = 0.5.dp,
                                color = SavioUi.CardBorder,
                            )
                            FieldTerrainRow(
                                leadingIcon = {
                                    Icon(
                                        Icons.Filled.CalendarToday,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = SavioUi.BusinessAccent,
                                    )
                                },
                                label = "Mise en service",
                                valueText = formatCommissioningDate(equipment.installDate),
                                trailingSquareIcon = Icons.Filled.CalendarToday,
                                onTrailingClick =
                                    if (!isReplaced) {
                                        {
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
                                        }
                                    } else {
                                        null
                                    },
                            )
                            if (!isBruleur) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    thickness = 0.5.dp,
                                    color = SavioUi.CardBorder,
                                )
                                FieldTerrainRow(
                                    leadingIcon = {
                                        Icon(
                                            Icons.Filled.Bolt,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = SavioUi.BusinessAccent,
                                        )
                                    },
                                    label = "Énergie raccordée",
                                    valueText = resolveEnergyDisplayLabel(equipment.energyCode, energies),
                                    trailingSquareIcon = Icons.Filled.Bolt,
                                    onTrailingClick =
                                        if (!isReplaced) {
                                            {
                                                energyDraftId =
                                                    resolveEnergyIdForCode(equipment.energyCode, energies)
                                                showEnergySheet = true
                                            }
                                        } else {
                                            null
                                        },
                                )
                            }
                        }
                    }
                    }

                    if (!refonte && showMeasures && !isReplaced) {
                        Surface(
                            onClick = {
                                onMeasureClick(
                                    viewModel.currentInterventionId,
                                    equipment.order ?: 0,
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(0.5.dp, SavioUi.CardBorder),
                            shadowElevation = 0.dp,
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Icon(
                                    Icons.Filled.Analytics,
                                    contentDescription = null,
                                    tint = SavioUi.BusinessAccent,
                                    modifier = Modifier.size(24.dp),
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "Mesures",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                    )
                                    Text(
                                        "Saisir les mesures de combustion",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                Icon(
                                    Icons.Filled.ChevronRight,
                                    contentDescription = null,
                                    tint = SavioUi.BusinessAccent,
                                )
                            }
                        }
                    } else if (!refonte && showEcsControl && !isReplaced) {
                        Surface(
                            onClick = {
                                onAttestationVeClick(
                                    viewModel.currentInterventionId,
                                    equipment.order ?: 0,
                                    "ECS",
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(0.5.dp, SavioUi.CardBorder),
                            shadowElevation = 0.dp,
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Icon(
                                    Icons.Filled.WaterDrop,
                                    contentDescription = null,
                                    tint = SavioUi.BusinessAccent,
                                    modifier = Modifier.size(24.dp),
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "Contrôle ECS",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                    )
                                    Text(
                                        "Contrôle chauffe-eau / ballon ECS",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                Icon(
                                    Icons.Filled.ChevronRight,
                                    contentDescription = null,
                                    tint = SavioUi.BusinessAccent,
                                )
                            }
                        }
                    }

                    if (!refonte && isPacOrClim && !isReplaced) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Surface(
                                onClick = {
                                    onPacMeasureClick(
                                        viewModel.currentInterventionId,
                                        equipment.order ?: 0,
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(0.5.dp, SavioUi.CardBorder),
                                shadowElevation = 0.dp,
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
                                            color = SavioUi.ChipBackground,
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    Icons.Filled.AcUnit,
                                                    contentDescription = null,
                                                    tint = SavioUi.BusinessAccent,
                                                    modifier = Modifier.size(20.dp),
                                                )
                                            }
                                        }
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            "Mesures PAC",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                        )
                                        Text(
                                            "Saisie terrain",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                    Icon(
                                        Icons.Filled.ChevronRight,
                                        contentDescription = null,
                                        tint = SavioUi.BusinessAccent,
                                    )
                                }
                            }
                            Surface(
                                onClick = {
                                    onCerfaClick(
                                        viewModel.currentInterventionId,
                                        equipment.id,
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(0.5.dp, SavioUi.CardBorder),
                                shadowElevation = 0.dp,
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
                                            color = SavioUi.ChipBackground,
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    Icons.Filled.Description,
                                                    contentDescription = null,
                                                    tint = SavioUi.BusinessAccent,
                                                    modifier = Modifier.size(20.dp),
                                                )
                                            }
                                        }
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            "CERFA fluides frigorigènes",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                        )
                                        Text(
                                            "Remplir ou consulter",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                    Icon(
                                        Icons.Filled.ChevronRight,
                                        contentDescription = null,
                                        tint = SavioUi.BusinessAccent,
                                    )
                                }
                            }
                        }
                    }

                    // Attestation VE — après Mesures ; pas sur le brûleur (rattaché à la chaudière)
                    if (!refonte && !isReplaced && !isBruleur && !showEcsControl) {
                        if (isPartOfHybrideAsPac) {
                            val ch = checkNotNull(chauffageHybride)
                            val chaudiereLabel =
                                listOfNotNull(
                                    ch.brand?.takeIf { it.isNotBlank() },
                                    ch.model?.takeIf { it.isNotBlank() },
                                ).joinToString(" ")
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Info,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp),
                                    )
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(4.dp),
                                    ) {
                                        Text(
                                            text = "Système PAC Hybride",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                        Text(
                                            text = "L'attestation se démarre depuis la chaudière $chaudiereLabel.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                            }
                        } else {
                            Surface(
                                onClick = { showAttestationPicker = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(0.5.dp, SavioUi.CardBorder),
                                shadowElevation = 0.dp,
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    Icon(
                                        Icons.Filled.Assignment,
                                        contentDescription = null,
                                        tint = SavioUi.BusinessAccent,
                                        modifier = Modifier.size(24.dp),
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            "Attestation d'entretien",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                        )
                                        Text(
                                            text = if (suggestedAttestationType != null) {
                                                "Suggestion : ${attestationTypeLabel(suggestedAttestationType)}"
                                            } else {
                                                "Choisir le type d'attestation"
                                            },
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                    Icon(
                                        Icons.Filled.ChevronRight,
                                        contentDescription = null,
                                        tint = SavioUi.BusinessAccent,
                                    )
                                }
                            }
                        }
                    }

                    // Corriger / Remplacer — bas de fiche (après Mesures + Attestation)
                    if (!refonte && !isReplaced) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.setChangeMode(true) },
                                modifier = Modifier.weight(1f),
                                border = BorderStroke(0.5.dp, SavioUi.BusinessAccent),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.onSurface,
                                    containerColor = Color.Transparent,
                                ),
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.SwapVert,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp),
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Corriger")
                            }
                            if (!isNew) {
                                OutlinedButton(
                                    onClick = {
                                        onReplaceClick(
                                            viewModel.currentInterventionId,
                                            equipment.id,
                                        )
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = SavioUi.DestructiveRed,
                                        containerColor = Color.Transparent,
                                    ),
                                    border = BorderStroke(
                                        0.5.dp,
                                        SavioUi.DestructiveRed,
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
                        }
                        if (isNew) {
                            Text(
                                text =
                                    "Appareil ajouté pendant cette intervention — " +
                                        "supprimez-le si vous voulez l'annuler",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        }
                    }

                    // Catalogue (change mode)
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

                    if (showAttestationPicker && !isBruleur) {
                        AttestationTypePickerSheet(
                            suggestedType = suggestedAttestationType,
                            onSelect = { type ->
                                showAttestationPicker = false
                                onAttestationVeClick(
                                    viewModel.currentInterventionId,
                                    equipment.order ?: 0,
                                    type,
                                )
                            },
                            onDismiss = { showAttestationPicker = false },
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PompeFioulInstallTab(
    viewModel: EquipementDetailViewModel,
    equipment: EquipmentEntity,
) {
    var marque by remember(equipment.id, equipment.attrsJson) { mutableStateOf("") }
    var modele by remember(equipment.id, equipment.attrsJson) { mutableStateOf("") }
    var pression by remember(equipment.id, equipment.attrsJson) { mutableStateOf("") }
    LaunchedEffect(equipment.attrsJson, equipment.id) {
        val o = try {
            JsonParser.parseString(equipment.attrsJson ?: "{}").asJsonObject
        } catch (_: Exception) {
            JsonObject()
        }
        val p = o.getAsJsonObject("pompe")
        marque = p?.get("marque")?.asString.orEmpty()
        modele = p?.get("modele")?.asString.orEmpty()
        pression = p?.get("pression")?.asString.orEmpty()
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(0.5.dp, SavioUi.CardBorder),
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Pompe", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedTextField(
                value = marque,
                onValueChange = { marque = it },
                label = { Text("Marque") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            OutlinedTextField(
                value = modele,
                onValueChange = { modele = it },
                label = { Text("Modèle") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            OutlinedTextField(
                value = pression,
                onValueChange = { pression = it },
                label = { Text("Pression (bar)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            TextButton(
                onClick = {
                    viewModel.saveInstallationAttrs(
                        mapOf("marque" to marque, "modele" to modele, "pression" to pression),
                        emptyMap(),
                    )
                },
            ) {
                Text("Enregistrer")
            }
        }
    }
}

@Composable
private fun GicleurFioulInstallTab(
    viewModel: EquipementDetailViewModel,
    equipment: EquipmentEntity,
) {
    var marque by remember(equipment.id, equipment.attrsJson) { mutableStateOf("") }
    var debitUs by remember(equipment.id, equipment.attrsJson) { mutableStateOf("") }
    var debitEu by remember(equipment.id, equipment.attrsJson) { mutableStateOf("") }
    var angle by remember(equipment.id, equipment.attrsJson) { mutableStateOf("") }
    var type by remember(equipment.id, equipment.attrsJson) { mutableStateOf("") }
    var norme by remember(equipment.id, equipment.attrsJson) { mutableStateOf("EU") }
    var boite by remember(equipment.id, equipment.attrsJson) { mutableStateOf("") }
    LaunchedEffect(equipment.attrsJson, equipment.id) {
        val o = try {
            JsonParser.parseString(equipment.attrsJson ?: "{}").asJsonObject
        } catch (_: Exception) {
            JsonObject()
        }
        val g = o.getAsJsonObject("gicleur")
        marque = g?.get("marque")?.asString.orEmpty()
        debitUs = g?.get("debit_us")?.asString.orEmpty()
        debitEu = g?.get("debit_eu")?.asString.orEmpty()
        angle = g?.get("angle")?.asString.orEmpty()
        type = g?.get("type")?.asString.orEmpty()
        val normeRaw = g?.get("norme")?.asString?.trim().orEmpty()
        norme = if (normeRaw.isBlank()) "EU" else normeRaw
        boite = g?.get("boite_controle")?.asString.orEmpty()
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(0.5.dp, SavioUi.CardBorder),
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Gicleur fioul", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedTextField(
                value = marque,
                onValueChange = { marque = it },
                label = { Text("Marque") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            OutlinedTextField(
                value = norme,
                onValueChange = { norme = it },
                label = { Text("Norme (EU / US)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            if (norme.uppercase(Locale.getDefault()).contains("US")) {
                OutlinedTextField(
                    value = debitUs,
                    onValueChange = { debitUs = it },
                    label = { Text("Débit (GPH)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            } else {
                OutlinedTextField(
                    value = debitEu,
                    onValueChange = { debitEu = it },
                    label = { Text("Débit (kg/h)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }
            OutlinedTextField(
                value = angle,
                onValueChange = { angle = it },
                label = { Text("Angle") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            OutlinedTextField(
                value = type,
                onValueChange = { type = it },
                label = { Text("Type (S / H / B)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            OutlinedTextField(
                value = boite,
                onValueChange = { boite = it },
                label = { Text("Boîte de contrôle") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            TextButton(
                onClick = {
                    viewModel.saveInstallationAttrs(
                        emptyMap(),
                        mapOf(
                            "marque" to marque,
                            "debit_us" to debitUs,
                            "debit_eu" to debitEu,
                            "angle" to angle,
                            "type" to type,
                            "norme" to norme,
                            "boite_controle" to boite,
                        ),
                    )
                },
            ) {
                Text("Enregistrer")
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

@Suppress("UNUSED_PARAMETER")
@Composable
private fun TypeEnergyBadge(text: String, isType: Boolean = false) {
    val label =
        if (isType) {
            formatEquipmentTypeLabel(text).ifBlank { text.uppercase(Locale.FRANCE) }
        } else {
            text.uppercase(Locale.FRANCE)
        }
    BadgeChip(
        background = SavioUi.ChipBackground,
        content = SavioUi.BusinessAccent,
        label = label,
    )
}

@Composable
private fun KwBadge(kw: Double) {
    BadgeChip(
        background = SavioUi.ChipBackground,
        content = SavioUi.BusinessAccent,
        label = "${if (kw % 1.0 == 0.0) kw.toInt() else kw} kW",
    )
}

@Composable
private fun BadgeChip(background: Color, content: Color, label: String) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = background,
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
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
    onTrailingClick: (() -> Unit)? = null,
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
                fontSize = 12.sp,
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
                fontWeight = FontWeight.Medium,
            )
            if (onTrailingClick != null) {
                Surface(
                    onClick = onTrailingClick,
                    modifier = Modifier.size(28.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = SavioUi.ChipBackground,
                    border = BorderStroke(0.5.dp, SavioUi.CardBorder),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            trailingSquareIcon,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = SavioUi.BusinessAccent,
                        )
                    }
                }
            }
        }
    }
}
