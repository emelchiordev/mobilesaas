package re.melchior.saviomobile.ui.screen.tournee

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.material3.MaterialTheme
import re.melchior.saviomobile.ui.refonte.SavioRefonteFab
import re.melchior.saviomobile.ui.theme.SavioUi
import re.melchior.saviomobile.ui.theme.useSavioRefonteUi
import re.melchior.saviomobile.ui.utils.rememberIsNetworkOnline
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import re.melchior.saviomobile.ui.designsystem.BottomNavBar
import re.melchior.saviomobile.ui.screen.client.mainBottomNavItems
import re.melchior.saviomobile.ui.theme.SavioPalette

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TourneeTabletScreen(
    parentNavController: NavController,
    onResumeIntervention: (String) -> Unit = {},
    onLogout: () -> Unit = {},
    onNewIntervention: () -> Unit = {},
    onOfflineIntervention: () -> Unit = {},
    onPendingOfflineList: () -> Unit = {},
    pendingSnackbar: String? = null,
    onConsumePendingSnackbar: () -> Unit = {},
    pendingFocusDateMillis: Long? = null,
    onConsumePendingFocusDate: () -> Unit = {},
    showBottomNav: Boolean = false,
    bottomNavSelectedIndex: Int = 0,
    onBottomNavSelect: (Int) -> Unit = {},
    viewModel: TourneeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val interventions by viewModel.interventions.collectAsStateWithLifecycle()
    val followUpInterventions by viewModel.followUpInterventions.collectAsStateWithLifecycle()
    val pendingCreating by viewModel.pendingCreatingForDate.collectAsStateWithLifecycle()
    val pendingSyncCount by viewModel.pendingSyncCount.collectAsStateWithLifecycle()
    val resumeCandidate by viewModel.resumeCandidate.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var dragAccumulator by remember { mutableFloatStateOf(0f) }
    val detailNavController = rememberNavController()
    val isOnline = rememberIsNetworkOnline()
    val pendingOfflineInterventionCount by viewModel.pendingOfflineInterventionCount.collectAsStateWithLifecycle()
    var showPendingCreationSheet by remember { mutableStateOf(false) }

    LaunchedEffect(pendingSnackbar, pendingFocusDateMillis) {
        var focusedDate = false
        pendingFocusDateMillis?.let { millis ->
            val date =
                Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
            viewModel.selectDate(date)
            onConsumePendingFocusDate()
            focusedDate = true
        }
        pendingSnackbar?.let { msg ->
            snackbarHostState.showSnackbar(msg, duration = SnackbarDuration.Short)
            onConsumePendingSnackbar()
            if (!focusedDate) {
                viewModel.pull(force = true)
            }
        }
    }

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
    val followUpItems = followUpInterventions.map { it.toInterventionItem() }
    val hasListContent = items.isNotEmpty() || followUpItems.isNotEmpty() || pendingCreating.isNotEmpty()

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { msg ->
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

    Scaffold(
        containerColor =
            if (useSavioRefonteUi()) MaterialTheme.colorScheme.background else SavioUi.PageBackground,
        snackbarHost = { SavioSnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (useSavioRefonteUi()) {
                SavioRefonteFab(onClick = onNewIntervention)
            } else {
                FloatingActionButton(
                    onClick = onNewIntervention,
                    containerColor = SavioPalette.Accent,
                    contentColor = SavioPalette.OnAccent,
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Nouvelle intervention")
                }
            }
        },
        bottomBar = {
            if (showBottomNav) {
                BottomNavBar(
                    items = mainBottomNavItems(),
                    selectedIndex = bottomNavSelectedIndex,
                    onSelect = onBottomNavSelect,
                )
            }
        },
    ) { padding ->
        val pullRefreshState = rememberPullToRefreshState()
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            PullToRefreshBox(
                isRefreshing = uiState.isSyncing,
                onRefresh = { viewModel.pull(force = true) },
                state = pullRefreshState,
                modifier = Modifier.fillMaxSize(),
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
                if (!isOnline && !hasListContent) {
                    SavioOfflineBannerSurface()
                }
                resumeCandidate?.let { entity ->
                    ResumeBanner(
                        intervention = entity.toInterventionItem(),
                        onResume = { viewModel.resumeIntervention(onResumeIntervention) },
                        onDismiss = { viewModel.ignoreResumeCandidate() }
                    )
                }
                if (!hasListContent) {
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
                        followUpItems = followUpItems,
                        pendingCreating = pendingCreating,
                        onPendingCreatingClick = { showPendingCreationSheet = true },
                        detailNavController = detailNavController,
                        onStartIntervention = { interventionId ->
                            parentNavController.navigate(
                                Screen.InterventionActive.createRoute(
                                    interventionId
                                )
                            )
                        },
                        onClientClick = { customerId ->
                            parentNavController.navigate(
                                Screen.ClientDetail.createRoute(customerId = customerId),
                            )
                        },
                        onNavigateToDevisSignature = { interventionId ->
                            parentNavController.navigate(
                                Screen.DevisSignature.createRoute(interventionId),
                            )
                        },
                        currentDateLabel = currentDateLabel,
                        selectedDate = uiState.selectedDate,
                        dateSubtitle =
                            run {
                                val total = items.size + pendingCreating.size
                                val inProgress =
                                    items.count {
                                        it.status == "in_progress" || it.syncStatus == "IN_PROGRESS"
                                    }
                                when {
                                    total == 0 -> "Aucune intervention"
                                    inProgress > 0 -> "$total interventions · $inProgress en cours"
                                    total == 1 -> "1 intervention"
                                    else -> "$total interventions"
                                }
                            },
                        onPreviousDay = { viewModel.selectDate(uiState.selectedDate.minusDays(1)) },
                        onNextDay = { viewModel.selectDate(uiState.selectedDate.plusDays(1)) },
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
            if (showPendingCreationSheet) {
                PendingCreationInfoSheet(onDismiss = { showPendingCreationSheet = false })
            }
        }
    }
}
