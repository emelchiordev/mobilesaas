package re.melchior.saviomobile.ui.screen.intervention.measure

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import re.melchior.saviomobile.ui.theme.SavioInterventionTabIndicator
import re.melchior.saviomobile.ui.theme.savioTabSelectedColor
import re.melchior.saviomobile.ui.theme.savioTabUnselectedColor
import re.melchior.saviomobile.ui.theme.savioTopAppBarColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeasureScreen(
    onBack: () -> Unit,
    windowSizeClass: WindowSizeClass,
    viewModel: MeasureViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) }

    fun leave() {
        viewModel.saveIfChanged()
        onBack()
    }

    BackHandler { leave() }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Mesures") },
                navigationIcon = {
                    IconButton(onClick = ::leave) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour",
                        )
                    }
                },
                colors = savioTopAppBarColors(),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 0.dp,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = savioTabUnselectedColor(),
                indicator = { positions -> SavioInterventionTabIndicator(positions, selectedTab) },
            ) {
                val tabs = listOf(
                    "Essentiel",
                    "Combustion",
                    "Gaz",
                    "Eau",
                    "Fuel",
                    "Contrôle",
                    "Divers",
                    "Observation",
                )
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        selectedContentColor = savioTabSelectedColor(),
                        unselectedContentColor = savioTabUnselectedColor(),
                        text = {
                            Text(
                                title,
                                style = MaterialTheme.typography.labelMedium,
                            )
                        },
                    )
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                when (selectedTab) {
                    0 -> MeasureEssentielTab(uiState, viewModel::updateField, windowSizeClass)
                    1 -> MeasureCombustionTab(uiState, viewModel::updateField, windowSizeClass)
                    2 -> MeasureGazTab(uiState, viewModel::updateField, windowSizeClass)
                    3 -> MeasureEauTab(uiState, viewModel::updateField, windowSizeClass)
                    4 -> MeasureFuelTab(uiState, viewModel::updateField, windowSizeClass)
                    5 -> MeasureControleTab(uiState, viewModel::updateField, windowSizeClass)
                    6 -> MeasureDiversTab(uiState, viewModel::updateField, windowSizeClass)
                    7 -> MeasureObservationTab(uiState, viewModel::updateField)
                }
            }
        }
    }
}
