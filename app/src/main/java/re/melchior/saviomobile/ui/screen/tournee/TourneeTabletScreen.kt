package re.melchior.saviomobile.ui.screen.tournee

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import re.melchior.saviomobile.R
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TourneeTabletScreen(
    parentNavController: NavController,
    onResumeIntervention: (String) -> Unit = {},
    viewModel: TourneeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val interventions by viewModel.interventions.collectAsStateWithLifecycle()
    val pendingSyncCount by viewModel.pendingSyncCount.collectAsStateWithLifecycle()
    val resumeCandidate by viewModel.resumeCandidate.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var dragAccumulator by remember { mutableFloatStateOf(0f) }
    val detailNavController = rememberNavController()

    val dateFormatter = remember {
        DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.FRENCH)
    }
    val currentDateLabel = remember(uiState.selectedDate) {
        uiState.selectedDate.format(dateFormatter).replaceFirstChar { it.uppercase() }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissError()
        }
    }

    LaunchedEffect(uiState.selectedDate) {
        val route = detailNavController.currentDestination?.route
        if (route != null && route.startsWith("intervention/")) {
            detailNavController.popBackStack(TourneeDetailPlaceholderRoute, inclusive = false)
        }
    }

    val items = interventions.map { it.toInterventionItem() }

    Scaffold(
        containerColor = colorResource(R.color.screen_bg),
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        val pullRefreshState = rememberPullToRefreshState()
        PullToRefreshBox(
            isRefreshing = uiState.isSyncing,
            onRefresh = viewModel::pull,
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
                resumeCandidate?.let { entity ->
                    ResumeBanner(
                        intervention = entity.toInterventionItem(),
                        onResume = { viewModel.resumeIntervention(onResumeIntervention) },
                        onDismiss = { viewModel.ignoreResumeCandidate() }
                    )
                }
                if (items.isEmpty() && !uiState.isSyncing) {
                    Column(Modifier.weight(1f).fillMaxWidth()) {
                        SavioTabletTopBar(
                            selectedIntervention = null,
                            currentDate = currentDateLabel,
                            remainingCount = 0,
                            pendingSyncCount = pendingSyncCount,
                            onSyncCatalog = viewModel::syncCatalog,
                            isCatalogSyncing = uiState.isCatalogSyncing,
                            onRefresh = viewModel::pull,
                            isRefreshing = uiState.isSyncing,
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Filled.CalendarToday,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Aucune intervention ce jour",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
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
                                re.melchior.saviomobile.ui.navigation.Screen.InterventionActive.createRoute(
                                    interventionId
                                )
                            )
                        },
                        currentDateLabel = currentDateLabel,
                        pendingSyncCount = pendingSyncCount,
                        onSyncCatalog = viewModel::syncCatalog,
                        isCatalogSyncing = uiState.isCatalogSyncing,
                        onRefresh = viewModel::pull,
                        isRefreshing = uiState.isSyncing,
                    )
                }
            }
        }
    }
}
