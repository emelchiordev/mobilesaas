package re.savio.mobile.ui.screen.intervention.cerfa

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import re.savio.mobile.data.local.entity.ColdMeasureEntity
import re.savio.mobile.data.local.objectbox.MeasurementDeviceBox
import re.savio.mobile.data.local.objectbox.RefrigerantContainerBox
import re.savio.mobile.data.local.objectbox.RefrigerantWastePartnerBox
import re.savio.mobile.data.repository.RefrigerantWastePartnerSyncRepository
import re.savio.mobile.ui.theme.SavioRefonte

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CerfaFroidScreen(
    onBack: () -> Unit,
    windowSizeClass: androidx.compose.material3.windowsizeclass.WindowSizeClass,
    onApercuPdf: () -> Unit = {},
    viewModel: CerfaFroidViewModel = hiltViewModel(),
) {
    @Suppress("UNUSED_VARIABLE")
    val unusedWidth = windowSizeClass.widthSizeClass
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()
    val hasPersistedCerfa by viewModel.hasPersistedData.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val pagerState =
        rememberPagerState(
            initialPage = 0,
            pageCount = { CERFA_FROID_TABS.size },
        )
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabStates =
        remember(state) {
            CERFA_FROID_TABS.indices.map { cerfaTabFillState(it, state) }
        }

    fun leave() {
        viewModel.saveIfChanged()
        onBack()
    }

    fun goToTab(index: Int) {
        val target = index.coerceIn(0, CERFA_FROID_TABS.lastIndex)
        selectedTab = target
        scope.launch { pagerState.animateScrollToPage(target) }
    }

    BackHandler { leave() }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }
            .distinctUntilChanged()
            .collect { page -> selectedTab = page }
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(CerfaPageBg),
    ) {
        CerfaFroidHeader(
            onBack = ::leave,
            onApercuPdf = onApercuPdf,
            apercuEnabled = hasPersistedCerfa,
        )
        CerfaFroidProgress(currentIndex = selectedTab, tabStates = tabStates)
        CerfaFroidTabRow(
            selectedIndex = selectedTab,
            tabStates = tabStates,
            onSelect = ::goToTab,
        )
        HorizontalPager(
            state = pagerState,
            modifier =
                Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            beyondViewportPageCount = 1,
        ) { page ->
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 14.dp),
            ) {
                when (page) {
                    0 -> CerfaTabEquipement(state, viewModel::update)
                    1 -> CerfaTabNature(state, viewModel::update)
                    2 -> CerfaTabObservations(state, viewModel)
                    3 -> CerfaTabFuites(state, viewModel)
                    4 -> CerfaTabFluides(state, viewModel)
                    5 -> CerfaTabTransport(state, viewModel)
                    6 -> CerfaTabObsFluides(state, viewModel::update)
                    7 -> CerfaTabBordereau(state, viewModel::update)
                }
            }
        }
        CerfaBottomNav(
            onPrevious = if (selectedTab > 0) ({ goToTab(selectedTab - 1) }) else null,
            nextLabel =
                when {
                    selectedTab >= CERFA_FROID_TABS.lastIndex ->
                        if (isSaving) "Enregistrement…" else "Enregistrer"
                    else -> "Onglet suivant"
                },
            onNext = {
                if (selectedTab >= CERFA_FROID_TABS.lastIndex) {
                    viewModel.saveLocally()
                } else {
                    goToTab(selectedTab + 1)
                }
            },
        )
    }
}

@Composable
private fun CerfaTabEquipement(
    state: ColdMeasureEntity,
    onUpdate: (String, String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        CerfaSectionCard {
            CerfaCardHead(
                icon = Icons.Filled.Build,
                title = "Équipement",
                subtitle = "Fluide et charge nominale",
            )
            Column(
                modifier = Modifier.padding(start = 15.dp, end = 15.dp, bottom = 15.dp),
                verticalArrangement = Arrangement.spacedBy(11.dp),
            ) {
                FrigoDropdown(state.frigo, onUpdate)
                CerfaInputField(
                    label = "Charge totale (kg)",
                    value = state.charg,
                    onValueChange = { onUpdate("CHARG", it) },
                    keyboardType = KeyboardType.Decimal,
                )
                if (FluideGwp.hasKnownGwp(state.frigo)) {
                    CerfaAutoField(
                        label = "Tonnage CO₂e (auto)",
                        value = state.tonnage,
                    )
                    val gwp = FluideGwp.gwpFor(state.frigo)
                    if (gwp != null && state.charg.isNotBlank()) {
                        Text(
                            text = "Calcul : charge × GWP $gwp / 1000",
                            fontSize = 12.sp,
                            color = Color(0xFF8893A2),
                        )
                    }
                } else {
                    CerfaInputField(
                        label = "Tonnage CO₂e",
                        value = state.tonnage,
                        onValueChange = { onUpdate("TONNAGE", it) },
                        keyboardType = KeyboardType.Decimal,
                    )
                    if (state.frigo.isNotBlank()) {
                        Text(
                            text = "GWP inconnu pour ce fluide — saisie manuelle",
                            fontSize = 12.sp,
                            color = Color(0xFF8893A2),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CerfaTabNature(
    state: ColdMeasureEntity,
    onUpdate: (String, String) -> Unit,
) {
    fun bind(key: String, value: String): Pair<Boolean, (Boolean) -> Unit> =
        (value == "O") to { on -> onUpdate(key, if (on) "O" else "N") }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(
            text = "Sélectionnez la ou les natures de l'intervention.",
            fontSize = 13.sp,
            color = Color(0xFF5A6573),
            modifier = Modifier.padding(horizontal = 2.dp),
        )
        CerfaSectionCard {
            CerfaGroupLabel("Intervention courante")
            val (m1, s1) = bind("MINTER1", state.minter1)
            CerfaToggleRow("Mise en service", m1, s1, showDivider = false)
            val (m2, s2) = bind("MINTER2", state.minter2)
            CerfaToggleRow("Maintenance", m2, s2)
            val (m3, s3) = bind("MINTER3", state.minter3)
            CerfaToggleRow("Contrôle d'étanchéité périodique", m3, s3)
            val (m8, s8) = bind("MINTER8", state.minter8)
            CerfaToggleRow("Contrôle d'étanchéité non périodique", m8, s8)
            Spacer(modifier = Modifier.height(6.dp))
        }
        CerfaSectionCard {
            CerfaGroupLabel("Cycle de vie de l'équipement")
            val (m6, s6) = bind("MINTER6", state.minter6)
            CerfaToggleRow("Assemblage", m6, s6, showDivider = false)
            val (m4, s4) = bind("MINTER4", state.minter4)
            CerfaToggleRow("Modification", m4, s4)
            val (m5, s5) = bind("MINTER5", state.minter5)
            CerfaToggleRow("Démantèlement", m5, s5)
            val (m7, s7) = bind("MINTER7", state.minter7)
            CerfaToggleRow("Autre", m7, s7)
            if (state.minter7 == "O") {
                Column(modifier = Modifier.padding(horizontal = 15.dp, vertical = 8.dp)) {
                    CerfaInputField(
                        label = "Précision (autre)",
                        value = state.minteraut,
                        onValueChange = { onUpdate("MINTERAUT", it) },
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
        }
    }
}

@Composable
private fun CerfaTabObservations(
    state: ColdMeasureEntity,
    viewModel: CerfaFroidViewModel,
) {
    val onUpdate = viewModel::update
    val devices by viewModel.measurementDevices.collectAsStateWithLifecycle()
    val canCreate by viewModel.canCreateMeasurementDevice.collectAsStateWithLifecycle()
    val deviceError by viewModel.measurementDeviceCreateError.collectAsStateWithLifecycle()
    var showCreateDevice by remember { mutableStateOf(false) }

    if (deviceError != null) {
        AlertDialog(
            onDismissRequest = viewModel::clearMeasurementDeviceCreateError,
            title = { Text("Appareil de mesure") },
            text = { Text(deviceError!!) },
            confirmButton = {
                TextButton(onClick = viewModel::clearMeasurementDeviceCreateError) { Text("OK") }
            },
        )
    }
    if (showCreateDevice) {
        CreateMeasurementDeviceDialog(
            onDismiss = { showCreateDevice = false },
            onConfirm = { brand, model, serial, controlDate ->
                viewModel.createMeasurementDeviceOnTheFly(
                    brand = brand,
                    model = model,
                    serialNumber = serial,
                    lastControlDate = controlDate,
                ) {
                    showCreateDevice = false
                }
            },
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        CerfaSectionCard {
            CerfaCardHead(icon = Icons.Filled.Info, title = "Observations", subtitle = "Cases 5–6")
            Column(
                modifier = Modifier.padding(start = 15.dp, end = 15.dp, bottom = 15.dp),
                verticalArrangement = Arrangement.spacedBy(11.dp),
            ) {
                CerfaInputField("Observation 1", state.obsern1, { onUpdate("OBSERN1", it) }, singleLine = false, minLines = 3)
                CerfaInputField("Observation 2", state.obsern2, { onUpdate("OBSERN2", it) }, singleLine = false, minLines = 3)
            }
        }
        CerfaSectionCard {
            CerfaCardHead(icon = Icons.Filled.Search, title = "Détecteur", subtitle = "Marque, modèle, date")
            Column(
                modifier = Modifier.padding(start = 15.dp, end = 15.dp, bottom = 15.dp),
                verticalArrangement = Arrangement.spacedBy(11.dp),
            ) {
                MeasurementDevicePickerField(
                    devices = devices,
                    selectedBrand = state.detm1,
                    selectedModel = state.dett1,
                    labelFor = viewModel::measurementDeviceLabel,
                    onSelect = viewModel::selectMeasurementDevice,
                    canCreate = canCreate,
                    onCreate = { showCreateDevice = true },
                    onRefresh = viewModel::refreshMeasurementDevices,
                )
                CerfaInputField("Marque détecteur", state.detm1, { onUpdate("DETM1", it) })
                CerfaInputField("Modèle détecteur", state.dett1, { onUpdate("DETT1", it) })
                CerfaInputField("Date contrôle", state.detd1, { onUpdate("DETD1", it) })
            }
        }
    }
}

@Composable
private fun CerfaTabFuites(
    state: ColdMeasureEntity,
    viewModel: CerfaFroidViewModel,
) {
    val onUpdate = viewModel::update
    val leaksOn = cerfaLeaksEnabled(state.pasfuite)
    var leakBlocks by remember { mutableIntStateOf(initialLeakBlockCount(state)) }
    LaunchedEffect(state.id, state.fuiteloc2, state.fuiterep2, state.fuiteloc3, state.fuiterep3) {
        val fromData = initialLeakBlockCount(state)
        if (fromData > leakBlocks) leakBlocks = fromData
    }
    var autoMode by remember {
        mutableStateOf(cerfaFrequencyMode(state) == CerfaFrequencyMode.Auto)
    }
    LaunchedEffect(state.freqa1, state.freqa2, state.freqa3, state.freqs1, state.freqs2, state.freqs3) {
        autoMode = cerfaFrequencyMode(state) == CerfaFrequencyMode.Auto
    }
    val selectedMonths = selectedFrequencyMonths(state)
    val recommended = recommendedFrequencyMonths(state.tonnage, autoMode)
    val options = frequencyOptions(autoMode)

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        CerfaSectionCard {
            CerfaCardHead(
                icon = Icons.Filled.WaterDrop,
                title = "Fuites constatées",
                subtitle = "Fuite détectée sur l'équipement ?",
                warnTint = leaksOn,
                checked = leaksOn,
                onCheckedChange = { on ->
                    onUpdate("PASFUITE", if (on) "N" else "O")
                    if (on && leakBlocks < 1) leakBlocks = 1
                },
            )
            Column(
                modifier = Modifier.padding(start = 15.dp, end = 15.dp, bottom = 15.dp),
                verticalArrangement = Arrangement.spacedBy(11.dp),
            ) {
                if (!leaksOn) {
                    CerfaEmptyState(
                        title = "Aucune fuite à déclarer",
                        subtitle = "Activez le bouton pour saisir localisation et réparation.",
                    )
                } else {
                    val visible = visibleLeakBlockIndices(true, leakBlocks)
                    if (1 in visible) {
                        CerfaFuiteEntry(
                            index = 1,
                            localisation = state.fuiteloc1,
                            reparation = state.fuiterep1,
                            onLocChange = { onUpdate("FUITELOC1", it) },
                            onRepChange = { onUpdate("FUITEREP1", it) },
                            onDelete = null,
                        )
                    }
                    if (2 in visible) {
                        CerfaFuiteEntry(
                            index = 2,
                            localisation = state.fuiteloc2,
                            reparation = state.fuiterep2,
                            onLocChange = { onUpdate("FUITELOC2", it) },
                            onRepChange = { onUpdate("FUITEREP2", it) },
                            onDelete = {
                                viewModel.removeLeakBlock(2)
                                leakBlocks = (leakBlocks - 1).coerceAtLeast(1)
                            },
                        )
                    }
                    if (3 in visible) {
                        CerfaFuiteEntry(
                            index = 3,
                            localisation = state.fuiteloc3,
                            reparation = state.fuiterep3,
                            onLocChange = { onUpdate("FUITELOC3", it) },
                            onRepChange = { onUpdate("FUITEREP3", it) },
                            onDelete = {
                                viewModel.removeLeakBlock(3)
                                leakBlocks = (leakBlocks - 1).coerceAtLeast(1)
                            },
                        )
                    }
                    if (leakBlocks < 3) {
                        CerfaAddDashedButton("+ Ajouter une fuite") {
                            leakBlocks = (leakBlocks + 1).coerceAtMost(3)
                        }
                    }
                }
            }
        }

        CerfaSectionCard {
            CerfaCardHead(
                icon = Icons.Filled.WaterDrop,
                title = "Seuils de charge fluide",
                subtitle = "Cases HCFC / HFC / HFO du CERFA",
            )
            Column(
                modifier = Modifier.padding(start = 15.dp, end = 15.dp, bottom = 15.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                CerfaChargeThresholdGroup(
                    title = "HCFC",
                    options =
                        listOf(
                            "1" to "≥ 2 kg",
                            "2" to "≥ 30 kg",
                            "3" to "≥ 300 kg",
                        ),
                    selected = state.qtefri,
                    onSelect = { onUpdate("QTEFRI", it) },
                )
                CerfaChargeThresholdGroup(
                    title = "HFC",
                    options =
                        listOf(
                            "1" to "≥ 5 t éq. CO₂",
                            "2" to "≥ 50 t éq. CO₂",
                            "3" to "≥ 500 t éq. CO₂",
                        ),
                    selected = state.qtefri2,
                    onSelect = { onUpdate("QTEFRI2", it) },
                )
                CerfaChargeThresholdGroup(
                    title = "HFO",
                    options =
                        listOf(
                            "1" to "≥ 1 t éq. CO₂",
                            "2" to "≥ 10 t éq. CO₂",
                            "3" to "≥ 100 t éq. CO₂",
                        ),
                    selected = state.qtefri3,
                    onSelect = { onUpdate("QTEFRI3", it) },
                )
            }
        }

        CerfaSectionCard {
            CerfaCardHead(
                icon = Icons.Outlined.Timer,
                title = "Fréquence de contrôle d'étanchéité",
                subtitle = "Périodicité réglementaire",
            )
            Column(
                modifier = Modifier.padding(start = 15.dp, end = 15.dp, bottom = 15.dp),
                verticalArrangement = Arrangement.spacedBy(11.dp),
            ) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(13.dp))
                            .background(SavioRefonte.Tint)
                            .border(1.dp, Color(0xFFDBE5F0), RoundedCornerShape(13.dp))
                            .padding(horizontal = 13.dp, vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(11.dp),
                ) {
                    Box(
                        modifier =
                            Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.White),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            if (autoMode) Icons.Filled.Notifications else Icons.Filled.Search,
                            contentDescription = null,
                            tint = SavioRefonte.Navy,
                            modifier = Modifier.size(17.dp),
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Détection automatique de fuite",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6E86A3),
                        )
                        Text(
                            text =
                                if (autoMode) {
                                    "Équipement équipé — périodicité doublée"
                                } else {
                                    "Pas de système — périodicité standard"
                                },
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = SavioRefonte.Navy,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }
                    Text(
                        text = "Modifier",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = SavioRefonte.Navy,
                        modifier =
                            Modifier
                                .clip(RoundedCornerShape(9.dp))
                                .background(Color.White)
                                .border(1.dp, Color(0xFFCFDDEC), RoundedCornerShape(9.dp))
                                .clickable {
                                    val next = !autoMode
                                    autoMode = next
                                    viewModel.setFrequencyMode(next)
                                }
                                .padding(horizontal = 12.dp, vertical = 7.dp),
                    )
                }
                Text(
                    text =
                        buildString {
                            append("Pré-sélection d'après la charge")
                            if (state.tonnage.isNotBlank()) {
                                append(" (${state.tonnage} t CO₂e)")
                            }
                            append(".")
                        },
                    fontSize = 12.5.sp,
                    color = Color(0xFF5A6573),
                )
                options.forEach { (months, sub) ->
                    CerfaRadioOption(
                        title = "$months mois",
                        subtitle = sub,
                        selected = selectedMonths == months,
                        recommended = recommended == months,
                        onClick = { viewModel.selectFrequencyMonths(months, autoMode) },
                    )
                }
            }
        }

        CerfaSectionCard {
            CerfaCardHead(
                icon = Icons.Filled.Check,
                title = "Autocontrôle de fuite",
                subtitle = "Réalisé après réparation",
                checked = state.autofuite == "O",
                onCheckedChange = { onUpdate("AUTOFUITE", if (it) "O" else "N") },
            )
        }
    }
}

@Composable
private fun CerfaTabFluides(
    state: ColdMeasureEntity,
    viewModel: CerfaFroidViewModel,
) {
    val onUpdate = viewModel::update
    val supplyContainers by viewModel.supplyContainers.collectAsStateWithLifecycle()
    val recoveryContainers by viewModel.recoveryContainers.collectAsStateWithLifecycle()
    val createError by viewModel.containerCreateError.collectAsStateWithLifecycle()
    val capacityUi by viewModel.capacityAttestation.collectAsStateWithLifecycle()
    var showCreateDialog by remember { mutableStateOf<String?>(null) }
    var capacityDraft by remember(capacityUi.currentNumber, capacityUi.needsCapture) {
        mutableStateOf(
            if (capacityUi.isDemo) "" else capacityUi.currentNumber,
        )
    }
    val reinVal = state.fluidrein.replace(",", ".").toDoubleOrNull() ?: 0.0
    val recupVal = state.fluidrecup.replace(",", ".").toDoubleOrNull() ?: 0.0
    val supplyRequired = reinVal > 0.0 && state.supplyContainerId.isBlank()
    val recoveryRequired = recupVal > 0.0 && state.recoveryContainerId.isBlank()

    if (createError != null) {
        AlertDialog(
            onDismissRequest = viewModel::clearContainerCreateError,
            title = { Text("Contenant fluide") },
            text = { Text(createError!!) },
            confirmButton = {
                TextButton(onClick = viewModel::clearContainerCreateError) { Text("OK") }
            },
        )
    }
    if (capacityUi.saveError != null) {
        AlertDialog(
            onDismissRequest = viewModel::clearCapacityAttestationError,
            title = { Text("Attestation de capacité") },
            text = { Text(capacityUi.saveError!!) },
            confirmButton = {
                TextButton(onClick = viewModel::clearCapacityAttestationError) { Text("OK") }
            },
        )
    }
    showCreateDialog?.let { kind ->
        CreateContainerDialog(
            kind = kind,
            onDismiss = { showCreateDialog = null },
            onConfirm = { identifier, capacity ->
                viewModel.createContainerOnTheFly(identifier, capacity, kind) {
                    showCreateDialog = null
                }
            },
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        if (capacityUi.needsCapture) {
            CerfaSectionCard {
                CerfaCardHead(
                    icon = Icons.Filled.Info,
                    title = "Attestation de capacité",
                    subtitle =
                        if (capacityUi.isDemo) {
                            "Numéro démo — à remplacer par le vrai numéro société"
                        } else {
                            "Obligatoire sur le CERFA (niveau société)"
                        },
                )
                Column(
                    modifier = Modifier.padding(start = 15.dp, end = 15.dp, bottom = 15.dp),
                    verticalArrangement = Arrangement.spacedBy(11.dp),
                ) {
                    if (capacityUi.canEdit) {
                        OutlinedTextField(
                            value = capacityDraft,
                            onValueChange = { capacityDraft = it },
                            label = { Text("N° attestation de capacité") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Button(
                            onClick = { viewModel.saveCapacityAttestation(capacityDraft) },
                            enabled = capacityDraft.isNotBlank() && !capacityUi.isSaving,
                        ) {
                            Text(if (capacityUi.isSaving) "Enregistrement…" else "Enregistrer")
                        }
                    } else {
                        Text(
                            "À compléter par le responsable (profil artisan).",
                            color = Color(0xFF5C6B7A),
                            fontSize = 13.sp,
                        )
                    }
                }
            }
        }
        CerfaSectionCard {
            CerfaCardHead(icon = Icons.Filled.WaterDrop, title = "Fluide chargé", subtitle = "Vapeur / liquide / gaz")
            Column(
                modifier = Modifier.padding(start = 15.dp, end = 15.dp, bottom = 15.dp),
                verticalArrangement = Arrangement.spacedBy(11.dp),
            ) {
                CerfaInputField("Fluide chargé — vapeur (kg)", state.fluidecv, { onUpdate("FLUIDECV", it) }, keyboardType = KeyboardType.Decimal)
                CerfaInputField("Fluide chargé — liquide (kg)", state.fluidecr, { onUpdate("FLUIDECR", it) }, keyboardType = KeyboardType.Decimal)
                CerfaInputField("Fluide chargé — gaz (kg)", state.fluidecrg, { onUpdate("FLUIDECRG", it) }, keyboardType = KeyboardType.Decimal)
                CerfaAutoField("Total fluide réinjecté (auto)", state.fluidrein)
                ContainerPickerField(
                    label = "Contenant approvisionnement",
                    selectedId = state.supplyContainerId,
                    containers = supplyContainers,
                    labelFor = viewModel::containerLabel,
                    isError = supplyRequired,
                    onSelect = viewModel::selectSupplyContainer,
                    onCreate = { showCreateDialog = "supply" },
                    onRefresh = viewModel::refreshContainers,
                )
            }
        }
        CerfaSectionCard {
            CerfaCardHead(icon = Icons.Filled.WaterDrop, title = "Fluide récupéré", subtitle = "Installation / hors installation")
            Column(
                modifier = Modifier.padding(start = 15.dp, end = 15.dp, bottom = 15.dp),
                verticalArrangement = Arrangement.spacedBy(11.dp),
            ) {
                CerfaInputField("Fluide récupéré — total installation (kg)", state.fluidert, { onUpdate("FLUIDERT", it) }, keyboardType = KeyboardType.Decimal)
                CerfaInputField("Fluide récupéré — hors installation (kg)", state.fluideru, { onUpdate("FLUIDERU", it) }, keyboardType = KeyboardType.Decimal)
                CerfaAutoField("Total fluide récupéré (auto)", state.fluidrecup)
                ContainerPickerField(
                    label = "Contenant récupération",
                    selectedId = state.recoveryContainerId,
                    containers = recoveryContainers,
                    labelFor = viewModel::containerLabel,
                    isError = recoveryRequired,
                    onSelect = viewModel::selectRecoveryContainer,
                    onCreate = { showCreateDialog = "recovery" },
                    onRefresh = viewModel::refreshContainers,
                )
            }
        }
    }
}

@Composable
private fun CerfaTabTransport(
    state: ColdMeasureEntity,
    viewModel: CerfaFroidViewModel,
) {
    val onUpdate = viewModel::update
    val destinations by viewModel.destinationPartners.collectAsStateWithLifecycle()
    val transporters by viewModel.transporterPartners.collectAsStateWithLifecycle()
    val partnerError by viewModel.partnerCreateError.collectAsStateWithLifecycle()
    var showCreateKind by remember { mutableStateOf<String?>(null) }

    if (partnerError != null) {
        AlertDialog(
            onDismissRequest = viewModel::clearPartnerCreateError,
            title = { Text("Partenaire fluide") },
            text = { Text(partnerError!!) },
            confirmButton = {
                TextButton(onClick = viewModel::clearPartnerCreateError) { Text("OK") }
            },
        )
    }
    showCreateKind?.let { kind ->
        CreateWastePartnerDialog(
            kind = kind,
            onDismiss = { showCreateKind = null },
            onConfirm = { label, siret, address1, address2, postal, city ->
                viewModel.createWastePartnerOnTheFly(
                    kind = kind,
                    label = label,
                    siret = siret,
                    addressLine1 = address1,
                    addressLine2 = address2,
                    postalCode = postal,
                    city = city,
                ) {
                    showCreateKind = null
                }
            },
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        CerfaSectionCard {
            CerfaCardHead(icon = Icons.Filled.Info, title = "Codes UN 1078", subtitle = "Transport matières dangereuses")
            Column(
                modifier = Modifier.padding(start = 15.dp, end = 15.dp, bottom = 15.dp),
                verticalArrangement = Arrangement.spacedBy(11.dp),
            ) {
                CerfaUnCodeGroup(
                    selected = state.un1078a,
                    optionOuiLabel = "UN 1078",
                    optionNonLabel = "Autre (14 06 01)",
                    onSelect = { onUpdate("UN1078A", it) },
                )
                CerfaInputField(
                    "Quantité (kg)",
                    state.un1078b,
                    { onUpdate("UN1078B", it) },
                    keyboardType = KeyboardType.Decimal,
                )
            }
        }
        CerfaSectionCard {
            CerfaCardHead(icon = Icons.Filled.Build, title = "Déchetterie", subtitle = "Destinataire des déchets")
            Column(
                modifier = Modifier.padding(start = 15.dp, end = 15.dp, bottom = 15.dp),
                verticalArrangement = Arrangement.spacedBy(11.dp),
            ) {
                WastePartnerPickerField(
                    label = "Choisir un destinataire",
                    partners = destinations,
                    selectedLabel = state.nomdec,
                    labelFor = viewModel::partnerLabel,
                    onSelect = viewModel::selectDestinationPartner,
                    onCreate = {
                        showCreateKind = RefrigerantWastePartnerSyncRepository.KIND_DESTINATION
                    },
                    onRefresh = viewModel::refreshWastePartners,
                )
                CerfaInputField("Nom déchetterie", state.nomdec, { onUpdate("NOMDEC", it) })
                CerfaInputField("Adresse ligne 1", state.adres1dec, { onUpdate("ADRES1DEC", it) })
                CerfaInputField("Adresse ligne 2", state.adres2dec, { onUpdate("ADRES2DEC", it) })
                CerfaInputField("Ville", state.villedec, { onUpdate("VILLEDEC", it) })
            }
        }
        CerfaSectionCard {
            CerfaCardHead(icon = Icons.Filled.Build, title = "Transporteur", subtitle = null)
            Column(
                modifier = Modifier.padding(start = 15.dp, end = 15.dp, bottom = 15.dp),
                verticalArrangement = Arrangement.spacedBy(11.dp),
            ) {
                WastePartnerPickerField(
                    label = "Choisir un transporteur",
                    partners = transporters,
                    selectedLabel = state.nomtrans,
                    labelFor = viewModel::partnerLabel,
                    onSelect = viewModel::selectTransporterPartner,
                    onCreate = {
                        showCreateKind = RefrigerantWastePartnerSyncRepository.KIND_TRANSPORTER
                    },
                    onRefresh = viewModel::refreshWastePartners,
                )
                CerfaInputField("Nom transporteur", state.nomtrans, { onUpdate("NOMTRANS", it) })
                CerfaInputField("Adresse ligne 1", state.adres1trans, { onUpdate("ADRES1TRANS", it) })
                CerfaInputField("Adresse ligne 2", state.adres2trans, { onUpdate("ADRES2TRANS", it) })
                CerfaInputField("Ville", state.villetrans, { onUpdate("VILLETRANS", it) })
            }
        }
    }
}

@Composable
private fun CerfaTabObsFluides(
    state: ColdMeasureEntity,
    onUpdate: (String, String) -> Unit,
) {
    CerfaSectionCard {
        CerfaCardHead(icon = Icons.Filled.Info, title = "Observations fluides", subtitle = "Case 12")
        Column(
            modifier = Modifier.padding(start = 15.dp, end = 15.dp, bottom = 15.dp),
            verticalArrangement = Arrangement.spacedBy(11.dp),
        ) {
            CerfaInputField("Observations fluides", state.fluidobs, { onUpdate("FLUIDOBS", it) }, singleLine = false, minLines = 3)
            CerfaInputField("Observations fluides (suite)", state.fluidobs2, { onUpdate("FLUIDOBS2", it) }, singleLine = false, minLines = 3)
        }
    }
}

@Composable
private fun CerfaTabBordereau(
    state: ColdMeasureEntity,
    onUpdate: (String, String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        CerfaSectionCard {
            CerfaCardHead(icon = Icons.Filled.Description, title = "Bordereau", subtitle = "Cases 13–14")
            Column(
                modifier = Modifier.padding(start = 15.dp, end = 15.dp, bottom = 15.dp),
                verticalArrangement = Arrangement.spacedBy(11.dp),
            ) {
                CerfaInputField("Bordereau — quantité", state.bordeqte, { onUpdate("BORDEQTE", it) })
                CerfaInputField("Bordereau — transport", state.bordetrans, { onUpdate("BORDETRANS", it) })
                CerfaInputField("Installation traitée", state.instatrait, { onUpdate("INSTATRAIT", it) })
                CerfaInputField("Code RD", state.coderd, { onUpdate("CODERD", it) })
                CerfaInputField("Quantité réception", state.qterecep, { onUpdate("QTERECEP", it) }, keyboardType = KeyboardType.Decimal)
                CerfaInputField("Frigo (2)", state.frigo2, { onUpdate("FRIGO2", it) })
                CerfaInputField("BSFF", state.bsff, { onUpdate("BSFF", it) })
                CerfaUnCodeGroup(
                    selected = state.un3161a,
                    optionOuiLabel = "UN 3161",
                    optionNonLabel = "Autre (16 05 04)",
                    onSelect = { onUpdate("UN3161A", it) },
                )
                CerfaInputField(
                    "Quantité UN 3161 (kg)",
                    state.un3161b,
                    { onUpdate("UN3161B", it) },
                    keyboardType = KeyboardType.Decimal,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FrigoDropdown(
    value: String,
    onUpdate: (String, String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text("Fluide frigorigène") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .menuAnchor(type = MenuAnchorType.PrimaryNotEditable, enabled = true),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            FLUIDES_FRIGORIGENES.forEach { fluid ->
                DropdownMenuItem(
                    text = { Text(fluid) },
                    onClick = {
                        onUpdate("FRIGO", fluid)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun CreateContainerDialog(
    kind: String,
    onDismiss: () -> Unit,
    onConfirm: (identifier: String, capacityKg: Double) -> Unit,
) {
    var identifier by remember(kind) { mutableStateOf("") }
    var capacity by remember(kind) { mutableStateOf("") }
    val title = if (kind == "supply") "Nouveau contenant appro." else "Nouveau contenant récup."
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = identifier,
                    onValueChange = { identifier = it },
                    label = { Text("Identifiant") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = capacity,
                    onValueChange = { capacity = it },
                    label = { Text("Capacité (kg)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cap = capacity.replace(",", ".").toDoubleOrNull()
                    if (identifier.isNotBlank() && cap != null && cap > 0) {
                        onConfirm(identifier.trim(), cap)
                    }
                },
                enabled = identifier.isNotBlank() && capacity.replace(",", ".").toDoubleOrNull()?.let { it > 0 } == true,
            ) { Text("Créer") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler") } },
    )
}

@Composable
private fun CreateWastePartnerDialog(
    kind: String,
    onDismiss: () -> Unit,
    onConfirm: (
        label: String,
        siret: String,
        addressLine1: String,
        addressLine2: String,
        postalCode: String,
        city: String,
    ) -> Unit,
) {
    var label by remember(kind) { mutableStateOf("") }
    var siret by remember(kind) { mutableStateOf("") }
    var address1 by remember(kind) { mutableStateOf("") }
    var address2 by remember(kind) { mutableStateOf("") }
    var postal by remember(kind) { mutableStateOf("") }
    var city by remember(kind) { mutableStateOf("") }
    val title =
        if (kind == RefrigerantWastePartnerSyncRepository.KIND_TRANSPORTER) {
            "Nouveau transporteur"
        } else {
            "Nouveau destinataire"
        }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Libellé") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = siret,
                    onValueChange = { siret = it },
                    label = { Text("SIRET") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = address1,
                    onValueChange = { address1 = it },
                    label = { Text("Adresse ligne 1") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = address2,
                    onValueChange = { address2 = it },
                    label = { Text("Adresse ligne 2") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = postal,
                    onValueChange = { postal = it },
                    label = { Text("Code postal") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = city,
                    onValueChange = { city = it },
                    label = { Text("Ville") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        label.trim(),
                        siret.trim(),
                        address1.trim(),
                        address2.trim(),
                        postal.trim(),
                        city.trim(),
                    )
                },
                enabled = label.isNotBlank() && siret.isNotBlank(),
            ) { Text("Créer") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler") } },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WastePartnerPickerField(
    label: String,
    partners: List<RefrigerantWastePartnerBox>,
    selectedLabel: String,
    labelFor: (RefrigerantWastePartnerBox) -> String,
    onSelect: (String?) -> Unit,
    onCreate: () -> Unit,
    onRefresh: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val display =
        partners.firstOrNull { it.label == selectedLabel }?.let(labelFor)
            ?: selectedLabel
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
            OutlinedTextField(
                value = display,
                onValueChange = {},
                readOnly = true,
                label = { Text(label) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable),
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                partners.forEach { partner ->
                    DropdownMenuItem(
                        text = { Text(labelFor(partner)) },
                        onClick = {
                            onSelect(partner.id)
                            expanded = false
                        },
                    )
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = onCreate) { Text("+ Créer") }
            TextButton(onClick = onRefresh) { Text("Actualiser") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContainerPickerField(
    label: String,
    selectedId: String,
    containers: List<RefrigerantContainerBox>,
    labelFor: (RefrigerantContainerBox) -> String,
    isError: Boolean,
    onSelect: (String?) -> Unit,
    onCreate: () -> Unit,
    onRefresh: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = containers.firstOrNull { it.id == selectedId }?.let(labelFor).orEmpty()
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
            OutlinedTextField(
                value = selectedLabel,
                onValueChange = {},
                readOnly = true,
                label = { Text(label) },
                isError = isError,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable),
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                DropdownMenuItem(
                    text = { Text("— Aucun —") },
                    onClick = {
                        onSelect(null)
                        expanded = false
                    },
                )
                containers.forEach { container ->
                    DropdownMenuItem(
                        text = { Text(labelFor(container)) },
                        onClick = {
                            onSelect(container.id)
                            expanded = false
                        },
                    )
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = onCreate) { Text("+ Créer") }
            TextButton(onClick = onRefresh) { Text("Actualiser") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MeasurementDevicePickerField(
    devices: List<MeasurementDeviceBox>,
    selectedBrand: String,
    selectedModel: String,
    labelFor: (MeasurementDeviceBox) -> String,
    onSelect: (String?) -> Unit,
    canCreate: Boolean,
    onCreate: () -> Unit,
    onRefresh: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val display =
        devices
            .firstOrNull { it.brand == selectedBrand && it.model == selectedModel }
            ?.let(labelFor)
            ?: listOf(selectedBrand, selectedModel)
                .filter { it.isNotBlank() }
                .joinToString(" ")
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
            OutlinedTextField(
                value = display,
                onValueChange = {},
                readOnly = true,
                label = { Text("Choisir un détecteur") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable),
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                if (devices.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("Aucun appareil synchronisé") },
                        onClick = { expanded = false },
                    )
                } else {
                    devices.forEach { device ->
                        DropdownMenuItem(
                            text = { Text(labelFor(device)) },
                            onClick = {
                                onSelect(device.id)
                                expanded = false
                            },
                        )
                    }
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (canCreate) {
                TextButton(onClick = onCreate) { Text("+ Créer") }
            }
            TextButton(onClick = onRefresh) { Text("Actualiser") }
        }
    }
}

@Composable
private fun CreateMeasurementDeviceDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        brand: String,
        model: String,
        serialNumber: String,
        lastControlDate: String,
    ) -> Unit,
) {
    var brand by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var serial by remember { mutableStateOf("") }
    var controlDate by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nouveau détecteur") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = brand,
                    onValueChange = { brand = it },
                    label = { Text("Marque") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = model,
                    onValueChange = { model = it },
                    label = { Text("Modèle") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = serial,
                    onValueChange = { serial = it },
                    label = { Text("N° de série") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = controlDate,
                    onValueChange = { controlDate = it },
                    label = { Text("Date contrôle (jj/mm/aaaa)") },
                    placeholder = { Text("25/12/2026") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(brand.trim(), model.trim(), serial.trim(), controlDate.trim())
                },
                enabled = brand.isNotBlank() && model.isNotBlank(),
            ) { Text("Créer") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler") } },
    )
}

@Composable
private fun CerfaChargeThresholdGroup(
    title: String,
    options: List<Pair<String, String>>,
    selected: String,
    onSelect: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = SavioRefonte.Navy,
        )
        options.forEach { (code, label) ->
            CerfaRadioOption(
                title = label,
                subtitle = null,
                selected = selected == code,
                recommended = false,
                onClick = { onSelect(if (selected == code) "" else code) },
            )
        }
    }
}

@Composable
private fun CerfaUnCodeGroup(
    selected: String,
    optionOuiLabel: String,
    optionNonLabel: String,
    onSelect: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        CerfaRadioOption(
            title = optionOuiLabel,
            subtitle = null,
            selected = selected == "O",
            recommended = false,
            onClick = { onSelect(if (selected == "O") "" else "O") },
        )
        CerfaRadioOption(
            title = optionNonLabel,
            subtitle = null,
            selected = selected == "N",
            recommended = false,
            onClick = { onSelect(if (selected == "N") "" else "N") },
        )
    }
}
