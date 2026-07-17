package re.savio.mobile.ui.screen.intervention.create

import re.savio.mobile.util.InterventionTimeSlot
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

fun defaultPlanningDateText(zoneId: ZoneId = ZoneId.systemDefault()): String =
    LocalDate.now(zoneId).toString()

fun buildPlanningScheduledAtMillis(
    date: LocalDate,
    timeSlot: InterventionTimeSlot,
    timeText: String,
    zoneId: ZoneId = ZoneId.systemDefault(),
): Long {
    val time = runCatching { LocalTime.parse(timeText.trim()) }.getOrNull()
    val hour = time?.hour ?: when (timeSlot) {
        InterventionTimeSlot.MATIN -> 8
        InterventionTimeSlot.APRES_MIDI -> 14
        InterventionTimeSlot.JOURNEE -> 8
    }
    val minute = time?.minute ?: 0
    return date.atTime(hour, minute)
        .atZone(zoneId)
        .toInstant()
        .toEpochMilli()
}

fun defaultScheduledMillis(zoneId: ZoneId = ZoneId.systemDefault()): Long =
    ceilToNextHalfHour(ZonedDateTime.now(zoneId)).toInstant().toEpochMilli()

fun ceilToNextHalfHour(zdt: ZonedDateTime): ZonedDateTime {
    val totalMinutes = zdt.hour * 60 + zdt.minute
    val nextSlotMinutes = ((totalMinutes + 29) / 30) * 30
    val dayStart = zdt.toLocalDate().atStartOfDay(zdt.zone)
    return dayStart.plusMinutes(nextSlotMinutes.toLong())
        .withSecond(0)
        .withNano(0)
}

fun scheduledAtToIso(millis: Long): String =
    Instant.ofEpochMilli(millis).toString()
