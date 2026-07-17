package re.savio.mobile.data.local.database

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Last account that successfully used this device.
 *
 * Lives in a dedicated SharedPreferences file — **must never** be cleared by
 * [TokenDataStore.clearSession] or any session wipe.
 */
@Singleton
class AccountFingerprintStore @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val prefs =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    data class Fingerprint(
        val userId: String,
        val societeSlug: String,
    )

    fun read(): Fingerprint? {
        val userId = prefs.getString(KEY_USER_ID, null)?.takeIf { it.isNotBlank() } ?: return null
        val slug = prefs.getString(KEY_SOCIETE_SLUG, null)?.takeIf { it.isNotBlank() } ?: return null
        return Fingerprint(userId = userId, societeSlug = slug)
    }

    fun write(userId: String, societeSlug: String) {
        prefs.edit()
            .putString(KEY_USER_ID, userId)
            .putString(KEY_SOCIETE_SLUG, societeSlug)
            .apply()
    }

    fun matches(userId: String, societeSlug: String): Boolean {
        val current = read() ?: return false
        return current.userId == userId && current.societeSlug == societeSlug
    }

    companion object {
        /** Dedicated file — do not merge into savio_prefs / clearSession. */
        const val PREFS_NAME = "savio_account_fingerprint"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_SOCIETE_SLUG = "societe_slug"
    }
}
