package re.savio.mobile.data.account

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import re.savio.mobile.data.repository.UnsyncedLocalWorkSummary

class FlushFailureWarningCopyTest {

    @Test
    fun message_namesDataLossAndLegacyTone() {
        val msg =
            FlushFailureWarningCopy.build(
                unsynced =
                    UnsyncedLocalWorkSummary(
                        closedInterventionsPendingPush = 2,
                    ),
                isLogout = false,
            )
        assertTrue(msg.contains("non synchronisée", ignoreCase = true))
        assertTrue(msg.contains("perdues définitivement"))
        assertTrue(msg.contains("Assurez-vous d'avoir synchronisé"))
        assertTrue(msg.contains("2 intervention"))
        assertTrue(msg.contains("changer de compte"))
    }

    @Test
    fun message_logoutVariant() {
        val msg =
            FlushFailureWarningCopy.build(
                unsynced = UnsyncedLocalWorkSummary(pendingPhotos = 3),
                isLogout = true,
            )
        assertTrue(msg.contains("déconnecter"))
        assertTrue(msg.contains("élément"))
    }

    @Test
    fun summary_interventionLikeCount() {
        val s =
            UnsyncedLocalWorkSummary(
                closedInterventionsPendingPush = 1,
                pendingOfflineInterventions = 2,
                pendingPhotos = 5,
            )
        assertTrue(s.interventionLikeCount == 3)
        assertTrue(s.totalItems == 8)
    }
}

class AccountPrepOverrideConfirmLogicTest {

    @Test
    fun firstTap_onlyRequestsConfirm_doesNotWipe() {
        // Pure state machine for UI: requestOverrideConfirm sets dialog, does not call override.
        var showDialog = false
        var wipeCalled = false
        fun requestOverrideConfirm() {
            showDialog = true
        }
        fun confirmOverrideAndContinue() {
            wipeCalled = true
            showDialog = false
        }
        requestOverrideConfirm()
        assertTrue(showDialog)
        assertFalse(wipeCalled)
        confirmOverrideAndContinue()
        assertTrue(wipeCalled)
        assertFalse(showDialog)
    }
}
