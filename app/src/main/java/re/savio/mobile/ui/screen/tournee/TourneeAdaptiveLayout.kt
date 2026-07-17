package re.savio.mobile.ui.screen.tournee

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import re.savio.mobile.ui.refonte.SavioDateNavigator
import re.savio.mobile.ui.theme.SavioRefonte
import re.savio.mobile.ui.theme.useSavioRefonteUi
import java.time.LocalDate
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import re.savio.mobile.data.local.entity.PendingInterventionEntity
import re.savio.mobile.ui.component.SavioOfflineBannerSurface
import re.savio.mobile.ui.navigation.Screen

/**
 * Master-detail (tablette / fenêtre élargie uniquement). Le mode compact utilise [TourneeScreen] tel quel.
 */
@Composable
fun TourneeAdaptiveLayout(
    modifier: Modifier = Modifier.fillMaxSize(),
    interventions: List<InterventionItem>,
    followUpItems: List<InterventionItem> = emptyList(),
    pendingCreating: List<PendingInterventionEntity> = emptyList(),
    onPendingCreatingClick: () -> Unit = {},
    detailNavController: NavHostController,
    onStartIntervention: (String) -> Unit,
    onClientClick: (customerId: String, contextInterventionId: String) -> Unit = { _, _ -> },
    onNavigateToDevisSignature: (String) -> Unit = {},
    currentDateLabel: String,
    selectedDate: LocalDate = LocalDate.now(),
    dateSubtitle: String? = null,
    onPreviousDay: () -> Unit = {},
    onNextDay: () -> Unit = {},
    pendingSyncCount: Int,
    pendingOfflineInterventionCount: Int = 0,
    onPendingOfflineList: (() -> Unit)? = null,
    onSyncCatalog: () -> Unit,
    isCatalogSyncing: Boolean = false,
    onRefresh: () -> Unit,
    isRefreshing: Boolean,
    isNetworkOnline: Boolean = true,
    onLogout: (() -> Unit)? = null,
) {
    val navEntry by detailNavController.currentBackStackEntryAsState()
    val selectedId = navEntry?.arguments?.getString("interventionId")
    val selectedIntervention = selectedId?.let { id ->
        interventions.find { it.id == id } ?: followUpItems.find { it.id == id }
    }
    val remainingCount = interventions.count { !it.isCompleted }

    Column(modifier.fillMaxSize()) {
        SavioTabletTopBar(
            selectedIntervention = selectedIntervention,
            currentDate = currentDateLabel,
            remainingCount = remainingCount,
            pendingSyncCount = pendingSyncCount,
            pendingOfflineInterventionCount = pendingOfflineInterventionCount,
            onPendingOfflineClick = onPendingOfflineList,
            onSyncCatalog = onSyncCatalog,
            isCatalogSyncing = isCatalogSyncing,
            onRefresh = onRefresh,
            isRefreshing = isRefreshing,
            isNetworkOnline = isNetworkOnline,
            onLogout = onLogout,
        )
        if (!isNetworkOnline) {
            SavioOfflineBannerSurface()
        }
        Row(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .fillMaxHeight()
        ) {
            Column(
                modifier =
                    Modifier
                        .width(if (useSavioRefonteUi()) 320.dp else 280.dp)
                        .fillMaxHeight()
                        .background(
                            if (useSavioRefonteUi()) SavioRefonte.BgPage else MaterialTheme.colorScheme.surface,
                        ),
            ) {
                if (useSavioRefonteUi()) {
                    SavioDateNavigator(
                        selectedDate = selectedDate,
                        subtitle = dateSubtitle,
                        onPreviousDay = onPreviousDay,
                        onNextDay = onNextDay,
                        isRefreshing = isRefreshing,
                    )
                }
                TourneeSidebar(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    interventions = interventions,
                    followUpItems = followUpItems,
                    pendingCreating = pendingCreating,
                    selectedId = selectedId,
                    onSelect = { id ->
                        detailNavController.navigate(Screen.InterventionDetail.createRoute(id)) {
                            launchSingleTop = true
                        }
                    },
                    onPendingCreatingClick = onPendingCreatingClick,
                )
            }
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp),
                color = if (useSavioRefonteUi()) SavioRefonte.Line else MaterialTheme.colorScheme.outline,
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                TourneeDetailNavHost(
                    modifier = Modifier.fillMaxSize(),
                    detailNavController = detailNavController,
                    onStartIntervention = onStartIntervention,
                    onClientClick = onClientClick,
                    onNavigateToDevisSignature = onNavigateToDevisSignature,
                )
            }
        }
    }
}
