package re.melchior.saviomobile.data.local.database

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
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
    }

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

    suspend fun clearSession() {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_ACCESS_TOKEN)
            prefs.remove(KEY_SOCIETE_SLUG)
            prefs.remove(KEY_SOCIETE_NAME)
            prefs.remove(KEY_USER_ID)
            prefs.remove(KEY_USER_EMAIL)
            prefs.remove(KEY_USER_FULLNAME)
        }
    }

    suspend fun getAccessToken(): String? =
        context.dataStore.data.map { it[KEY_ACCESS_TOKEN] }.first()

    suspend fun getUserId(): String? =
        context.dataStore.data.map { it[KEY_USER_ID] }.first()

    suspend fun saveTokenOnly(accessToken: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ACCESS_TOKEN] = accessToken
            // On ne touche PAS KEY_SOCIETE_SLUG
            // isLoggedIn = flow sur KEY_ACCESS_TOKEN → devient true ici !
        }
    }
    suspend fun getUserEmail(): String? =
        context.dataStore.data.map { it[KEY_USER_EMAIL] }.first()

    suspend fun getUserFullName(): String? =
        context.dataStore.data.map { it[KEY_USER_FULLNAME] }.first()
}