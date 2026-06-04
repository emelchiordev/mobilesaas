package re.melchior.saviomobile.ui.navigation

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import re.melchior.saviomobile.data.remote.dto.CustomerSearchRowDto
import re.melchior.saviomobile.ui.screen.client.ClientsScreen
import re.melchior.saviomobile.ui.screen.tournee.TourneeScreen
import re.melchior.saviomobile.ui.screen.tournee.TourneeTabletScreen
import re.melchior.saviomobile.ui.screen.tournee.TourneeViewModel
import re.melchior.saviomobile.ui.utils.NetworkUtils
import re.melchior.saviomobile.ui.utils.SavioWindowSize
import re.melchior.saviomobile.ui.utils.rememberSavioWindowSize

@Composable
fun MainShellScreen(
    parentNavController: NavHostController,
    windowSizeClass: WindowSizeClass,
    onLogout: () -> Unit,
    pendingSnackbar: String? = null,
    onConsumePendingSnackbar: () -> Unit = {},
    pendingFocusDateMillis: Long? = null,
    onConsumePendingFocusDate: () -> Unit = {},
    tourneeViewModel: TourneeViewModel = hiltViewModel(),
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val context = LocalContext.current
    val savioWindowSize = rememberSavioWindowSize(windowSizeClass)

    val onNewIntervention: () -> Unit = {
        if (NetworkUtils.isOnline(context)) {
            parentNavController.navigate(Screen.CreateIntervention.createRoute())
        } else {
            parentNavController.navigate(Screen.CreateOfflineIntervention.route)
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
            if (savioWindowSize == SavioWindowSize.EXPANDED) {
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
                        parentNavController.navigate(Screen.CreateOfflineIntervention.route)
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
                        parentNavController.navigate(Screen.CreateOfflineIntervention.route)
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
