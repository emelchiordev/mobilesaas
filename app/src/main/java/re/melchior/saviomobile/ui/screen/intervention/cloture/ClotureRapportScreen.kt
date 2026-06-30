package re.melchior.saviomobile.ui.screen.intervention.cloture

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import re.melchior.saviomobile.data.remote.dto.InterventionTypeDto
import re.melchior.saviomobile.data.remote.dto.stableKey
import re.melchior.saviomobile.ui.component.SavioSnackbarHost
import re.melchior.saviomobile.ui.refonte.SavioEdgeToEdgeScaffoldInsets
import re.melchior.saviomobile.ui.refonte.SavioClotureAiButton
import re.melchior.saviomobile.ui.refonte.SavioClotureClientCard
import re.melchior.saviomobile.ui.refonte.SavioClotureHeader
import re.melchior.saviomobile.ui.refonte.SavioClotureNextCtaBar
import re.melchior.saviomobile.ui.refonte.SavioClotureNoticeCard
import re.melchior.saviomobile.ui.refonte.SavioClotureProgressBar
import re.melchior.saviomobile.ui.refonte.SavioClotureReportField
import re.melchior.saviomobile.ui.refonte.SavioFormSection
import re.melchior.saviomobile.ui.refonte.SavioTypeChipGrid
import re.melchior.saviomobile.ui.theme.SavioRefonte
import re.melchior.saviomobile.ui.theme.savioTopAppBarColors
import re.melchior.saviomobile.ui.theme.savioTopAppBarSubtitleColor
import re.melchior.saviomobile.ui.theme.useSavioRefonteUi

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ClotureRapportScreen(
    onBack: () -> Unit,
    onNext: (interventionId: String, preselectedActualTypeKeys: String) -> Unit,
    onNavigateToInvoice: (interventionId: String) -> Unit = {},
    viewModel: ClotureRapportViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.navigateToInvoice.collect { interventionId ->
            onNavigateToInvoice(interventionId)
        }
    }

    LaunchedEffect(uiState.renewalError) {
        val message = uiState.renewalError ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        viewModel.clearRenewalError()
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissError()
        }
    }

    val refonte = useSavioRefonteUi()

    if (refonte) {
        Scaffold(
            containerColor = SavioRefonte.BgPage,
            snackbarHost = { SavioSnackbarHost(snackbarHostState) },
            contentWindowInsets = SavioEdgeToEdgeScaffoldInsets,
            topBar = {
                SavioClotureHeader(
                    title = "Clôture",
                    subtitle = "Compte-rendu",
                    step = 1,
                    totalSteps = 2,
                    onBack = onBack,
                )
            },
            bottomBar = {
                SavioClotureNextCtaBar(
                    text = "Suivant — Signature",
                    onClick = {
                        scope.launch {
                            val saved = viewModel.saveReport()
                            if (saved) {
                                uiState.intervention?.id?.let { id ->
                                    onNext(id, viewModel.preselectedKeysForNavigation())
                                }
                            }
                        }
                    },
                    enabled = uiState.canProceed,
                    loading = uiState.isLoading,
                )
            },
        ) { padding ->
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(padding),
            ) {
                SavioClotureProgressBar(currentStep = 1, totalSteps = 2)
                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    uiState.intervention?.let { intervention ->
                        SavioClotureClientCard(
                            clientName =
                                "${intervention.customerFirstName.orEmpty()} ${intervention.customerLastName.orEmpty()}"
                                    .trim()
                                    .ifEmpty { "Client non renseigné" },
                            address = "${intervention.unitStreet}, ${intervention.unitCity}",
                            typeLabel = intervention.typeLabel,
                        )
                    }

                    if (uiState.closeTypesLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(32.dp))
                    } else if (uiState.closeTypesError != null) {
                        SavioClotureNoticeCard(
                            text = uiState.closeTypesError!!,
                            isError = true,
                        )
                    } else {
                        SavioFormSection(
                            title = "Type(s) réel(s) de l'intervention",
                            help = "Sélectionnez ce qui a réellement été fait sur place",
                        ) {
                            SavioTypeChipGrid(
                                labels = uiState.closeTypes.map { it.label },
                                selectedLabels =
                                    uiState.selectedCloseTypes.map { it.label }.toSet(),
                                onToggle = { label ->
                                    uiState.closeTypes
                                        .find { it.label == label }
                                        ?.let(viewModel::toggleCloseType)
                                },
                            )
                            if (uiState.selectedCloseTypes.isEmpty()) {
                                Text(
                                    text = "Sélectionnez au moins un type",
                                    fontSize = 12.sp,
                                    color = Color(0xFFC0392B),
                                    modifier = Modifier.padding(top = 6.dp),
                                )
                            }
                        }
                    }

                    if (uiState.isVeChanged) {
                        SavioClotureNoticeCard(
                            text =
                                "Attention : cette intervention était planifiée comme une " +
                                    "Visite d'entretien. Changer le type annulera la couverture VE du contrat.",
                            isError = true,
                        )
                    }

                    if (!uiState.showReport) {
                        SavioClotureNoticeCard(
                            text = "Aucun compte-rendu requis pour les types sélectionnés.",
                        )
                    }

                    if (uiState.isAbsent) {
                        SavioClotureNoticeCard(
                            text =
                                "Absence : le compte-rendu et la signature client peuvent être non requis selon les types.",
                        )
                    }

                    ContractVeSummaryCard(
                        contractInfo = uiState.contractInfo,
                        lastVe = uiState.lastVe,
                        nextVe = uiState.nextVe,
                        coverage = uiState.coverage,
                        showRenewCta = uiState.showRenewContractCta,
                        renewPriceTtc = uiState.renewalEligibility?.priceTtc,
                        renewLoading = uiState.renewalLoading,
                        onRenewClick = viewModel::renewContractOnSite,
                    )

                    ClosureConsequencesCard(consequences = uiState.consequences)

                    ClosureAnomaliesSection(
                        drafts = buildAnomalyDraftDisplays(uiState.anomalyDrafts, uiState.anomalyCatalog),
                        onAddClick = viewModel::openAddAnomalySheet,
                        onCorrectedChange = viewModel::setAnomalyCorrected,
                        onDeleteDraft = viewModel::removeAnomalyDraft,
                    )

                    if (uiState.showReport) {
                        SavioFormSection(
                            title = "Compte-rendu d'intervention",
                            trailing = {
                                SavioClotureAiButton(
                                    text = uiState.reportGenerateButtonText,
                                    onClick = viewModel::generateReport,
                                    enabled =
                                        uiState.canGenerateReport &&
                                            !uiState.isReportGenerationBusy &&
                                            !uiState.isLoading,
                                    loading = uiState.isReportGenerationBusy,
                                )
                            },
                        ) {
                            if (!uiState.canGenerateReport) {
                                Text(
                                    text = "Saisissez des observations, mesures, attestation, pièces, anomalies ou contrôle installation pour activer la génération.",
                                    fontSize = 12.5.sp,
                                    color = SavioRefonte.Muted,
                                    modifier = Modifier.padding(bottom = 10.dp),
                                )
                            }
                            SavioClotureReportField(
                                value = uiState.report,
                                onValueChange = viewModel::onReportChange,
                                onClear = viewModel::clearReport,
                                placeholder =
                                    "Décrivez l'intervention réalisée, les pièces remplacées, " +
                                        "les réglages effectués…",
                            )
                            if (uiState.reportFallbackUsed) {
                                Text(
                                    text = "Généré via assistant de secours",
                                    fontSize = 11.5.sp,
                                    color = SavioRefonte.Muted,
                                    modifier = Modifier.padding(top = 8.dp),
                                )
                            }
                        }
                    }

                    ClosureFollowUpSection(
                        followUpRequired = uiState.followUpRequired,
                        followUpNote = uiState.followUpNote,
                        onFollowUpRequiredChange = viewModel::onFollowUpRequiredChange,
                        onFollowUpNoteChange = viewModel::onFollowUpNoteChange,
                        useRefonte = true,
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    } else {
        ClotureRapportScreenLegacy(
            uiState = uiState,
            snackbarHostState = snackbarHostState,
            onBack = onBack,
            onNext = onNext,
            viewModel = viewModel,
        )
    }

    if (uiState.showAddAnomalySheet) {
        AddAnomalyBottomSheet(
            catalogTypes = uiState.anomalyCatalog,
            rootEquipments = uiState.rootEquipments,
            onDismiss = viewModel::dismissAddAnomalySheet,
            onConfirm = viewModel::addAnomalyDraft,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun ClotureRapportScreenLegacy(
    uiState: ClotureRapportUiState,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onNext: (interventionId: String, preselectedActualTypeKeys: String) -> Unit,
    viewModel: ClotureRapportViewModel,
) {
    val scope = rememberCoroutineScope()
    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        snackbarHost = { SavioSnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                colors = savioTopAppBarColors(),
                title = {
                    Column {
                        Text(
                            text = "Clôture — étape 1 / 2",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = "Compte-rendu",
                            style = MaterialTheme.typography.labelMedium,
                            color = savioTopAppBarSubtitleColor(),
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            LinearProgressIndicator(
                progress = { 0.5f },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                uiState.intervention?.let { intervention ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(
                                text = "${intervention.customerFirstName ?: ""} " +
                                    "${intervention.customerLastName ?: ""}".trim()
                                        .ifEmpty { "Client non renseigné" },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = "${intervention.unitStreet}, ${intervention.unitCity}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Text(
                            text = intervention.typeLabel,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }

                if (uiState.closeTypesLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                } else if (uiState.closeTypesError != null) {
                    Text(
                        text = uiState.closeTypesError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                } else {
                    ActualTypeChips(
                        types = uiState.closeTypes,
                        selectedTypes = uiState.selectedCloseTypes,
                        onToggle = viewModel::toggleCloseType,
                    )
                }

                if (uiState.isVeChanged) {
                    Surface(modifier = Modifier.fillMaxWidth(), color = Color(0xFFFFEBEE)) {
                        Text(
                            text = "Attention : cette intervention était planifiée comme une " +
                                "Visite d'entretien. Changer le type annulera la couverture VE du contrat.",
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFB71C1C),
                        )
                    }
                }

                if (!uiState.showReport) {
                    Surface(modifier = Modifier.fillMaxWidth(), color = Color(0xFFE3F2FD)) {
                        Text(
                            text = "Aucun compte-rendu requis pour les types sélectionnés.",
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }

                if (uiState.isAbsent) {
                    Surface(modifier = Modifier.fillMaxWidth(), color = Color(0xFFE3F2FD)) {
                        Text(
                            text = "Absence : le compte-rendu et la signature client peuvent être non requis selon les types.",
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }

                ContractVeSummaryCard(
                    contractInfo = uiState.contractInfo,
                    lastVe = uiState.lastVe,
                    nextVe = uiState.nextVe,
                    coverage = uiState.coverage,
                    showRenewCta = uiState.showRenewContractCta,
                    renewPriceTtc = uiState.renewalEligibility?.priceTtc,
                    renewLoading = uiState.renewalLoading,
                    onRenewClick = viewModel::renewContractOnSite,
                )

                ClosureConsequencesCard(consequences = uiState.consequences)

                ClosureAnomaliesSection(
                    drafts = buildAnomalyDraftDisplays(uiState.anomalyDrafts, uiState.anomalyCatalog),
                    onAddClick = viewModel::openAddAnomalySheet,
                    onCorrectedChange = viewModel::setAnomalyCorrected,
                    onDeleteDraft = viewModel::removeAnomalyDraft,
                )

                if (uiState.showReport) {
                    OutlinedButton(
                        onClick = viewModel::generateReport,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = uiState.canGenerateReport &&
                            !uiState.isReportGenerationBusy &&
                            !uiState.isLoading,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            if (uiState.isReportGenerationBusy) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Outlined.AutoAwesome,
                                    contentDescription = "Génération assistée",
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Text(text = uiState.reportGenerateButtonText)
                        }
                    }
                    if (!uiState.canGenerateReport) {
                        Text(
                            text = "Saisissez des observations, mesures, attestation, pièces, anomalies ou contrôle installation pour activer la génération.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    OutlinedTextField(
                        value = uiState.report,
                        onValueChange = viewModel::onReportChange,
                        label = { Text("Compte-rendu d'intervention") },
                        placeholder = {
                            Text(
                                text = "Décrivez les opérations effectuées, " +
                                    "les observations, les pièces remplacées...",
                                style = MaterialTheme.typography.bodySmall,
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp),
                        maxLines = Int.MAX_VALUE,
                        trailingIcon = {
                            if (uiState.report.isNotEmpty()) {
                                IconButton(onClick = viewModel::clearReport) {
                                    Icon(
                                        imageVector = Icons.Outlined.Clear,
                                        contentDescription = "Vider le compte rendu",
                                    )
                                }
                            }
                        },
                        supportingText = {
                            Text(
                                text = "${uiState.report.length} caractères",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        },
                    )
                }

                ClosureFollowUpSection(
                    followUpRequired = uiState.followUpRequired,
                    followUpNote = uiState.followUpNote,
                    onFollowUpRequiredChange = viewModel::onFollowUpRequiredChange,
                    onFollowUpNoteChange = viewModel::onFollowUpNoteChange,
                    useRefonte = false,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        scope.launch {
                            val saved = viewModel.saveReport()
                            if (saved) {
                                uiState.intervention?.id?.let { id ->
                                    onNext(id, viewModel.preselectedKeysForNavigation())
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    enabled = uiState.canProceed && !uiState.isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text(
                            text = "Suivant — Signature →",
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ActualTypeChips(
    types: List<InterventionTypeDto>,
    selectedTypes: List<InterventionTypeDto>,
    onToggle: (InterventionTypeDto) -> Unit,
) {
    Column {
        Text(
            text = "Type(s) réel(s) de l'intervention",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            types.forEach { type ->
                val isSelected = selectedTypes.any {
                    it.stableKey() == type.stableKey()
                }
                val typeColor = remember(type.color) {
                    try {
                        type.color?.let {
                            Color(android.graphics.Color.parseColor(it))
                        }
                    } catch (e: Exception) {
                        null
                    }
                } ?: MaterialTheme.colorScheme.primary

                FilterChip(
                    selected = isSelected,
                    onClick = { onToggle(type) },
                    label = { Text(type.label, fontSize = 13.sp) },
                    leadingIcon = if (isSelected) {
                        {
                            Icon(
                                Icons.Filled.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    } else {
                        null
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = typeColor.copy(alpha = 0.15f),
                        selectedLabelColor = typeColor,
                        selectedLeadingIconColor = typeColor,
                    ),
                )
            }
        }

        if (selectedTypes.isEmpty()) {
            Text(
                text = "Sélectionnez au moins un type",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

@Composable
private fun ClosureFollowUpSection(
    followUpRequired: Boolean,
    followUpNote: String,
    onFollowUpRequiredChange: (Boolean) -> Unit,
    onFollowUpNoteChange: (String) -> Unit,
    useRefonte: Boolean,
) {
    val content: @Composable () -> Unit = {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = followUpRequired,
                onCheckedChange = onFollowUpRequiredChange,
            )
            Text(
                text = "Intervention à revoir",
                fontSize = if (useRefonte) 14.sp else 15.sp,
                fontWeight = if (useRefonte) FontWeight.Medium else FontWeight.Normal,
            )
        }
        if (followUpRequired) {
            OutlinedTextField(
                value = followUpNote,
                onValueChange = onFollowUpNoteChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Note de suivi") },
                placeholder = {
                    Text(
                        text = "Devis à faire, pièce à commander…",
                        fontSize = if (useRefonte) 13.sp else 14.sp,
                    )
                },
                minLines = 2,
                maxLines = 5,
            )
        }
    }

    if (useRefonte) {
        SavioFormSection(title = "Suivi") {
            content()
        }
    } else {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Suivi",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            content()
        }
    }
}
