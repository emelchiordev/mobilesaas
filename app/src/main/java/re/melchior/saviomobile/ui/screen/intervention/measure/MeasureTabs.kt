package re.melchior.saviomobile.ui.screen.intervention.measure

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass

@Composable
fun MeasureGrid(
    windowSizeClass: WindowSizeClass,
    content: LazyGridScope.() -> Unit,
) {
    val columns = when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Expanded,
        WindowWidthSizeClass.Medium,
        -> 2
        else -> 1
    }
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        content = content,
    )
}

@Composable
fun MeasureEssentielTab(
    state: MeasureUiState,
    onUpdate: (String, String) -> Unit,
    windowSizeClass: WindowSizeClass,
) {
    MeasureGrid(windowSizeClass) {
        item {
            MeasureNumericField(
                value = state.co,
                onValueChange = { onUpdate("co", it) },
                label = "CO Fumée",
                isWarning = true,
            )
        }
        item {
            MeasureNumericField(
                value = state.coamb,
                onValueChange = { onUpdate("coamb", it) },
                label = "CO Ambiant",
                isWarning = true,
            )
        }
        item {
            MeasureNumericField(
                value = state.co2,
                onValueChange = { onUpdate("co2", it) },
                label = "CO2",
                isWarning = true,
            )
        }
        item {
            MeasureNumericField(
                value = state.o2,
                onValueChange = { onUpdate("o2", it) },
                label = "O2",
                isWarning = true,
            )
        }
        item {
            MeasureNumericField(
                value = state.tair,
                onValueChange = { onUpdate("tair", it) },
                label = "Température air",
                isWarning = true,
            )
        }
        item {
            MeasureNumericField(
                value = state.temfu,
                onValueChange = { onUpdate("temfu", it) },
                label = "Température fumées",
                isWarning = true,
            )
        }
        item {
            MeasureNumericField(
                value = state.rend,
                onValueChange = { onUpdate("rend", it) },
                label = "Rendement",
                isWarning = true,
            )
        }
        item {
            MeasureNumericField(
                value = state.nox,
                onValueChange = { onUpdate("nox", it) },
                label = "NOx",
                isWarning = true,
            )
        }
        item {
            MeasureNumericField(
                value = state.eta,
                onValueChange = { onUpdate("eta", it) },
                label = "Excès air",
                isWarning = true,
            )
        }
    }
}

@Composable
fun MeasureCombustionTab(
    state: MeasureUiState,
    onUpdate: (String, String) -> Unit,
    windowSizeClass: WindowSizeClass,
) {
    MeasureGrid(windowSizeClass) {
        item {
            MeasureNumericField(
                value = state.thpa,
                onValueChange = { onUpdate("thpa", it) },
                label = "Tirage HPA",
            )
        }
        item {
            MeasureNumericField(
                value = state.no,
                onValueChange = { onUpdate("no", it) },
                label = "Monoxyde Azote",
            )
        }
        item {
            MeasureNumericField(
                value = state.no2,
                onValueChange = { onUpdate("no2", it) },
                label = "Dioxyde Azote",
            )
        }
        item {
            MeasureNumericField(
                value = state.o2ven,
                onValueChange = { onUpdate("o2ven", it) },
                label = "O2 Ventouse",
            )
        }
        item {
            MeasureNumericField(
                value = state.condilu,
                onValueChange = { onUpdate("condilu", it) },
                label = "CO non dilué",
            )
        }
        item {
            MeasureNumericField(
                value = state.tgaz,
                onValueChange = { onUpdate("tgaz", it) },
                label = "Température gaz",
            )
        }
        item {
            MeasureNumericField(
                value = state.ta,
                onValueChange = { onUpdate("ta", it) },
                label = "Température air comburant",
            )
        }
    }
}

@Composable
fun MeasureGazTab(
    state: MeasureUiState,
    onUpdate: (String, String) -> Unit,
    windowSizeClass: WindowSizeClass,
) {
    MeasureGrid(windowSizeClass) {
        item {
            MeasureNumericField(
                value = state.pregas,
                onValueChange = { onUpdate("pregas", it) },
                label = "Pression gaz amont statique",
            )
        }
        item {
            MeasureNumericField(
                value = state.prega,
                onValueChange = { onUpdate("prega", it) },
                label = "Pression gaz amont dynamique",
            )
        }
        item {
            MeasureNumericField(
                value = state.pregn,
                onValueChange = { onUpdate("pregn", it) },
                label = "Pression gaz mini",
            )
        }
        item {
            MeasureNumericField(
                value = state.pregm,
                onValueChange = { onUpdate("pregm", it) },
                label = "Pression gaz maxi",
            )
        }
        item {
            MeasureNumericField(
                value = state.puisgaz,
                onValueChange = { onUpdate("puisgaz", it) },
                label = "Puissance gaz KW",
            )
        }
        item {
            MeasureNumericField(
                value = state.debga,
                onValueChange = { onUpdate("debga", it) },
                label = "Débit gaz",
            )
        }
    }
}

@Composable
fun MeasureEauTab(
    state: MeasureUiState,
    onUpdate: (String, String) -> Unit,
    windowSizeClass: WindowSizeClass,
) {
    MeasureGrid(windowSizeClass) {
        item {
            MeasureNumericField(
                value = state.temec,
                onValueChange = { onUpdate("temec", it) },
                label = "Température eau chaude",
            )
        }
        item {
            MeasureNumericField(
                value = state.temef,
                onValueChange = { onUpdate("temef", it) },
                label = "Température eau froide",
            )
        }
        item {
            MeasureNumericField(
                value = state.delta,
                onValueChange = { onUpdate("delta", it) },
                label = "Delta T",
            )
        }
        item {
            MeasureNumericField(
                value = state.debio,
                onValueChange = { onUpdate("debio", it) },
                label = "Débit eau",
            )
        }
    }
}

@Composable
fun MeasureFuelTab(
    state: MeasureUiState,
    onUpdate: (String, String) -> Unit,
    windowSizeClass: WindowSizeClass,
) {
    MeasureGrid(windowSizeClass) {
        item {
            MeasureNumericField(
                value = state.debfuel,
                onValueChange = { onUpdate("debfuel", it) },
                label = "Débit fuel Kg/h",
            )
        }
        item {
            MeasureNumericField(
                value = state.prefp,
                onValueChange = { onUpdate("prefp", it) },
                label = "Pression fuel pompe",
            )
        }
        item {
            MeasureNumericField(
                value = state.puisfuel,
                onValueChange = { onUpdate("puisfuel", it) },
                label = "Puissance fuel KW",
            )
        }
        item {
            MeasureNumericField(
                value = state.pulve,
                onValueChange = { onUpdate("pulve", it) },
                label = "Pression pulvérisateur",
            )
        }
    }
}

@Composable
fun MeasureControleTab(
    state: MeasureUiState,
    onUpdate: (String, String) -> Unit,
    windowSizeClass: WindowSizeClass,
) {
    MeasureGrid(windowSizeClass) {
        item {
            MeasureNumericField(
                value = state.spot,
                onValueChange = { onUpdate("spot", it) },
                label = "Temps sécu. SPOT (s)",
            )
        }
        item {
            MeasureDropdownField(
                value = state.testdsc,
                onValueChange = { onUpdate("testdsc", it) },
                label = "Test déclenchement DSC",
                options = listOf("", "Conforme", "Non Conforme", "Inexistante"),
            )
        }
        item {
            MeasureDropdownField(
                value = state.remplacond,
                onValueChange = { onUpdate("remplacond", it) },
                label = "Remplacement condensateur",
                options = listOf("", "Oui", "Non"),
            )
        }
        item {
            MeasureDropdownField(
                value = state.templagigleur,
                onValueChange = { onUpdate("templagigleur", it) },
                label = "Remplacement gicleur",
                options = listOf("", "Oui", "Non"),
            )
        }
        item {
            MeasureDropdownField(
                value = state.remplapoly,
                onValueChange = { onUpdate("remplapoly", it) },
                label = "Remplacement cartouche polyphosphates",
                options = listOf("", "Oui", "Non"),
            )
        }
        item {
            MeasureDropdownField(
                value = state.etaventil,
                onValueChange = { onUpdate("etaventil", it) },
                label = "État ventilation",
                options = listOf("", "Libre", "A dégager", "Absente"),
            )
        }
        item {
            MeasureDropdownField(
                value = state.ctranode,
                onValueChange = { onUpdate("ctranode", it) },
                label = "Contrôle anode",
                options = listOf("", "Oui", "Non"),
            )
        }
        item {
            MeasureDropdownField(
                value = state.ctrextvmc,
                onValueChange = { onUpdate("ctrextvmc", it) },
                label = "Contrôle extracteur VMC",
                options = listOf("", "Oui", "Non"),
            )
        }
    }
}

@Composable
fun MeasureDiversTab(
    state: MeasureUiState,
    onUpdate: (String, String) -> Unit,
    windowSizeClass: WindowSizeClass,
) {
    MeasureGrid(windowSizeClass) {
        item {
            MeasureNumericField(
                value = state.suie1,
                onValueChange = { onUpdate("suie1", it) },
                label = "Suie 1",
            )
        }
        item {
            MeasureNumericField(
                value = state.suie2,
                onValueChange = { onUpdate("suie2", it) },
                label = "Suie 2",
            )
        }
        item {
            MeasureNumericField(
                value = state.suie3,
                onValueChange = { onUpdate("suie3", it) },
                label = "Suie 3",
            )
        }
        item {
            MeasureNumericField(
                value = state.residhuil,
                onValueChange = { onUpdate("residhuil", it) },
                label = "Résidus huileux",
            )
        }
        item {
            MeasureNumericField(
                value = state.opaci,
                onValueChange = { onUpdate("opaci", it) },
                label = "Opacité",
            )
        }
        item {
            MeasureNumericField(
                value = state.ionis,
                onValueChange = { onUpdate("ionis", it) },
                label = "Ionisation / Thermocouple",
            )
        }
        item {
            MeasureNumericField(
                value = state.pgevg,
                onValueChange = { onUpdate("pgevg", it) },
                label = "Pression expansion avant gonflage",
            )
        }
        item {
            MeasureNumericField(
                value = state.pgepg,
                onValueChange = { onUpdate("pgepg", it) },
                label = "Pression expansion après gonflage",
            )
        }
        item {
            MeasureNumericField(
                value = state.depre,
                onValueChange = { onUpdate("depre", it) },
                label = "Dépression cheminée froid",
            )
        }
        item {
            MeasureNumericField(
                value = state.depr2,
                onValueChange = { onUpdate("depr2", it) },
                label = "Dépression cheminée chaud",
            )
        }
        item {
            MeasureNumericField(
                value = state.gican,
                onValueChange = { onUpdate("gican", it) },
                label = "Gicleur angle",
            )
        }
        item {
            MeasureNumericField(
                value = state.gicle,
                onValueChange = { onUpdate("gicle", it) },
                label = "Gicleur galon",
            )
        }
        item {
            MeasureNumericField(
                value = state.pabs,
                onValueChange = { onUpdate("pabs", it) },
                label = "Pression absolue",
            )
        }
        item {
            MeasureNumericField(
                value = state.perte,
                onValueChange = { onUpdate("perte", it) },
                label = "Perte",
            )
        }
        item {
            MeasureNumericField(
                value = state.ppm,
                onValueChange = { onUpdate("ppm", it) },
                label = "PPM",
            )
        }
    }
}

@Composable
fun MeasureObservationTab(
    state: MeasureUiState,
    onUpdate: (String, String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
    ) {
        OutlinedTextField(
            value = state.obser,
            onValueChange = { onUpdate("obser", it) },
            label = { Text("Observation") },
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.4f),
            maxLines = 10,
            minLines = 5,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Default,
            ),
            textStyle = MaterialTheme.typography.bodyMedium,
        )
    }
}
