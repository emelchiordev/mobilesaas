package re.savio.mobile.ui.navigation

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import re.savio.mobile.data.remote.dto.CustomerSearchRowDto
import re.savio.mobile.ui.screen.client.ClientsScreen
import re.savio.mobile.ui.screen.tournee.TourneeScreen
import re.savio.mobile.ui.screen.tournee.TourneeTabletScreen
import re.savio.mobile.ui.screen.tournee.TourneeViewModel
import re.savio.mobile.ui.utils.NetworkUtils
import re.savio.mobile.ui.utils.SavioWindowSize
import re.savio.mobile.ui.utils.rememberSavioWindowSize

@Composable
fun MainShellScreen(
    parentNavController: NavHostController,
    windowSizeClass: WindowSizeClass,
    onLogout: () -> Unit,
    pendingSnackbar: String? = null,
    onConsumePendingSnackbar: () -> Unit = {},
    pendingFocusDateMillis: Long? = null,
    onConsumePendingFocusDate: () -> Unit = {},
    pendingShowDemoCompletionBanner: Boolean = false,
    onConsumePendingShowDemoCompletionBanner: () -> Unit = {},
    tourneeViewModel: TourneeViewModel = hiltViewModel(),
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val context = LocalContext.current
    val savioWindowSize = rememberSavioWindowSize(windowSizeClass)

    LaunchedEffect(pendingFocusDateMillis) {
        if (pendingFocusDateMillis != null) selectedTab = 0
    }

    LaunchedEffect(pendingShowDemoCompletionBanner) {
        if (pendingShowDemoCompletionBanner) {
            selectedTab = 0
            tourneeViewModel.notifyDemoInterventionCompleted()
            onConsumePendingShowDemoCompletionBanner()
        }
    }

    val tourneeUiState by tourneeViewModel.uiState.collectAsStateWithLifecycle()
    val planningDefaultDate = tourneeUiState.selectedDate.toString()

    val onNewIntervention: () -> Unit = {
        if (NetworkUtils.isOnline(context)) {
            parentNavController.navigate(
                Screen.CreateIntervention.createRoute(defaultDate = planningDefaultDate),
            )
        } else {
            parentNavController.navigate(
                Screen.CreateOfflineIntervention.createRoute(defaultDate = planningDefaultDate),
            )
        }
    }

    val onClientClick: (CustomerSearchRowDto) -> Unit = { row ->
        row.customerId?.takeIf { it.isNotBlank() }?.let { customerId ->
            parentNavController.navigate(
                Screen.ClientDetail.createRoute(
                    customerId = customerId,
                    unitId = row.unitId,
                    displayName = row.resolvedDisplayName(),
                    addressLine = row.formattedAddress(),
                ),
            )
        }
    }

    when (selectedTab) {
        0 -> {
            if (savioWindowSize == SavioWindowSize.TABLET) {
                TourneeTabletScreen(
                    parentNavController = parentNavController,
                    onResumeIntervention = { interventionId ->
                        parentNavController.navigate(
                            Screen.InterventionActive.createRoute(interventionId),
                        )
                    },
                    onLogout = onLogout,
                    onNewIntervention = onNewIntervention,
                    onOfflineIntervention = {
                        parentNavController.navigate(
                            Screen.CreateOfflineIntervention.createRoute(defaultDate = planningDefaultDate),
                        )
                    },
                    onPendingOfflineList = {
                        parentNavController.navigate(Screen.PendingOfflineInterventions.route)
                    },
                    pendingSnackbar = pendingSnackbar,
                    onConsumePendingSnackbar = onConsumePendingSnackbar,
                    pendingFocusDateMillis = pendingFocusDateMillis,
                    onConsumePendingFocusDate = onConsumePendingFocusDate,
                    showBottomNav = true,
                    bottomNavSelectedIndex = selectedTab,
                    onBottomNavSelect = { selectedTab = it },
                    viewModel = tourneeViewModel,
                )
            } else {
                TourneeScreen(
                    onInterventionClick = { interventionId ->
                        parentNavController.navigate(
                            Screen.InterventionDetail.createRoute(interventionId),
                        )
                    },
                    onResumeIntervention = { interventionId ->
                        parentNavController.navigate(
                            Screen.InterventionActive.createRoute(interventionId),
                        )
                    },
                    onLogout = onLogout,
                    onNewIntervention = onNewIntervention,
                    onOfflineIntervention = {
                        parentNavController.navigate(
                            Screen.CreateOfflineIntervention.createRoute(defaultDate = planningDefaultDate),
                        )
                    },
                    onPendingOfflineList = {
                        parentNavController.navigate(Screen.PendingOfflineInterventions.route)
                    },
                    pendingSnackbar = pendingSnackbar,
                    onConsumePendingSnackbar = onConsumePendingSnackbar,
                    pendingFocusDateMillis = pendingFocusDateMillis,
                    onConsumePendingFocusDate = onConsumePendingFocusDate,
                    showBottomNav = true,
                    bottomNavSelectedIndex = selectedTab,
                    onBottomNavSelect = { selectedTab = it },
                    viewModel = tourneeViewModel,
                )
            }
        }

        1 -> {
            ClientsScreen(
                onCreateClient = { parentNavController.navigate(Screen.CreateClient.route) },
                onClientClick = onClientClick,
                showBottomNav = true,
                bottomNavSelectedIndex = selectedTab,
                onBottomNavSelect = { selectedTab = it },
            )
        }
    }
}
