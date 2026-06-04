package re.melchior.saviomobile.ui.screen.tournee

import re.melchior.saviomobile.data.local.entity.InterventionEntity
import re.melchior.saviomobile.util.formatScheduledAtTime
import java.time.Duration
import java.time.Instant

data class InterventionItem(
    val id: String,
    val time: String,
    val clientName: String,
    val address: String,
    val status: String,
    val syncStatus: String,
    val typeLabel: String,
    val isCompleted: Boolean,
    val elapsedTime: String,
    val conflictResolveAttempts: Int = 0,
    val conflictBannerText: String? = null,
)

fun InterventionEntity.displayTypeLabel(): String =
    actualTypeLabel?.takeIf { it.isNotBlank() } ?: typeLabel

fun InterventionEntity.toInterventionItem(): InterventionItem {
    val time = formatScheduledAtTime(scheduledAt)
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
        clientName = name,
        address = address,
        status = status,
        syncStatus = syncStatus,
        typeLabel = typeLabel,
        isCompleted = isCompleted,
        elapsedTime = elapsed,
        conflictResolveAttempts = conflictResolveAttempts,
        conflictBannerText = conflictBannerText(),
    )
}

internal fun InterventionEntity.conflictBannerText(): String? {
    val stale = conflictResolveAttempts >= 3
    return when (syncStatus) {
        "CONFLICT_IMMUTABLE" ->
            if (stale) "Contactez votre responsable" else "Clôturée sur un autre appareil"
        "CONFLICT_VERSION" ->
            if (stale) "Contactez votre responsable" else "Synchronisation en cours..."
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
