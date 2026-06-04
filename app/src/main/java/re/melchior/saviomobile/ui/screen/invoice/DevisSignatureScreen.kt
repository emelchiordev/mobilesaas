package re.melchior.saviomobile.ui.screen.invoice

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import re.melchior.saviomobile.ui.component.SavioSnackbarHost
import re.melchior.saviomobile.ui.screen.intervention.cloture.DrawPoint
import re.melchior.saviomobile.ui.theme.savioTopAppBarColors

private const val HAMON_CLAUSE =
    "Je soussigné(e) reconnais avoir été informé(e) de mon droit de rétractation de 14 jours. " +
        "Je demande expressément le démarrage immédiat des travaux et renonce en conséquence à mon " +
        "droit de rétractation conformément à l'article L221-28 du Code de la consommation."

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevisSignatureScreen(
    onBack: () -> Unit,
    onCompleted: () -> Unit,
    viewModel: DevisSignatureViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val devisPoints by viewModel.devisPoints.collectAsStateWithLifecycle()
    val hamonPoints by viewModel.hamonPoints.collectAsStateWithLifecycle()
    val snackbarHostState = remember { androidx.compose.material3.SnackbarHostState() }

    val padEnabled =
        uiState.invoice != null && !uiState.isLoading && !uiState.isCompleted

    LaunchedEffect(uiState.isCompleted) {
        if (uiState.isCompleted) {
            onCompleted()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.dismissError()
        }
    }

    Scaffold(
        snackbarHost = { SavioSnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                colors = savioTopAppBarColors(),
                title = {
                    Text(
                        when (uiState.step) {
                            1 -> "Signature du devis"
                            else -> "Démarrage immédiat des travaux"
                        },
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (uiState.step == 2) viewModel.goBackToDevisStep()
                            else onBack()
                        },
                        enabled = !uiState.isLoading,
                    ) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
            )
        },
        bottomBar = {
            DevisSignatureBottomBar(
                uiState = uiState,
                onCancel = onBack,
                onContinue = viewModel::continueToHamonStep,
                onFinalize = viewModel::finalizeAcceptance,
            )
        },
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SignatureStepIndicator(currentStep = uiState.step)

            if (uiState.invoice == null && !uiState.isLoading) {
                Text(
                    "Créez d'abord les lignes sur l'écran Facturation, puis revenez signer le devis.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            when (uiState.step) {
                1 ->
                    DevisAcceptanceStep(
                        uiState = uiState,
                        devisPoints = devisPoints,
                        padEnabled = padEnabled,
                        onDevisDragStart = { x, y ->
                            viewModel.addDevisPoint(DrawPoint(x, y, isStart = true))
                        },
                        onDevisDrag = { x, y ->
                            viewModel.addDevisPoint(DrawPoint(x, y, isStart = false))
                        },
                        onDevisSizeChanged = viewModel::onDevisCanvasSizeChanged,
                        onClearDevis = viewModel::clearDevisSignature,
                    )
                else ->
                    HamonClauseStep(
                        uiState = uiState,
                        hamonPoints = hamonPoints,
                        padEnabled = padEnabled,
                        onHamonChecked = viewModel::setHamonImmediateRequested,
                        onHamonDragStart = { x, y ->
                            viewModel.addHamonPoint(DrawPoint(x, y, isStart = true))
                        },
                        onHamonDrag = { x, y ->
                            viewModel.addHamonPoint(DrawPoint(x, y, isStart = false))
                        },
                        onHamonSizeChanged = viewModel::onHamonCanvasSizeChanged,
                        onClearHamon = viewModel::clearHamonSignature,
                    )
            }
        }
    }
}

@Composable
private fun SignatureStepIndicator(currentStep: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StepChip(label = "1. Acceptation", active = currentStep == 1)
        Text("→", color = MaterialTheme.colorScheme.onSurfaceVariant)
        StepChip(label = "2. Loi Hamon", active = currentStep == 2)
    }
}

@Composable
private fun StepChip(label: String, active: Boolean) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color =
            if (active) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
            color =
                if (active) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
        )
    }
}

@Composable
private fun DevisAcceptanceStep(
    uiState: DevisSignatureUiState,
    devisPoints: List<DrawPoint>,
    padEnabled: Boolean,
    onDevisDragStart: (Float, Float) -> Unit,
    onDevisDrag: (Float, Float) -> Unit,
    onDevisSizeChanged: (Int, Int) -> Unit,
    onClearDevis: () -> Unit,
) {
    uiState.invoice?.let { inv ->
        Text(
            "Total TTC : %.2f €".format(inv.totalTtc),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
    }

    Text(
        "Récapitulatif",
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    if (uiState.lines.isEmpty()) {
        Text(
            "Aucune ligne facturée à afficher",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    } else {
        uiState.lines.forEach { line ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    line.label,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    "%.2f €".format(line.totalHt * (1 + line.vatRate / 100.0)),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }

    HorizontalDivider()

    Text(
        "Bon pour accord - Lu et approuvé",
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Medium,
    )

    DevisSignaturePad(
        points = devisPoints,
        hasSignature = uiState.hasDevisSignature,
        enabled = padEnabled,
        placeholder = "Signez ici",
        onDragStart = onDevisDragStart,
        onDrag = onDevisDrag,
        onSizeChanged = onDevisSizeChanged,
    )

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        OutlinedButton(
            onClick = onClearDevis,
            enabled = uiState.hasDevisSignature && padEnabled,
        ) {
            Text("Effacer")
        }
    }
}

@Composable
private fun HamonClauseStep(
    uiState: DevisSignatureUiState,
    hamonPoints: List<DrawPoint>,
    padEnabled: Boolean,
    onHamonChecked: (Boolean) -> Unit,
    onHamonDragStart: (Float, Float) -> Unit,
    onHamonDrag: (Float, Float) -> Unit,
    onHamonSizeChanged: (Int, Int) -> Unit,
    onClearHamon: () -> Unit,
) {
    Text(
        HAMON_CLAUSE,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface,
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
    ) {
        Checkbox(
            checked = uiState.hamonImmediateRequested,
            onCheckedChange = onHamonChecked,
            enabled = padEnabled,
        )
        Text(
            "Le client demande le démarrage immédiat des travaux",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 12.dp),
        )
    }

    if (uiState.hamonImmediateRequested) {
        Text(
            "Signature de renonciation",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        DevisSignaturePad(
            points = hamonPoints,
            hasSignature = uiState.hasHamonSignature,
            enabled = padEnabled,
            placeholder = "Signez ici",
            onDragStart = onHamonDragStart,
            onDrag = onHamonDrag,
            onSizeChanged = onHamonSizeChanged,
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            OutlinedButton(
                onClick = onClearHamon,
                enabled = uiState.hasHamonSignature && padEnabled,
            ) {
                Text("Effacer")
            }
        }
    }
}

@Composable
private fun DevisSignatureBottomBar(
    uiState: DevisSignatureUiState,
    onCancel: () -> Unit,
    onContinue: () -> Unit,
    onFinalize: () -> Unit,
) {
    val canContinueStep1 =
        uiState.invoice != null &&
            uiState.hasDevisSignature &&
            !uiState.isLoading &&
            !uiState.isCompleted

    val canFinalizeStep2 =
        uiState.invoice != null &&
            !uiState.isLoading &&
            !uiState.isCompleted &&
            (
                !uiState.hamonImmediateRequested ||
                    uiState.hasHamonSignature
            )

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier.weight(1f),
            enabled = !uiState.isLoading && !uiState.isCompleted,
        ) {
            Text("Annuler")
        }
        if (uiState.step == 1) {
            Button(
                onClick = onContinue,
                modifier = Modifier.weight(1f),
                enabled = canContinueStep1,
            ) {
                Text("Continuer")
            }
        } else {
            Button(
                onClick = onFinalize,
                modifier = Modifier.weight(1f),
                enabled = canFinalizeStep2,
            ) {
                if (uiState.isLoading) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                        Spacer(Modifier.width(10.dp))
                        Text("Enregistrement…")
                    }
                } else {
                    Text(
                        if (uiState.hamonImmediateRequested) {
                            "Valider et démarrer les travaux"
                        } else {
                            "Valider sans démarrage immédiat"
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun DevisSignaturePad(
    points: List<DrawPoint>,
    hasSignature: Boolean,
    enabled: Boolean,
    placeholder: String,
    onDragStart: (Float, Float) -> Unit,
    onDrag: (Float, Float) -> Unit,
    onSizeChanged: (Int, Int) -> Unit,
) {
    val strokeColor = MaterialTheme.colorScheme.onSurface
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(
                    width = if (hasSignature) 2.dp else 1.dp,
                    color =
                        if (hasSignature) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outline
                        },
                    shape = RoundedCornerShape(12.dp),
                )
                .onSizeChanged { size -> onSizeChanged(size.width, size.height) }
                .then(
                    if (enabled) {
                        Modifier.pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { offset -> onDragStart(offset.x, offset.y) },
                                onDrag = { change, _ ->
                                    onDrag(change.position.x, change.position.y)
                                },
                            )
                        }
                    } else {
                        Modifier
                    },
                ),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (points.isEmpty()) return@Canvas
            val path = Path()
            points.forEach { point ->
                if (point.isStart) path.moveTo(point.x, point.y)
                else path.lineTo(point.x, point.y)
            }
            drawPath(
                path = path,
                color = strokeColor,
                style =
                    Stroke(
                        width = 4f,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round,
                    ),
            )
        }
        if (!hasSignature) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    if (enabled) placeholder else "Facture requise",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
