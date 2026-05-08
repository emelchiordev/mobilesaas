package re.melchior.saviomobile.ui.screen.intervention.cerfa

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
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import re.melchior.saviomobile.data.local.entity.ColdMeasureEntity

private val TAB_TITLES = listOf(
    "[3] Équipement",
    "[4] Nature",
    "[5–6] Obs.",
    "[7–9] Fuites",
    "[10] Fluides",
    "[11] Transport",
    "[12] Obs. fluides",
    "[13–14] Bordereau",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CerfaFroidScreen(
    onBack: () -> Unit,
    windowSizeClass: WindowSizeClass,
    onApercuPdf: () -> Unit = {},
    viewModel: CerfaFroidViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()
    val hasPersistedCerfa by viewModel.hasPersistedData.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) }
    val lifecycleOwner = LocalLifecycleOwner.current
    val columns = when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Expanded -> 3
        WindowWidthSizeClass.Medium -> 2
        else -> 1
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                viewModel.saveLocally()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    fun leave() {
        viewModel.saveLocally()
        onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("CERFA 15497") },
                navigationIcon = {
                    IconButton(onClick = ::leave) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    TextButton(
                        onClick = onApercuPdf,
                        enabled = hasPersistedCerfa,
                    ) {
                        Text("Aperçu PDF")
                    }
                    TextButton(
                        onClick = { viewModel.saveLocally() },
                        enabled = !isSaving,
                    ) {
                        Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Enregistrer")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            ScrollableTabRow(selectedTabIndex = selectedTab) {
                TAB_TITLES.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, maxLines = 1, style = MaterialTheme.typography.labelMedium) },
                    )
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            ) {
                when (selectedTab) {
                    0 -> CerfaFroidTab3(state, viewModel::update, columns)
                    1 -> CerfaFroidTab4(state, viewModel::update, columns)
                    2 -> CerfaFroidTab56(state, viewModel::update, columns)
                    3 -> CerfaFroidTab789(state, viewModel::update, columns)
                    4 -> CerfaFroidTab10(state, viewModel::update, columns)
                    5 -> CerfaFroidTab11(state, viewModel::update, columns)
                    6 -> CerfaFroidTab12(state, viewModel::update, columns)
                    7 -> CerfaFroidTab1314(state, viewModel::update, columns)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CerfaFroidTab3(
    state: ColdMeasureEntity,
    onUpdate: (String, String) -> Unit,
    columns: Int,
) {
    FormGrid(columns) {
        FrigoDropdown(state.frigo, onUpdate)
        OutlinedField("Charge totale (kg)", state.charg, "CHARG", onUpdate, singleLine = true)
        OutlinedField("Tonnage CO₂", state.tonnage, "TONNAGE", onUpdate, singleLine = true)
    }
}

@Composable
private fun CerfaFroidTab4(
    state: ColdMeasureEntity,
    onUpdate: (String, String) -> Unit,
    columns: Int,
) {
    FormGrid(columns) {
        SwitchRow("Mise en service", state.minter1, "MINTER1", onUpdate)
        SwitchRow("Maintenance", state.minter2, "MINTER2", onUpdate)
        SwitchRow("Contrôle étanchéité périodique", state.minter3, "MINTER3", onUpdate)
        SwitchRow("Modification", state.minter4, "MINTER4", onUpdate)
        SwitchRow("Démantèlement", state.minter5, "MINTER5", onUpdate)
        SwitchRow("Assemblage", state.minter6, "MINTER6", onUpdate)
        SwitchRow("Autre", state.minter7, "MINTER7", onUpdate)
        SwitchRow("Contrôle étanchéité non périodique", state.minter8, "MINTER8", onUpdate)
        if (state.minter7 == "O") {
            OutlinedField("Précision (autre)", state.minteraut, "MINTERAUT", onUpdate)
        }
    }
}

@Composable
private fun CerfaFroidTab56(
    state: ColdMeasureEntity,
    onUpdate: (String, String) -> Unit,
    columns: Int,
) {
    FormGrid(columns) {
        OutlinedField("Observation 1", state.obsern1, "OBSERN1", onUpdate, minLines = 3)
        OutlinedField("Observation 2", state.obsern2, "OBSERN2", onUpdate, minLines = 3)
        OutlinedField("Marque détecteur", state.detm1, "DETM1", onUpdate)
        OutlinedField("Modèle détecteur", state.dett1, "DETT1", onUpdate)
        OutlinedField("Date", state.detd1, "DETD1", onUpdate, singleLine = true)
    }
}

@Composable
private fun CerfaFroidTab789(
    state: ColdMeasureEntity,
    onUpdate: (String, String) -> Unit,
    columns: Int,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Fuites constatées", modifier = Modifier.weight(1f))
            Switch(
                checked = state.pasfuite == "N",
                onCheckedChange = { onUpdate("PASFUITE", if (it) "N" else "O") },
            )
        }
        OutlinedField("Localisation fuite 1", state.fuiteloc1, "FUITELOC1", onUpdate)
        OutlinedField("Réparation fuite 1", state.fuiterep1, "FUITEREP1", onUpdate)
        OutlinedField("Localisation fuite 2", state.fuiteloc2, "FUITELOC2", onUpdate)
        OutlinedField("Réparation fuite 2", state.fuiterep2, "FUITEREP2", onUpdate)
        OutlinedField("Localisation fuite 3", state.fuiteloc3, "FUITELOC3", onUpdate)
        OutlinedField("Réparation fuite 3", state.fuiterep3, "FUITEREP3", onUpdate)

        Text("Fréquences sans système automatique (12 / 6 / 3 mois)", style = MaterialTheme.typography.titleSmall)
        FreqRadioRow(
            options = listOf("FREQS1" to "12 mois", "FREQS2" to "6 mois", "FREQS3" to "3 mois"),
            values = listOf(state.freqs1, state.freqs2, state.freqs3),
            onUpdate = onUpdate,
        )
        Text("Fréquences avec système (24 / 12 / 6 mois)", style = MaterialTheme.typography.titleSmall)
        FreqRadioRow(
            options = listOf("FREQA1" to "24 mois", "FREQA2" to "12 mois", "FREQA3" to "6 mois"),
            values = listOf(state.freqa1, state.freqa2, state.freqa3),
            onUpdate = onUpdate,
        )
        SwitchRow("Autocontrôle fuite", state.autofuite, "AUTOFUITE", onUpdate)
        OutlinedField("Quantité fuite 1", state.qtefri, "QTEFRI", onUpdate, singleLine = true)
        OutlinedField("Quantité fuite 2", state.qtefri2, "QTEFRI2", onUpdate, singleLine = true)
    }
}

@Composable
private fun CerfaFroidTab10(
    state: ColdMeasureEntity,
    onUpdate: (String, String) -> Unit,
    columns: Int,
) {
    val recupVal = state.fluidrecup.replace(",", ".").toDoubleOrNull() ?: 0.0
    val fluidercRequired = recupVal > 0.0 && state.fluiderc.isBlank()
    FormGrid(columns) {
        OutlinedField("Fluide chargé — vapeur (kg)", state.fluidecv, "FLUIDECV", onUpdate, singleLine = true)
        OutlinedField("Fluide chargé — liquide (kg)", state.fluidecr, "FLUIDECR", onUpdate, singleLine = true)
        OutlinedField("Fluide chargé — gaz (kg)", state.fluidecrg, "FLUIDECRG", onUpdate, singleLine = true)
        OutlinedTextField(
            value = state.fluidrein,
            onValueChange = {},
            readOnly = true,
            label = { Text("Total fluide réinjecté (auto)") },
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedField("Fluide récupéré — total installation (kg)", state.fluidert, "FLUIDERT", onUpdate, singleLine = true)
        OutlinedField("Fluide récupéré — hors installation (kg)", state.fluideru, "FLUIDERU", onUpdate, singleLine = true)
        OutlinedTextField(
            value = state.fluidrecup,
            onValueChange = {},
            readOnly = true,
            label = { Text("Total fluide récupéré (auto)") },
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedField(
            "Identifiant contenant (obligatoire si récup. > 0)",
            state.fluiderc,
            "FLUIDERC",
            onUpdate,
            singleLine = true,
            isError = fluidercRequired,
        )
    }
}

@Composable
private fun CerfaFroidTab11(
    state: ColdMeasureEntity,
    onUpdate: (String, String) -> Unit,
    columns: Int,
) {
    FormGrid(columns) {
        OutlinedField("UN 1078 (a)", state.un1078a, "UN1078A", onUpdate)
        OutlinedField("UN 1078 (b)", state.un1078b, "UN1078B", onUpdate)
        OutlinedField("Nom déchetterie", state.nomdec, "NOMDEC", onUpdate)
        OutlinedField("Adresse déchetterie ligne 1", state.adres1dec, "ADRES1DEC", onUpdate)
        OutlinedField("Adresse déchetterie ligne 2", state.adres2dec, "ADRES2DEC", onUpdate)
        OutlinedField("Ville déchetterie", state.villedec, "VILLEDEC", onUpdate)
        OutlinedField("Nom transporteur", state.nomtrans, "NOMTRANS", onUpdate)
        OutlinedField("Adresse transporteur ligne 1", state.adres1trans, "ADRES1TRANS", onUpdate)
        OutlinedField("Adresse transporteur ligne 2", state.adres2trans, "ADRES2TRANS", onUpdate)
        OutlinedField("Ville transporteur", state.villetrans, "VILLETRANS", onUpdate)
    }
}

@Composable
private fun CerfaFroidTab12(
    state: ColdMeasureEntity,
    onUpdate: (String, String) -> Unit,
    columns: Int,
) {
    FormGrid(columns) {
        OutlinedField("Observations fluides", state.fluidobs, "FLUIDOBS", onUpdate, minLines = 3)
        OutlinedField("Observations fluides (suite)", state.fluidobs2, "FLUIDOBS2", onUpdate, minLines = 3)
    }
}

@Composable
private fun CerfaFroidTab1314(
    state: ColdMeasureEntity,
    onUpdate: (String, String) -> Unit,
    columns: Int,
) {
    FormGrid(columns) {
        OutlinedField("Bordereau — quantité", state.bordeqte, "BORDEQTE", onUpdate)
        OutlinedField("Bordereau — transport", state.bordetrans, "BORDETRANS", onUpdate)
        OutlinedField("Installation traitée", state.instatrait, "INSTATRAIT", onUpdate)
        OutlinedField("Code RD", state.coderd, "CODERD", onUpdate)
        OutlinedField("Quantité réception", state.qterecep, "QTERECEP", onUpdate, singleLine = true)
        OutlinedField("Frigo (2)", state.frigo2, "FRIGO2", onUpdate)
        OutlinedField("BSFF", state.bsff, "BSFF", onUpdate)
        OutlinedField("UN 3161 (a)", state.un3161a, "UN3161A", onUpdate)
        OutlinedField("UN 3161 (b)", state.un3161b, "UN3161B", onUpdate)
    }
}

@Composable
private fun FormGrid(
    @Suppress("UNUSED_PARAMETER") columns: Int,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FrigoDropdown(
    value: String,
    onUpdate: (String, String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text("Fluide frigorigène") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(
                    type = MenuAnchorType.PrimaryNotEditable,
                    enabled = true,
                ),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
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
private fun SwitchRow(
    label: String,
    value: String,
    key: String,
    onUpdate: (String, String) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Switch(
            checked = value == "O",
            onCheckedChange = { onUpdate(key, if (it) "O" else "N") },
        )
    }
}

@Composable
private fun FreqRadioRow(
    options: List<Pair<String, String>>,
    values: List<String>,
    onUpdate: (String, String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        options.forEachIndexed { index, (key, label) ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                RadioButton(
                    selected = values[index] == "O",
                    onClick = { onUpdate(key, "O") },
                )
                Text(label, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun OutlinedField(
    label: String,
    value: String,
    key: String,
    onUpdate: (String, String) -> Unit,
    singleLine: Boolean = false,
    minLines: Int = 1,
    isError: Boolean = false,
) {
    OutlinedTextField(
        value = value,
        onValueChange = { onUpdate(key, it) },
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = singleLine,
        minLines = minLines,
        isError = isError,
    )
}
