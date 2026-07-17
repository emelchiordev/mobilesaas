package re.savio.mobile.ui.screen.tournee

import re.savio.mobile.data.local.entity.InterventionEntity
import re.savio.mobile.util.InterventionTimeSlot
import re.savio.mobile.util.PlanningSortKey
import re.savio.mobile.util.UnitEnergySummaryItem
import re.savio.mobile.util.formatPlanningLabel
import re.savio.mobile.util.formatScheduledAtTime
import re.savio.mobile.util.isFollowUpPending
import re.savio.mobile.util.parseInterventionTimeSlot
import java.time.Duration
import java.time.Instant

data class InterventionItem(
    val id: String,
    val time: String,
    val planningLabel: String,
    val timeSlot: InterventionTimeSlot,
    val scheduledAtMillis: Long,
    val number: String? = null,
    val isUrgent: Boolean,
    val clientName: String,
    val address: String,
    val status: String,
    val syncStatus: String,
    val typeLabel: String,
    val isCompleted: Boolean,
    val elapsedTime: String,
    val conflictResolveAttempts: Int = 0,
    val conflictBannerText: String? = null,
    val followUpPending: Boolean = false,
    val energyBadges: List<UnitEnergySummaryItem> = emptyList(),
)

fun InterventionItem.toPlanningSortKey(): PlanningSortKey =
    PlanningSortKey(
        id = id,
        scheduledAtMillis = scheduledAtMillis,
        timeSlot = timeSlot,
        isUrgent = isUrgent,
    )

fun InterventionEntity.displayTypeLabel(): String =
    actualTypeLabel?.takeIf { it.isNotBlank() } ?: typeLabel

fun InterventionEntity.detailHeaderSubtitle(): String {
    val parts = mutableListOf<String>()
    number?.takeIf { it.isNotBlank() }?.let { parts.add(it) }
    parts.add(formatPlanningLabel(parseInterventionTimeSlot(timeSlot), scheduledAt))
    return parts.joinToString(" · ")
}

fun InterventionEntity.toInterventionItem(
    energyBadges: List<UnitEnergySummaryItem> = emptyList(),
): InterventionItem {
    val slot = parseInterventionTimeSlot(timeSlot)
    val planningLabel = formatPlanningLabel(slot, scheduledAt)
    val time = formatScheduledAtTime(scheduledAt)
    val scheduledAtMillis =
        runCatching { Instant.parse(scheduledAt).toEpochMilli() }.getOrDefault(0L)
    val name = listOfNotNull(customerFirstName, customerLastName)
        .joinToString(" ")
        .trim()
        .ifEmpty { "—" }
    val address = listOf(unitStreet, "$unitPostalCode $unitCity")
        .joinToString(", ")
    val isCompleted = status == "completed" && syncStatus == "SYNCED"

    val elapsed = if (status == "in_progress" || syncStatus == "IN_PROGRESS") {
        formatElapsed(startedAt)
    } else {
        ""
    }

    return InterventionItem(
        id = id,
        time = time,
        planningLabel = planningLabel,
        timeSlot = slot,
        scheduledAtMillis = scheduledAtMillis,
        number = number?.takeIf { it.isNotBlank() },
        isUrgent = isUrgent,
        clientName = name,
        address = address,
        status = status,
        syncStatus = syncStatus,
        typeLabel = displayTypeLabel(),
        isCompleted = isCompleted,
        elapsedTime = elapsed,
        conflictResolveAttempts = conflictResolveAttempts,
        conflictBannerText = conflictBannerText(),
        followUpPending = isFollowUpPending(),
        energyBadges = energyBadges,
    )
}

internal fun InterventionEntity.conflictBannerText(): String? {
    val stale = conflictResolveAttempts >= 3
    return when (syncStatus) {
        "CONFLICT_IMMUTABLE" ->
            if (stale) "Contactez votre responsable" else "Clôturée sur un autre appareil"
        "CONFLICT_VERSION" ->
            if (stale) "Contactez votre responsable"
            else "Planning modifié — relancez la clôture"
        else -> null
    }
}

private fun formatElapsed(startedAt: String?): String {
    if (startedAt.isNullOrBlank()) return "—"
    return try {
        val start = Instant.parse(startedAt)
        val minutes = Duration.between(start, Instant.now()).toMinutes().coerceAtLeast(0)
        val h = minutes / 60
        val m = minutes % 60
        when {
            h > 0 -> "${h}h${m}m"
            else -> "${m}m"
        }
    } catch (_: Exception) {
        "—"
    }
}

