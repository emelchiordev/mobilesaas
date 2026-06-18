package re.melchior.saviomobile.util

import re.melchior.saviomobile.data.local.entity.InterventionEntity

fun InterventionEntity.isFollowUpPending(): Boolean {
    val status = followUpStatus.trim()
    when (status) {
        "pending" -> return true
        "done" -> return false
        "none" -> return false
    }
    return followUpRequired && this.status in setOf("completed", "pending_validation")
}

fun isFollowUpPending(
    followUpStatus: String?,
    followUpRequired: Boolean,
    interventionStatus: String,
): Boolean {
    val status = followUpStatus?.trim().orEmpty()
    when (status) {
        "pending" -> return true
        "done" -> return false
        "none" -> return false
    }
    return followUpRequired &&
        interventionStatus in setOf("completed", "pending_validation")
}
