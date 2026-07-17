package re.savio.mobile.data.local.database

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "savio_prefs")

@Singleton
class TokenDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private val KEY_ACCESS_TOKEN = stringPreferencesKey("access_token")
        private val KEY_SOCIETE_SLUG = stringPreferencesKey("societe_slug")
        private val KEY_SOCIETE_NAME = stringPreferencesKey("societe_name")
        private val KEY_USER_ID = stringPreferencesKey("user_id")
        private val KEY_USER_EMAIL = stringPreferencesKey("user_email")
        private val KEY_USER_FULLNAME = stringPreferencesKey("user_fullname")
        private val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        private val KEY_DEMO_COMPLETION_BANNER_DISMISSED =
            booleanPreferencesKey("demo_completion_banner_dismissed")

        /** Pending switch credentials — same store as session; never in AccountSwitchStateStore. */
        private val KEY_PENDING_SWITCH_ACCESS_TOKEN =
            stringPreferencesKey("pending_switch_access_token")
        private val KEY_PENDING_SWITCH_SOCIETE_SLUG =
            stringPreferencesKey("pending_switch_societe_slug")
        private val KEY_PENDING_SWITCH_SOCIETE_NAME =
            stringPreferencesKey("pending_switch_societe_name")
        private val KEY_PENDING_SWITCH_USER_ID =
            stringPreferencesKey("pending_switch_user_id")
        private val KEY_PENDING_SWITCH_USER_EMAIL =
            stringPreferencesKey("pending_switch_user_email")
        private val KEY_PENDING_SWITCH_USER_FULLNAME =
            stringPreferencesKey("pending_switch_user_fullname")
        private val KEY_PENDING_SWITCH_ONBOARDING_COMPLETED =
            booleanPreferencesKey("pending_switch_onboarding_completed")
    }

    data class PendingSwitchSession(
        val accessToken: String,
        val societeSlug: String,
        val societeName: String,
        val userId: String,
        val userEmail: String,
        val userFullName: String?,
        val onboardingCompleted: Boolean,
    )

    val accessToken: Flow<String?> = context.dataStore.data
        .map { it[KEY_ACCESS_TOKEN] }

    val societeSlug: Flow<String?> = context.dataStore.data
        .map { it[KEY_SOCIETE_SLUG] }

    val societeName: Flow<String?> = context.dataStore.data
        .map { it[KEY_SOCIETE_NAME] }

    val isLoggedIn: Flow<Boolean> = context.dataStore.data
        .map { prefs ->
            prefs[KEY_ACCESS_TOKEN] != null &&
                    !prefs[KEY_SOCIETE_SLUG].isNullOrBlank()
        }

    /** Absent = utilisateurs existants (comportement legacy : considéré comme terminé). */
    val onboardingCompleted: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_ONBOARDING_COMPLETED] ?: true
    }

    val demoCompletionBannerDismissed: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_DEMO_COMPLETION_BANNER_DISMISSED] ?: false
    }

    suspend fun setDemoCompletionBannerDismissed(value: Boolean) {
        context.dataStore.edit { it[KEY_DEMO_COMPLETION_BANNER_DISMISSED] = value }
    }

    suspend fun saveSession(
        accessToken: String,
        societeSlug: String,
        societeName: String,
        userId: String,
        userEmail: String,
        userFullName: String?
    ) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ACCESS_TOKEN] = accessToken
            prefs[KEY_SOCIETE_SLUG] = societeSlug
            prefs[KEY_SOCIETE_NAME] = societeName
            prefs[KEY_USER_ID] = userId
            prefs[KEY_USER_EMAIL] = userEmail
            userFullName?.let { prefs[KEY_USER_FULLNAME] = it }
        }
    }

    /**
     * Clears the active session.
     * @param clearPendingSwitch when false, keeps pending switch credentials
     *   (needed on HTTP 401 during AccountPrep flush — otherwise the new JWT is wiped).
     */
    suspend fun clearSession(clearPendingSwitch: Boolean = true) {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_ACCESS_TOKEN)
            prefs.remove(KEY_SOCIETE_SLUG)
            prefs.remove(KEY_SOCIETE_NAME)
            prefs.remove(KEY_USER_ID)
            prefs.remove(KEY_USER_EMAIL)
            prefs.remove(KEY_USER_FULLNAME)
            prefs.remove(KEY_ONBOARDING_COMPLETED)
            prefs.remove(KEY_DEMO_COMPLETION_BANNER_DISMISSED)
            if (clearPendingSwitch) {
                removePendingSwitchKeys(prefs)
            }
        }
    }

    suspend fun savePendingSwitchSession(session: PendingSwitchSession) {
        context.dataStore.edit { prefs ->
            prefs[KEY_PENDING_SWITCH_ACCESS_TOKEN] = session.accessToken
            prefs[KEY_PENDING_SWITCH_SOCIETE_SLUG] = session.societeSlug
            prefs[KEY_PENDING_SWITCH_SOCIETE_NAME] = session.societeName
            prefs[KEY_PENDING_SWITCH_USER_ID] = session.userId
            prefs[KEY_PENDING_SWITCH_USER_EMAIL] = session.userEmail
            session.userFullName?.let { prefs[KEY_PENDING_SWITCH_USER_FULLNAME] = it }
                ?: prefs.remove(KEY_PENDING_SWITCH_USER_FULLNAME)
            prefs[KEY_PENDING_SWITCH_ONBOARDING_COMPLETED] = session.onboardingCompleted
        }
    }

    suspend fun getPendingSwitchSession(): PendingSwitchSession? {
        val prefs = context.dataStore.data.first()
        val token = prefs[KEY_PENDING_SWITCH_ACCESS_TOKEN]?.takeIf { it.isNotBlank() } ?: return null
        val slug = prefs[KEY_PENDING_SWITCH_SOCIETE_SLUG]?.takeIf { it.isNotBlank() } ?: return null
        val userId = prefs[KEY_PENDING_SWITCH_USER_ID]?.takeIf { it.isNotBlank() } ?: return null
        return PendingSwitchSession(
            accessToken = token,
            societeSlug = slug,
            societeName = prefs[KEY_PENDING_SWITCH_SOCIETE_NAME].orEmpty(),
            userId = userId,
            userEmail = prefs[KEY_PENDING_SWITCH_USER_EMAIL].orEmpty(),
            userFullName = prefs[KEY_PENDING_SWITCH_USER_FULLNAME],
            onboardingCompleted = prefs[KEY_PENDING_SWITCH_ONBOARDING_COMPLETED] ?: true,
        )
    }

    /** Apply pending as active session and remove pending keys immediately. */
    suspend fun consumePendingSwitchSession(): PendingSwitchSession? {
        val pending = getPendingSwitchSession() ?: return null
        context.dataStore.edit { prefs ->
            prefs[KEY_ACCESS_TOKEN] = pending.accessToken
            prefs[KEY_SOCIETE_SLUG] = pending.societeSlug
            prefs[KEY_SOCIETE_NAME] = pending.societeName
            prefs[KEY_USER_ID] = pending.userId
            prefs[KEY_USER_EMAIL] = pending.userEmail
            pending.userFullName?.let { prefs[KEY_USER_FULLNAME] = it }
                ?: prefs.remove(KEY_USER_FULLNAME)
            prefs[KEY_ONBOARDING_COMPLETED] = pending.onboardingCompleted
            removePendingSwitchKeys(prefs)
        }
        return pending
    }

    suspend fun clearPendingSwitchSession() {
        context.dataStore.edit { removePendingSwitchKeys(it) }
    }

    suspend fun hasPendingSwitchSession(): Boolean = getPendingSwitchSession() != null

    private fun removePendingSwitchKeys(prefs: androidx.datastore.preferences.core.MutablePreferences) {
        prefs.remove(KEY_PENDING_SWITCH_ACCESS_TOKEN)
        prefs.remove(KEY_PENDING_SWITCH_SOCIETE_SLUG)
        prefs.remove(KEY_PENDING_SWITCH_SOCIETE_NAME)
        prefs.remove(KEY_PENDING_SWITCH_USER_ID)
        prefs.remove(KEY_PENDING_SWITCH_USER_EMAIL)
        prefs.remove(KEY_PENDING_SWITCH_USER_FULLNAME)
        prefs.remove(KEY_PENDING_SWITCH_ONBOARDING_COMPLETED)
    }

    suspend fun setOnboardingCompleted(value: Boolean) {
        context.dataStore.edit { it[KEY_ONBOARDING_COMPLETED] = value }
    }

    suspend fun isOnboardingCompletedFirst(): Boolean =
        context.dataStore.data.map { it[KEY_ONBOARDING_COMPLETED] ?: true }.first()

    suspend fun getSocieteSlugStored(): String? =
        context.dataStore.data.map { it[KEY_SOCIETE_SLUG] }.first()?.takeIf { it.isNotBlank() }

    suspend fun getAccessToken(): String? =
        context.dataStore.data.map { it[KEY_ACCESS_TOKEN] }.first()

    suspend fun getUserId(): String? =
        context.dataStore.data.map { it[KEY_USER_ID] }.first()

    suspend fun saveTokenOnly(accessToken: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ACCESS_TOKEN] = accessToken
        }
    }
    suspend fun getUserEmail(): String? =
        context.dataStore.data.map { it[KEY_USER_EMAIL] }.first()

    suspend fun getUserFullName(): String? =
        context.dataStore.data.map { it[KEY_USER_FULLNAME] }.first()

    suspend fun getSocieteName(): String? =
        context.dataStore.data.map { it[KEY_SOCIETE_NAME] }.first()
}