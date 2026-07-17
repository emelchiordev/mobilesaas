package re.savio.mobile.data.account

import re.savio.mobile.data.local.database.AccountFingerprintStore

/**
 * Pure decision: whether AccountPrep (flush→wipe→pull) is required.
 */
object AccountSwitchDecision {

    fun requiresAccountPrep(
        fingerprint: AccountFingerprintStore.Fingerprint?,
        newUserId: String,
        newSocieteSlug: String,
        hasLocalTenantData: Boolean,
        /** Active/outgoing session before login commit (may be null on Welcome). */
        currentUserId: String? = null,
        currentSocieteSlug: String? = null,
    ): Boolean {
        // Same account reconnecting: keep local DB, just refresh session + fingerprint.
        if (
            !currentUserId.isNullOrBlank() &&
            !currentSocieteSlug.isNullOrBlank() &&
            currentUserId == newUserId &&
            currentSocieteSlug == newSocieteSlug
        ) {
            return false
        }
        if (fingerprint == null) {
            return hasLocalTenantData
        }
        val same =
            fingerprint.userId == newUserId && fingerprint.societeSlug == newSocieteSlug
        if (same) return false
        // Different account: wipe only if leftover local data remains.
        return hasLocalTenantData
    }
}
