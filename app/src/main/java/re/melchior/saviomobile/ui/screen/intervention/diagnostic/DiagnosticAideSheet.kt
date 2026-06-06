package re.melchior.saviomobile.ui.screen.intervention.diagnostic

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import re.melchior.saviomobile.data.local.entity.EquipmentEntity
import re.melchior.saviomobile.data.remote.dto.RagSearchResultDto
import re.melchior.saviomobile.ui.refonte.SavioBetaBadge
import re.melchior.saviomobile.ui.refonte.SavioGhostButton
import re.melchior.saviomobile.ui.theme.SavioRefonte
import re.melchior.saviomobile.ui.theme.formatEquipmentTypeLabel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnosticAideSheet(
    interventionId: String,
    equipment: EquipmentEntity?,
    onDismiss: () -> Unit,
    viewModel: DiagnosticAideViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val expandedObservations = remember { mutableStateMapOf<Int, Boolean>() }

    LaunchedEffect(interventionId, equipment?.id) {
        viewModel.init(interventionId, equipment)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
    ) {
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = null,
                        tint = SavioRefonte.Navy,
                    )
                    Text(
                        text = "Aide au diagnostic",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = SavioRefonte.Ink,
                    )
                    SavioBetaBadge()
                }
            }

            item {
                Text(
                    text = "Suggestions basées sur l'historique — à confirmer sur le terrain.",
                    fontSize = 13.sp,
                    color = SavioRefonte.Muted,
                )
            }

            item {
                OutlinedTextField(
                    value = uiState.query,
                    onValueChange = viewModel::onQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Symptôme ou panne") },
                    placeholder = {
                        Text(
                            "Décris le symptôme… (ex. chaudière éteinte, circulateur bruyant, fuite eau)",
                        )
                    },
                    minLines = 2,
                    maxLines = 4,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { viewModel.search() }),
                )
            }

            item {
                EquipmentFilterBanner(
                    equipment = uiState.selectedEquipment,
                    filterEnabled = uiState.equipmentFilterEnabled,
                    onClearFilter = viewModel::clearEquipmentFilter,
                )
            }

            uiState.error?.let { message ->
                item {
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 13.sp,
                    )
                }
            }

            item {
                Button(
                    onClick = viewModel::search,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading && !uiState.isSynthesizing,
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = Color.White,
                        )
                    } else {
                        Text("Rechercher")
                    }
                }
            }

            if (uiState.hasSearched && !uiState.isLoading) {
                item {
                    HorizontalDivider(color = SavioRefonte.Line)
                }
                item {
                    Text(
                        text = "Résultats",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SavioRefonte.Ink,
                    )
                }

                if (uiState.results.isEmpty() && uiState.error == null) {
                    item {
                        Text(
                            text = "Aucun cas similaire trouvé.",
                            fontSize = 13.sp,
                            color = SavioRefonte.Muted,
                        )
                    }
                } else if (uiState.results.isNotEmpty()) {
                    val canSynthesize =
                        uiState.results.size >= 3 &&
                            uiState.showSynthesizeButton &&
                            uiState.synthesis == null &&
                            !uiState.isSynthesizing

                    if (canSynthesize) {
                        item {
                            SavioGhostButton(
                                text = "Analyser ces résultats (Beta)",
                                icon = Icons.Outlined.Psychology,
                                onClick = viewModel::synthesize,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }

                    if (uiState.isSynthesizing) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    strokeWidth = 2.dp,
                                )
                            }
                        }
                    }

                    uiState.synthesis?.let { synthesis ->
                        item {
                            DiagnosticSynthesisCard(synthesis = synthesis)
                        }
                    }

                    if (uiState.synthesis != null) {
                        item {
                            Text(
                                text = "Sources",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SavioRefonte.Muted,
                            )
                        }
                    }

                    items(
                        items = uiState.results.withIndex().toList(),
                        key = { (index, result) -> "${index}_${result.date}_${result.score}" },
                    ) { (index, result) ->
                        DiagnosticResultCard(
                            result = result,
                            expanded = expandedObservations[index] == true,
                            onToggleExpand = {
                                expandedObservations[index] = expandedObservations[index] != true
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EquipmentFilterBanner(
    equipment: EquipmentEntity?,
    filterEnabled: Boolean,
    onClearFilter: () -> Unit,
) {
    if (equipment == null || !filterEnabled) return
    val label =
        buildString {
            equipment.brand?.trim()?.takeIf { it.isNotEmpty() }?.let { append(it) }
            equipment.model?.trim()?.takeIf { it.isNotEmpty() }?.let {
                if (isNotEmpty()) append(' ')
                append(it)
            }
            equipment.typeCode?.let { type ->
                if (isNotEmpty()) append(" · ")
                append(formatEquipmentTypeLabel(type))
            }
        }.ifBlank { "Équipement principal" }

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = SavioRefonte.Tint,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Recherche sur",
                    fontSize = 11.sp,
                    color = SavioRefonte.Muted,
                )
                Text(
                    text = label,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = SavioRefonte.Ink,
                )
            }
            IconButton(onClick = onClearFilter) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Désactiver le filtre équipement",
                    tint = SavioRefonte.Muted,
                )
            }
        }
    }
}

@Composable
private fun DiagnosticResultCard(
    result: RagSearchResultDto,
    expanded: Boolean,
    onToggleExpand: () -> Unit,
) {
    val scorePct = (result.score * 100).toInt().coerceIn(0, 100)
    val scoreColor = scoreColor(result.score)
    val observations = result.observations.trim()
    val truncated =
        if (observations.length <= 150 || expanded) {
            observations
        } else {
            observations.take(150).trimEnd() + "…"
        }
    val canExpand = observations.length > 150

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF8FAFC),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(10.dp)
                            .background(scoreColor, CircleShape),
                )
                Text(
                    text = "$scorePct%",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = scoreColor,
                )
                Text(
                    text = formatResultDate(result.date),
                    fontSize = 12.sp,
                    color = SavioRefonte.Muted,
                )
                if (result.interventionType.isNotBlank()) {
                    Text(
                        text = result.interventionType.uppercase(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SavioRefonte.Navy,
                    )
                }
            }
            val equipLabel =
                listOfNotNull(
                    result.marque.takeIf { it.isNotBlank() },
                    result.modele.takeIf { it.isNotBlank() },
                ).joinToString(" ")
            if (equipLabel.isNotBlank()) {
                Text(
                    text = equipLabel,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = SavioRefonte.Ink,
                )
            }
            if (observations.isNotBlank()) {
                Text(
                    text = "\"$truncated\"",
                    fontSize = 13.sp,
                    color = SavioRefonte.Ink,
                    modifier =
                        if (canExpand) {
                            Modifier.clickable(onClick = onToggleExpand)
                        } else {
                            Modifier
                        },
                )
            }
        }
    }
}

private fun scoreColor(score: Double): Color =
    when {
        score > 0.85 -> SavioRefonte.StatusDoneFg
        score > 0.70 -> SavioRefonte.OrangeAction
        else -> Color(0xFFDC2626)
    }

private fun formatResultDate(raw: String): String {
    val trimmed = raw.trim()
    if (trimmed.isEmpty()) return "—"
    return try {
        LocalDate.parse(trimmed.take(10)).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    } catch (_: DateTimeParseException) {
        trimmed.take(10)
    }
}
