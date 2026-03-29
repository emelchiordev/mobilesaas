package re.melchior.saviomobile.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import re.melchior.saviomobile.data.local.database.TokenDataStore
import re.melchior.saviomobile.ui.screen.auth.AuthViewModel
import re.melchior.saviomobile.ui.screen.auth.LoginScreen
import re.melchior.saviomobile.ui.screen.auth.SelectSocieteScreen
import re.melchior.saviomobile.ui.screen.intervention.ClientDetailScreen
import re.melchior.saviomobile.ui.screen.intervention.EquipementDetailScreen
import re.melchior.saviomobile.ui.screen.intervention.InterventionActiveScreen
import re.melchior.saviomobile.ui.screen.intervention.InterventionDetailScreen
import re.melchior.saviomobile.ui.screen.intervention.cloture.ClotureRapportScreen
import re.melchior.saviomobile.ui.screen.intervention.cloture.ClotureSignatureScreen
import re.melchior.saviomobile.ui.screen.tournee.TourneeScreen

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object SelectSociete : Screen("select_societe")
    object Tournee : Screen("tournee")
    object InterventionDetail : Screen("intervention/{interventionId}") {
        fun createRoute(interventionId: String) = "intervention/$interventionId"
    }

    object ClotureRapport : Screen("intervention/{interventionId}/cloture/rapport") {
        fun createRoute(interventionId: String) = "intervention/$interventionId/cloture/rapport"
    }

    object ClotureSignature : Screen("intervention/{interventionId}/cloture/signature") {
        fun createRoute(interventionId: String) =
            "intervention/$interventionId/cloture/signature"
    }
    object InterventionActive : Screen("intervention/{interventionId}/active") {
        fun createRoute(interventionId: String) = "intervention/$interventionId/active"
    }
    object ClientDetail : Screen("client/{customerId}") {
        fun createRoute(customerId: String) = "client/$customerId"
    }

    object EquipementDetail : Screen("equipement/{equipmentId}") {
        fun createRoute(equipmentId: String) = "equipement/$equipmentId"
    }
}

@Composable
fun AppNavigation(tokenDataStore: TokenDataStore) {
    val navController = rememberNavController()
    val isLoggedIn by tokenDataStore.isLoggedIn.collectAsStateWithLifecycle(false)
    val startDestination = if (isLoggedIn) Screen.Tournee.route else Screen.Login.route

    val authViewModel: AuthViewModel = hiltViewModel()
    val authUiState by authViewModel.uiState.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        composable(Screen.ClotureSignature.route) {
            ClotureSignatureScreen(
                onBack = { navController.popBackStack() },
                onCompleted = {
                    navController.navigate(Screen.Tournee.route) {
                        popUpTo(Screen.Tournee.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.ClotureRapport.route) {
            ClotureRapportScreen(
                onBack = { navController.popBackStack() },
                onNext = { interventionId ->
                    navController.navigate(
                        Screen.ClotureSignature.createRoute(interventionId)
                    )
                }
            )
        }


        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Tournee.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onChooseSociete = {
                    navController.navigate(Screen.SelectSociete.route)
                },
                viewModel = authViewModel
            )
        }

        composable(Screen.SelectSociete.route) {
            SelectSocieteScreen(
                societes = authUiState.societesToChoose,
                onSocieteSelected = {
                    navController.navigate(Screen.Tournee.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                viewModel = authViewModel
            )
        }

        composable(Screen.Tournee.route) {
            TourneeScreen(
                onInterventionClick = { interventionId ->
                    navController.navigate(
                        Screen.InterventionDetail.createRoute(interventionId)
                    )
                }
            )
        }

        composable(Screen.InterventionDetail.route) {
            InterventionDetailScreen(
                onBack = { navController.popBackStack() },
                onStartIntervention = { interventionId ->
                    navController.navigate(
                        Screen.InterventionActive.createRoute(interventionId)
                    )
                }
            )
        }

        composable(Screen.InterventionActive.route) {
            InterventionActiveScreen(
                onQuit = { navController.popBackStack(Screen.Tournee.route, false) },
                onCloture = { interventionId ->
                    navController.navigate(
                        Screen.ClotureRapport.createRoute(interventionId)
                    )
                },
                onEquipementClick = { equipmentId ->
                    navController.navigate(
                        Screen.EquipementDetail.createRoute(equipmentId)
                    )
                },
                onClientClick = { customerId ->
                    navController.navigate(Screen.ClientDetail.createRoute(customerId))
                },
                onPhotosClick = { interventionId ->
                    // PhotosScreen — à venir
                }
            )
        }

        composable(Screen.EquipementDetail.route) {
            EquipementDetailScreen(
                onBack = { navController.popBackStack() }
            )
        }


        composable(Screen.ClientDetail.route) {
            ClientDetailScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}