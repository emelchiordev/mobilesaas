@file:OptIn(ExperimentalMaterial3Api::class)

package re.melchior.saviomobile.ui.screen.intervention.attestation

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import re.melchior.saviomobile.data.local.AttestationVeControlPoints
import re.melchior.saviomobile.data.local.entity.AttestationVeEntity
import re.melchior.saviomobile.ui.theme.SavioPalette
import re.melchior.saviomobile.ui.theme.SavioInterventionTabIndicator
import re.melchior.saviomobile.ui.theme.savioTabSelectedColor
import re.melchior.saviomobile.ui.theme.savioTabUnselectedColor
import re.melchior.saviomobile.ui.theme.savioTopAppBarColors

@Composable
@Suppress("UNUSED_PARAMETER")
fun AttestationVeScreen(
    interventionId: String,
    equipmentOrder: Int,
    type: String,
    onBack: () -> Unit,
    viewModel: AttestationVeViewModel = hiltViewModel(),
) {
    val attestation by viewModel.attestation.collectAsStateWithLifecycle()
    val evacuationMode by viewModel.evacuationMode.collectAsStateWithLifecycle()
    val points by viewModel.points.collectAsStateWithLifecycle()
    val validatedCount by viewModel.validatedCount.collectAsStateWithLifecycle()
    val nonValidatedCount by viewModel.nonValidatedCount.collectAsStateWithLifecycle()

    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    fun leave() {
        viewModel.saveIfChanged()
        onBack()
    }

    BackHandler { leave() }

    val typeLabel = when (type) {
        "GAZ" -> "Chaudière Gaz"
        "FIOUL" -> "Chaudière Fioul"
        "BOIS" -> "Chaudière Bois"
        "PAC" -> "PAC"
        "PAC_HYBRIDE_GAZ" -> "PAC Hybride Gaz"
        "PAC_HYBRIDE_FIOUL" -> "PAC Hybride Fioul"
        else -> type
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Attestation $typeLabel") },
                navigationIcon = {
                    IconButton(onClick = ::leave) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour",
                        )
                    }
                },
                colors = savioTopAppBarColors(),
                actions = {
                    if (nonValidatedCount > 0) {
                        Surface(
                            color = Color(0xFFD32F2F),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.padding(end = 8.dp),
                        ) {
                            Text(
                                text = "⚠ $nonValidatedCount",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(
                                    horizontal = 8.dp,
                                    vertical = 4.dp,
                                ),
                            )
                        }
                    }
                    if (validatedCount > 0) {
                        Surface(
                            color = Color(0xFF388E3C),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.padding(end = 8.dp),
                        ) {
                            Text(
                                text = "✓ $validatedCount",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(
                                    horizontal = 8.dp,
                                    vertical = 4.dp,
                                ),
                            )
                        }
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = savioTabUnselectedColor(),
                indicator = { positions -> SavioInterventionTabIndicator(positions, selectedTab) },
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    selectedContentColor = savioTabSelectedColor(),
                    unselectedContentColor = savioTabUnselectedColor(),
                    text = { Text("Installation") },
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    selectedContentColor = savioTabSelectedColor(),
                    unselectedContentColor = savioTabUnselectedColor(),
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text("Contrôles")
                            if (nonValidatedCount > 0) {
                                Surface(
                                    color = Color(0xFFD32F2F),
                                    shape = CircleShape,
                                ) {
                                    Text(
                                        text = "$nonValidatedCount",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        modifier = Modifier.padding(
                                            horizontal = 5.dp,
                                            vertical = 2.dp,
                                        ),
                                    )
                                }
                            }
                        }
                    },
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Conclusions") },
                )
            }

            Box(modifier = Modifier.weight(1f)) {
                when (selectedTab) {
                    0 -> AttestationInstallationTab(
                        attestation = attestation,
                        type = type,
                        evacuationMode = evacuationMode,
                        onFieldChange = { key, value ->
                            viewModel.updateField(key, value)
                            viewModel.scheduleAutoSave()
                        },
                    )
                    1 -> AttestationPointsControleTab(
                        controlPoints = viewModel.controlPoints,
                        points = points,
                        onToggle = { cle, resultat ->
                            viewModel.togglePoint(cle, resultat)
                        },
                    )
                    2 -> AttestationConclusionsTab(
                        attestation = attestation,
                        type = type,
                        onFieldChange = { key, value ->
                            viewModel.updateField(key, value)
                            viewModel.scheduleAutoSave()
                        },
                    )
                }
            }
        }
    }
}

@Composable
fun AttestationInstallationTab(
    attestation: AttestationVeEntity?,
    type: String,
    evacuationMode: String?,
    onFieldChange: (String, String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        when {
            type == "PAC_HYBRIDE_GAZ" || type == "PAC_HYBRIDE_FIOUL" -> {
                item {
                    AttestationTextField(
                        value = attestation?.appareilMesureGenerateur ?: "",
                        onValueChange = { onFieldChange("appareilMesureGenerateur", it) },
                        label = "Appareil de mesure (chaudière / fumées)",
                        singleLine = true,
                    )
                }
            }
            type != "PAC" -> {
                item {
                    AttestationTextField(
                        value = attestation?.appareilMesure ?: "",
                        onValueChange = { onFieldChange("appareilMesure", it) },
                        label = "Appareil de mesure utilisé",
                        singleLine = true,
                    )
                }
            }
        }

        item {
            AttestationSectionTitle("Mesures obligatoires")
        }

        when (type) {
            "GAZ", "PAC_HYBRIDE_GAZ" -> {
                item {
                    AttestationNumericField(
                        value = attestation?.co ?: "",
                        onValueChange = { onFieldChange("co", it) },
                        label = "CO ambiant (ppm)",
                        isNegative = false,
                    )
                }
                if (evacuationMode == "3CEP") {
                    item {
                        AttestationNumericField(
                            value = attestation?.coConduitPpm?.toString() ?: "",
                            onValueChange = { onFieldChange("coConduitPpm", it) },
                            label = "CO conduit d'amenée d'air (ppm)",
                            isNegative = false,
                        )
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AttestationNumericField(
                            modifier = Modifier.weight(1f),
                            value = attestation?.tempFumees ?: "",
                            onValueChange = { onFieldChange("tempFumees", it) },
                            label = "T° fumées (°C)",
                        )
                        AttestationNumericField(
                            modifier = Modifier.weight(1f),
                            value = attestation?.tempAmbiante ?: "",
                            onValueChange = { onFieldChange("tempAmbiante", it) },
                            label = "T° ambiante (°C)",
                        )
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AttestationNumericField(
                            modifier = Modifier.weight(1f),
                            value = attestation?.co2Fumees ?: "",
                            onValueChange = { onFieldChange("co2Fumees", it) },
                            label = "CO₂ fumées (%)",
                        )
                        AttestationNumericField(
                            modifier = Modifier.weight(1f),
                            value = attestation?.o2Fumees ?: "",
                            onValueChange = { onFieldChange("o2Fumees", it) },
                            label = "O₂ fumées (%)",
                        )
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AttestationNumericField(
                            modifier = Modifier.weight(1f),
                            value = attestation?.rendementEvalue ?: "",
                            onValueChange = { onFieldChange("rendementEvalue", it) },
                            label = "Rendement évalué (%)",
                        )
                        AttestationNumericField(
                            modifier = Modifier.weight(1f),
                            value = attestation?.noxEmissions ?: "",
                            onValueChange = { onFieldChange("noxEmissions", it) },
                            label = "NOx émissions",
                        )
                    }
                }
                item {
                    AttestationTextField(
                        value = attestation?.classeEnergetique ?: "",
                        onValueChange = { onFieldChange("classeEnergetique", it) },
                        label = "Classe énergétique évaluée",
                        singleLine = true,
                    )
                }
            }

            "BOIS" -> {
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AttestationNumericField(
                            modifier = Modifier.weight(1f),
                            value = attestation?.tempFumees ?: "",
                            onValueChange = { onFieldChange("tempFumees", it) },
                            label = "T° fumées (°C)",
                        )
                        AttestationNumericField(
                            modifier = Modifier.weight(1f),
                            value = attestation?.tempAmbiante ?: "",
                            onValueChange = { onFieldChange("tempAmbiante", it) },
                            label = "T° ambiante (°C)",
                        )
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AttestationNumericField(
                            modifier = Modifier.weight(1f),
                            value = attestation?.co2Fumees ?: "",
                            onValueChange = { onFieldChange("co2Fumees", it) },
                            label = "CO₂ fumées (%)",
                        )
                        AttestationNumericField(
                            modifier = Modifier.weight(1f),
                            value = attestation?.o2Fumees ?: "",
                            onValueChange = { onFieldChange("o2Fumees", it) },
                            label = "O₂ fumées (%)",
                        )
                    }
                }
                item {
                    AttestationNumericField(
                        value = attestation?.co ?: "",
                        onValueChange = { onFieldChange("co", it) },
                        label = "CO proximité chaudière (ppm)",
                        isNegative = false,
                    )
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AttestationNumericField(
                            modifier = Modifier.weight(1f),
                            value = attestation?.rendementEvalue ?: "",
                            onValueChange = { onFieldChange("rendementEvalue", it) },
                            label = "Rendement évalué (%)",
                        )
                        AttestationNumericField(
                            modifier = Modifier.weight(1f),
                            value = attestation?.emissionsPoussieres ?: "",
                            onValueChange = { onFieldChange("emissionsPoussieres", it) },
                            label = "Émissions poussières (mg/Nm³ à 10% O₂)",
                            isNegative = false,
                        )
                    }
                }
                item {
                    AttestationNumericField(
                        value = attestation?.emissionsCov ?: "",
                        onValueChange = { onFieldChange("emissionsCov", it) },
                        label = "Émissions COV (mg C₃H₈/Nm³ à 10% O₂)",
                        isNegative = false,
                    )
                }
            }

            "FIOUL", "PAC_HYBRIDE_FIOUL" -> {
                item {
                    AttestationNumericField(
                        value = attestation?.co ?: "",
                        onValueChange = { onFieldChange("co", it) },
                        label = "CO ambiant (ppm)",
                        isNegative = false,
                    )
                }
                if (evacuationMode == "3CEP") {
                    item {
                        AttestationNumericField(
                            value = attestation?.coConduitPpm?.toString() ?: "",
                            onValueChange = { onFieldChange("coConduitPpm", it) },
                            label = "CO conduit d'amenée d'air (ppm)",
                            isNegative = false,
                        )
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AttestationNumericField(
                            modifier = Modifier.weight(1f),
                            value = attestation?.indiceNoircissement ?: "",
                            onValueChange = { onFieldChange("indiceNoircissement", it) },
                            label = "Indice noircissement",
                            isNegative = false,
                        )
                        AttestationNumericField(
                            modifier = Modifier.weight(1f),
                            value = attestation?.pressionGicleur ?: "",
                            onValueChange = { onFieldChange("pressionGicleur", it) },
                            label = "Pression gicleur (bar)",
                            isNegative = false,
                        )
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AttestationNumericField(
                            modifier = Modifier.weight(1f),
                            value = attestation?.tempFumees ?: "",
                            onValueChange = { onFieldChange("tempFumees", it) },
                            label = "T° fumées (°C)",
                        )
                        AttestationNumericField(
                            modifier = Modifier.weight(1f),
                            value = attestation?.tempAmbiante ?: "",
                            onValueChange = { onFieldChange("tempAmbiante", it) },
                            label = "T° ambiante (°C)",
                        )
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AttestationNumericField(
                            modifier = Modifier.weight(1f),
                            value = attestation?.co2Fumees ?: "",
                            onValueChange = { onFieldChange("co2Fumees", it) },
                            label = "CO₂ fumées (%)",
                        )
                        AttestationNumericField(
                            modifier = Modifier.weight(1f),
                            value = attestation?.o2Fumees ?: "",
                            onValueChange = { onFieldChange("o2Fumees", it) },
                            label = "O₂ fumées (%)",
                        )
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AttestationNumericField(
                            modifier = Modifier.weight(1f),
                            value = attestation?.rendementEvalue ?: "",
                            onValueChange = { onFieldChange("rendementEvalue", it) },
                            label = "Rendement évalué (%)",
                        )
                        AttestationNumericField(
                            modifier = Modifier.weight(1f),
                            value = attestation?.noxEmissions ?: "",
                            onValueChange = { onFieldChange("noxEmissions", it) },
                            label = "NOx émissions",
                        )
                    }
                }
                item {
                    AttestationTextField(
                        value = attestation?.classeEnergetique ?: "",
                        onValueChange = { onFieldChange("classeEnergetique", it) },
                        label = "Classe énergétique évaluée",
                        singleLine = true,
                    )
                }
            }

            "PAC" -> {
                item {
                    AttestationSectionTitle("Températures — Mode chauffage")
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AttestationNumericField(
                            modifier = Modifier.weight(1f),
                            value = attestation?.tExterieurChauf ?: "",
                            onValueChange = { onFieldChange("tExterieurChauf", it) },
                            label = "T° ext. chauffage (°C)",
                            isNegative = true,
                        )
                        AttestationNumericField(
                            modifier = Modifier.weight(1f),
                            value = attestation?.tInterieurChauf ?: "",
                            onValueChange = { onFieldChange("tInterieurChauf", it) },
                            label = "T° int. chauffage (°C)",
                            isNegative = true,
                        )
                    }
                }
                item {
                    AttestationSectionTitle("Températures — Mode refroidissement")
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AttestationNumericField(
                            modifier = Modifier.weight(1f),
                            value = attestation?.tExterieurRefroid ?: "",
                            onValueChange = { onFieldChange("tExterieurRefroid", it) },
                            label = "T° ext. refroid. (°C)",
                            isNegative = true,
                        )
                        AttestationNumericField(
                            modifier = Modifier.weight(1f),
                            value = attestation?.tInterieurRefroid ?: "",
                            onValueChange = { onFieldChange("tInterieurRefroid", it) },
                            label = "T° int. refroid. (°C)",
                            isNegative = true,
                        )
                    }
                }
                item {
                    AttestationTextField(
                        value = attestation?.appareilMesure ?: "",
                        onValueChange = { onFieldChange("appareilMesure", it) },
                        label = "Appareil mesure températures",
                        singleLine = true,
                    )
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AttestationNumericField(
                            modifier = Modifier.weight(1f),
                            value = attestation?.tensionStatique ?: "",
                            onValueChange = { onFieldChange("tensionStatique", it) },
                            label = "Tension statique (V)",
                            isNegative = false,
                        )
                        AttestationNumericField(
                            modifier = Modifier.weight(1f),
                            value = attestation?.tensionDynamique ?: "",
                            onValueChange = { onFieldChange("tensionDynamique", it) },
                            label = "Tension dynamique (V)",
                            isNegative = false,
                        )
                    }
                }
                item {
                    AttestationTextField(
                        value = attestation?.appareilMesureTension ?: "",
                        onValueChange = { onFieldChange("appareilMesureTension", it) },
                        label = "Appareil mesure tensions",
                        singleLine = true,
                    )
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AttestationTextField(
                            modifier = Modifier.weight(1f),
                            value = attestation?.fluideRef ?: "",
                            onValueChange = { onFieldChange("fluideRef", it) },
                            label = "Fluide frigorigène",
                            singleLine = true,
                        )
                        AttestationNumericField(
                            modifier = Modifier.weight(1f),
                            value = attestation?.chargeTotale ?: "",
                            onValueChange = { onFieldChange("chargeTotale", it) },
                            label = "Charge totale (kg)",
                            isNegative = false,
                        )
                    }
                }
                item {
                    AttestationSectionTitle("Étanchéité")
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AttestationNumericField(
                            modifier = Modifier.weight(1f),
                            value = attestation?.pressionBp ?: "",
                            onValueChange = { onFieldChange("pressionBp", it) },
                            label = "Pression relevée (bar)",
                            isNegative = false,
                        )
                        AttestationNumericField(
                            modifier = Modifier.weight(1f),
                            value = attestation?.pressionHp ?: "",
                            onValueChange = { onFieldChange("pressionHp", it) },
                            label = "Pression HP (bar)",
                            isNegative = false,
                        )
                    }
                }
            }
        }

        if (type == "PAC_HYBRIDE_GAZ" || type == "PAC_HYBRIDE_FIOUL") {
            item {
                AttestationSectionTitle("Mesures PAC")
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AttestationNumericField(
                        modifier = Modifier.weight(1f),
                        value = attestation?.tExterieurChauf ?: "",
                        onValueChange = { onFieldChange("tExterieurChauf", it) },
                        label = "T° ext. chauffage (°C)",
                        isNegative = true,
                    )
                    AttestationNumericField(
                        modifier = Modifier.weight(1f),
                        value = attestation?.tInterieurChauf ?: "",
                        onValueChange = { onFieldChange("tInterieurChauf", it) },
                        label = "T° int. chauffage (°C)",
                        isNegative = true,
                    )
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AttestationNumericField(
                        modifier = Modifier.weight(1f),
                        value = attestation?.tExterieurRefroid ?: "",
                        onValueChange = { onFieldChange("tExterieurRefroid", it) },
                        label = "T° ext. refroid. (°C)",
                        isNegative = true,
                    )
                    AttestationNumericField(
                        modifier = Modifier.weight(1f),
                        value = attestation?.tInterieurRefroid ?: "",
                        onValueChange = { onFieldChange("tInterieurRefroid", it) },
                        label = "T° int. refroid. (°C)",
                        isNegative = true,
                    )
                }
            }
            item {
                AttestationTextField(
                    value = attestation?.appareilMesure ?: "",
                    onValueChange = { onFieldChange("appareilMesure", it) },
                    label = "Appareil mesure températures",
                    singleLine = true,
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AttestationNumericField(
                        modifier = Modifier.weight(1f),
                        value = attestation?.tensionStatique ?: "",
                        onValueChange = { onFieldChange("tensionStatique", it) },
                        label = "Tension statique (V)",
                        isNegative = false,
                    )
                    AttestationNumericField(
                        modifier = Modifier.weight(1f),
                        value = attestation?.tensionDynamique ?: "",
                        onValueChange = { onFieldChange("tensionDynamique", it) },
                        label = "Tension dynamique (V)",
                        isNegative = false,
                    )
                }
            }
            item {
                AttestationTextField(
                    value = attestation?.appareilMesureTension ?: "",
                    onValueChange = { onFieldChange("appareilMesureTension", it) },
                    label = "Appareil mesure tensions",
                    singleLine = true,
                )
            }
            item {
                AttestationSectionTitle("Étanchéité")
            }
            item {
                AttestationNumericField(
                    value = attestation?.pressionBp ?: "",
                    onValueChange = { onFieldChange("pressionBp", it) },
                    label = "Pression relevée (bar)",
                    isNegative = false,
                )
            }
        }

        item {
            AttestationSectionTitle("Remarques")
        }
        item {
            AttestationTextField(
                value = attestation?.remarquesHydraulique ?: "",
                onValueChange = { onFieldChange("remarquesHydraulique", it) },
                label = "Remarques réseau hydraulique",
                minLines = 3,
            )
        }
        item {
            AttestationTextField(
                value = attestation?.remarquesRegulation ?: "",
                onValueChange = { onFieldChange("remarquesRegulation", it) },
                label = "Remarques régulation",
                minLines = 3,
            )
        }
        item {
            AttestationTextField(
                value = attestation?.remarquesGenerateur ?: "",
                onValueChange = { onFieldChange("remarquesGenerateur", it) },
                label = "Remarques générateur",
                minLines = 3,
            )
        }
    }
}

@Composable
fun AttestationPointsControleTab(
    controlPoints: List<AttestationVeControlPoints.ControlPoint>,
    points: Map<String, String>,
    onToggle: (String, String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        items(controlPoints, key = { it.cle }) { point ->
            when (point.type) {
                AttestationVeControlPoints.LineType.HEAD -> {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = point.description,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                        )
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.primary,
                            thickness = 1.dp,
                        )
                    }
                }

                AttestationVeControlPoints.LineType.SUBHEAD -> {
                    Text(
                        text = point.description,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                top = 8.dp,
                                bottom = 4.dp,
                                start = 8.dp,
                            ),
                    )
                }

                AttestationVeControlPoints.LineType.BODY -> {
                    val resultat = points[point.cle] ?: ""
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = when (resultat) {
                                "V" -> Color(0xFFE8F5E9)
                                "N" -> Color(0xFFFFEBEE)
                                "S" -> Color(0xFFF5F5F5)
                                else -> MaterialTheme.colorScheme.surfaceContainerLow
                            },
                        ),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Column(
                            modifier = Modifier.padding(
                                horizontal = 12.dp,
                                vertical = 8.dp,
                            ),
                        ) {
                            Text(
                                text = point.description,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.fillMaxWidth(),
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                AttestationChip(
                                    label = "✓ Validé",
                                    selected = resultat == "V",
                                    selectedColor = Color(0xFF388E3C),
                                    onClick = { onToggle(point.cle, "V") },
                                )
                                if (point.hasSansObjet) {
                                    AttestationChip(
                                        label = "○ S/O",
                                        selected = resultat == "S",
                                        selectedColor = Color(0xFF757575),
                                        onClick = { onToggle(point.cle, "S") },
                                    )
                                }
                                AttestationChip(
                                    label = "✗ Non validé",
                                    selected = resultat == "N",
                                    selectedColor = Color(0xFFD32F2F),
                                    onClick = { onToggle(point.cle, "N") },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AttestationConclusionsTab(
    attestation: AttestationVeEntity?,
    @Suppress("UNUSED_PARAMETER") type: String,
    onFieldChange: (String, String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            AttestationTextField(
                value = attestation?.defautsCorriges ?: "",
                onValueChange = { onFieldChange("defautsCorriges", it) },
                label = "Défauts constatés et actions correctives",
                minLines = 4,
            )
        }
        item {
            AttestationSectionTitle("Conseils et recommandations")
        }
        item {
            AttestationTextField(
                value = attestation?.recommandationUsage ?: "",
                onValueChange = { onFieldChange("recommandationUsage", it) },
                label = "Bon usage de l'équipement",
                minLines = 3,
            )
        }
        item {
            AttestationTextField(
                value = attestation?.recommandationAmeliorations ?: "",
                onValueChange = { onFieldChange("recommandationAmeliorations", it) },
                label = "Améliorations possibles de l'installation",
                minLines = 3,
            )
        }
        item {
            AttestationTextField(
                value = attestation?.recommandationRemplacement ?: "",
                onValueChange = { onFieldChange("recommandationRemplacement", it) },
                label = "Intérêt éventuel du remplacement",
                minLines = 3,
            )
        }
        item {
            AttestationSectionTitle("Informations complémentaires")
        }
        item {
            AttestationTextField(
                value = attestation?.commentaire ?: "",
                onValueChange = { onFieldChange("commentaire", it) },
                label = "Commentaire",
                minLines = 3,
            )
        }
        item {
            AttestationTextField(
                value = attestation?.nomPersonnePresente ?: "",
                onValueChange = { onFieldChange("nomPersonnePresente", it) },
                label = "Nom de la personne présente lors de l'entretien",
                singleLine = true,
            )
        }
    }
}

@Composable
fun AttestationSectionTitle(
    title: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(vertical = 4.dp),
    )
}

@Composable
fun AttestationChip(
    label: String,
    selected: Boolean,
    selectedColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val border = if (!selected) {
        BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline)
    } else {
        null
    }
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = if (selected) {
            selectedColor
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        border = border,
    ) {
        Text(
            text = label,
            color = if (selected) {
                Color.White
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            fontSize = 12.sp,
            fontWeight = if (selected) {
                FontWeight.SemiBold
            } else {
                FontWeight.Normal
            },
            modifier = Modifier.padding(
                horizontal = 10.dp,
                vertical = 5.dp,
            ),
        )
    }
}

@Composable
fun AttestationTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = false,
    minLines: Int = 1,
) {
    var textFieldValue by remember { mutableStateOf(TextFieldValue(value)) }
    LaunchedEffect(value) {
        if (value != textFieldValue.text) {
            textFieldValue = textFieldValue.copy(text = value)
        }
    }
    TextField(
        value = textFieldValue,
        onValueChange = { new ->
            textFieldValue = new
            onValueChange(new.text)
        },
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
        },
        modifier = modifier.fillMaxWidth(),
        singleLine = singleLine,
        minLines = minLines,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            unfocusedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(
                alpha = 0.2f,
            ),
        ),
        keyboardOptions = KeyboardOptions(
            imeAction = if (singleLine) ImeAction.Next else ImeAction.Default,
        ),
    )
}

@Composable
fun AttestationNumericField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isNegative: Boolean = true,
) {
    val focusManager = LocalFocusManager.current
    TextField(
        value = value,
        onValueChange = { new ->
            val s = new.replace(",", ".")
            if (!isNegative && s.startsWith("-")) {
                return@TextField
            }
            if (!s.matches(Regex("^-?\\d*(\\.\\d{0,2})?$"))) {
                return@TextField
            }
            onValueChange(s)
        },
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
        },
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            unfocusedContainerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(
                alpha = 0.4f,
            ),
        ),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal,
            imeAction = ImeAction.Next,
        ),
        keyboardActions = KeyboardActions(
            onNext = { focusManager.moveFocus(FocusDirection.Next) },
        ),
    )
}
