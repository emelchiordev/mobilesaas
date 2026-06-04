package re.melchior.saviomobile.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** Tokens intervention — mode light (valeurs logo) ; dark via repli Material. */
object SavioInterventionColors {
    val BusinessAccent = Color(0xFFF5A623)
    val TabUnselected = Color(0xFF9E9E9E)
    val NotesDispatcherBg = Color(0xFFF0F4F8)
    val NotesDispatcherText = Color(0xFF1A1A1A)

    val PlanifBg = Color(0xFFD6E4F5)
    val PlanifFg = Color(0xFF1B4F8A)
    val EnCoursBg = Color(0xFFFFE0A0)
    val EnCoursFg = Color(0xFFE67E00)
    val TermineeBg = Color(0xFFD4EDDA)
    val TermineeFg = Color(0xFF28A745)
    val AnnuleeBg = Color(0xFFF8D7DA)
    val AnnuleeFg = Color(0xFFDC3545)
    val ValidationBg = Color(0xFFFFF3CD)
    val ValidationFg = Color(0xFF856404)
}

data class InterventionStatusBadge(
    val background: Color,
    val foreground: Color,
    val label: String,
)

/** Bleu navigation / liens (#1B4F8A en light). */
@Composable
fun savioNavColor(): Color = MaterialTheme.colorScheme.primary

@Composable
fun savioTabSelectedColor(): Color = MaterialTheme.colorScheme.primary

@Composable
fun SavioInterventionTabIndicator(tabPositions: List<androidx.compose.material3.TabPosition>, selectedIndex: Int) {
    if (selectedIndex < tabPositions.size) {
        TabRowDefaults.SecondaryIndicator(
            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedIndex]),
            height = 3.dp,
            color = savioTabSelectedColor(),
        )
    }
}

@Composable
fun savioTabUnselectedColor(): Color =
    if (isSystemInDarkTheme()) {
        MaterialTheme.colorScheme.onSurfaceVariant
    } else {
        SavioInterventionColors.TabUnselected
    }

fun interventionStatusLabel(status: String, syncStatus: String): String =
    when {
        syncStatus == "CONFLICT_IMMUTABLE" -> "🔴 Clôturée ailleurs"
        syncStatus == "CONFLICT_VERSION" -> "🟠 Sync…"
        syncStatus == "CONFLICT" -> "Conflit"
        syncStatus == "SKIPPED" -> "Ignorée"
        syncStatus == "COMPLETED" -> "En attente"
        syncStatus == "IN_PROGRESS" -> "En cours"
        syncStatus == "SYNCED" && status == "completed" -> "Terminée"
        status == "pending_validation" -> "En validation"
        status == "scheduled" -> "Planifiée"
        status == "in_progress" -> "En cours"
        status == "cancelled" -> "Annulée"
        status == "completed" -> "Terminée"
        else -> formatStatusKeyFr(status)
    }

@Composable
fun interventionStatusBadge(status: String, syncStatus: String): InterventionStatusBadge {
    val label = interventionStatusLabel(status, syncStatus)
    if (!isSystemInDarkTheme()) {
        val (bg, fg) =
            when {
                syncStatus in CONFLICT_SYNC ->
                    MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
                syncStatus == "SKIPPED" ->
                    SavioInterventionColors.AnnuleeBg to SavioInterventionColors.AnnuleeFg
                status == "cancelled" ->
                    SavioInterventionColors.AnnuleeBg to SavioInterventionColors.AnnuleeFg
                status == "pending_validation" ->
                    SavioInterventionColors.ValidationBg to SavioInterventionColors.ValidationFg
                status == "completed" || (syncStatus == "SYNCED" && status == "completed") ->
                    SavioInterventionColors.TermineeBg to SavioInterventionColors.TermineeFg
                syncStatus == "IN_PROGRESS" || status == "in_progress" || syncStatus == "COMPLETED" ->
                    SavioInterventionColors.EnCoursBg to SavioInterventionColors.EnCoursFg
                status == "scheduled" ->
                    SavioInterventionColors.PlanifBg to SavioInterventionColors.PlanifFg
                else ->
                    MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
            }
        return InterventionStatusBadge(bg, fg, label)
    }
    val (fg, bg) =
        when {
            syncStatus in CONFLICT_SYNC ->
                MaterialTheme.colorScheme.error to MaterialTheme.colorScheme.errorContainer
            syncStatus == "SKIPPED" ->
                MaterialTheme.colorScheme.onSurfaceVariant to MaterialTheme.colorScheme.surfaceVariant
            status == "pending_validation" ->
                SavioPalette.Accent to SavioUi.StatusTintBg
            syncStatus == "SYNCED" && status == "completed" ->
                SavioPalette.Success to SavioUi.StatusSuccessBg
            syncStatus == "IN_PROGRESS" || status == "in_progress" || syncStatus == "COMPLETED" ->
                SavioPalette.Accent to SavioUi.StatusTintBg
            status == "scheduled" ->
                MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.primaryContainer
            status == "cancelled" ->
                MaterialTheme.colorScheme.error to MaterialTheme.colorScheme.errorContainer
            else ->
                MaterialTheme.colorScheme.onSurfaceVariant to MaterialTheme.colorScheme.surfaceVariant
        }
    return InterventionStatusBadge(background = bg, foreground = fg, label = label)
}

fun formatStatusKeyFr(raw: String): String {
    val key = raw.trim().lowercase()
    return STATUS_LABELS_FR[key]
        ?: key.replace('_', ' ')
            .split(' ')
            .filter { it.isNotBlank() }
            .joinToString(" ") { word ->
                word.replaceFirstChar { c -> c.uppercaseChar() }
            }
}

fun formatEquipmentTypeLabel(code: String?): String {
    if (code.isNullOrBlank()) return ""
    val key = code.trim().lowercase()
    return EQUIPMENT_TYPE_LABELS_FR[key] ?: formatStatusKeyFr(key)
}

private val CONFLICT_SYNC = setOf("CONFLICT", "CONFLICT_IMMUTABLE", "CONFLICT_VERSION")

private val STATUS_LABELS_FR =
    mapOf(
        "other" to "Autre",
        "scheduled" to "Planifiée",
        "in_progress" to "En cours",
        "completed" to "Terminée",
        "cancelled" to "Annulée",
        "pending_validation" to "En validation",
    )

private val EQUIPMENT_TYPE_LABELS_FR =
    mapOf(
        "other" to "Autre",
        "replaced" to "Remplacé",
        "pac" to "Pompe à chaleur",
        "clim" to "Climatisation",
        "chaudiere" to "Chaudière",
        "chaudière" to "Chaudière",
        "ballon" to "Ballon",
        "vmc" to "VMC",
        "poele" to "Poêle",
        "poêle" to "Poêle",
        "radiateur" to "Radiateur",
        "ecs" to "Eau chaude sanitaire",
    )
