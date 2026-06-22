package re.melchior.saviomobile.util

import re.melchior.saviomobile.data.local.entity.InterventionEntity

fun shouldApplyFollowUpFromPull(
    local: InterventionEntity,
    remote: InterventionEntity,
): Boolean {
    if (local.followUpStatus == "done" && remote.followUpStatus != "done") {
        return false
    }
    return local.followUpRequired != remote.followUpRequired ||
        local.followUpNote != remote.followUpNote ||
        local.followUpStatus != remote.followUpStatus
}
