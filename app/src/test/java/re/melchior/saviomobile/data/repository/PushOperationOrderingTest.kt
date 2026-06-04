package re.melchior.saviomobile.data.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import re.melchior.saviomobile.data.remote.dto.PushOperationDto

class PushOperationOrderingTest {

    @Test
    fun sort_createEquipment_before_saveColdMeasure() {
        val ops = listOf(
            op("cold", "SAVE_COLD_MEASURE", "2026-05-24T12:00:00Z"),
            op("create", "CREATE_EQUIPMENT", "2026-05-24T11:00:00Z"),
        )
        val sorted = PushOperationOrdering.sortPushOperations(ops)
        assertEquals("CREATE_EQUIPMENT", sorted[0].type)
        assertEquals("SAVE_COLD_MEASURE", sorted[1].type)
    }

    @Test
    fun partition_groups_by_priority_tier() {
        val ops = listOf(
            op("close", "CLOSE_INTERVENTION", "2026-05-24T14:00:00Z"),
            op("create", "CREATE_EQUIPMENT", "2026-05-24T11:00:00Z"),
            op("start", "START_INTERVENTION", "2026-05-24T12:00:00Z"),
        )
        val tiers = PushOperationOrdering.partitionIntoTiers(ops)
        assertEquals(listOf(1, 2, 3), tiers.map { it.first })
        assertEquals("CREATE_EQUIPMENT", tiers[0].second.single().type)
        assertEquals("START_INTERVENTION", tiers[1].second.single().type)
        assertEquals("CLOSE_INTERVENTION", tiers[2].second.single().type)
    }

    @Test
    fun isHardPushFailure_excludes_ok_and_skipped() {
        assertTrue(!PushOperationOrdering.isHardPushFailure("ok"))
        assertTrue(!PushOperationOrdering.isHardPushFailure("skipped"))
        assertTrue(PushOperationOrdering.isHardPushFailure("failed"))
    }

    @Test
    fun isClosurePushFailureStatus_only_ok_succeeds() {
        assertTrue(!PushOperationOrdering.isClosurePushFailureStatus("ok"))
        assertTrue(PushOperationOrdering.isClosurePushFailureStatus("skipped"))
        assertTrue(PushOperationOrdering.isClosurePushFailureStatus("failed"))
    }

    @Test
    fun sort_createAnomaly_after_closeIntervention() {
        val ops = listOf(
            op("anomaly", "CREATE_ANOMALY", "2026-05-24T15:00:00Z"),
            op("close", "CLOSE_INTERVENTION", "2026-05-24T14:00:00Z"),
        )
        val sorted = PushOperationOrdering.sortPushOperations(ops)
        assertEquals("CLOSE_INTERVENTION", sorted[0].type)
        assertEquals("CREATE_ANOMALY", sorted[1].type)
    }

    @Test
    fun partition_groups_createAnomaly_in_tier_four() {
        val ops = listOf(
            op("anomaly", "CREATE_ANOMALY", "2026-05-24T15:00:00Z"),
            op("close", "CLOSE_INTERVENTION", "2026-05-24T14:00:00Z"),
            op("create", "CREATE_EQUIPMENT", "2026-05-24T11:00:00Z"),
        )
        val tiers = PushOperationOrdering.partitionIntoTiers(ops)
        assertEquals(listOf(1, 3, 4), tiers.map { it.first })
        assertEquals("CREATE_ANOMALY", tiers.last().second.single().type)
    }

    private fun op(id: String, type: String, occurredAt: String) =
        PushOperationDto(
            id = id,
            type = type,
            occurredAt = occurredAt,
            payload = emptyMap(),
        )
}
