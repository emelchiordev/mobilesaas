package re.melchior.saviomobile.data.repository

import re.melchior.saviomobile.data.remote.dto.PushOperationDto

/** Utilitaires push clôture : rafraîchissement des payloads après remap CREATE_EQUIPMENT. */
object PushClosurePayloadRefresh {

    fun isRoomBackedPendingOp(op: PushOperationDto): Boolean {
        if (op.id.startsWith("op-start-")) return false
        if (op.id.startsWith("op-close-")) return false
        if (op.id.startsWith("op-complete-")) return false
        if (op.id.startsWith("op-update-")) return false
        if (op.id.startsWith("op-update-invoice-")) return false
        return true
    }

    fun refreshTierOpsPayloads(
        tierOps: List<PushOperationDto>,
        pendingPayloadByOpId: Map<String, Map<String, Any?>>,
    ): List<PushOperationDto> =
        tierOps.map { op ->
            if (!isRoomBackedPendingOp(op)) return@map op
            val payload = pendingPayloadByOpId[op.id] ?: return@map op
            op.copy(payload = payload)
        }
}
