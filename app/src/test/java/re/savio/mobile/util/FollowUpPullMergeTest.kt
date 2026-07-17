package re.savio.mobile.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import re.savio.mobile.data.local.entity.InterventionEntity

class FollowUpPullMergeTest {

    private fun entity(
        followUpRequired: Boolean = false,
        followUpNote: String? = null,
        followUpStatus: String = "none",
    ) = InterventionEntity(
        id = "int-1",
        scheduledAt = "2026-06-15T08:00:00Z",
        status = "completed",
        typeCode = "VE",
        typeLabel = "Visite entretien",
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
        customerId = null,
        customerFirstName = null,
        customerLastName = null,
        customerPhone = null,
        customerEmail = null,
        contractType = null,
        contractRenewalDate = null,
        contractTariff = null,
        contractVatRate = null,
        pulledAt = "2026-06-15T10:00:00Z",
        followUpRequired = followUpRequired,
        followUpNote = followUpNote,
        followUpStatus = followUpStatus,
    )

    @Test
    fun applyWhenBureauResolvedPendingFollowUp() {
        val local = entity(followUpRequired = true, followUpNote = "Devis", followUpStatus = "pending")
        val remote = entity(followUpRequired = false, followUpNote = "Devis", followUpStatus = "done")
        assertTrue(shouldApplyFollowUpFromPull(local, remote))
    }

    @Test
    fun skipWhenLocalResolvedButServerStillPending() {
        val local = entity(followUpRequired = false, followUpStatus = "done")
        val remote = entity(followUpRequired = true, followUpStatus = "pending")
        assertFalse(shouldApplyFollowUpFromPull(local, remote))
    }

    @Test
    fun skipWhenAlreadyAligned() {
        val local = entity(followUpRequired = false, followUpStatus = "done")
        val remote = entity(followUpRequired = false, followUpStatus = "done")
        assertFalse(shouldApplyFollowUpFromPull(local, remote))
    }
}
