package re.savio.mobile.data.local.database

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Crash-resumable account-switch progress.
 *
 * Non-secret fields only — **never** store a JWT here.
 * Dedicated SharedPreferences — **must never** be cleared by [TokenDataStore.clearSession].
 */
@Singleton
class AccountSwitchStateStore @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val prefs =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    enum class State {
        IDLE,
        FLUSHING,
        FLUSHED,
        WIPING,
        PULLING,
    }

    data class Snapshot(
        val state: State,
        val pendingUserId: String?,
        val pendingSocieteSlug: String?,
        val outgoingUserId: String?,
        val outgoingSocieteSlug: String?,
        /** Logout wipe (no pending session to consume). */
        val isLogout: Boolean,
    )

    fun read(): Snapshot {
        val raw = prefs.getString(KEY_STATE, State.IDLE.name) ?: State.IDLE.name
        val state = runCatching { State.valueOf(raw) }.getOrDefault(State.IDLE)
        return Snapshot(
            state = state,
            pendingUserId = prefs.getString(KEY_PENDING_USER_ID, null),
            pendingSocieteSlug = prefs.getString(KEY_PENDING_SOCIETE_SLUG, null),
            outgoingUserId = prefs.getString(KEY_OUTGOING_USER_ID, null),
            outgoingSocieteSlug = prefs.getString(KEY_OUTGOING_SOCIETE_SLUG, null),
            isLogout = prefs.getBoolean(KEY_IS_LOGOUT, false),
        )
    }

    fun write(
        state: State,
        pendingUserId: String? = read().pendingUserId,
        pendingSocieteSlug: String? = read().pendingSocieteSlug,
        outgoingUserId: String? = read().outgoingUserId,
        outgoingSocieteSlug: String? = read().outgoingSocieteSlug,
        isLogout: Boolean = read().isLogout,
    ) {
        prefs.edit()
            .putString(KEY_STATE, state.name)
            .putString(KEY_PENDING_USER_ID, pendingUserId)
            .putString(KEY_PENDING_SOCIETE_SLUG, pendingSocieteSlug)
            .putString(KEY_OUTGOING_USER_ID, outgoingUserId)
            .putString(KEY_OUTGOING_SOCIETE_SLUG, outgoingSocieteSlug)
            .putBoolean(KEY_IS_LOGOUT, isLogout)
            .commit()
    }

    fun resetToIdle() {
        prefs.edit()
            .putString(KEY_STATE, State.IDLE.name)
            .remove(KEY_PENDING_USER_ID)
            .remove(KEY_PENDING_SOCIETE_SLUG)
            .remove(KEY_OUTGOING_USER_ID)
            .remove(KEY_OUTGOING_SOCIETE_SLUG)
            .putBoolean(KEY_IS_LOGOUT, false)
            .commit()
    }

    companion object {
        /** Dedicated file — do not merge into savio_prefs / clearSession. */
        const val PREFS_NAME = "savio_account_switch"
        private const val KEY_STATE = "state"
        private const val KEY_PENDING_USER_ID = "pending_user_id"
        private const val KEY_PENDING_SOCIETE_SLUG = "pending_societe_slug"
        private const val KEY_OUTGOING_USER_ID = "outgoing_user_id"
        private const val KEY_OUTGOING_SOCIETE_SLUG = "outgoing_societe_slug"
        private const val KEY_IS_LOGOUT = "is_logout"
    }
}
