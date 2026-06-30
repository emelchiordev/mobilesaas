package re.melchior.saviomobile.ui.navigation

import android.content.Intent
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import re.melchior.saviomobile.ActivationDeepLink
import re.melchior.saviomobile.data.local.database.TokenDataStore
import re.melchior.saviomobile.data.remote.interceptor.AuthEvent
import re.melchior.saviomobile.data.remote.interceptor.AuthEventBus
import re.melchior.saviomobile.ui.screen.auth.ActivationScreen
import re.melchior.saviomobile.ui.screen.auth.AuthViewModel
import re.melchior.saviomobile.ui.screen.auth.LoginScreen
import re.melchior.saviomobile.ui.screen.auth.OnboardingScreen
import re.melchior.saviomobile.ui.screen.auth.RegisterScreen
import re.melchior.saviomobile.ui.screen.auth.SelectSocieteScreen
import re.melchior.saviomobile.ui.screen.auth.SplashScreen
import re.melchior.saviomobile.ui.screen.auth.WelcomeScreen
import re.melchior.saviomobile.ui.screen.client.CreateClientScreen
import re.melchior.saviomobile.ui.screen.intervention.create.CreateInterventionScreen
import re.melchior.saviomobile.ui.screen.intervention.offline.CreateOfflineInterventionScreen
import re.melchior.saviomobile.ui.utils.NetworkUtils
import androidx.compose.ui.platform.LocalContext
import re.melchior.saviomobile.ui.screen.intervention.offline.PendingOfflineInterventionsScreen
import re.melchior.saviomobile.ui.screen.intervention.CameraScreen
import re.melchior.saviomobile.ui.screen.intervention.ClientDetailScreen
import re.melchior.saviomobile.ui.screen.intervention.EquipementDetailScreen
import re.melchior.saviomobile.ui.screen.intervention.EquipementDetailViewModel
import re.melchior.saviomobile.ui.screen.intervention.cerfa.CerfaFroidScreen
import re.melchior.saviomobile.ui.screen.intervention.cerfa.CerfaScreen
import re.melchior.saviomobile.ui.screen.intervention.CatalogSearchScreen
import re.melchior.saviomobile.ui.screen.intervention.EquipmentFormScreen
import re.melchior.saviomobile.ui.screen.intervention.PlateScanScreen
import re.melchior.saviomobile.ui.screen.intervention.attestation.AttestationVeScreen
import re.melchior.saviomobile.ui.screen.intervention.measure.MeasureScreen
import re.melchior.saviomobile.ui.screen.intervention.pacmeasure.PacMeasureScreen
import re.melchior.saviomobile.ui.screen.intervention.InterventionActiveScreen
import re.melchior.saviomobile.ui.screen.intervention.InterventionDetailScreen
import re.melchior.saviomobile.ui.screen.invoice.DevisSignatureScreen
import re.melchior.saviomobile.ui.screen.invoice.InvoiceScreen
import re.melchior.saviomobile.ui.screen.intervention.PhotosScreen
import re.melchior.saviomobile.ui.screen.intervention.cloture.ClotureRapportScreen
import re.melchior.saviomobile.ui.screen.intervention.cloture.ClotureSignatureScreen
import re.melchior.saviomobile.ui.navigation.MainShellScreen
import re.melchior.saviomobile.ui.screen.tournee.TourneeViewModel
import re.melchior.saviomobile.ui.viewmodel.PhotoViewModel
import com.google.gson.Gson

@Composable
fun AppNavigation(
    tokenDataStore: TokenDataStore,
    authEventBus: AuthEventBus,
    windowSizeClass: WindowSizeClass,
    deepLinkIntent: Intent?,
    onConsumeDeepLinkIntent: () -> Unit,
) {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()
    val authUiState by authViewModel.uiState.collectAsStateWithLifecycle()

    val logoutAndGoWelcome: () -> Unit = {
        authViewModel.logout {
            navController.navigate(Screen.Welcome.route) {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    LaunchedEffect(Unit) {
        authEventBus.events.collect { event ->
            when (event) {
                is AuthEvent.Unauthorized -> {
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
        }
    }

    LaunchedEffect(deepLinkIntent?.dataString) {
        val token = ActivationDeepLink.extractToken(deepLinkIntent) ?: return@LaunchedEffect
        navController.navigate(Screen.Activation.createRoute(token)) {
            popUpTo(Screen.Splash.route) { inclusive = true }
            launchSingleTop = true
        }
        onConsumeDeepLinkIntent()
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
    ) {

        composable(Screen.Splash.route) {
            SplashScreen(
                tokenDataStore = tokenDataStore,
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
            )
        }

        composable(
            route = Screen.Activation.route,
            arguments =
                listOf(
                    navArgument("token") {
                        type = NavType.StringType
                    },
                ),
        ) {
            ActivationScreen(
                authViewModel = authViewModel,
                onActivationAuthenticated = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(navController.graph.id) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onActivationChooseSociete = {
                    navController.navigate(Screen.SelectSociete.createRoute(false)) {
                        popUpTo(Screen.Activation.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onResendEmail = {
                    navController.navigate(Screen.Register.route) {
                        popUpTo(Screen.Activation.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
            )
        }

        composable(Screen.Welcome.route) {
            WelcomeScreen(
                onLoginClick = {
                    navController.navigate(Screen.Login.route)
                },
                onRegisterClick = {
                    navController.navigate(Screen.Register.route)
                },
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onBack = { navController.popBackStack() },
                authViewModel = authViewModel,
                onNavigateOnboarding = {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onNavigateSelectSociete = {
                    navController.navigate(Screen.SelectSociete.createRoute(true))
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
            )
        }

        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onFinished = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onChooseSociete = {
                    navController.navigate(Screen.SelectSociete.createRoute(false))
                },
                onBack = { navController.popBackStack() },
                onNavigateRegister = {
                    navController.navigate(Screen.Register.route)
                },
                viewModel = authViewModel,
            )
        }

        composable(
            route = Screen.SelectSociete.route,
            arguments =
                listOf(
                    navArgument("registerFlow") {
                        type = NavType.BoolType
                        defaultValue = false
                    },
                ),
        ) { entry ->
            val registerFlow = entry.arguments?.getBoolean("registerFlow") ?: false
            SelectSocieteScreen(
                societes = authUiState.societesToChoose,
                isRegistrationFlow = registerFlow,
                onSocieteSelected = {
                    if (registerFlow) {
                        navController.navigate(Screen.Onboarding.route) {
                            popUpTo(Screen.Register.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    } else {
                        navController.navigate(Screen.Main.route) {
                            popUpTo(navController.graph.id) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                },
                viewModel = authViewModel,
            )
        }

        composable(Screen.Main.route) { backStackEntry ->
            val tourneeViewModel: TourneeViewModel = hiltViewModel(backStackEntry)
            val pendingSnackbar =
                backStackEntry.savedStateHandle.get<String>("pending_snackbar")
            val pendingFocusDateMillis =
                backStackEntry.savedStateHandle.get<Long>("pending_focus_date_millis")
            MainShellScreen(
                parentNavController = navController,
                windowSizeClass = windowSizeClass,
                onLogout = logoutAndGoWelcome,
                pendingSnackbar = pendingSnackbar,
                onConsumePendingSnackbar = {
                    backStackEntry.savedStateHandle.remove<String>("pending_snackbar")
                },
                pendingFocusDateMillis = pendingFocusDateMillis,
                onConsumePendingFocusDate = {
                    backStackEntry.savedStateHandle.remove<Long>("pending_focus_date_millis")
                },
                tourneeViewModel = tourneeViewModel,
            )
        }

        composable(Screen.Tournee.route) {
            LaunchedEffect(Unit) {
                navController.navigate(Screen.Main.route) {
                    popUpTo(Screen.Tournee.route) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }

        composable(Screen.CreateClient.route) {
            CreateClientScreen(
                onBack = { navController.popBackStack() },
                onNavigateToCreateIntervention = { customerId, unitId, displayName, addressLine ->
                    navController.navigate(
                        Screen.CreateIntervention.createRoute(
                            unitId = unitId,
                            customerId = customerId,
                            displayName = displayName,
                            addressLine = addressLine,
                        ),
                    ) {
                        popUpTo(Screen.CreateClient.route) { inclusive = true }
                    }
                },
            )
        }

        composable(
            route = Screen.CreateIntervention.route,
            arguments = listOf(
                navArgument("unitId") {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument("customerId") {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument("displayName") {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument("addressLine") {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument("defaultDate") {
                    type = NavType.StringType
                    defaultValue = ""
                },
            ),
        ) {
            CreateInterventionScreen(
                onBack = { navController.popBackStack() },
                onCreated = { scheduledAtMillis ->
                    navController.getBackStackEntry(Screen.Main.route).savedStateHandle.apply {
                        set("pending_snackbar", "Intervention créée ✅")
                        set("pending_focus_date_millis", scheduledAtMillis)
                    }
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Main.route) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onNavigateOffline = { defaultDate ->
                    navController.navigate(
                        Screen.CreateOfflineIntervention.createRoute(defaultDate = defaultDate),
                    ) {
                        popUpTo(Screen.CreateIntervention.route) { inclusive = true }
                    }
                },
                onCreateClient = { navController.navigate(Screen.CreateClient.route) },
            )
        }

        composable(
            route = Screen.CreateOfflineIntervention.route,
            arguments = listOf(
                navArgument("defaultDate") {
                    type = NavType.StringType
                    defaultValue = ""
                },
            ),
        ) {
            CreateOfflineInterventionScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.PendingOfflineInterventions.route) {
            PendingOfflineInterventionsScreen(onBack = { navController.popBackStack() })
        }

        composable(
            route = Screen.ClotureSignature.route,
            arguments = listOf(
                navArgument("interventionId") { type = NavType.StringType },
                navArgument("preselectedActualTypeKeys") {
                    type = NavType.StringType
                    defaultValue = "_"
                },
            ),
        ) {
            ClotureSignatureScreen(
                onBack = { navController.popBackStack() },
                onCompleted = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Main.route) { inclusive = true }
                    }
                },
            )
        }

        composable(Screen.ClotureRapport.route) {
            ClotureRapportScreen(
                onBack = { navController.popBackStack() },
                onNext = { interventionId, preselectedActualTypeKeys ->
                    navController.navigate(
                        Screen.ClotureSignature.createRoute(interventionId, preselectedActualTypeKeys),
                    )
                },
                onNavigateToInvoice = { interventionId ->
                    navController.navigate(Screen.Invoice.createRoute(interventionId))
                },
            )
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
                onBack = { navController.popBackStack() },
                onStartIntervention = { id ->
                    navController.navigate(Screen.InterventionActive.createRoute(id))
                },
                onClientClick = { customerId ->
                    navController.navigate(Screen.ClientDetail.createRoute(customerId))
                },
                onNavigateToDevisSignature = {
                    navController.navigate(Screen.DevisSignature.createRoute(interventionId))
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
                onBack = { navController.popBackStack() },
                onNavigateToDevisSignature = {
                    navController.navigate(Screen.DevisSignature.createRoute(interventionId))
                },
            )
        }

        composable(
            route = Screen.DevisSignature.route,
            arguments = listOf(
                navArgument("interventionId") { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val interventionId = backStackEntry.arguments?.getString("interventionId")
                ?: return@composable
            DevisSignatureScreen(
                onBack = { navController.popBackStack() },
                onCompleted = {
                    val popped = navController.popBackStack()
                    if (!popped) {
                        navController.navigate(Screen.Invoice.createRoute(interventionId)) {
                            popUpTo(Screen.Main.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
            )
        }

        composable(Screen.InterventionActive.route) { backStackEntry ->
            val interventionId =
                backStackEntry.arguments?.getString("interventionId") ?: return@composable
            InterventionActiveScreen(
                onQuit = { navController.popBackStack(Screen.Main.route, false) },
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
                    navController.navigate(
                        Screen.ClientDetail.createRouteFromIntervention(
                            customerId = customerId,
                            interventionId = interventionId,
                        ),
                    )
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
            val formViewModel: re.melchior.saviomobile.ui.screen.intervention.EquipmentFormViewModel =
                hiltViewModel(backStackEntry)
            val scanResultJsonFlow = remember(backStackEntry) {
                backStackEntry.savedStateHandle.getStateFlow<String?>(ScanPlateNav.RESULT_JSON, null)
            }
            val scanResultJson by scanResultJsonFlow.collectAsState()
            var scanSnackbarMessage by remember { mutableStateOf<String?>(null) }

            LaunchedEffect(scanResultJson) {
                val json = scanResultJson ?: return@LaunchedEffect
                val payload = Gson().fromJson(json, ScanPlateNavPayload::class.java)
                formViewModel.applyScanPayload(payload)
                backStackEntry.savedStateHandle.remove<String>(ScanPlateNav.RESULT_JSON)
                val snackbar = backStackEntry.savedStateHandle.get<String>(ScanPlateNav.SNACKBAR)
                if (snackbar != null) {
                    scanSnackbarMessage = snackbar
                    backStackEntry.savedStateHandle.remove<String>(ScanPlateNav.SNACKBAR)
                } else if (payload.parsed != null) {
                    scanSnackbarMessage = "Plaque analysée — vérifiez les champs"
                }
            }

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
                onScanPlate = {
                    navController.navigate(
                        Screen.PlateScan.createRoute(interventionId, unitId),
                    )
                },
                scanSnackbarMessage = scanSnackbarMessage,
                onConsumeScanSnackbar = { scanSnackbarMessage = null },
                viewModel = formViewModel,
            )
        }

        composable(
            route = Screen.PlateScan.route,
            arguments = listOf(
                navArgument("interventionId") { type = NavType.StringType },
                navArgument("unitId") { type = NavType.StringType },
            ),
        ) {
            PlateScanScreen(
                onBack = { navController.popBackStack() },
                onScanComplete = { payload, snackbarMessage ->
                    navController.previousBackStackEntry?.savedStateHandle?.apply {
                        set(ScanPlateNav.RESULT_JSON, Gson().toJson(payload))
                        snackbarMessage?.let { msg -> set(ScanPlateNav.SNACKBAR, msg) }
                    }
                    navController.popBackStack()
                },
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
            route = Screen.PacMeasure.route,
            arguments = listOf(
                navArgument("interventionId") { type = NavType.StringType },
                navArgument("equipmentOrder") { type = NavType.IntType },
            ),
        ) {
            PacMeasureScreen(
                onBack = { navController.popBackStack() },
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
                onPacMeasureClick = { interventionId, order ->
                    navController.navigate(
                        Screen.PacMeasure.createRoute(interventionId, order),
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


        composable(
            route = Screen.ClientDetail.route,
            arguments = listOf(
                navArgument("customerId") { type = NavType.StringType },
                navArgument("unitId") {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument("displayName") {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument("addressLine") {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument("contextInterventionId") {
                    type = NavType.StringType
                    defaultValue = ""
                },
            ),
        ) {
            ClientDetailScreen(
                windowSizeClass = windowSizeClass,
                onBack = { navController.popBackStack() },
                onNewIntervention = { unitId, customerId, displayName, addressLine ->
                    navController.navigate(
                        Screen.CreateIntervention.createRoute(
                            unitId = unitId,
                            customerId = customerId,
                            displayName = displayName,
                            addressLine = addressLine,
                        ),
                    )
                },
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