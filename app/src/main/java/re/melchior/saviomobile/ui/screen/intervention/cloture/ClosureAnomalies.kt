package re.melchior.saviomobile.ui.screen.intervention.cloture

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import re.melchior.saviomobile.ui.refonte.SavioClotureDashedAddButton
import re.melchior.saviomobile.ui.refonte.SavioFormSection
import re.melchior.saviomobile.ui.theme.useSavioRefonteUi
import re.melchior.saviomobile.data.local.entity.AnomalyDraftEntity
import re.melchior.saviomobile.data.local.entity.AnomalyTypeEntity
import re.melchior.saviomobile.data.local.entity.EquipmentEntity

private enum class AnomalyScope(val value: String, val label: String) {
    INSTALLATION("installation", "Installation"),
    EQUIPMENT("equipment", "Un appareil"),
    BOTH("both", "Les deux"),
}

data class AnomalyDraftDisplay(
    val draft: AnomalyDraftEntity,
    val label: String,
    val levelEmoji: String,
    val levelTag: String,
)

fun buildAnomalyDraftDisplays(
    drafts: List<AnomalyDraftEntity>,
    catalog: List<AnomalyTypeEntity>,
): List<AnomalyDraftDisplay> {
    val byCode = catalog.associateBy { it.code }
    return drafts.map { draft ->
        val type = draft.anomalyTypeCode?.let { byCode[it] }
        val label = type?.designation ?: draft.customDescription ?: "Anomalie"
        val (emoji, tag) = when (type?.nomenclature ?: type?.level) {
            "dgi" -> "🔴" to "DGI"
            "a2", "majeure" -> "🟡" to "A2"
            else -> "🟢" to "A1"
        }
        AnomalyDraftDisplay(draft, label, emoji, tag)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ClosureAnomaliesSection(
    drafts: List<AnomalyDraftDisplay>,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val refonte = useSavioRefonteUi()
    if (refonte) {
        SavioFormSection(
            title = "Anomalies constatées",
            help = "Optionnel — non-conformités ou défauts observés",
            modifier = modifier,
        ) {
            if (drafts.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 12.dp),
                ) {
                    drafts.forEach { item ->
                        AssistChip(
                            onClick = {},
                            label = {
                                Text("${item.levelEmoji} ${item.levelTag} — ${item.label}")
                            },
                        )
                    }
                }
            }
            SavioClotureDashedAddButton(
                text = "Ajouter une anomalie",
                onClick = onAddClick,
            )
        }
        return
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Anomalies constatées",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = "Optionnel — non-conformités ou défauts observés",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
        )

        if (drafts.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 12.dp),
            ) {
                drafts.forEach { item ->
                    AssistChip(
                        onClick = {},
                        label = {
                            Text("${item.levelEmoji} ${item.levelTag} — ${item.label}")
                        },
                    )
                }
            }
        }

        OutlinedButton(onClick = onAddClick, modifier = Modifier.fillMaxWidth()) {
            Text("+ Ajouter une anomalie")
        }
    }
}

@Composable
fun ClosureDgiSummaryBanner(dgiCount: Int) {
    if (dgiCount <= 0) return
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFFFEBEE),
    ) {
        Text(
            text = "⚠ $dgiCount anomalie${if (dgiCount > 1) "s" else ""} DGI — client informé",
            modifier = Modifier.padding(12.dp),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFFB71C1C),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddAnomalyBottomSheet(
    catalogTypes: List<AnomalyTypeEntity>,
    rootEquipments: List<EquipmentEntity>,
    onDismiss: () -> Unit,
    onConfirm: (
        scope: String,
        equipmentId: String?,
        anomalyTypeCode: String?,
        customDescription: String?,
        action: String?,
    ) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var scope by rememberSaveable { mutableStateOf(AnomalyScope.INSTALLATION) }
    var selectedEquipmentId by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedTypeCode by rememberSaveable { mutableStateOf<String?>(null) }
    var customDescription by rememberSaveable { mutableStateOf("") }
    var useCustom by rememberSaveable { mutableStateOf(false) }
    var action by rememberSaveable { mutableStateOf("") }
    var clientInformed by rememberSaveable { mutableStateOf(false) }
    var searchQuery by rememberSaveable { mutableStateOf("") }

    val selectedType = catalogTypes.find { it.code == selectedTypeCode }
    val isDgi = selectedType?.level == "dgi" || selectedType?.nomenclature == "dgi"
    val needsEquipment = scope == AnomalyScope.EQUIPMENT || scope == AnomalyScope.BOTH

    val filteredTypes = remember(catalogTypes, searchQuery) {
        val q = searchQuery.trim().lowercase()
        if (q.isEmpty()) catalogTypes
        else catalogTypes.filter {
            it.code.contains(q) ||
                it.designation.lowercase().contains(q)
        }
    }

    val grouped = remember(filteredTypes) {
        linkedMapOf(
            "dgi" to filteredTypes.filter { it.nomenclature == "dgi" || it.level == "dgi" },
            "a2" to filteredTypes.filter { it.nomenclature == "a2" || it.level == "majeure" },
            "a1" to filteredTypes.filter { it.nomenclature == "a1" || it.level == "mineure" },
        ).filterValues { it.isNotEmpty() }
    }

    val canSubmit = run {
        if (needsEquipment && selectedEquipmentId.isNullOrBlank()) return@run false
        if (useCustom) {
            if (customDescription.trim().length < 3) return@run false
        } else {
            if (selectedTypeCode.isNullOrBlank()) return@run false
            if (isDgi && !clientInformed) return@run false
        }
        true
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Ajouter une anomalie",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            Text(
                text = "Constaté sur :",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AnomalyScope.entries.forEach { option ->
                    FilterChip(
                        selected = scope == option,
                        onClick = {
                            scope = option
                            if (option == AnomalyScope.INSTALLATION) {
                                selectedEquipmentId = null
                            }
                        },
                        label = { Text(option.label) },
                    )
                }
            }

            if (needsEquipment) {
                Text(
                    text = "Appareil concerné",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (rootEquipments.isEmpty()) {
                    Text(
                        text = "Aucun appareil disponible sur ce logement.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                } else {
                    rootEquipments.forEach { equipment ->
                        FilterChip(
                            selected = selectedEquipmentId == equipment.id,
                            onClick = { selectedEquipmentId = equipment.id },
                            label = {
                                Text(equipmentLabel(equipment))
                            },
                            modifier = Modifier.padding(bottom = 4.dp),
                        )
                    }
                }
            }

            HorizontalDivider()

            Text(
                text = "Type d'anomalie :",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Rechercher") },
            )

            grouped.forEach { (groupKey, types) ->
                val (header, color) = when (groupKey) {
                    "dgi" -> "🔴 DANGER GRAVE ET IMMÉDIAT (DGI)" to Color(0xFFB71C1C)
                    "a2" -> "🟡 ANOMALIE MAJEURE (A2)" to Color(0xFFE65100)
                    else -> "🟢 ANOMALIE MINEURE (A1)" to Color(0xFF2E7D32)
                }
                Text(
                    text = header,
                    style = MaterialTheme.typography.labelLarge,
                    color = color,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp),
                )
                types.forEach { type ->
                    FilterChip(
                        selected = !useCustom && selectedTypeCode == type.code,
                        onClick = {
                            useCustom = false
                            selectedTypeCode = type.code
                            clientInformed = false
                        },
                        label = { Text("${type.code} — ${type.designation}") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                    )
                }
            }

            FilterChip(
                selected = useCustom,
                onClick = {
                    useCustom = true
                    selectedTypeCode = null
                    clientInformed = false
                },
                label = { Text("➕ Autre (description libre)") },
                modifier = Modifier.fillMaxWidth(),
            )

            if (useCustom) {
                OutlinedTextField(
                    value = customDescription,
                    onValueChange = { customDescription = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Description de l'anomalie") },
                    minLines = 2,
                )
            }

            if (isDgi && !useCustom) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFFFFEBEE),
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "⚠ DANGER GRAVE ET IMMÉDIAT",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFB71C1C),
                        )
                        Text(
                            text = "Informez le client — coupure gaz requise",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFB71C1C),
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Checkbox(
                        checked = clientInformed,
                        onCheckedChange = { clientInformed = it },
                    )
                    Text(
                        text = "J'ai informé le client du danger",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            OutlinedTextField(
                value = action,
                onValueChange = { action = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Action à mener (optionnel)") },
                placeholder = { Text("Ex: Remplacement flexible prévu le...") },
                singleLine = false,
                minLines = 2,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Annuler")
                }
                Button(
                    onClick = {
                        onConfirm(
                            scope.value,
                            if (needsEquipment) selectedEquipmentId else null,
                            if (useCustom) null else selectedTypeCode,
                            if (useCustom) customDescription.trim() else null,
                            action.trim().takeIf { it.isNotEmpty() },
                        )
                        onDismiss()
                    },
                    enabled = canSubmit,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Ajouter")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

private fun equipmentLabel(equipment: EquipmentEntity): String {
    val brand = equipment.brand?.trim().orEmpty()
    val model = equipment.model?.trim().orEmpty()
    val base = listOf(brand, model).filter { it.isNotEmpty() }.joinToString(" ")
    return base.ifEmpty { "Appareil ${equipment.order}" }
}
