package re.savio.mobile.data.account

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import re.savio.mobile.data.local.database.AccountFingerprintStore

class AccountSwitchDecisionTest {

    @Test
    fun firstDevice_emptyDb_skipsPrep() {
        assertFalse(
            AccountSwitchDecision.requiresAccountPrep(
                fingerprint = null,
                newUserId = "u1",
                newSocieteSlug = "acme",
                hasLocalTenantData = false,
            ),
        )
    }

    @Test
    fun legacyData_noFingerprint_requiresPrep() {
        assertTrue(
            AccountSwitchDecision.requiresAccountPrep(
                fingerprint = null,
                newUserId = "u1",
                newSocieteSlug = "acme",
                hasLocalTenantData = true,
            ),
        )
    }

    @Test
    fun sameOutgoingSession_skipsPrepEvenWithoutFingerprint() {
        assertFalse(
            AccountSwitchDecision.requiresAccountPrep(
                fingerprint = null,
                newUserId = "u1",
                newSocieteSlug = "acme",
                hasLocalTenantData = true,
                currentUserId = "u1",
                currentSocieteSlug = "acme",
            ),
        )
    }

    @Test
    fun sameIdentity_skipsPrep() {
        assertFalse(
            AccountSwitchDecision.requiresAccountPrep(
                fingerprint = AccountFingerprintStore.Fingerprint("u1", "acme"),
                newUserId = "u1",
                newSocieteSlug = "acme",
                hasLocalTenantData = true,
            ),
        )
    }

    @Test
    fun otherUser_withLocalData_requiresPrep() {
        assertTrue(
            AccountSwitchDecision.requiresAccountPrep(
                fingerprint = AccountFingerprintStore.Fingerprint("u1", "acme"),
                newUserId = "u2",
                newSocieteSlug = "acme",
                hasLocalTenantData = true,
            ),
        )
    }

    @Test
    fun otherUser_emptyDb_skipsPrep() {
        assertFalse(
            AccountSwitchDecision.requiresAccountPrep(
                fingerprint = AccountFingerprintStore.Fingerprint("u1", "acme"),
                newUserId = "u2",
                newSocieteSlug = "acme",
                hasLocalTenantData = false,
            ),
        )
    }

    @Test
    fun otherSociete_sameUser_withData_requiresPrep() {
        assertTrue(
            AccountSwitchDecision.requiresAccountPrep(
                fingerprint = AccountFingerprintStore.Fingerprint("u1", "acme"),
                newUserId = "u1",
                newSocieteSlug = "other",
                hasLocalTenantData = true,
            ),
        )
    }
}
