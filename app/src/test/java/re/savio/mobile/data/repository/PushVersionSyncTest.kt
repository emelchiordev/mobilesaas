package re.savio.mobile.data.repository

import org.junit.Assert.assertEquals
import org.junit.Test
import re.savio.mobile.data.remote.dto.PushResultDto

class PushVersionSyncTest {
    @Test
    fun resolveServerVersion_prefersServerData() {
        val result =
            PushResultDto(
                operationId = "op-planning-1",
                status = "ok",
                reason = null,
                message = null,
                conflictType = null,
                serverData = mapOf("version" to 3),
                data = mapOf("version" to 2),
            )
        assertEquals(3, PushVersionSync.resolveServerVersion(result, localVersion = 1))
    }

    @Test
    fun resolveServerVersion_fallsBackToLocalPlusOne() {
        val result =
            PushResultDto(
                operationId = "op-planning-1",
                status = "ok",
                reason = null,
                message = null,
                conflictType = null,
                serverData = null,
                data = null,
            )
        assertEquals(2, PushVersionSync.resolveServerVersion(result, localVersion = 1))
    }

    @Test
    fun operationalSyncStatus_inProgress_restoresInProgress() {
        assertEquals(
            "IN_PROGRESS",
            PushVersionSync.operationalSyncStatus(
                status = "in_progress",
                syncStatus = "CONFLICT_VERSION",
                completedAt = null,
            ),
        )
    }

    @Test
    fun operationalSyncStatus_completedAt_restoresCompleted() {
        assertEquals(
            "COMPLETED",
            PushVersionSync.operationalSyncStatus(
                status = "in_progress",
                syncStatus = "CONFLICT_VERSION",
                completedAt = "2026-06-07T00:00:00Z",
            ),
        )
    }
}
