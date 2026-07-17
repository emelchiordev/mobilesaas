package re.savio.mobile.data.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import re.savio.mobile.data.local.entity.EquipmentEntity
import re.savio.mobile.data.local.entity.InterventionEntity
import re.savio.mobile.data.remote.dto.EquipmentDto
import re.savio.mobile.data.remote.dto.InterventionDto
import re.savio.mobile.data.remote.dto.InterventionTypeDto
import re.savio.mobile.data.remote.dto.UnitDto

class InterventionPullMergeTest {

    private fun intervention(
        status: String,
        syncStatus: String = "SYNCED",
        hasLocalChanges: Boolean = false,
        scheduledAt: String = "2026-06-04T08:00:00Z",
        timeSlot: String = "matin",
        startedAt: String? = null,
    ) = InterventionEntity(
        id = "int-1",
        scheduledAt = scheduledAt,
        timeSlot = timeSlot,
        status = status,
        syncStatus = syncStatus,
        hasLocalChanges = hasLocalChanges,
        typeCode = "depannage",
        typeLabel = "Dépannage",
        typeColor = null,
        unitId = "unit-1",
        unitStreet = "1 rue Test",
        unitAddressLine2 = null,
        unitPostalCode = "75001",
        unitCity = "Paris",
        unitFloor = null,
        unitDoorCode = null,
        unitLatitude = null,
        unitLongitude = null,
        customerId = "cust-1",
        customerFirstName = "Jean",
        customerLastName = "Dupont",
        customerPhone = null,
        customerEmail = null,
        contractType = null,
        contractRenewalDate = null,
        contractTariff = null,
        contractVatRate = null,
        pulledAt = "2026-06-04T07:00:00Z",
        startedAt = startedAt,
    )

    @Test
    fun merge_preservesInProgress_whenRemotePullReturnsScheduled() {
        val local =
            intervention(
                status = "in_progress",
                syncStatus = "IN_PROGRESS",
                hasLocalChanges = true,
                startedAt = "2026-06-04T09:00:00Z",
            )
        val remote = intervention(status = "scheduled", syncStatus = "SYNCED")

        val merged = mergePullInterventionWithLocalLifecycle(remote, local)

        assertEquals("in_progress", merged.status)
        assertEquals("IN_PROGRESS", merged.syncStatus)
        assertEquals("2026-06-04T09:00:00Z", merged.startedAt)
        assertTrue(merged.hasLocalChanges)
    }

    @Test
    fun merge_appliesRemoteWhenLocalIsScheduled() {
        val local = intervention(status = "scheduled")
        val remote =
            intervention(
                status = "scheduled",
                scheduledAt = "2026-06-04T10:00:00Z",
                timeSlot = "apres_midi",
            )

        val merged = mergePullInterventionWithLocalLifecycle(remote, local)

        assertEquals("2026-06-04T10:00:00Z", merged.scheduledAt)
        assertEquals("apres_midi", merged.timeSlot)
    }

    @Test
    fun detectDrift_whenRemoteSlotChanges() {
        val before = intervention(status = "in_progress", syncStatus = "IN_PROGRESS", hasLocalChanges = true)
        val remote =
            interventionDto(
                scheduledAt = "2026-06-04T14:00:00Z",
                timeSlot = "apres_midi",
            )

        assertTrue(detectInterventionBusinessDrift(before, emptyList(), remote))
    }

    @Test
    fun detectDrift_falseWhenOnlyStatusDiffers() {
        val before =
            intervention(
                status = "in_progress",
                syncStatus = "IN_PROGRESS",
                hasLocalChanges = true,
            )
        val remote =
            interventionDto(
                status = "scheduled",
                scheduledAt = before.scheduledAt,
                timeSlot = before.timeSlot,
            )

        assertFalse(detectInterventionBusinessDrift(before, emptyList(), remote))
    }

    @Test
    fun delayedPullScenario_localInProgressNeverRevertedByScheduledRemote() {
        val localAfterStart =
            intervention(
                status = "in_progress",
                syncStatus = "IN_PROGRESS",
                hasLocalChanges = true,
                startedAt = "2026-06-04T09:00:00Z",
            )
        val delayedRemote =
            intervention(
                status = "scheduled",
                syncStatus = "SYNCED",
                startedAt = null,
            )

        val afterDelayedPull = mergePullInterventionWithLocalLifecycle(delayedRemote, localAfterStart)

        assertEquals("in_progress", afterDelayedPull.status)
        assertEquals("IN_PROGRESS", afterDelayedPull.syncStatus)
    }

    private fun interventionDto(
        status: String = "scheduled",
        scheduledAt: String = "2026-06-04T08:00:00Z",
        timeSlot: String = "matin",
        equipment: List<EquipmentDto> = emptyList(),
    ) = InterventionDto(
        id = "int-1",
        scheduledAt = scheduledAt,
        timeSlot = timeSlot,
        status = status,
        type = InterventionTypeDto(id = "t1", code = "depannage", label = "Dépannage", color = null),
        unit =
            UnitDto(
                id = "unit-1",
                street = "1 rue Test",
                addressLine2 = null,
                postalCode = "75001",
                city = "Paris",
                floor = null,
                doorCode = null,
                latitude = null,
                longitude = null,
                coverageUnavailable = null,
                coverageAttested = null,
                coverageExpected = null,
                coverageComplete = null,
            ),
        customer = null,
        equipment = equipment,
        contract = null,
    )
}
