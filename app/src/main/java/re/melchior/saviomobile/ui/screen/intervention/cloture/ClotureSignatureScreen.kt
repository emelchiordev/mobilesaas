package re.melchior.saviomobile.ui.screen.intervention.cloture

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.SnackbarHostState
import re.melchior.saviomobile.ui.component.SavioSnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import re.melchior.saviomobile.ui.theme.SavioPalette
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ClotureSignatureScreen(
    onBack: () -> Unit,
    onCompleted: () -> Unit,
    viewModel: ClotureSignatureViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val techPoints by viewModel.techPoints.collectAsStateWithLifecycle()
    val clientPoints by viewModel.clientPoints.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    var techCanvasWidth by remember { mutableStateOf(0) }
    var techCanvasHeight by remember { mutableStateOf(0) }
    var clientCanvasWidth by remember { mutableStateOf(0) }
    var clientCanvasHeight by remember { mutableStateOf(0) }

    LaunchedEffect(uiState.isCompleted) {
        if (uiState.isCompleted) {
            if (uiState.isPendingValidation) {
                snackbarHostState.showSnackbar(
                    "Intervention soumise — en attente de validation du dispatcher"
                )
                kotlinx.coroutines.delay(2000)
            }
            onCompleted()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissError()
        }
    }

    Scaffold(
        snackbarHost = { SavioSnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Clôture — étape 2 / 2",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Signatures",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LinearProgressIndicator(
                progress = { 1f },
                modifier = Modifier.fillMaxWidth()
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Infos client
                uiState.intervention?.let { intervention ->
                    Text(
                        text = "${intervention.customerFirstName ?: ""} " +
                                "${intervention.customerLastName ?: ""}".trim()
                                    .ifEmpty { "Client non renseigné" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (uiState.closeTypesLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                } else if (uiState.closeTypesError != null) {
                    Text(
                        text = uiState.closeTypesError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    ActualTypeChips(
                        types = uiState.closeTypes,
                        selectedTypes = uiState.selectedCloseTypes,
                        onToggle = viewModel::toggleCloseType
                    )
                }

                if (uiState.isVeChanged) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFFFFEBEE)
                    ) {
                        Text(
                            text = "Attention : cette intervention était planifiée comme une " +
                                    "Visite d'entretien. La changer annulera la couverture VE du contrat.",
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFB71C1C)
                        )
                    }
                }

                Text(
                    text = "Signature du technicien",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                SignatureZone(
                    points = techPoints,
                    hasSignature = uiState.hasTechSignature,
                    placeholder = "Technicien — signez ici",
                    onDragStart = { x, y ->
                        viewModel.addTechPoint(DrawPoint(x, y, isStart = true))
                    },
                    onDrag = { x, y ->
                        viewModel.addTechPoint(DrawPoint(x, y, isStart = false))
                    },
                    onSizeChanged = { w, h ->
                        techCanvasWidth = w
                        techCanvasHeight = h
                    }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = viewModel::clearTechSignature,
                        enabled = uiState.hasTechSignature && !uiState.isLoading
                    ) {
                        Text("Effacer")
                    }
                }

                if (uiState.showClientSignature) {
                    HorizontalDivider()

                    Text(
                        text = "Signature du client",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "Le client atteste avoir reçu le compte-rendu de l'intervention.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    SignatureZone(
                        points = clientPoints,
                        hasSignature = uiState.hasClientSignature,
                        placeholder = "Client — signez ici",
                        onDragStart = { x, y ->
                            viewModel.addClientPoint(DrawPoint(x, y, isStart = true))
                        },
                        onDrag = { x, y ->
                            viewModel.addClientPoint(DrawPoint(x, y, isStart = false))
                        },
                        onSizeChanged = { w, h ->
                            clientCanvasWidth = w
                            clientCanvasHeight = h
                        }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = viewModel::clearClientSignature,
                            enabled = uiState.hasClientSignature && !uiState.isLoading
                        ) {
                            Text("Effacer")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Bouton clôturer
                Button(
                    onClick = {
                        viewModel.completeIntervention(
                            filesDir = context.filesDir,
                            techCanvasWidth = techCanvasWidth,
                            techCanvasHeight = techCanvasHeight,
                            clientCanvasWidth = clientCanvasWidth,
                            clientCanvasHeight = clientCanvasHeight
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = uiState.canComplete && !uiState.isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Clôturer et terminer l'intervention",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun SignatureZone(
    points: List<DrawPoint>,
    hasSignature: Boolean,
    placeholder: String,
    onDragStart: (Float, Float) -> Unit,
    onDrag: (Float, Float) -> Unit,
    onSizeChanged: (Int, Int) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(SavioPalette.SurfaceCard)
            .border(
                width = if (hasSignature) 2.dp else 1.dp,
                color = if (hasSignature) SavioPalette.Accent else SavioPalette.TextHint,
                shape = RoundedCornerShape(12.dp)
            )
            .onSizeChanged { size -> onSizeChanged(size.width, size.height) }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        onDragStart(offset.x, offset.y)
                    },
                    onDrag = { change, _ ->
                        onDrag(change.position.x, change.position.y)
                    }
                )
            }
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
                color = SavioPalette.TextPrimary,
                style = Stroke(
                    width = 4f,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }

        if (!hasSignature) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyLarge,
                    color = SavioPalette.TextSecondary
                )
            }
        }
    }
}