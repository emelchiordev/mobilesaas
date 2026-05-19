package re.melchior.saviomobile.ui.screen.intervention.create

import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

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
