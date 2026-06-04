package re.melchior.saviomobile.ui.screen.tournee

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import re.melchior.saviomobile.ui.navigation.Screen
import re.melchior.saviomobile.ui.screen.intervention.InterventionDetailScreen

internal const val TourneeDetailPlaceholderRoute = "tournee_master_detail_placeholder"

@Composable
fun TourneeDetailNavHost(
    modifier: Modifier,
    detailNavController: NavHostController,
    onStartIntervention: (String) -> Unit,
    onClientClick: (String) -> Unit = {},
    onNavigateToDevisSignature: (String) -> Unit = {},
) {
    NavHost(
        navController = detailNavController,
        startDestination = TourneeDetailPlaceholderRoute,
        modifier = modifier.fillMaxSize(),
    ) {
        composable(TourneeDetailPlaceholderRoute) {
            EmptyDetailPane(Modifier.fillMaxSize())
        }
        composable(
            route = Screen.InterventionDetail.route,
            arguments = listOf(
                navArgument("interventionId") { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val interventionId =
                backStackEntry.arguments?.getString("interventionId") ?: return@composable
            InterventionDetailScreen(
                onBack = { detailNavController.popBackStack() },
                onStartIntervention = onStartIntervention,
                onClientClick = onClientClick,
                onNavigateToDevisSignature = { onNavigateToDevisSignature(interventionId) },
                embeddedInMasterDetail = true,
            )
        }
    }
}
