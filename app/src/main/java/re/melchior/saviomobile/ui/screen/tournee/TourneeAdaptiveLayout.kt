package re.melchior.saviomobile.ui.screen.tournee

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import re.melchior.saviomobile.ui.navigation.Screen

/**
 * Master-detail (tablette / fenêtre élargie uniquement). Le mode compact utilise [TourneeScreen] tel quel.
 */
@Composable
fun TourneeAdaptiveLayout(
    modifier: Modifier = Modifier.fillMaxSize(),
    interventions: List<InterventionItem>,
    detailNavController: NavHostController,
    onStartIntervention: (String) -> Unit,
    currentDateLabel: String,
    pendingSyncCount: Int,
    onSyncCatalog: () -> Unit,
    isCatalogSyncing: Boolean = false,
    onRefresh: () -> Unit,
    isRefreshing: Boolean,
) {
    val navEntry by detailNavController.currentBackStackEntryAsState()
    val selectedId = navEntry?.arguments?.getString("interventionId")
    val selectedIntervention = selectedId?.let { id -> interventions.find { it.id == id } }
    val remainingCount = interventions.count { !it.isCompleted }

    Column(modifier.fillMaxSize()) {
        SavioTabletTopBar(
            selectedIntervention = selectedIntervention,
            currentDate = currentDateLabel,
            remainingCount = remainingCount,
            pendingSyncCount = pendingSyncCount,
            onSyncCatalog = onSyncCatalog,
            isCatalogSyncing = isCatalogSyncing,
            onRefresh = onRefresh,
            isRefreshing = isRefreshing,
        )
        Row(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .fillMaxHeight()
        ) {
            TourneeSidebar(
                modifier = Modifier
                    .width(280.dp)
                    .fillMaxHeight(),
                interventions = interventions,
                selectedId = selectedId,
                onSelect = { id ->
                    detailNavController.navigate(Screen.InterventionDetail.createRoute(id)) {
                        launchSingleTop = true
                    }
                }
            )
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(0.5.dp),
                color = Color(0xFFE8E8E8)
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
                )
            }
        }
    }
}
