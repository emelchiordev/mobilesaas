package re.melchior.saviomobile.ui.screen.intervention

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.VpnKey
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import kotlinx.coroutines.launch
import re.melchior.saviomobile.data.local.entity.EquipmentEntity
import re.melchior.saviomobile.data.local.entity.InterventionEntity
import re.melchior.saviomobile.ui.screen.tournee.displayTypeLabel
import re.melchior.saviomobile.util.formatScheduledAtDate
import re.melchior.saviomobile.util.formatScheduledAtTime
import re.melchior.saviomobile.data.local.entity.InterventionHistoryEntity
import re.melchior.saviomobile.data.local.entity.PhotoEntity
import re.melchior.saviomobile.ui.component.ClientHistoryEmbeddedSection
import re.melchior.saviomobile.ui.component.HistoriquePage
import re.melchior.saviomobile.ui.component.SavioEmptyState
import re.melchior.saviomobile.ui.component.SavioInterventionDetailSkeleton
import re.melchior.saviomobile.ui.component.SavioSnackbarHost
import re.melchior.saviomobile.ui.component.PhotoViewerDialog
import re.melchior.saviomobile.ui.designsystem.SectionDivider
import re.melchior.saviomobile.ui.refonte.SavioHeaderStyle
import re.melchior.saviomobile.ui.refonte.SavioInterventionDetailContent
import re.melchior.saviomobile.ui.refonte.SavioInterventionDetailContent
import re.melchior.saviomobile.ui.refonte.SavioEdgeToEdgeScaffoldInsets
import re.melchior.saviomobile.ui.refonte.SavioNavyHeader
import re.melchior.saviomobile.ui.refonte.SavioRefonteCard
import re.melchior.saviomobile.ui.refonte.SavioRefonteOrangeCtaBar
import re.melchior.saviomobile.ui.refonte.SavioRefonteTabRow
import re.melchior.saviomobile.ui.refonte.SavioStatusPill
import re.melchior.saviomobile.ui.theme.SavioRefonte
import re.melchior.saviomobile.ui.theme.useSavioRefonteUi
import re.melchior.saviomobile.ui.theme.SavioDimens
import re.melchior.saviomobile.ui.theme.SavioPalette
import re.melchior.saviomobile.ui.theme.SavioType
import re.melchior.saviomobile.ui.theme.SavioUi
import re.melchior.saviomobile.ui.theme.SavioInterventionColors
import re.melchior.saviomobile.ui.theme.interventionStatusBadge
import re.melchior.saviomobile.ui.theme.savioNavColor
import re.melchior.saviomobile.ui.theme.savioTabSelectedColor
import re.melchior.saviomobile.ui.theme.savioTabUnselectedColor
import re.melchior.saviomobile.ui.theme.savioTopAppBarColors
import re.melchior.saviomobile.ui.theme.savioTopAppBarContentColor
import re.melchior.saviomobile.ui.viewmodel.PhotoViewModel
import java.io.File
import java.util.Locale

private data class DetailTab(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InterventionDetailScreen(
    onBack: () -> Unit,
    onStartIntervention: (String) -> Unit,
    onClientClick: (customerId: String) -> Unit = {},
    onNavigateToDevisSignature: () -> Unit = {},
    embeddedInMasterDetail: Boolean = false,
    viewModel: InterventionDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val refonte = useSavioRefonteUi()

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissError()
        }
    }

    Scaffold(
        containerColor = if (refonte) SavioRefonte.BgPage else MaterialTheme.colorScheme.background,
        snackbarHost = { SavioSnackbarHost(snackbarHostState) },
        contentWindowInsets = SavioEdgeToEdgeScaffoldInsets,
        topBar = {
            if (!embeddedInMasterDetail) {
                val refonte = useSavioRefonteUi()
                if (refonte) {
                    SavioNavyHeader(
                        title = uiState.intervention?.displayTypeLabel() ?: "Intervention",
                        style = SavioHeaderStyle.Intervention,
                        subtitle =
                            uiState.intervention?.let { intervention ->
                                val time = formatScheduledAtTime(intervention.scheduledAt)
                                "Aujourd'hui · $time"
                            },
                        leading = {
                            IconButton(onClick = onBack) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Retour",
                                    tint = androidx.compose.ui.graphics.Color.White,
                                )
                            }
                        },
                        trailing = {
                            uiState.intervention?.let { intervention ->
                                SavioStatusPill(
                                    status = intervention.status,
                                    syncStatus = intervention.syncStatus,
                                    big = true,
                                    modifier = Modifier.padding(end = 4.dp),
                                )
                            }
                        },
                    )
                } else {
                TopAppBar(
                    colors = savioTopAppBarColors(),
                    title = {
                        Column {
                            Text(
                                text = uiState.intervention?.displayTypeLabel() ?: "Intervention",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Medium,
                                color = savioTopAppBarContentColor(),
                            )
                            uiState.intervention?.let {
                                Text(
                                    text = formatScheduledAtTime(it.scheduledAt),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = savioTopAppBarContentColor(),
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.Filled.ArrowBack,
                                contentDescription = "Retour",
                                tint = savioTopAppBarContentColor(),
                            )
                        }
                    },
                    actions = {
                        uiState.intervention?.let { intervention ->
                            val sync = intervention.syncStatus
                            val st = intervention.status
                            if (sync == "CONFLICT") {
                                Row(
                                    modifier = Modifier
                                        .padding(end = 12.dp)
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(MaterialTheme.colorScheme.errorContainer)
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.error),
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Conflit",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.error,
                                        fontWeight = FontWeight.Medium,
                                    )
                                }
                            } else {
                                val badge =
                                    interventionStatusBadge(
                                        status = st,
                                        syncStatus = sync,
                                    )
                                Row(
                                    modifier = Modifier
                                        .padding(end = 12.dp)
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(badge.background)
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(badge.foreground),
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = badge.label,
                                        fontSize = 12.sp,
                                        color = badge.foreground,
                                        fontWeight = FontWeight.Medium,
                                    )
                                }
                            }
                        }
                    },
                )
                }
            }
        },
        bottomBar = {
            if (!embeddedInMasterDetail) {
                uiState.intervention?.let { intervention ->
                    val isActionable = intervention.status in listOf("scheduled", "in_progress")
                    if (isActionable) {
                        val label =
                            if (intervention.status == "scheduled") {
                                "Démarrer l'intervention"
                            } else {
                                "Reprendre l'intervention"
                            }
                        val onPrimary = {
                            if (intervention.status == "scheduled") viewModel.startIntervention()
                            onStartIntervention(intervention.id)
                        }
                        if (refonte) {
                            SavioRefonteOrangeCtaBar(text = label, onClick = onPrimary)
                        } else {
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                                .navigationBarsPadding(),
                        ) {
                            Button(
                                onClick = onPrimary,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary,
                                ),
                                shape = RoundedCornerShape(28.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = label,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                )
                            }
                        }
                        }
                    }
                }
            }
        },
    ) { padding ->
        when {
            uiState.intervention == null -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    SavioInterventionDetailSkeleton()
                }
            }
            else -> {
                val intervention = uiState.intervention!!

                val isCompleted = intervention.status == "completed"

                val tabs = remember(isCompleted) {
                    buildList {
                        if (isCompleted) add(DetailTab("Rapport", Icons.Filled.Assessment))
                        add(DetailTab("Détail", Icons.Filled.Person))
                        add(DetailTab("Équipements", Icons.Filled.Build))
                        add(DetailTab("Historique", Icons.Filled.History))
                    }
                }

                val pagerState = rememberPagerState(
                    initialPage = 0,
                    pageCount = { tabs.size }
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .background(if (refonte) SavioRefonte.BgPage else MaterialTheme.colorScheme.background),
                ) {
                    val actionable = intervention.status in listOf("scheduled", "in_progress")
                    val contentBottomPad =
                        if (refonte && actionable && !embeddedInMasterDetail) 96.dp else 0.dp
                    val pagePadding = if (refonte) 14.dp else 16.dp
                    val pagerModifier =
                        if (embeddedInMasterDetail && actionable) Modifier.weight(1f).fillMaxWidth()
                        else Modifier.fillMaxSize()

                    if (intervention.status == "pending_validation") {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = SavioInterventionColors.ValidationBg,
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(SavioInterventionColors.ValidationFg)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "En attente de validation par le dispatcher",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = SavioInterventionColors.ValidationFg,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    if (refonte) {
                        SavioRefonteTabRow(
                            tabs = tabs.map { it.label },
                            selectedIndex = pagerState.currentPage,
                            onTabSelected = { index ->
                                scope.launch { pagerState.animateScrollToPage(index) }
                            },
                        )
                    } else {
                    TabRow(
                        selectedTabIndex = pagerState.currentPage,
                        containerColor = MaterialTheme.colorScheme.background,
                        contentColor = savioTabUnselectedColor(),
                        divider = {},
                        indicator = { tabPositions ->
                            val i = pagerState.currentPage
                            if (i < tabPositions.size) {
                                TabRowDefaults.SecondaryIndicator(
                                    modifier = Modifier.tabIndicatorOffset(tabPositions[i]),
                                    height = 3.dp,
                                    color = savioTabSelectedColor(),
                                )
                            }
                        },
                    ) {
                        tabs.forEachIndexed { index, tab ->
                            val selected = pagerState.currentPage == index
                            Tab(
                                selected = selected,
                                onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                                selectedContentColor = savioTabSelectedColor(),
                                unselectedContentColor = savioTabUnselectedColor(),
                                text = {
                                    Text(
                                        text = tab.label,
                                        fontSize = 14.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    )
                                },
                                icon = {
                                    Icon(
                                        imageVector = tab.icon,
                                        contentDescription = tab.label,
                                        modifier = Modifier.size(20.dp),
                                    )
                                },
                            )
                        }
                    }
                    }

                    HorizontalPager(state = pagerState, key = { it }, modifier = pagerModifier) { page ->
                        // Si isCompleted, page 0 = Rapport, sinon page 0 = Détail
                        val adjustedPage = if (isCompleted) page else page + 1

                        when (adjustedPage) {
                            0 -> RapportPage(
                                intervention = intervention,
                                interventionId = intervention.id
                            )
                            1 -> Column(
                                modifier =
                                    Modifier
                                        .fillMaxSize()
                                        .verticalScroll(rememberScrollState())
                                        .padding(pagePadding)
                                        .padding(bottom = contentBottomPad),
                                verticalArrangement = Arrangement.spacedBy(if (refonte) 14.dp else 12.dp),
                            ) {
                                if (refonte) {
                                    SavioInterventionDetailContent(
                                        intervention = intervention,
                                        onClientClick = onClientClick,
                                        onCallClick = { phone ->
                                            context.startActivity(
                                                Intent(Intent.ACTION_DIAL).apply {
                                                    data = Uri.parse("tel:$phone")
                                                },
                                            )
                                        },
                                        onNavigateClick = {
                                            val lat = intervention.unitLatitude
                                            val lng = intervention.unitLongitude
                                            val address =
                                                "${intervention.unitStreet}, ${intervention.unitPostalCode} ${intervention.unitCity}"
                                            val uri =
                                                if (lat != null && lng != null) {
                                                    Uri.parse("geo:$lat,$lng?q=$lat,$lng($address)")
                                                } else {
                                                    Uri.parse("geo:0,0?q=${Uri.encode(address)}")
                                                }
                                            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                                        },
                                    )
                                } else {
                                    DetailPage(
                                        intervention = intervention,
                                        onClientClick = onClientClick,
                                        onCallClick = { phone ->
                                            context.startActivity(
                                                Intent(Intent.ACTION_DIAL).apply {
                                                    data = Uri.parse("tel:$phone")
                                                },
                                            )
                                        },
                                        onNavigateClick = {
                                            val lat = intervention.unitLatitude
                                            val lng = intervention.unitLongitude
                                            val address =
                                                "${intervention.unitStreet}, ${intervention.unitPostalCode} ${intervention.unitCity}"
                                            val uri =
                                                if (lat != null && lng != null) {
                                                    Uri.parse("geo:$lat,$lng?q=$lat,$lng($address)")
                                                } else {
                                                    Uri.parse("geo:0,0?q=${Uri.encode(address)}")
                                                }
                                            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                                        },
                                    )
                                }
                                if (intervention.syncStatus in listOf("COMPLETED", "CONFLICT")) {
                                    SyncStatusCard(
                                        intervention = intervention,
                                        pendingHamonIssue = uiState.pendingInvoiceHamonIssue,
                                        isRetrying = uiState.isSyncRetrying,
                                        onRetrySync = { viewModel.retrySync() },
                                        onDevisSignature = onNavigateToDevisSignature,
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            2 -> Column(
                                modifier =
                                    Modifier
                                        .fillMaxSize()
                                        .verticalScroll(rememberScrollState())
                                        .padding(pagePadding)
                                        .padding(bottom = contentBottomPad),
                                verticalArrangement = Arrangement.spacedBy(if (refonte) 14.dp else 12.dp),
                            ) {
                                if (uiState.equipments.isNotEmpty()) {
                                    PlanningEquipmentsSection(
                                        interventionId = intervention.id,
                                        equipments = uiState.equipments,
                                        refonte = refonte,
                                    )
                                } else {
                                    Box(
                                        modifier =
                                            Modifier
                                                .fillMaxWidth()
                                                .padding(top = 24.dp, bottom = 16.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        SavioEmptyState(
                                            icon = Icons.Outlined.VpnKey,
                                            title = "Aucun équipement enregistré",
                                            subtitle = "Aucun appareil n'est associé à cette intervention.",
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            3 ->
                                if (refonte) {
                                    Column(
                                        modifier =
                                            Modifier
                                                .fillMaxSize()
                                                .verticalScroll(rememberScrollState())
                                                .padding(pagePadding)
                                                .padding(bottom = contentBottomPad),
                                    ) {
                                        ClientHistoryEmbeddedSection(
                                            history = uiState.history,
                                            photoUrls = uiState.historyPhotoUrls,
                                            defaultVisibleCount = 3,
                                            useLazyList = false,
                                        )
                                    }
                                } else {
                                    HistoriquePage(
                                        history = uiState.history,
                                        photoUrls = uiState.historyPhotoUrls,
                                    )
                                }
                            else -> Box(modifier = Modifier.fillMaxSize())
                        }
                    }
                    if (embeddedInMasterDetail && actionable) {
                        InterventionDetailEmbeddedFooter(
                            intervention = intervention,
                            onPause = onBack,
                            onPrimaryClick = {
                                if (intervention.status == "scheduled") viewModel.startIntervention()
                                onStartIntervention(intervention.id)
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InterventionDetailEmbeddedFooter(
    intervention: InterventionEntity,
    onPause: () -> Unit,
    onPrimaryClick: () -> Unit,
) {
    val primaryLabel =
        if (intervention.status == "scheduled") "Démarrer l'intervention" else "Reprendre l'intervention"
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp,
        tonalElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .navigationBarsPadding(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedButton(
                onClick = onPause,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                border = BorderStroke(1.dp, SavioUi.BusinessAccent),
                shape = RoundedCornerShape(10.dp),
            ) {
                Text(
                    text = "Mettre en pause",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
            Button(
                onClick = onPrimaryClick,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                shape = RoundedCornerShape(10.dp),
            ) {
                Text(
                    text = primaryLabel,
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

@Composable
private fun SyncStatusCard(
    intervention: InterventionEntity,
    pendingHamonIssue: Boolean,
    isRetrying: Boolean,
    onRetrySync: () -> Unit,
    onDevisSignature: () -> Unit,
) {
    val isConflict = intervention.syncStatus == "CONFLICT"
    val syncColor =
        if (isConflict) MaterialTheme.colorScheme.error
        else MaterialTheme.colorScheme.primary
    val message =
        when {
            isConflict -> "Conflit de synchronisation — contactez le support si le problème persiste."
            pendingHamonIssue ->
                "La signature Hamon n'a pas été envoyée. Reprenez la signature du devis ou réessayez la synchronisation après correction côté bureau."
            else -> "En attente de synchronisation avec le serveur."
        }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = syncColor.copy(alpha = 0.08f),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(syncColor),
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (isConflict) "Conflit de synchronisation" else "Synchronisation en attente",
                    style = MaterialTheme.typography.bodyMedium,
                    color = syncColor,
                    fontWeight = FontWeight.Medium,
                )
            }
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (!isConflict) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onRetrySync,
                        enabled = !isRetrying,
                    ) {
                        Text(if (isRetrying) "Synchronisation…" else "Réessayer la synchronisation")
                    }
                    if (pendingHamonIssue) {
                        OutlinedButton(onClick = onDevisSignature) {
                            Text("Signature du devis")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(intervention: InterventionEntity, modifier: Modifier = Modifier) {
    val badge =
        interventionStatusBadge(
            status = intervention.status,
            syncStatus = intervention.syncStatus,
        )

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = badge.background,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(badge.foreground)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = badge.label,
                style = MaterialTheme.typography.labelLarge,
                color = badge.foreground,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = intervention.typeLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun plannedDetailInitials(first: String?, last: String?): String {
    val f = first?.trim().orEmpty()
    val l = last?.trim().orEmpty()
    return when {
        f.isNotEmpty() && l.isNotEmpty() ->
            "${f.first().uppercaseChar()}${l.first().uppercaseChar()}"
        f.length >= 2 -> f.take(2).uppercase(Locale.FRANCE)
        f.isNotEmpty() -> f.first().uppercaseChar().toString()
        l.length >= 2 -> l.take(2).uppercase(Locale.FRANCE)
        l.isNotEmpty() -> l.first().uppercaseChar().toString()
        else -> "?"
    }
}

@Composable
private fun DetailPage(
    intervention: InterventionEntity,
    onClientClick: (customerId: String) -> Unit,
    onCallClick: (String) -> Unit,
    onNavigateClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Filled.Build,
                    contentDescription = null,
                    tint = SavioUi.BusinessAccent,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Type d'intervention",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = intervention.typeLabel,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
            if (!intervention.notes.isNullOrBlank()) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outline,
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = null,
                        tint = SavioUi.BusinessAccent,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Notes dispatcher",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            color = SavioInterventionColors.NotesDispatcherBg,
                            shadowElevation = 0.dp,
                        ) {
                            Text(
                                text = intervention.notes,
                                fontSize = 14.sp,
                                color = SavioInterventionColors.NotesDispatcherText,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(10.dp),
                            )
                        }
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outline,
            )

            val customerId = intervention.customerId
            val clientName = if (intervention.customerFirstName != null) {
                "${intervention.customerFirstName} ${intervention.customerLastName}".trim()
            } else {
                "Non renseigné"
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (customerId != null) {
                            Modifier.clickable { onClientClick(customerId) }
                        } else {
                            Modifier
                        },
                    )
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = plannedDetailInitials(
                            intervention.customerFirstName,
                            intervention.customerLastName,
                        ),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SavioUi.BusinessAccent,
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Client",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = clientName.uppercase(Locale.FRANCE),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                if (customerId != null) {
                    Icon(
                        imageVector = Icons.Filled.ChevronRight,
                        contentDescription = "Fiche client",
                        tint = savioNavColor(),
                        modifier = Modifier.size(22.dp),
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outline,
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 4.dp, top = 14.dp, bottom = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = null,
                    tint = SavioUi.BusinessAccent,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Adresse",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = intervention.unitStreet,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    intervention.unitAddressLine2?.let {
                        Text(
                            text = it,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Text(
                        text = "${intervention.unitPostalCode} ${intervention.unitCity}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (!intervention.unitFloor.isNullOrBlank() ||
                        !intervention.unitDoorCode.isNullOrBlank()
                    ) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            intervention.unitFloor?.let {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                ) {
                                    Text(
                                        text = "Étage $it",
                                        fontSize = 11.sp,
                                        color = SavioUi.BusinessAccent,
                                        fontWeight = FontWeight.Medium,
                                    )
                                }
                            }
                            intervention.unitDoorCode?.let {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                ) {
                                    Text(
                                        text = "Code $it",
                                        fontSize = 11.sp,
                                        color = SavioUi.BusinessAccent,
                                        fontWeight = FontWeight.Medium,
                                    )
                                }
                            }
                        }
                    }
                }
                IconButton(onClick = onNavigateClick) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = "Naviguer",
                        tint = SavioUi.BusinessAccent,
                    )
                }
            }

            intervention.customerPhone?.let { phone ->
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outline,
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        onClick = { onCallClick(phone) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline),
                        shadowElevation = 0.dp,
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Call,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = SavioUi.BusinessAccent,
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = phone,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = savioNavColor(),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RapportPage(
    intervention: InterventionEntity,
    interventionId: String,
    photoViewModel: PhotoViewModel = hiltViewModel()
) {
    val photos by photoViewModel.photos.collectAsStateWithLifecycle()
    var selectedPhoto by remember { mutableStateOf<PhotoEntity?>(null) }

    selectedPhoto?.let { photo ->
        PhotoViewerDialog(
            photo = photo,
            onDismiss = { selectedPhoto = null }
        )
    }

    LaunchedEffect(interventionId) {
        photoViewModel.loadPhotos(interventionId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column {
                // Compte rendu
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Assignment,
                        contentDescription = null,
                        tint = SavioUi.BusinessAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Compte rendu",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = intervention.report?.takeIf { it.isNotBlank() }
                                ?: "Aucun compte rendu saisi",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (!intervention.report.isNullOrBlank())
                                FontWeight.Normal else FontWeight.Normal,
                            color = if (!intervention.report.isNullOrBlank())
                                MaterialTheme.colorScheme.onSurface
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Date de clôture
                intervention.completedAt?.let { completedAt ->
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = SavioUi.BusinessAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Clôturée le",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${formatScheduledAtDate(completedAt)} à ${formatScheduledAtTime(completedAt)}",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Photos
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.CameraAlt,
                        contentDescription = null,
                        tint = SavioUi.BusinessAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Photos",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (photos.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(SavioUi.BusinessAccent)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = photos.size.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                color = SavioPalette.OnAccent,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                if (photos.isNotEmpty()) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        photos.chunked(3).forEach { row ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                row.forEach { photo ->
                                    AsyncImage(
                                        model = if (photo.syncStatus == "SYNCED" && photo.remoteUrl != null)
                                            photo.remoteUrl
                                        else
                                            File(photo.localPath),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { selectedPhoto = photo }
                                    )
                                }
                                repeat(3 - row.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                } else {
                    SavioEmptyState(
                        icon = Icons.Outlined.PhotoCamera,
                        title = "Aucune photo ajoutée",
                        subtitle = "Aucune image n'a été jointe au rapport.",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}



@Composable
private fun PlanningEquipmentsSection(
    interventionId: String,
    equipments: List<EquipmentEntity>,
    refonte: Boolean = false,
) {
    val newEquipmentIds = emptySet<String>()
    if (refonte) {
        SavioRefonteCard {
            PlanningEquipmentsSectionBody(
                interventionId = interventionId,
                equipments = equipments,
                newEquipmentIds = newEquipmentIds,
                refonte = true,
            )
        }
        return
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        PlanningEquipmentsSectionBody(
            interventionId = interventionId,
            equipments = equipments,
            newEquipmentIds = newEquipmentIds,
            refonte = false,
        )
    }
}

@Composable
private fun PlanningEquipmentsSectionBody(
    interventionId: String,
    equipments: List<EquipmentEntity>,
    newEquipmentIds: Set<String>,
    refonte: Boolean,
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = if (refonte) 15.dp else 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.Build,
                contentDescription = null,
                tint = if (refonte) SavioRefonte.Navy else SavioUi.BusinessAccent,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Équipements",
                fontSize = if (refonte) 16.sp else 14.sp,
                color = if (refonte) SavioRefonte.Ink else MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .defaultMinSize(minWidth = 20.dp, minHeight = 20.dp)
                    .clip(CircleShape)
                    .background(if (refonte) SavioRefonte.Tint else SavioUi.BusinessAccent),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = equipments.size.toString(),
                    fontSize = 11.sp,
                    color = if (refonte) SavioRefonte.Navy else Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                )
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = if (refonte) 15.dp else 16.dp),
            thickness = 1.dp,
            color = if (refonte) SavioRefonte.Line else MaterialTheme.colorScheme.outline,
        )

            val allActive = equipments.filter { it.typeCode != "replaced" }
            val allReplaced = equipments.filter { it.typeCode == "replaced" }
            val rootEquipments = allActive
                .filter { it.parentEquipmentId == null }
                .sortedWith(
                    compareByDescending<EquipmentEntity> { it.isPrimary }
                        .thenBy { it.order },
                )
            val childrenByParent = allActive
                .filter { it.parentEquipmentId != null }
                .groupBy { it.parentEquipmentId }
                .mapValues { (_, list) ->
                    list.sortedWith(
                        compareBy<EquipmentEntity> { it.order }.thenBy { it.id },
                    )
                }
            val orphans = allActive.filter { eq ->
                eq.parentEquipmentId != null &&
                    rootEquipments.none { it.id == eq.parentEquipmentId }
            }
            val rootReplaced = allReplaced
                .filter { it.parentEquipmentId == null }
                .sortedWith(
                    compareBy<EquipmentEntity> { it.order }.thenBy { it.id },
                )

            val hybrideGroups = computeHybrideGroups(
                equipmentRoots = rootEquipments,
                allEquipments = allActive,
                childrenByParent = childrenByParent,
            )
            val hybrideIds = hybrideEquipmentIds(hybrideGroups)
            val normalRootAndOrphans = (rootEquipments + orphans).filter { it.id !in hybrideIds }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                hybrideGroups.forEach { group ->
                    HybrideGroupCard(
                        group = group,
                        newEquipmentIds = newEquipmentIds,
                        interventionId = interventionId,
                        interactive = false,
                        onEquipementClick = { _, _ -> },
                    )
                }
                normalRootAndOrphans.forEach { parent ->
                    InterventionEquipmentGroupCard(
                        parent = parent,
                        children = childrenByParent[parent.id].orEmpty(),
                        newEquipmentIds = newEquipmentIds,
                        interventionId = interventionId,
                        interactive = false,
                        onEquipementClick = { _, _ -> },
                    )
                }
                rootReplaced.forEach { parent ->
                    InterventionEquipmentGroupCard(
                        parent = parent,
                        children = childrenByParent[parent.id].orEmpty(),
                        newEquipmentIds = newEquipmentIds,
                        interventionId = interventionId,
                        interactive = false,
                        onEquipementClick = { _, _ -> },
                    )
                }
            }
    }
}
