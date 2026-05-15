package re.melchior.saviomobile.ui.screen.tournee

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import re.melchior.saviomobile.ui.component.SavioEmptyState
import re.melchior.saviomobile.ui.component.SavioNetworkErrorState
import re.melchior.saviomobile.ui.component.SavioOfflineBannerSurface
import re.melchior.saviomobile.ui.component.SavioSnackbarHost
import re.melchior.saviomobile.ui.component.SavioTourneeListSkeleton
import re.melchior.saviomobile.ui.navigation.Screen
import re.melchior.saviomobile.ui.theme.SavioPalette
import re.melchior.saviomobile.ui.theme.SavioUi
import re.melchior.saviomobile.ui.utils.rememberIsNetworkOnline
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TourneeTabletScreen(
    parentNavController: NavController,
    onResumeIntervention: (String) -> Unit = {},
    onLogout: () -> Unit = {},
    onCreateClient: () -> Unit = {},
    onOfflineIntervention: () -> Unit = {},
    onPendingOfflineList: () -> Unit = {},
    viewModel: TourneeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val interventions by viewModel.interventions.collectAsStateWithLifecycle()
    val pendingSyncCount by viewModel.pendingSyncCount.collectAsStateWithLifecycle()
    val resumeCandidate by viewModel.resumeCandidate.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var dragAccumulator by remember { mutableFloatStateOf(0f) }
    val detailNavController = rememberNavController()
    val isOnline = rememberIsNetworkOnline()
    val pendingOfflineInterventionCount by viewModel.pendingOfflineInterventionCount.collectAsStateWithLifecycle()
    var fabMenuExpanded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val dateFormatter = remember {
        DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.FRENCH)
    }
    val currentDateLabel = remember(uiState.selectedDate) {
        uiState.selectedDate.format(dateFormatter).replaceFirstChar { it.uppercase() }
    }

    LaunchedEffect(uiState.selectedDate) {
        val route = detailNavController.currentDestination?.route
        if (route != null && route.startsWith("intervention/")) {
            detailNavController.popBackStack(TourneeDetailPlaceholderRoute, inclusive = false)
        }
    }

    val items = interventions.map { it.toInterventionItem() }

    LaunchedEffect(uiState.errorMessage, items.isNotEmpty()) {
        uiState.errorMessage?.let { msg ->
            if (items.isNotEmpty()) {
                val result =
                    snackbarHostState.showSnackbar(
                        message = msg,
                        actionLabel = "Réessayer",
                        duration = SnackbarDuration.Short,
                    )
                viewModel.dismissError()
                if (result == SnackbarResult.ActionPerformed) {
                    viewModel.pull(force = true)
                }
            }
        }
    }

    Scaffold(
        containerColor = SavioUi.PageBackground,
        snackbarHost = { SavioSnackbarHost(snackbarHostState) },
        floatingActionButton = {
            Box {
                BadgedBox(
                    badge = {
                        if (pendingOfflineInterventionCount > 0) {
                            Badge(
                                containerColor = SavioPalette.Accent,
                                contentColor = SavioPalette.OnAccent,
                            ) {
                                Text(pendingOfflineInterventionCount.toString())
                            }
                        }
                    },
                ) {
                    FloatingActionButton(
                        onClick = { fabMenuExpanded = true },
                        containerColor = SavioPalette.Accent,
                        contentColor = SavioPalette.OnAccent,
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Actions")
                    }
                }
                DropdownMenu(
                    expanded = fabMenuExpanded,
                    onDismissRequest = { fabMenuExpanded = false },
                ) {
                    DropdownMenuItem(
                        text = { Text("Nouveau client") },
                        onClick = {
                            fabMenuExpanded = false
                            onCreateClient()
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("Nouvelle intervention") },
                        onClick = {
                            fabMenuExpanded = false
                            if (isOnline) {
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        "Création connectée : prochainement",
                                        duration = SnackbarDuration.Short,
                                    )
                                }
                            } else {
                                onOfflineIntervention()
                            }
                        },
                    )
                }
            }
        },
    ) { padding ->
        val pullRefreshState = rememberPullToRefreshState()
        PullToRefreshBox(
            isRefreshing = uiState.isSyncing,
            onRefresh = { viewModel.pull(force = true) },
            state = pullRefreshState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragStart = { dragAccumulator = 0f },
                            onHorizontalDrag = { _, dragAmount ->
                                dragAccumulator += dragAmount
                            },
                            onDragEnd = {
                                when {
                                    dragAccumulator < -100f -> {
                                        viewModel.selectDate(uiState.selectedDate.plusDays(1))
                                    }
                                    dragAccumulator > 100f -> {
                                        viewModel.selectDate(uiState.selectedDate.minusDays(1))
                                    }
                                }
                                dragAccumulator = 0f
                            }
                        )
                    }
            ) {
                if (!isOnline && items.isEmpty()) {
                    SavioOfflineBannerSurface()
                }
                resumeCandidate?.let { entity ->
                    ResumeBanner(
                        intervention = entity.toInterventionItem(),
                        onResume = { viewModel.resumeIntervention(onResumeIntervention) },
                        onDismiss = { viewModel.ignoreResumeCandidate() }
                    )
                }
                if (items.isEmpty()) {
                    Column(Modifier.weight(1f).fillMaxWidth()) {
                        SavioTabletTopBar(
                            selectedIntervention = null,
                            currentDate = currentDateLabel,
                            remainingCount = 0,
                            pendingSyncCount = pendingSyncCount,
                            pendingOfflineInterventionCount = pendingOfflineInterventionCount,
                            onPendingOfflineClick = onPendingOfflineList,
                            onSyncCatalog = viewModel::syncCatalog,
                            isCatalogSyncing = uiState.isCatalogSyncing,
                            onRefresh = { viewModel.pull(force = true) },
                            isRefreshing = uiState.isSyncing,
                            isNetworkOnline = isOnline,
                            onLogout = onLogout,
                        )
                        when {
                            !uiState.isSyncing &&
                                items.isEmpty() &&
                                uiState.errorMessage != null -> {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth(),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    SavioNetworkErrorState(onRetry = { viewModel.pull(force = true) })
                                }
                            }
                            uiState.isSyncing && uiState.errorMessage == null -> {
                                SavioTourneeListSkeleton(
                                    Modifier
                                        .weight(1f)
                                        .fillMaxWidth(),
                                )
                            }
                            else -> {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth(),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    SavioEmptyState(
                                        icon = Icons.Outlined.CalendarToday,
                                        title = "Aucune intervention aujourd'hui",
                                        subtitle = "Profitez de votre journée ☀️",
                                    )
                                }
                            }
                        }
                    }
                } else {
                    TourneeAdaptiveLayout(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        interventions = items,
                        detailNavController = detailNavController,
                        onStartIntervention = { interventionId ->
                            parentNavController.navigate(
                                Screen.InterventionActive.createRoute(
                                    interventionId
                                )
                            )
                        },
                        onClientClick = { customerId ->
                            parentNavController.navigate(Screen.ClientDetail.createRoute(customerId))
                        },
                        currentDateLabel = currentDateLabel,
                        pendingSyncCount = pendingSyncCount,
                        pendingOfflineInterventionCount = pendingOfflineInterventionCount,
                        onPendingOfflineList = onPendingOfflineList,
                        onSyncCatalog = viewModel::syncCatalog,
                        isCatalogSyncing = uiState.isCatalogSyncing,
                        onRefresh = { viewModel.pull(force = true) },
                        isRefreshing = uiState.isSyncing,
                        isNetworkOnline = isOnline,
                        onLogout = onLogout,
                    )
                }
            }
        }
    }
}
