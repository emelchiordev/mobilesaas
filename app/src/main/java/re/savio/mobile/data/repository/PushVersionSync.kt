package re.savio.mobile.data.repository

import re.savio.mobile.data.remote.dto.PushResultDto

object PushVersionSync {
    val VERSIONED_PUSH_TYPES = setOf(
        "START_INTERVENTION",
        "CLOSE_INTERVENTION",
        "COMPLETE_INTERVENTION",
        "UPDATE_INTERVENTION",
        "ADD_SIGNATURE",
    )

    fun readVersionFromPayload(payload: Map<String, Any?>?): Int? {
        if (payload == null) return null
        return (payload["version"] as? Number)?.toInt()
            ?: (payload["serverVersion"] as? Number)?.toInt()
    }

    fun resolveServerVersion(result: PushResultDto, localVersion: Int): Int {
        readVersionFromPayload(result.serverData)?.let { return it.coerceAtLeast(1) }
        readVersionFromPayload(result.data)?.let { return it.coerceAtLeast(1) }
        return (localVersion + 1).coerceAtLeast(1)
    }

    fun resolveVersionedOpType(result: PushResultDto, pendingOpType: String?): String? {
        if (pendingOpType != null && pendingOpType in VERSIONED_PUSH_TYPES) {
            return pendingOpType
        }
        return when {
            result.operationId.startsWith("op-start-") -> "START_INTERVENTION"
            result.operationId.startsWith("op-close-") -> "CLOSE_INTERVENTION"
            result.operationId.startsWith("op-complete-") -> "COMPLETE_INTERVENTION"
            else -> null
        }
    }

    fun interventionIdFromVersionedResult(result: PushResultDto): String? {
        readInterventionId(result.resultPayload())?.let { return it }
        return when {
            result.operationId.startsWith("op-start-") ->
                result.operationId.removePrefix("op-start-")
            result.operationId.startsWith("op-close-") ->
                result.operationId.removePrefix("op-close-")
            result.operationId.startsWith("op-complete-") ->
                result.operationId.removePrefix("op-complete-")
            else -> null
        }
    }

    fun isVersionMismatch(result: PushResultDto): Boolean =
        result.conflictType == "VERSION_MISMATCH" ||
            result.reason == "VERSION_MISMATCH" ||
            result.message == "VERSION_MISMATCH"

    /** Statut sync opérationnel après resync version (hors CONFLICT_VERSION). */
    fun operationalSyncStatus(
        status: String,
        syncStatus: String,
        completedAt: String?,
    ): String {
        if (syncStatus == "COMPLETED") return "COMPLETED"
        if (!completedAt.isNullOrBlank()) return "COMPLETED"
        if (status.equals("completed", ignoreCase = true)) return "COMPLETED"
        if (status.equals("in_progress", ignoreCase = true)) return "IN_PROGRESS"
        return "SYNCED"
    }

    private fun readInterventionId(payload: Map<String, Any?>?): String? {
        val raw = payload?.get("interventionId") ?: return null
        return raw as? String
    }
}
