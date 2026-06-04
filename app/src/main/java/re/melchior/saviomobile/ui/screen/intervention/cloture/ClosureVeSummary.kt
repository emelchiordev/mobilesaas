package re.melchior.saviomobile.ui.screen.intervention.cloture

import re.melchior.saviomobile.data.local.entity.InterventionEntity
import re.melchior.saviomobile.data.local.entity.InterventionHistoryEntity
import re.melchior.saviomobile.util.SavioTimeZone
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

data class ContractSummary(
    val type: String,
    val status: String,
    val renewalDate: LocalDate?,
)

data class LastVeSummary(
    val completedAt: LocalDate,
    val technicianFirstName: String?,
)

enum class VeDueUrgency { OK, SOON, OVERDUE }

data class NextVeDisplay(
    val date: LocalDate,
    val urgency: VeDueUrgency,
)

private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

fun formatClosureDate(date: LocalDate): String = date.format(dateFormatter)

fun contractStatusLabel(status: String?): String =
    when (status?.lowercase()) {
        "active" -> "Actif"
        "paused" -> "Suspendu"
        null, "" -> "—"
        else -> status.replaceFirstChar { it.uppercase() }
    }

fun contractTypeLabel(type: String?): String =
    type?.replaceFirstChar { it.uppercase() }?.ifBlank { "—" } ?: "—"

fun contractSummaryFrom(intervention: InterventionEntity): ContractSummary? {
    val type = intervention.contractType?.trim().orEmpty()
    if (type.isEmpty() && intervention.contractRenewalDate.isNullOrBlank()) return null
    return ContractSummary(
        type = type.ifEmpty { "Contrat" },
        status = intervention.contractStatus.orEmpty(),
        renewalDate = parseIsoToLocalDate(intervention.contractRenewalDate),
    )
}

fun lastVeSummaryFrom(history: InterventionHistoryEntity?): LastVeSummary? {
    val completedIso = history?.completedAt?.takeIf { it.isNotBlank() } ?: return null
    val date = parseIsoToLocalDate(completedIso) ?: return null
    return LastVeSummary(
        completedAt = date,
        technicianFirstName = history.technicianFirstName?.trim()?.takeIf { it.isNotEmpty() },
    )
}

fun computeNextVeDate(
    lastVe: LastVeSummary?,
    contractRenewal: LocalDate?,
): LocalDate? =
    lastVe?.completedAt?.plusYears(1) ?: contractRenewal

fun nextVeDisplay(nextDate: LocalDate, today: LocalDate = LocalDate.now(SavioTimeZone.appZone)): NextVeDisplay {
    val urgency = when {
        nextDate.isBefore(today) -> VeDueUrgency.OVERDUE
        !nextDate.isAfter(today.plusDays(30)) -> VeDueUrgency.SOON
        else -> VeDueUrgency.OK
    }
    return NextVeDisplay(date = nextDate, urgency = urgency)
}

fun parseIsoToLocalDate(iso: String?): LocalDate? {
    if (iso.isNullOrBlank()) return null
    return try {
        if (iso.length >= 10 && iso[10] != 'T') {
            LocalDate.parse(iso.take(10))
        } else {
            Instant.parse(iso).atZone(SavioTimeZone.appZone).toLocalDate()
        }
    } catch (_: Exception) {
        try {
            LocalDate.parse(iso.take(10))
        } catch (_: Exception) {
            null
        }
    }
}

fun daysUntil(from: LocalDate, to: LocalDate): Long = ChronoUnit.DAYS.between(from, to)
