package re.melchior.saviomobile.ui.screen.intervention

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import re.melchior.saviomobile.data.local.entity.InterventionEntity
import re.melchior.saviomobile.ui.refonte.SavioInterventionTabBar
import re.melchior.saviomobile.ui.refonte.SavioInterventionTabItem
import re.melchior.saviomobile.ui.refonte.SavioNavyStatusBarEffect
import re.melchior.saviomobile.ui.screen.tournee.displayTypeLabel
import re.melchior.saviomobile.ui.theme.SavioRefonte
import re.melchior.saviomobile.ui.theme.useSavioRefonteUi

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InterventionActiveScreen(
    onQuit: () -> Unit,
    onCloture: (String) -> Unit,
    onEquipementClick: (interventionId: String, equipmentId: String) -> Unit,
    onClientClick: (String) -> Unit,
    onOpenCamera: (unitId: String, customerId: String) -> Unit,
    onFactureClick: (interventionId: String) -> Unit,
    onAddEquipment: (interventionId: String, unitId: String, parentEquipmentId: String?) -> Unit,
    onReplaceEquipment: (interventionId: String, unitId: String, existingEquipmentId: String) -> Unit,
    viewModel: InterventionActiveViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val newEquipmentIds by viewModel.newEquipmentIds.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.navigateToInvoice.collect { id ->
            onFactureClick(id)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.navigateBackToPlanning.collect {
            onQuit()
        }
    }

    val inProgress = uiState.intervention?.status == "in_progress"
    BackHandler(enabled = inProgress) {
        // Bloqué intentionnellement tant que l'intervention est en cours
    }

    if (uiState.showQuitDialog) {
        QuitInterventionDialog(
            onConfirm = viewModel::onQuitConfirmed,
            onDismiss = viewModel::onQuitDismissed,
        )
    }

    var selectedTab by rememberSaveable { mutableStateOf(InterventionTab.DETAIL) }
    val refonte = useSavioRefonteUi()
    if (refonte) {
        SavioNavyStatusBarEffect()
    }
    val interventionTabs =
        remember {
            InterventionTab.entries.map { tab ->
                SavioInterventionTabItem(
                    label = tab.label,
                    icon = tab.icon,
                    selectedIcon = tab.selectedIcon,
                )
            }
        }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor =
            if (refonte) SavioRefonte.BgPage else MaterialTheme.colorScheme.background,
        bottomBar = {
            SavioInterventionTabBar(
                tabs = interventionTabs,
                selectedIndex = selectedTab.ordinal,
                onSelect = { index -> selectedTab = InterventionTab.entries[index] },
            )
        },
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding),
        ) {
            uiState.intervention?.let { intervention ->
                InterventionActiveTopBar(
                    intervention = intervention,
                    onCloseClick = viewModel::onCloseClick,
                    onClotureClick = { onCloture(intervention.id) },
                )
            } ?: run {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(16.dp),
                )
            }

            Box(
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(
                            if (refonte) SavioRefonte.BgPage else MaterialTheme.colorScheme.background,
                        ),
            ) {
                when (selectedTab) {
                    InterventionTab.DETAIL ->
                        InterventionDetailTab(
                            uiState = uiState,
                            onClientClick = onClientClick,
                            elapsedLabel = uiState.elapsedSeconds.toElapsedLabel(),
                            startTimeLabel = uiState.startTimeLabel,
                        )

                    InterventionTab.EQUIPEMENTS ->
                        InterventionEquipementsTab(
                            uiState = uiState,
                            newEquipmentIds = newEquipmentIds,
                            onEquipementClick = onEquipementClick,
                            onAddEquipment = onAddEquipment,
                        )

                    InterventionTab.PHOTOS ->
                        InterventionPhotosTab(
                            uiState = uiState,
                            onOpenCamera = onOpenCamera,
                        )

                    InterventionTab.FACTURE ->
                        InterventionFactureTab(
                            uiState = uiState,
                            onFactureClick = onFactureClick,
                            viewModel = viewModel,
                        )
                }
            }
        }
    }
}

@Composable
private fun InterventionActiveTopBar(
    intervention: InterventionEntity,
    onCloseClick: () -> Unit,
    onClotureClick: () -> Unit,
) {
    val refonte = useSavioRefonteUi()
    Box(
        Modifier
            .fillMaxWidth()
            .background(if (refonte) SavioRefonte.Navy else MaterialTheme.colorScheme.primary),
    ) {
        Column(Modifier.statusBarsPadding()) {
        val onBar = Color.White
        Row(
            Modifier
                .fillMaxWidth()
                .padding(
                    start = if (refonte) 16.dp else 16.dp,
                    end = if (refonte) 16.dp else 16.dp,
                    top = if (refonte) 6.dp else 14.dp,
                    bottom = if (refonte) 16.dp else 14.dp,
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(9.dp),
            ) {
                if (refonte) {
                    Icon(
                        imageVector = Icons.Filled.Build,
                        contentDescription = null,
                        tint = onBar,
                        modifier = Modifier.size(22.dp),
                    )
                }
                Text(
                    text = intervention.displayTypeLabel(),
                    color = onBar,
                    fontSize = if (refonte) 23.sp else 20.sp,
                    fontWeight = if (refonte) FontWeight.Bold else FontWeight.Medium,
                    letterSpacing = if (refonte) (-0.01).sp else 0.sp,
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    onClick = onClotureClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor =
                            if (refonte) Color.White else MaterialTheme.colorScheme.onPrimary,
                        contentColor =
                            if (refonte) SavioRefonte.Navy else MaterialTheme.colorScheme.primary,
                    ),
                    modifier = Modifier.clip(RoundedCornerShape(999.dp)),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 9.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                ) {
                    Text(
                        "Clôturer",
                        fontSize = if (refonte) 15.sp else 12.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
                IconButton(onClick = onCloseClick) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Quitter l'intervention",
                        tint = onBar,
                        modifier = Modifier.size(if (refonte) 24.dp else 20.dp),
                    )
                }
            }
        }
        if (intervention.syncStatus == "CONFLICT") {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                color = MaterialTheme.colorScheme.errorContainer,
                shape = RoundedCornerShape(4.dp),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        Icons.Filled.Warning,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                    )
                    Text(
                        "Conflit de synchronisation",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
        }
    }
}
