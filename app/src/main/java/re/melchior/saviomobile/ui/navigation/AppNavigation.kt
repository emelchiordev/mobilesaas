package re.melchior.saviomobile.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import android.net.Uri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import re.melchior.saviomobile.data.local.database.TokenDataStore
import re.melchior.saviomobile.data.remote.interceptor.AuthEvent
import re.melchior.saviomobile.data.remote.interceptor.AuthEventBus
import re.melchior.saviomobile.ui.screen.auth.AuthViewModel
import re.melchior.saviomobile.ui.screen.auth.LoginScreen
import re.melchior.saviomobile.ui.screen.auth.SelectSocieteScreen
import re.melchior.saviomobile.ui.screen.intervention.CameraScreen
import re.melchior.saviomobile.ui.screen.intervention.ClientDetailScreen
import re.melchior.saviomobile.ui.screen.intervention.EquipementDetailScreen
import re.melchior.saviomobile.ui.screen.intervention.InterventionActiveScreen
import re.melchior.saviomobile.ui.screen.intervention.InterventionActiveViewModel
import re.melchior.saviomobile.ui.screen.intervention.InterventionDetailScreen
import re.melchior.saviomobile.ui.screen.invoice.InvoiceScreen
import re.melchior.saviomobile.ui.screen.intervention.PhotosScreen
import re.melchior.saviomobile.ui.screen.intervention.cloture.ClotureRapportScreen
import re.melchior.saviomobile.ui.screen.intervention.cloture.ClotureSignatureScreen
import re.melchior.saviomobile.ui.screen.tournee.TourneeScreen
import re.melchior.saviomobile.ui.viewmodel.PhotoViewModel

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object SelectSociete : Screen("select_societe")
    object Tournee : Screen("tournee")
    object InterventionDetail : Screen("intervention/{interventionId}") {
        fun createRoute(interventionId: String) = "intervention/$interventionId"
    }

    object Invoice : Screen("invoice/{interventionId}") {
        fun createRoute(interventionId: String) = "invoice/$interventionId"
    }

    object ClotureRapport : Screen("intervention/{interventionId}/cloture/rapport") {
        fun createRoute(interventionId: String) = "intervention/$interventionId/cloture/rapport"
    }

    object ClotureSignature :
        Screen("intervention/{interventionId}/cloture/signature/{preselectedActualTypeKeys}") {
        fun createRoute(interventionId: String, preselectedActualTypeKeys: String = "_") =
            "intervention/$interventionId/cloture/signature/${
                Uri.encode(preselectedActualTypeKeys, "UTF-8")
            }"
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

    object Photos : Screen(
        "intervention/{interventionId}/photos/{unitId}/{customerId}"
    ) {
        fun createRoute(
            interventionId: String,
            unitId: String,
            customerId: String
        ) = "intervention/$interventionId/photos/$unitId/$customerId"
    }

    object Camera : Screen(
        "intervention/{interventionId}/camera/{unitId}/{customerId}"
    ) {
        fun createRoute(
            interventionId: String,
            unitId: String,
            customerId: String
        ) = "intervention/$interventionId/camera/$unitId/$customerId"
    }
}

@Composable
fun AppNavigation(
    tokenDataStore: TokenDataStore,
    authEventBus: AuthEventBus
) {
    val navController = rememberNavController()
    val isLoggedIn by tokenDataStore.isLoggedIn.collectAsStateWithLifecycle(null)


    // Écoute les événements 401 → redirige vers Login
    LaunchedEffect(Unit) {
        authEventBus.events.collect { event ->
            when (event) {
                is AuthEvent.Unauthorized -> {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
        }
    }
    // 2. Tant qu'on ne sait pas si l'utilisateur est connecté, on n'affiche pas le NavHost
    if (isLoggedIn == null) {
        // Optionnel : un simple Box vide ou un indicateur de chargement
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            // CircularProgressIndicator()
        }
        return
    }

    // 3. Maintenant isLoggedIn est soit true soit false, et restera stable durant la rotation
    val startDestination = remember {
        if (isLoggedIn == true) Screen.Tournee.route else Screen.Login.route
    }

    val authViewModel: AuthViewModel = hiltViewModel()
    val authUiState by authViewModel.uiState.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        composable(
            route = Screen.ClotureSignature.route,
            arguments = listOf(
                navArgument("interventionId") { type = NavType.StringType },
                navArgument("preselectedActualTypeKeys") {
                    type = NavType.StringType
                    defaultValue = "_"
                }
            )
        ) {
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
                onNext = { interventionId, preselectedActualTypeKeys ->
                    navController.navigate(
                        Screen.ClotureSignature.createRoute(interventionId, preselectedActualTypeKeys)
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

        composable(
            route = Screen.Invoice.route,
            arguments = listOf(
                navArgument("interventionId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val interventionId = backStackEntry.arguments?.getString("interventionId")
                ?: return@composable
            InvoiceScreen(
                interventionId = interventionId,
                onBack = { navController.popBackStack() }
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
                onPhotosClick = { interventionId, unitId, customerId ->
                    navController.navigate(
                        Screen.Photos.createRoute(interventionId, unitId, customerId)
                    )
                },
                onFactureClick = { interventionId ->
                    navController.navigate(Screen.Invoice.createRoute(interventionId))
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

        composable(Screen.Photos.route) { backStackEntry ->
            val interventionId = backStackEntry.arguments?.getString("interventionId") ?: return@composable
            val unitId = backStackEntry.arguments?.getString("unitId") ?: return@composable
            val customerId = backStackEntry.arguments?.getString("customerId") ?: return@composable

            PhotosScreen(
                interventionId = interventionId,
                unitId = unitId,
                customerId = customerId,
                onBack = { navController.popBackStack() },
                onOpenCamera = { uid, cid ->
                    navController.navigate(
                        Screen.Camera.createRoute(interventionId, uid, cid)
                    )
                }
            )
        }

        composable(Screen.Camera.route) { backStackEntry ->
            val interventionId = backStackEntry.arguments?.getString("interventionId") ?: return@composable
            val unitId = backStackEntry.arguments?.getString("unitId") ?: return@composable
            val customerId = backStackEntry.arguments?.getString("customerId") ?: return@composable

            // ViewModel propre à CameraScreen — pas de partage
            val photoViewModel: PhotoViewModel = hiltViewModel()

            CameraScreen(
                onPhotoCaptured = { file ->
                    photoViewModel.savePhoto(
                        interventionId = interventionId,
                        unitId = unitId,
                        customerId = customerId,
                        sourceFile = file
                    )
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() }
            )
        }
    }
}