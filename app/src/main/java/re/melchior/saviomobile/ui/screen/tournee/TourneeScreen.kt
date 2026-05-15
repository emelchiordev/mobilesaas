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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
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
import re.melchior.saviomobile.ui.utils.rememberIsNetworkOnline
import re.melchior.saviomobile.data.local.entity.InterventionEntity
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
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
    onCreateClient: () -> Unit = {},
    onOfflineIntervention: () -> Unit = {},
    onPendingOfflineList: () -> Unit = {},
    viewModel: TourneeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val interventions by viewModel.interventions.collectAsStateWithLifecycle()
    val pendingSyncCount by viewModel.pendingSyncCount.collectAsStateWithLifecycle()
    val pendingOfflineInterventionCount by viewModel.pendingOfflineInterventionCount.collectAsStateWithLifecycle()
    val resumeCandidate by viewModel.resumeCandidate.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var dragAccumulator by remember { mutableFloatStateOf(0f) }
    val isOnline = rememberIsNetworkOnline()
    var fabMenuExpanded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState.errorMessage, interventions.isNotEmpty()) {
        uiState.errorMessage?.let { msg ->
            if (interventions.isNotEmpty()) {
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
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Ma tournée",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.onBackground,
                ),
                actions = {
                    IconButton(
                        onClick = onLogout,
                        enabled = !uiState.isSyncing && !uiState.isCatalogSyncing,
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Se déconnecter",
                            tint = MaterialTheme.colorScheme.onBackground,
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
                                tint = MaterialTheme.colorScheme.onBackground,
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
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.LibraryBooks,
                                contentDescription = "Synchroniser le catalogue",
                                tint = MaterialTheme.colorScheme.onBackground,
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
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = "Rafraîchir",
                                tint = MaterialTheme.colorScheme.onBackground,
                            )
                        }
                    }
                }
            )
        },
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
    )  { padding ->
        val pullRefreshState = rememberPullToRefreshState()

        PullToRefreshBox(
            isRefreshing = uiState.isSyncing,
            onRefresh = { viewModel.pull(force = true) },
            state = pullRefreshState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
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

                // Sélecteur de date
                DateSelector(
                    selectedDate = uiState.selectedDate,
                    onPreviousDay = {
                        viewModel.selectDate(uiState.selectedDate.minusDays(1))
                    },
                    onNextDay = {
                        viewModel.selectDate(uiState.selectedDate.plusDays(1))
                    }
                )

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
                when {
                    !uiState.isSyncing &&
                        interventions.isEmpty() &&
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
                    interventions.isEmpty() &&
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
                            InterventionCard(
                                intervention = intervention,
                                onClick = { onInterventionClick(intervention.id) }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(8.dp)) }
                    }
                    }
                }
            }
        }
    }

}

@Composable
private fun DateSelector(
    selectedDate: LocalDate,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit
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
                tint = SavioUi.Blue,
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (isToday) {
                Text(
                    text = "Aujourd'hui",
                    fontSize = 13.sp,
                    color = SavioUi.Blue,
                    fontWeight = FontWeight.Bold,
                )
            }
            Text(
                text = selectedDate.format(formatter).replaceFirstChar { it.uppercase() },
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        IconButton(onClick = onNextDay) {
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = "Jour suivant",
                tint = SavioUi.Blue,
            )
        }
    }
}

@Composable
private fun InterventionCard(
    intervention: InterventionEntity,
    onClick: () -> Unit
) {
    val (statusBadgeBg, statusBadgeText) = when {
        intervention.status == "scheduled" ->
            SavioUi.PlanifListBadgeBg to SavioUi.PlanifListBadgeFg
        intervention.syncStatus == "CONFLICT" ->
            MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
        intervention.syncStatus == "SYNCED" && intervention.status == "completed" ->
            SavioPalette.SuccessDark to SavioPalette.Success
        intervention.syncStatus == "IN_PROGRESS" || intervention.status == "in_progress" ->
            SavioPalette.WarningTintBg to SavioPalette.Accent
        intervention.syncStatus == "COMPLETED" ->
            SavioPalette.WarningTintBg to SavioPalette.Accent
        else ->
            SavioPalette.SurfaceElevated to SavioPalette.TextSecondary
    }

    val statusText = when {
        intervention.syncStatus == "CONFLICT" -> "Conflit"
        intervention.syncStatus == "COMPLETED" -> "En attente"
        intervention.syncStatus == "IN_PROGRESS" -> "En cours"
        intervention.syncStatus == "SYNCED" && intervention.status == "completed" -> "Terminée"
        intervention.status == "pending_validation" -> "À valider"
        intervention.status == "scheduled" -> "Planifiée"
        else -> intervention.status
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = SavioPalette.SurfaceCard,
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
                        text = intervention.scheduledAt
                            .substringAfter("T")
                            .substring(0, 5),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = SavioUi.Blue,
                    )
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
                            color = SavioUi.Blue,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(statusBadgeBg)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = statusText,
                        fontSize = 11.sp,
                        color = statusBadgeText,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }

            if (intervention.syncStatus == "CONFLICT") {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
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
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                        )
                        Text(
                            "Conflit",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onErrorContainer,
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
