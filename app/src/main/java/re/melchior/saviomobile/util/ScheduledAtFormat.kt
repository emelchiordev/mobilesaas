package re.melchior.saviomobile.util

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
private val frenchDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.FRENCH)
private val frenchTimeFormatter = DateTimeFormatter.ofPattern("HH'h'mm", Locale.FRENCH)
private val readableDateFormatter =
    DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.FRENCH)
private val shortDateFormatter =
    DateTimeFormatter.ofPattern("d MMMM", Locale.FRENCH)

/** Heure locale métier (Europe/Paris) à partir d’un ISO UTC stocké en Room. */
fun formatScheduledAtTime(
    scheduledAtIso: String?,
    zone: ZoneId = SavioTimeZone.appZone,
): String {
    if (scheduledAtIso.isNullOrBlank()) return "—"
    return try {
        Instant.parse(scheduledAtIso).atZone(zone).format(timeFormatter)
    } catch (_: Exception) {
        scheduledAtIso.substringAfter("T").take(5).ifBlank { "—" }
    }
}

/** Date française (dd/MM/yyyy) à partir d’un ISO UTC. */
fun formatScheduledAtDateFrench(
    scheduledAtIso: String?,
    zone: ZoneId = SavioTimeZone.appZone,
): String {
    if (scheduledAtIso.isNullOrBlank()) return "—"
    return try {
        Instant.parse(scheduledAtIso).atZone(zone).format(frenchDateFormatter)
    } catch (_: Exception) {
        runCatching {
            java.time.LocalDate.parse(scheduledAtIso.take(10)).format(frenchDateFormatter)
        }.getOrElse { scheduledAtIso.take(10).ifBlank { "—" } }
    }
}

/** Heure française (07h39) à partir d’un ISO UTC. */
fun formatScheduledAtTimeFrench(
    scheduledAtIso: String?,
    zone: ZoneId = SavioTimeZone.appZone,
): String {
    if (scheduledAtIso.isNullOrBlank()) return "—"
    return try {
        Instant.parse(scheduledAtIso).atZone(zone).format(frenchTimeFormatter)
    } catch (_: Exception) {
        scheduledAtIso.substringAfter("T").take(5).replace(':', 'h').ifBlank { "—" }
    }
}

/** Libellé complet de clôture : « 22/06/2026 à 07h39 ». */
fun formatCompletedAtDisplay(
    completedAtIso: String?,
    zone: ZoneId = SavioTimeZone.appZone,
): String {
    if (completedAtIso.isNullOrBlank()) return "—"
    val date = formatScheduledAtDateFrench(completedAtIso, zone)
    val time = formatScheduledAtTimeFrench(completedAtIso, zone)
    if (date == "—") return "—"
    if (time == "—") return date
    return "$date à $time"
}

/** Date calendaire métier (yyyy-MM-dd) à partir d’un ISO UTC. */
fun formatScheduledAtDate(
    scheduledAtIso: String?,
    zone: ZoneId = SavioTimeZone.appZone,
): String {
    if (scheduledAtIso.isNullOrBlank()) return "—"
    return try {
        Instant.parse(scheduledAtIso).atZone(zone).format(dateFormatter)
    } catch (_: Exception) {
        scheduledAtIso.take(10).ifBlank { "—" }
    }
}

/** Date courte (ex. « 4 juin ») à partir d’un ISO UTC. */
fun formatScheduledAtDateShort(
    scheduledAtIso: String?,
    zone: ZoneId = SavioTimeZone.appZone,
): String {
    if (scheduledAtIso.isNullOrBlank()) return "—"
    return try {
        Instant.parse(scheduledAtIso).atZone(zone).format(shortDateFormatter)
    } catch (_: Exception) {
        formatScheduledAtDate(scheduledAtIso, zone)
    }
}

/** Date lisible (ex. « 4 juin 2026 ») à partir d’un ISO UTC. */
fun formatScheduledAtDateReadable(
    scheduledAtIso: String?,
    zone: ZoneId = SavioTimeZone.appZone,
): String {
    if (scheduledAtIso.isNullOrBlank()) return "—"
    return try {
        Instant.parse(scheduledAtIso).atZone(zone).format(readableDateFormatter)
    } catch (_: Exception) {
        formatScheduledAtDate(scheduledAtIso, zone)
    }
}
