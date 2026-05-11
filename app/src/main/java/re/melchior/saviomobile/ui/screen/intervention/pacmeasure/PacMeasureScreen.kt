package re.melchior.saviomobile.ui.screen.intervention.pacmeasure

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import re.melchior.saviomobile.ui.screen.intervention.measure.MeasureDropdownField
import re.melchior.saviomobile.ui.screen.intervention.measure.MeasureNumericField
import re.melchior.saviomobile.ui.theme.SavioPalette

private val onOffOptions = listOf("", "O", "N")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PacMeasureScreen(
    onBack: () -> Unit,
    viewModel: PacMeasureViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var tab by remember { mutableIntStateOf(0) }

    DisposableEffect(Unit) {
        onDispose { viewModel.save() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mesures froid") },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            viewModel.save()
                            onBack()
                        },
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour",
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SavioPalette.Primary,
                    titleContentColor = SavioPalette.White,
                    navigationIconContentColor = SavioPalette.White,
                ),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            TabRow(selectedTabIndex = tab) {
                listOf(
                    "Extérieur",
                    "Électrique",
                    "Températures",
                    "Tests",
                    "Remarques",
                ).forEachIndexed { index, label ->
                    Tab(
                        selected = tab == index,
                        onClick = { tab = index },
                        text = { Text(label, style = MaterialTheme.typography.labelMedium) },
                    )
                }
            }
            val scroll = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scroll)
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                when (tab) {
                    0 -> PacTabExterieur(state, viewModel::updateField)
                    1 -> PacTabElectrique(state, viewModel::updateField)
                    2 -> PacTabTemperatures(state, viewModel::updateField)
                    3 -> PacTabTests(state, viewModel::updateField)
                    4 -> PacTabRemarques(state, viewModel::updateField)
                }
            }
        }
    }
}

@Composable
private fun PacTabExterieur(
    state: re.melchior.saviomobile.data.local.entity.PacMeasureEntity,
    onUpdate: (String, String) -> Unit,
) {
    MeasureDropdownField(
        state.pacVentilation,
        { onUpdate("pacVentilation", it) },
        "Ventilation groupe",
        onOffOptions,
    )
    MeasureDropdownField(
        state.pacNetail,
        { onUpdate("pacNetail", it) },
        "Nettoyage ailettes",
        onOffOptions,
    )
    MeasureDropdownField(
        state.pacVerail,
        { onUpdate("pacVerail", it) },
        "Vérification ailettes",
        onOffOptions,
    )
    MeasureDropdownField(
        state.pacFiltre,
        { onUpdate("pacFiltre", it) },
        "Nettoyage filtre",
        onOffOptions,
    )
    MeasureDropdownField(
        state.pacFuite,
        { onUpdate("pacFuite", it) },
        "Fuite hydraulique",
        onOffOptions,
    )
    MeasureDropdownField(
        state.pacEvac,
        { onUpdate("pacEvac", it) },
        "Évacuation condensats",
        onOffOptions,
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        MeasureDropdownField(
            state.pacPression1,
            { onUpdate("pacPression1", it) },
            "Pression hydraulique",
            onOffOptions,
            modifier = Modifier.weight(1f),
        )
        MeasureNumericField(
            state.pacPression2,
            { onUpdate("pacPression2", it) },
            "Valeur (bar)",
            modifier = Modifier.weight(1f),
        )
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        MeasureDropdownField(
            state.pacGlycol1,
            { onUpdate("pacGlycol1", it) },
            "% Glycol",
            onOffOptions,
            modifier = Modifier.weight(1f),
        )
        MeasureNumericField(
            state.pacGlycol2,
            { onUpdate("pacGlycol2", it) },
            "Valeur",
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun PacTabElectrique(
    state: re.melchior.saviomobile.data.local.entity.PacMeasureEntity,
    onUpdate: (String, String) -> Unit,
) {
    MeasureNumericField(state.pacTenStat, { onUpdate("pacTenStat", it) }, "Tension statique (V)")
    MeasureNumericField(state.pacTenDyna, { onUpdate("pacTenDyna", it) }, "Tension dynamique (V)")
    MeasureNumericField(state.pacIntensite, { onUpdate("pacIntensite", it) }, "Intensité compresseur (A)")
    MeasureDropdownField(
        state.pacResserage1,
        { onUpdate("pacResserage1", it) },
        "Resserrage bornes PAC",
        onOffOptions,
    )
    MeasureDropdownField(
        state.pacResserage2,
        { onUpdate("pacResserage2", it) },
        "Resserrage bornes coffret",
        onOffOptions,
    )
}

@Composable
private fun PacTabTemperatures(
    state: re.melchior.saviomobile.data.local.entity.PacMeasureEntity,
    onUpdate: (String, String) -> Unit,
) {
    MeasureNumericField(state.pacInterieure, { onUpdate("pacInterieure", it) }, "T° intérieure (°C)")
    MeasureNumericField(state.pacExterieure, { onUpdate("pacExterieure", it) }, "T° extérieure (°C)")
    MeasureNumericField(state.pacDepart, { onUpdate("pacDepart", it) }, "T° départ PAC (°C)")
    MeasureNumericField(state.pacRetour, { onUpdate("pacRetour", it) }, "T° retour PAC (°C)")
    OutlinedTextField(
        value = state.pacDeltaT,
        onValueChange = {},
        readOnly = true,
        label = { Text("Delta T (°C)") },
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
            unfocusedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
        ),
    )
    MeasureNumericField(state.pacHiver, { onUpdate("pacHiver", it) }, "T° consigne hiver (°C)")
    MeasureNumericField(state.pacAppoint, { onUpdate("pacAppoint", it) }, "T° enclenchement appoint (°C)")
    MeasureNumericField(state.pacConfort, { onUpdate("pacConfort", it) }, "T° consigne confort (°C)")
    MeasureNumericField(state.pacNonChauf, { onUpdate("pacNonChauf", it) }, "T° non chauffage (°C)")
    MeasureNumericField(state.pacEcsConsigne, { onUpdate("pacEcsConsigne", it) }, "T° consigne ECS (°C)")
    MeasureNumericField(state.pacEcs, { onUpdate("pacEcs", it) }, "T° ECS (°C)")
    MeasureNumericField(state.pacManometreBp, { onUpdate("pacManometreBp", it) }, "Manomètre BP (bar)")
    MeasureNumericField(state.pacManometreHp, { onUpdate("pacManometreHp", it) }, "Manomètre HP (bar)")
}

@Composable
private fun PacTabTests(
    state: re.melchior.saviomobile.data.local.entity.PacMeasureEntity,
    onUpdate: (String, String) -> Unit,
) {
    MeasureDropdownField(
        state.pacDegivrage,
        { onUpdate("pacDegivrage", it) },
        "Fonctionnement dégivrage",
        onOffOptions,
    )
    MeasureDropdownField(
        state.pacInversion,
        { onUpdate("pacInversion", it) },
        "Test inversion relève",
        onOffOptions,
    )
    MeasureNumericField(state.pacHFonct, { onUpdate("pacHFonct", it) }, "Heures fonctionnement total")
    MeasureNumericField(state.pacHComp1, { onUpdate("pacHComp1", it) }, "Heures compresseur 1")
    MeasureNumericField(state.pacHVenti, { onUpdate("pacHVenti", it) }, "Heures ventilateur")
    MeasureNumericField(state.pacNbDemarr, { onUpdate("pacNbDemarr", it) }, "Nb démarrages compresseur")
    MeasureNumericField(state.pacHAppoint1, { onUpdate("pacHAppoint1", it) }, "Heures appoint 1")
    MeasureNumericField(state.pacHAppoint2, { onUpdate("pacHAppoint2", it) }, "Heures appoint 2")
}

@Composable
private fun PacTabRemarques(
    state: re.melchior.saviomobile.data.local.entity.PacMeasureEntity,
    onUpdate: (String, String) -> Unit,
) {
    OutlinedTextField(
        value = state.pacAlarme1,
        onValueChange = { onUpdate("pacAlarme1", it.take(20)) },
        label = { Text("Alarme 1 (max 20)") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth().padding(4.dp),
    )
    OutlinedTextField(
        value = state.pacAlarme2,
        onValueChange = { onUpdate("pacAlarme2", it.take(20)) },
        label = { Text("Alarme 2 (max 20)") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth().padding(4.dp),
    )
    OutlinedTextField(
        value = state.pacBlocage1,
        onValueChange = { onUpdate("pacBlocage1", it.take(20)) },
        label = { Text("Blocage 1 (max 20)") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth().padding(4.dp),
    )
    OutlinedTextField(
        value = state.pacBlocage2,
        onValueChange = { onUpdate("pacBlocage2", it.take(20)) },
        label = { Text("Blocage 2 (max 20)") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth().padding(4.dp),
    )
    MeasureNumericField(state.pacReleve, { onUpdate("pacReleve", it) }, "Puissance relevée (kW)")
    OutlinedTextField(
        value = state.pacRem1,
        onValueChange = { onUpdate("pacRem1", it) },
        label = { Text("Commentaire") },
        minLines = 3,
        modifier = Modifier.fillMaxWidth().padding(4.dp),
    )
}
