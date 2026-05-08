package re.melchior.saviomobile.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import re.melchior.saviomobile.ui.screen.intervention.EquipementDetailViewModel
import re.melchior.saviomobile.ui.screen.intervention.cerfa.CerfaFroidScreen
import re.melchior.saviomobile.ui.screen.intervention.cerfa.CerfaScreen
import re.melchior.saviomobile.ui.screen.intervention.CatalogSearchScreen
import re.melchior.saviomobile.ui.screen.intervention.EquipmentFormScreen
import re.melchior.saviomobile.ui.screen.intervention.attestation.AttestationVeScreen
import re.melchior.saviomobile.ui.screen.intervention.measure.MeasureScreen
import re.melchior.saviomobile.ui.screen.intervention.InterventionActiveScreen
import re.melchior.saviomobile.ui.screen.intervention.InterventionDetailScreen
import re.melchior.saviomobile.ui.screen.invoice.InvoiceScreen
import re.melchior.saviomobile.ui.screen.intervention.PhotosScreen
import re.melchior.saviomobile.ui.screen.intervention.cloture.ClotureRapportScreen
import re.melchior.saviomobile.ui.screen.intervention.cloture.ClotureSignatureScreen
import re.melchior.saviomobile.ui.screen.tournee.TourneeTabletScreen
import re.melchior.saviomobile.ui.screen.tournee.TourneeScreen
import re.melchior.saviomobile.ui.screen.tournee.TourneeViewModel
import re.melchior.saviomobile.ui.utils.SavioWindowSize
import re.melchior.saviomobile.ui.utils.rememberSavioWindowSize
import re.melchior.saviomobile.ui.viewmodel.PhotoViewModel

@Composable
fun AppNavigation(
    tokenDataStore: TokenDataStore,
    authEventBus: AuthEventBus,
    windowSizeClass: WindowSizeClass,
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

        composable(Screen.Tournee.route) { backStackEntry ->
            val savioWindowSize = rememberSavioWindowSize(windowSizeClass)
            // Un seul ViewModel partagé pour les deux écrans
            val tourneeViewModel: TourneeViewModel = hiltViewModel(backStackEntry)

            if (savioWindowSize == SavioWindowSize.EXPANDED) {
                TourneeTabletScreen(
                    parentNavController = navController,
                    onResumeIntervention = { interventionId ->
                        navController.navigate(
                            Screen.InterventionActive.createRoute(interventionId)
                        )
                    },
                    viewModel = tourneeViewModel,
                )
            } else {
                TourneeScreen(
                    onInterventionClick = { interventionId ->
                        navController.navigate(
                            Screen.InterventionDetail.createRoute(interventionId)
                        )
                    },
                    onResumeIntervention = { interventionId ->
                        navController.navigate(
                            Screen.InterventionActive.createRoute(interventionId)
                        )
                    },
                    viewModel = tourneeViewModel,
                )
            }
        }

        composable(Screen.InterventionDetail.route) {
            InterventionDetailScreen(
                onBack = { navController.popBackStack() },
                onStartIntervention = { interventionId ->
                    navController.navigate(
                        Screen.InterventionActive.createRoute(interventionId)
                    )
                },
                onClientClick = { customerId ->
                    navController.navigate(Screen.ClientDetail.createRoute(customerId))
                },
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

        composable(Screen.InterventionActive.route) { backStackEntry ->
            val interventionId =
                backStackEntry.arguments?.getString("interventionId") ?: return@composable
            InterventionActiveScreen(
                onQuit = { navController.popBackStack(Screen.Tournee.route, false) },
                onCloture = { id ->
                    navController.navigate(
                        Screen.ClotureRapport.createRoute(id)
                    )
                },
                onEquipementClick = { interventionId, equipmentId ->
                    navController.navigate(
                        Screen.EquipementDetail.createRoute(interventionId, equipmentId)
                    )
                },
                onClientClick = { customerId ->
                    navController.navigate(Screen.ClientDetail.createRoute(customerId))
                },
                onOpenCamera = { unitId, customerId ->
                    navController.navigate(
                        Screen.Camera.createRoute(interventionId, unitId, customerId),
                    )
                },
                onFactureClick = { iid ->
                    navController.navigate(Screen.Invoice.createRoute(iid))
                },
                onAddEquipment = { iid, unitId, parentEquipmentId ->
                    navController.navigate(
                        Screen.CatalogSearch.createRoute(
                            interventionId = iid,
                            unitId = unitId,
                            parentEquipmentId = parentEquipmentId,
                            existingEquipmentId = null,
                        )
                    )
                },
                onReplaceEquipment = { iid, unitId, existingEquipmentId ->
                    navController.navigate(
                        Screen.CatalogSearch.createRoute(
                            interventionId = iid,
                            unitId = unitId,
                            parentEquipmentId = null,
                            existingEquipmentId = existingEquipmentId,
                        )
                    )
                },
            )
        }

        composable(
            route = Screen.CatalogSearch.route,
            arguments = listOf(
                navArgument("interventionId") { type = NavType.StringType },
                navArgument("unitId") { type = NavType.StringType },
                navArgument("existingEquipmentId") {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument("parentEquipmentId") {
                    type = NavType.StringType
                    defaultValue = ""
                },
            ),
        ) { backStackEntry ->
            val interventionId = backStackEntry.arguments?.getString("interventionId") ?: return@composable
            val unitId = backStackEntry.arguments?.getString("unitId") ?: return@composable
            val parentEquipmentId = backStackEntry.arguments?.getString("parentEquipmentId")
                ?.takeIf { it.isNotBlank() }
            val existingEquipmentId = backStackEntry.arguments?.getString("existingEquipmentId")
                ?.takeIf { it.isNotBlank() }
            CatalogSearchScreen(
                interventionId = interventionId,
                unitId = unitId,
                existingEquipmentId = existingEquipmentId,
                parentEquipmentId = parentEquipmentId,
                onManualEntry = {
                    navController.navigate(
                        Screen.EquipmentForm.createRoute(
                            interventionId,
                            unitId,
                            null,
                            existingEquipmentId,
                            parentEquipmentId,
                        )
                    )
                },
                onBack = { navController.popBackStack() },
                onEquipmentSelected = { _ ->
                    // Remonter jusqu'à InterventionActive sans recréer
                    // en utilisant la route template (pas la route résolue)
                    navController.navigate(
                        Screen.InterventionActive.createRoute(interventionId)
                    ) {
                        // Supprimer CatalogSearch et EquipementDetail du stack
                        // sans recréer InterventionActive
                        popUpTo(Screen.InterventionActive.route) {
                            inclusive = false // false pour ne pas recréer
                        }
                        launchSingleTop = true // réutilise l'instance existante
                    }
                },
            )
        }

        composable(
            route = Screen.EquipmentForm.route,
            arguments = listOf(
                navArgument("interventionId") { type = NavType.StringType },
                navArgument("unitId") { type = NavType.StringType },
                navArgument("catalogEquipmentId") {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument("existingEquipmentId") {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument("parentEquipmentId") {
                    type = NavType.StringType
                    defaultValue = ""
                },
            ),
        ) { backStackEntry ->
            val interventionId = backStackEntry.arguments?.getString("interventionId") ?: return@composable
            val unitId = backStackEntry.arguments?.getString("unitId") ?: return@composable
            val catalogEquipmentId = backStackEntry.arguments?.getString("catalogEquipmentId")
                ?.takeIf { it.isNotBlank() }
            val existingEquipmentId = backStackEntry.arguments?.getString("existingEquipmentId")
                ?.takeIf { it.isNotBlank() }
            val parentEquipmentId = backStackEntry.arguments?.getString("parentEquipmentId")
                ?.takeIf { it.isNotBlank() }
            EquipmentFormScreen(
                interventionId = interventionId,
                unitId = unitId,
                catalogEquipmentId = catalogEquipmentId,
                existingEquipmentId = existingEquipmentId,
                parentEquipmentId = parentEquipmentId,
                onSaved = {
                    navController.popBackStack(
                        route = Screen.InterventionActive.createRoute(interventionId),
                        inclusive = false,
                    )
                },
                onBack = { navController.popBackStack() },
                viewModel = hiltViewModel(backStackEntry),
            )
        }

        composable(
            route = Screen.Measure.route,
            arguments = listOf(
                navArgument("interventionId") { type = NavType.StringType },
                navArgument("equipmentOrder") { type = NavType.IntType },
            ),
        ) {
            MeasureScreen(
                onBack = { navController.popBackStack() },
                windowSizeClass = windowSizeClass,
            )
        }

        composable(
            route = Screen.AttestationVe.route,
            arguments = listOf(
                navArgument("interventionId") { type = NavType.StringType },
                navArgument("equipmentOrder") { type = NavType.IntType },
                navArgument("type") { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val interventionId =
                backStackEntry.arguments?.getString("interventionId") ?: return@composable
            val equipmentOrder =
                backStackEntry.arguments?.getInt("equipmentOrder") ?: return@composable
            val type = backStackEntry.arguments?.getString("type") ?: return@composable
            AttestationVeScreen(
                interventionId = interventionId,
                equipmentOrder = equipmentOrder,
                type = type,
                onBack = { navController.popBackStack() },
                viewModel = hiltViewModel(backStackEntry),
            )
        }

        composable(
            route = Screen.EquipementDetail.route,
            arguments = listOf(
                navArgument("interventionId") { type = NavType.StringType },
                navArgument("equipmentId") { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val equipVm = hiltViewModel<EquipementDetailViewModel>(backStackEntry)
            val unitId by equipVm.interventionUnitId.collectAsStateWithLifecycle()

            EquipementDetailScreen(
                onBack = { navController.popBackStack() },
                onCerfaClick = { interventionId, equipmentId ->
                    navController.navigate(
                        Screen.CerfaFroid.createRoute(interventionId, equipmentId)
                    )
                },
                onAttestationVeClick = { _, equipmentOrder, attestationType ->
                    navController.navigate(
                        Screen.AttestationVe.createRoute(
                            equipVm.currentInterventionId,
                            equipmentOrder,
                            attestationType,
                        ),
                    )
                },
                onMeasureClick = { interventionId, order ->
                    navController.navigate(
                        Screen.Measure.createRoute(interventionId, order),
                    )
                },
                onReplaceClick = { interventionId, equipmentId ->
                    android.util.Log.d(
                        "NAV",
                        "onReplaceClick interventionId=$interventionId equipmentId=$equipmentId unitId=$unitId"
                    )
                    unitId?.let { uid ->
                        val route = Screen.CatalogSearch.createRoute(
                            interventionId = interventionId,
                            unitId = uid,
                            existingEquipmentId = equipmentId,
                            parentEquipmentId = null,
                        )
                        android.util.Log.d("NAV", "navigating to $route")
                        navController.navigate(route)
                    } ?: android.util.Log.w("NAV", "unitId is null — navigation annulée")
                },
                viewModel = equipVm,
            )
        }

        composable(
            route = Screen.CerfaFroid.route,
            arguments = listOf(
                navArgument("interventionId") { type = NavType.StringType },
                navArgument("equipmentId") { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val interventionId =
                backStackEntry.arguments?.getString("interventionId") ?: return@composable
            val equipmentId =
                backStackEntry.arguments?.getString("equipmentId") ?: return@composable
            CerfaFroidScreen(
                onBack = { navController.popBackStack() },
                windowSizeClass = windowSizeClass,
                onApercuPdf = {
                    navController.navigate(
                        Screen.CerfaPdf.createRoute(interventionId, equipmentId),
                    )
                },
            )
        }

        composable(
            route = Screen.CerfaPdf.route,
            arguments = listOf(
                navArgument("interventionId") { type = NavType.StringType },
                navArgument("equipmentId") { type = NavType.StringType },
            ),
        ) {
            CerfaScreen(
                onBack = { navController.popBackStack() },
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