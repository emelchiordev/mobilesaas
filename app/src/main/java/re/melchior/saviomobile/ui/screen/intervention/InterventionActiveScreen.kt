package re.melchior.saviomobile.ui.screen.intervention

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.adaptive.navigationsuite.ExperimentalMaterial3AdaptiveNavigationSuiteApi
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import kotlinx.coroutines.flow.collect
import re.melchior.saviomobile.data.local.entity.InterventionEntity
import re.melchior.saviomobile.ui.screen.tournee.displayTypeLabel
import re.melchior.saviomobile.ui.theme.SavioUi
import re.melchior.saviomobile.ui.theme.savioTabSelectedColor
import re.melchior.saviomobile.ui.theme.savioTabUnselectedColor
@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalMaterial3AdaptiveNavigationSuiteApi::class,
)
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

    NavigationSuiteScaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        navigationSuiteColors =
            NavigationSuiteDefaults.colors(
                navigationBarContainerColor = MaterialTheme.colorScheme.background,
                navigationBarContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
        navigationSuiteItems = {
            InterventionTab.entries.forEach { tab ->
                item(
                    icon = {
                        Icon(
                            imageVector =
                                if (selectedTab == tab) tab.selectedIcon else tab.icon,
                            contentDescription = tab.label,
                            tint =
                                if (selectedTab == tab) {
                                    savioTabSelectedColor()
                                } else {
                                    savioTabUnselectedColor()
                                },
                            modifier = Modifier.size(24.dp),
                        )
                    },
                    label = {
                        Text(
                            text = tab.label,
                            fontSize = 10.sp,
                            fontWeight =
                                if (selectedTab == tab) FontWeight.Bold else FontWeight.Medium,
                            color =
                                if (selectedTab == tab) {
                                    savioTabSelectedColor()
                                } else {
                                    savioTabUnselectedColor()
                                },
                        )
                    },
                    selected = selectedTab == tab,
                    onClick = { selectedTab = tab },
                )
            }
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding(),
        ) {
            uiState.intervention?.let { intervention ->
                InterventionActiveTopBar(
                    startTimeLabel = uiState.startTimeLabel,
                    elapsedLabel = uiState.elapsedSeconds.toElapsedLabel(),
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
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background),
            ) {
                when (selectedTab) {
                    InterventionTab.DETAIL ->
                        InterventionDetailTab(
                            uiState = uiState,
                            onClientClick = onClientClick,
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
    startTimeLabel: String,
    elapsedLabel: String,
    intervention: InterventionEntity,
    onCloseClick: () -> Unit,
    onClotureClick: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
            .windowInsetsPadding(WindowInsets.statusBars),
    ) {
        val onBar = MaterialTheme.colorScheme.onPrimary
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = intervention.displayTypeLabel(),
                    color = onBar,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = "Début $startTimeLabel · $elapsedLabel",
                    color = onBar.copy(alpha = 0.65f),
                    fontSize = 11.sp,
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Button(
                    onClick = onClotureClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onPrimary,
                        contentColor = MaterialTheme.colorScheme.primary,
                    ),
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .padding(end = 4.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                ) {
                    Text(
                        "Clôturer",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
                IconButton(onClick = onCloseClick) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Quitter l'intervention",
                        tint = onBar.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp),
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

