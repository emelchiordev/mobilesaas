package re.savio.mobile.util

import java.time.LocalDate
import java.time.ZoneId

/** Bornes ISO UTC pour filtrer `scheduledAt` sur un jour calendaire métier (comparaison lexicographique). */
data class ScheduledAtIsoRange(
    val startIso: String,
    val endIso: String,
)

fun LocalDate.toScheduledAtIsoRange(zone: ZoneId = SavioTimeZone.appZone): ScheduledAtIsoRange {
    val startIso = atStartOfDay(zone).toInstant().toString()
    val endIso = plusDays(1).atStartOfDay(zone).toInstant().toString()
    return ScheduledAtIsoRange(startIso, endIso)
}
