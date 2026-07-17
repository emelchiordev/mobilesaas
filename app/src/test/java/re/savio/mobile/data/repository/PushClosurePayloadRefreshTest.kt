package re.savio.mobile.data.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import re.savio.mobile.data.remote.dto.PushOperationDto

class PushClosurePayloadRefreshTest {

    @Test
    fun isRoomBackedPendingOp_falseForSyntheticOps() {
        assertFalse(
            PushClosurePayloadRefresh.isRoomBackedPendingOp(
                PushOperationDto(
                    id = "op-start-int-1",
                    type = "START_INTERVENTION",
                    occurredAt = "2026-06-16T00:00:00Z",
                    payload = emptyMap(),
                ),
            ),
        )
        assertFalse(
            PushClosurePayloadRefresh.isRoomBackedPendingOp(
                PushOperationDto(
                    id = "op-close-int-1",
                    type = "CLOSE_INTERVENTION",
                    occurredAt = "2026-06-16T00:00:00Z",
                    payload = emptyMap(),
                ),
            ),
        )
    }

    @Test
    fun refreshTierOpsPayloads_updatesEquipmentIdFromRoomSnapshot() {
        val localId = "afa020b4-c9cc-4211-8818-b405f6b6cc26"
        val serverId = "b1103488-eea8-489e-85f2-23fcca4c38cd"
        val tierOps =
            listOf(
                PushOperationDto(
                    id = localId,
                    type = "UPDATE_EQUIPMENT",
                    occurredAt = "2026-06-16T00:00:00Z",
                    payload = mapOf("equipmentId" to localId, "serialNumber" to "SN1"),
                ),
                PushOperationDto(
                    id = "op-start-int-1",
                    type = "START_INTERVENTION",
                    occurredAt = "2026-06-16T00:00:00Z",
                    payload = mapOf("interventionId" to "int-1"),
                ),
            )
        val refreshed =
            PushClosurePayloadRefresh.refreshTierOpsPayloads(
                tierOps,
                mapOf(
                    localId to mapOf("equipmentId" to serverId, "serialNumber" to "SN1"),
                ),
            )
        assertEquals(serverId, refreshed[0].payload["equipmentId"])
        assertEquals(localId, tierOps[0].payload["equipmentId"])
        assertEquals("int-1", refreshed[1].payload["interventionId"])
    }

    @Test
    fun refreshTierOpsPayloads_keepsOpWhenNoRoomSnapshot() {
        val op =
            PushOperationDto(
                id = "missing-op",
                type = "UPDATE_EQUIPMENT",
                occurredAt = "2026-06-16T00:00:00Z",
                payload = mapOf("equipmentId" to "local"),
            )
        val refreshed =
            PushClosurePayloadRefresh.refreshTierOpsPayloads(
                listOf(op),
                emptyMap(),
            )
        assertEquals(op, refreshed.single())
    }
}
