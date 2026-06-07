package re.melchior.saviomobile.util

import re.melchior.saviomobile.data.local.entity.InterventionEntity

enum class MobilePlanningPermission {    READ_ONLY,
    LIMITED_EDIT,
    FULL_EDIT,
}

fun parseMobilePlanningPermission(raw: String?): MobilePlanningPermission =
    when (raw?.trim()?.uppercase()) {
        "READ_ONLY" -> MobilePlanningPermission.READ_ONLY
        "FULL_EDIT" -> MobilePlanningPermission.FULL_EDIT
        else -> MobilePlanningPermission.LIMITED_EDIT
    }

enum class InterventionTimeSlot(val wire: String, val label: String) {
    MATIN("matin", "Matin"),
    APRES_MIDI("apres_midi", "Après-midi"),
    JOURNEE("journee", "Journée"),
}

fun parseInterventionTimeSlot(raw: String?): InterventionTimeSlot =
    InterventionTimeSlot.entries.firstOrNull { it.wire == raw?.trim() } ?: InterventionTimeSlot.MATIN

fun formatPlanningLabel(timeSlot: InterventionTimeSlot, scheduledAtIso: String): String {
    val time = formatScheduledAtTime(scheduledAtIso)
    if (time == "—" || time == "00:00") return timeSlot.label
    if (timeSlot == InterventionTimeSlot.JOURNEE && time == "08:00") return timeSlot.label
    return "${timeSlot.label} — $time"
}

fun compareInterventionsForPlanning(
    a: PlanningSortKey,
    b: PlanningSortKey,
): Int {
    if (a.isUrgent != b.isUrgent) return if (a.isUrgent) -1 else 1
    val slotA = a.timeSlot.ordinal
    val slotB = b.timeSlot.ordinal
    if (slotA != slotB) return slotA - slotB
    val timeA = a.scheduledAtMillis
    val timeB = b.scheduledAtMillis
    if (timeA != timeB) return timeA.compareTo(timeB)
    return a.id.compareTo(b.id)
}

data class PlanningSortKey(
    val id: String,
    val scheduledAtMillis: Long,
    val timeSlot: InterventionTimeSlot,
    val isUrgent: Boolean,
)

fun InterventionEntity.toPlanningSortKey(): PlanningSortKey {
    val millis = runCatching { java.time.Instant.parse(scheduledAt).toEpochMilli() }.getOrDefault(0L)
    return PlanningSortKey(
        id = id,
        scheduledAtMillis = millis,
        timeSlot = parseInterventionTimeSlot(timeSlot),
        isUrgent = isUrgent,
    )
}
