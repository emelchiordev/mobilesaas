package re.savio.mobile.data.repository

import re.savio.mobile.data.remote.dto.PushOperationDto

/**
 * Ordre d'envoi du batch push mobile : créations d'abord, mesures / clôture en dernier.
 */
object PushOperationOrdering {

    val PUSH_PRIORITY_BY_TYPE: Map<String, Int> = mapOf(
        // Priorité 1 — créations
        "CREATE_EQUIPMENT" to 1,
        "REPLACE_EQUIPMENT" to 1,
        "CREATE_CUSTOMER" to 1,
        "CREATE_UNIT" to 1,
        "CREATE_INTERVENTION" to 1,
        // Priorité 2 — cycle intervention / mises à jour
        "START_INTERVENTION" to 2,
        "COMPLETE_INTERVENTION" to 2,
        "UPDATE_CUSTOMER" to 2,
        "UPDATE_UNIT_ACCESS" to 2,
        "UPDATE_EQUIPMENT" to 2,
        "DELETE_EQUIPMENT" to 2,
        "UPDATE_INTERVENTION" to 2,
        "RESOLVE_FOLLOW_UP" to 2,
        "SAVE_MEASURE" to 2,
        "SAVE_PAC_MEASURE" to 2,
        "SAVE_INSTALLATION_CHECK" to 2,
        "CREATE_INVOICE" to 2,
        "ADD_INVOICE_LINE" to 2,
        "REMOVE_INVOICE_LINE" to 2,
        "VALIDATE_INVOICE" to 2,
        "SUBMIT_INVOICE" to 2,
        // Priorité 3 — dépendances fortes + clôture
        "SAVE_COLD_MEASURE" to 3,
        "SAVE_ATTESTATION_VE" to 3,
        "SUBMIT_INVOICE_FULL" to 3,
        "UPDATE_INVOICE_MOBILE" to 3,
        "ADD_SIGNATURE" to 3,
        "CLOSE_INTERVENTION" to 3,
        "CREATE_ANOMALY" to 4,
    )

    private val TIER_ORDER = listOf(1, 2, 3, 4, 99)

    fun pushPriority(type: String): Int = PUSH_PRIORITY_BY_TYPE[type] ?: 99

    fun isHardPushFailure(status: String): Boolean = status !in setOf("ok", "skipped")

    /** Push de clôture : tout statut autre que « ok » annule la transaction locale. */
    fun isClosurePushFailureStatus(status: String): Boolean = status != "ok"

    fun sortPushOperations(ops: List<PushOperationDto>): List<PushOperationDto> =
        ops.sortedWith(
            compareBy(
                { pushPriority(it.type) },
                { it.occurredAt },
                { it.id },
            ),
        )

    /** Vagues P1 → P2 → P3 → reste (99), chaque liste triée par occurredAt. */
    fun partitionIntoTiers(ops: List<PushOperationDto>): List<Pair<Int, List<PushOperationDto>>> {
        val sorted = sortPushOperations(ops)
        return TIER_ORDER.mapNotNull { tier ->
            val chunk = sorted.filter { pushPriority(it.type) == tier }
            if (chunk.isEmpty()) null else tier to chunk
        }
    }
}
