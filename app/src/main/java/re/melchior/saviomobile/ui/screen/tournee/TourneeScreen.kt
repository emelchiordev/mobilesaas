package re.melchior.saviomobile.ui.screen.tournee

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import re.melchior.saviomobile.ui.component.SavioEmptyState
import re.melchior.saviomobile.ui.component.SavioNetworkErrorState
import re.melchior.saviomobile.ui.component.SavioOfflineBannerSurface
import re.melchior.saviomobile.ui.component.SavioSnackbarHost
import re.melchior.saviomobile.ui.component.SavioTourneeListSkeleton
import re.melchior.saviomobile.ui.theme.SavioPalette
import re.melchior.saviomobile.ui.theme.SavioUi
import re.melchior.saviomobile.ui.theme.interventionStatusBadge
import re.melchior.saviomobile.ui.theme.savioTopAppBarColors
import re.melchior.saviomobile.ui.theme.savioTopAppBarContentColor
import re.melchior.saviomobile.ui.utils.rememberIsNetworkOnline
import re.melchior.saviomobile.data.local.entity.InterventionEntity
import re.melchior.saviomobile.util.formatPlanningLabel
import re.melchior.saviomobile.util.parseInterventionTimeSlot
import re.melchior.saviomobile.util.formatScheduledAtTime
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import re.melchior.saviomobile.ui.designsystem.BottomNavBar
import re.melchior.saviomobile.ui.refonte.SavioEdgeToEdgeScaffoldInsets
import re.melchior.saviomobile.ui.refonte.SavioDateNavigator
import re.melchior.saviomobile.ui.refonte.SavioHeaderStyle
import re.melchior.saviomobile.ui.refonte.SavioNavyHeader
import re.melchior.saviomobile.ui.refonte.SavioPlanningCard
import re.melchior.saviomobile.ui.refonte.SavioRefonteFab
import re.melchior.saviomobile.ui.screen.client.mainBottomNavItems
import re.melchior.saviomobile.ui.theme.useSavioRefonteUi
import kotlinx.coroutines.launch

import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TourneeScreen(
    onInterventionClick: (String) -> Unit,
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
    val pendingSyncCount by viewModel.pendingSyncCount.collectAsStateWithLifecycle()
    val pendingOfflineInterventionCount by viewModel.pendingOfflineInterventionCount.collectAsStateWithLifecycle()
    val resumeCandidate by viewModel.resumeCandidate.collectAsStateWithLifecycle()
    val pendingCreating by viewModel.pendingCreatingForDate.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var dragAccumulator by remember { mutableFloatStateOf(0f) }
    var showPendingCreationSheet by remember { mutableStateOf(false) }
    val isOnline = rememberIsNetworkOnline()
    val scope = rememberCoroutineScope()

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

    val refonte = useSavioRefonteUi()
    Scaffold(
        containerColor =
            if (refonte) MaterialTheme.colorScheme.background else SavioUi.PageBackground,
        snackbarHost = { SavioSnackbarHost(snackbarHostState) },
        contentWindowInsets = if (refonte) SavioEdgeToEdgeScaffoldInsets else androidx.compose.material3.ScaffoldDefaults.contentWindowInsets,
        topBar = {
            if (refonte) {
                SavioNavyHeader(
                    title = "Planning",
                    style = SavioHeaderStyle.Primary,
                    actions = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            PlanningTopBarActions(
                                uiState = uiState,
                                isOnline = isOnline,
                                pendingOfflineInterventionCount = pendingOfflineInterventionCount,
                                pendingSyncCount = pendingSyncCount,
                                onLogout = onLogout,
                                onPendingOfflineList = onPendingOfflineList,
                                onSyncCatalog = viewModel::syncCatalog,
                                onRefresh = { viewModel.pull(force = true) },
                                contentColor = androidx.compose.ui.graphics.Color.White,
                            )
                        }
                    },
                )
            } else {
            TopAppBar(
                title = {
                    Text(
                        "Planning",
                        color = savioTopAppBarContentColor(),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                    )
                },
                colors = savioTopAppBarColors(),
                actions = {
                    IconButton(
                        onClick = onLogout,
                        enabled = !uiState.isSyncing && !uiState.isCatalogSyncing,
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Se déconnecter",
                            tint = savioTopAppBarContentColor(),
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    if (pendingOfflineInterventionCount > 0) {
                        BadgedBox(
                            badge = {
                                Badge(
                                    containerColor = SavioPalette.Accent,
                                    contentColor = SavioPalette.OnAccent,
                                ) { Text(pendingOfflineInterventionCount.toString()) }
                            },
                        ) {
                            IconButton(onClick = onPendingOfflineList) {
                                Text("☁️", fontSize = 20.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    // Badge interventions en attente de sync
                    if (pendingSyncCount > 0) {
                        BadgedBox(
                            badge = {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.error,
                                    contentColor = MaterialTheme.colorScheme.onError,
                                ) { Text(pendingSyncCount.toString()) }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Sync,
                                contentDescription = "Sync en attente",
                                tint = savioTopAppBarContentColor(),
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    IconButton(
                        onClick = viewModel::syncCatalog,
                        enabled = isOnline && !uiState.isCatalogSyncing && !uiState.isSyncing
                    ) {
                        if (uiState.isCatalogSyncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = savioTopAppBarContentColor(),
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.LibraryBooks,
                                contentDescription = "Synchroniser le catalogue",
                                tint = savioTopAppBarContentColor(),
                            )
                        }
                    }
                    // Bouton refresh manuel
                    IconButton(
                        onClick = { viewModel.pull(force = true) },
                        enabled = isOnline && !uiState.isSyncing
                    ) {
                        if (uiState.isSyncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = savioTopAppBarContentColor(),
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = "Rafraîchir",
                                tint = savioTopAppBarContentColor(),
                            )
                        }
                    }
                }
            )
            }
        },
        floatingActionButton = {
            if (refonte) {
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
    )  { padding ->
        val pullRefreshState = rememberPullToRefreshState()

        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            PullToRefreshBox(
                isRefreshing = uiState.isSyncing,
                onRefresh = { viewModel.pull(force = true) },
                state = pullRefreshState,
                modifier = Modifier.fillMaxSize(),
            ) {
          //  var dragAccumulator = remember { 0f }

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
                                        // Swipe gauche → jour suivant
                                        viewModel.selectDate(
                                            uiState.selectedDate.plusDays(1)
                                        )
                                    }
                                    dragAccumulator > 100f -> {
                                        // Swipe droite → jour précédent
                                        viewModel.selectDate(
                                            uiState.selectedDate.minusDays(1)
                                        )
                                    }
                                }
                                dragAccumulator = 0f
                            }
                        )
                    }
            ) {
                // Barre de progression sync — supprime le LinearProgressIndicator
                // PullToRefreshBox gère déjà l'indicateur visuel

                if (!isOnline) {
                    SavioOfflineBannerSurface()
                }

                if (refonte) {
                    val totalCount = interventions.size + pendingCreating.size
                    val inProgressCount =
                        interventions.count {
                            it.status == "in_progress" || it.syncStatus == "IN_PROGRESS"
                        }
                    val dateSubtitle =
                        when {
                            totalCount == 0 -> "Aucune intervention"
                            inProgressCount > 0 ->
                                "$totalCount interventions · $inProgressCount en cours"
                            totalCount == 1 -> "1 intervention"
                            else -> "$totalCount interventions"
                        }
                    SavioDateNavigator(
                        selectedDate = uiState.selectedDate,
                        isRefreshing = uiState.isDatePullRefreshing,
                        subtitle = dateSubtitle,
                        onPreviousDay = {
                            viewModel.selectDate(uiState.selectedDate.minusDays(1))
                        },
                        onNextDay = {
                            viewModel.selectDate(uiState.selectedDate.plusDays(1))
                        },
                    )
                } else {
                    DateSelector(
                        selectedDate = uiState.selectedDate,
                        isRefreshing = uiState.isDatePullRefreshing,
                        onPreviousDay = {
                            viewModel.selectDate(uiState.selectedDate.minusDays(1))
                        },
                        onNextDay = {
                            viewModel.selectDate(uiState.selectedDate.plusDays(1))
                        },
                    )
                }

                resumeCandidate?.let { entity ->
                    ResumeBanner(
                        intervention = entity.toInterventionItem(),
                        onResume = {
                            viewModel.resumeIntervention(onResumeIntervention)
                        },
                        onDismiss = { viewModel.ignoreResumeCandidate() }
                    )
                }

                // Liste interventions
                val hasListContent = interventions.isNotEmpty() || pendingCreating.isNotEmpty()

                when {
                    !uiState.isSyncing &&
                        !hasListContent &&
                        uiState.errorMessage != null -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            SavioNetworkErrorState(onRetry = { viewModel.pull(force = true) })
                        }
                    }
                    uiState.isSyncing &&
                        interventions.isEmpty() &&
                        uiState.errorMessage == null -> {
                        SavioTourneeListSkeleton(Modifier.fillMaxSize())
                    }
                    !hasListContent &&
                        !uiState.isSyncing &&
                        uiState.errorMessage == null -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            SavioEmptyState(
                                icon = Icons.Outlined.CalendarToday,
                                title = "Aucune intervention aujourd'hui",
                                subtitle = "Profitez de votre journée ☀️",
                            )
                        }
                    }
                    else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item { Spacer(modifier = Modifier.height(8.dp)) }
                        items(
                            items = interventions,
                            key = { it.id }
                        ) { intervention ->
                            if (refonte) {
                                SavioPlanningCard(
                                    intervention = intervention,
                                    onClick = { onInterventionClick(intervention.id) },
                                )
                            } else {
                                InterventionCard(
                                    intervention = intervention,
                                    onClick = { onInterventionClick(intervention.id) },
                                )
                            }
                        }
                        items(
                            items = pendingCreating,
                            key = { it.localId }
                        ) { pending ->
                            PendingCreatingInterventionCard(
                                pending = pending,
                                onClick = { showPendingCreationSheet = true },
                            )
                        }
                        item { Spacer(modifier = Modifier.height(8.dp)) }
                    }
                    }
                }
            }
        }
            if (showPendingCreationSheet) {
                PendingCreationInfoSheet(onDismiss = { showPendingCreationSheet = false })
            }
        }
    }

}

@Composable
private fun DateSelector(
    selectedDate: LocalDate,
    isRefreshing: Boolean,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
) {
    val isToday = selectedDate == LocalDate.now()
    val formatter = DateTimeFormatter.ofPattern("EEEE d MMMM", Locale.FRENCH)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onPreviousDay) {
            Icon(
                Icons.Filled.ChevronLeft,
                contentDescription = "Jour précédent",
                tint = SavioUi.BusinessAccent,
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (isToday) {
                Text(
                    text = "Aujourd'hui",
                    fontSize = 13.sp,
                    color = SavioUi.BusinessAccent,
                    fontWeight = FontWeight.Bold,
                )
            }
            Text(
                text = selectedDate.format(formatter).replaceFirstChar { it.uppercase() },
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (isRefreshing) {
                Spacer(modifier = Modifier.height(4.dp))
                CircularProgressIndicator(
                    modifier = Modifier.size(14.dp),
                    strokeWidth = 2.dp,
                    color = SavioUi.BusinessAccent,
                )
            }
        }

        IconButton(onClick = onNextDay) {
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = "Jour suivant",
                tint = SavioUi.BusinessAccent,
            )
        }
    }
}

@Composable
private fun InterventionCard(
    intervention: InterventionEntity,
    onClick: () -> Unit
) {
    val statusBadge =
        interventionStatusBadge(
            status = intervention.status,
            syncStatus = intervention.syncStatus,
        )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(0.5.dp, SavioUi.CardBorder),
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = formatPlanningLabel(
                            parseInterventionTimeSlot(intervention.timeSlot),
                            intervention.scheduledAt,
                        ),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = SavioUi.BusinessAccent,
                    )
                    if (intervention.isUrgent) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(MaterialTheme.colorScheme.errorContainer)
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                        ) {
                            Text(
                                text = "Urgent",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(SavioUi.ChipBackground)
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                    ) {
                        Text(
                            text = intervention.typeLabel,
                            fontSize = 11.sp,
                            color = SavioUi.BusinessAccent,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(statusBadge.background)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = statusBadge.label,
                        fontSize = 11.sp,
                        color = statusBadge.foreground,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }

            intervention.conflictBannerText()?.let { banner ->
                Spacer(modifier = Modifier.height(8.dp))
                val bannerColors =
                    when (intervention.syncStatus) {
                        "CONFLICT_VERSION" ->
                            SavioUi.StatusTintBg to SavioPalette.Accent
                        else ->
                            MaterialTheme.colorScheme.errorContainer to
                                MaterialTheme.colorScheme.onErrorContainer
                    }
                Surface(
                    color = bannerColors.first,
                    shape = RoundedCornerShape(8.dp),
                    shadowElevation = 0.dp,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            Icons.Filled.Warning,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = bannerColors.second,
                        )
                        Text(
                            banner,
                            fontSize = 10.sp,
                            color = bannerColors.second,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (intervention.customerFirstName != null) {
                Text(
                    text = "${intervention.customerFirstName} ${intervention.customerLastName}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = intervention.unitStreet,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "${intervention.unitPostalCode} ${intervention.unitCity}",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            intervention.number?.let {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = it,
                    fontSize = 11.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                )
            }

            if (!intervention.unitFloor.isNullOrBlank() ||
                !intervention.unitDoorCode.isNullOrBlank()
            ) {
                Spacer(modifier = Modifier.height(4.dp))
                Row {
                    intervention.unitFloor?.let {
                        Text(
                            text = "Étage : $it",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    intervention.unitDoorCode?.let {
                        Text(
                            text = "Code : $it",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlanningTopBarActions(
    uiState: TourneeUiState,
    isOnline: Boolean,
    pendingOfflineInterventionCount: Int,
    pendingSyncCount: Int,
    onLogout: () -> Unit,
    onPendingOfflineList: () -> Unit,
    onSyncCatalog: () -> Unit,
    onRefresh: () -> Unit,
    contentColor: androidx.compose.ui.graphics.Color,
) {
    if (pendingOfflineInterventionCount > 0) {
        BadgedBox(
            badge = {
                Badge(
                    containerColor = SavioPalette.Accent,
                    contentColor = SavioPalette.OnAccent,
                ) { Text(pendingOfflineInterventionCount.toString()) }
            },
        ) {
            IconButton(onClick = onPendingOfflineList) {
                Text("☁️", fontSize = 20.sp)
            }
        }
    }
    if (pendingSyncCount > 0) {
        BadgedBox(
            badge = {
                Badge(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError,
                ) { Text(pendingSyncCount.toString()) }
            },
        ) {
            Icon(
                imageVector = Icons.Filled.Sync,
                contentDescription = "Sync en attente",
                tint = contentColor,
            )
        }
    }
    IconButton(
        onClick = onSyncCatalog,
        enabled = isOnline && !uiState.isCatalogSyncing && !uiState.isSyncing,
    ) {
        if (uiState.isCatalogSyncing) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = contentColor,
            )
        } else {
            Icon(
                imageVector = Icons.Filled.LibraryBooks,
                contentDescription = "Synchroniser le catalogue",
                tint = contentColor,
            )
        }
    }
    IconButton(
        onClick = onRefresh,
        enabled = isOnline && !uiState.isSyncing,
    ) {
        if (uiState.isSyncing) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = contentColor,
            )
        } else {
            Icon(
                imageVector = Icons.Filled.Refresh,
                contentDescription = "Rafraîchir",
                tint = contentColor,
            )
        }
    }
    IconButton(
        onClick = onLogout,
        enabled = !uiState.isSyncing && !uiState.isCatalogSyncing,
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.Logout,
            contentDescription = "Se déconnecter",
            tint = contentColor,
        )
    }
}
