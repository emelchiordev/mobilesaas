package re.savio.mobile.data.repository

import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import re.savio.mobile.data.local.dao.SettingsDao
import re.savio.mobile.data.remote.api.CompanySettingsApi
import re.savio.mobile.data.remote.api.SetRefrigerantCapacityAttestationBody
import re.savio.mobile.util.UserProfile

@Singleton
class RefrigerantCapacityAttestationRepository @Inject constructor(
    private val api: CompanySettingsApi,
    private val settingsDao: SettingsDao,
) {
    companion object {
        private const val TAG = "CapacityAttestation"
        const val DEMO_NUMBER = "DEMO000000"
    }

    fun needsCapture(number: String?, isDemo: Boolean): Boolean {
        val trimmed = number?.trim().orEmpty()
        return trimmed.isBlank() || isDemo || trimmed.equals(DEMO_NUMBER, ignoreCase = true)
    }

    fun validateForSave(raw: String): Result<String> {
        val trimmed = raw.trim()
        if (trimmed.length < 4 || trimmed.length > 50) {
            return Result.failure(IllegalArgumentException("Le numéro doit contenir entre 4 et 50 caractères."))
        }
        if (trimmed.equals(DEMO_NUMBER, ignoreCase = true)) {
            return Result.failure(IllegalArgumentException("Remplacez le numéro de démonstration."))
        }
        return Result.success(trimmed)
    }

    /**
     * Offline-first : maj locale immédiate, PATCH en arrière-plan (retry au sync).
     */
    suspend fun saveLocallyAndEnqueue(rawNumber: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            val validated = validateForSave(rawNumber)
            if (validated.isFailure) return@withContext Result.failure(validated.exceptionOrNull()!!)
            val number = validated.getOrThrow()
            val current = settingsDao.getSettingsOnce()
                ?: return@withContext Result.failure(IllegalStateException("Paramètres locaux absents"))
            if (current.profile != UserProfile.ARTISAN_SOLO) {
                return@withContext Result.failure(IllegalStateException("Réservé au profil ARTISAN_SOLO"))
            }
            settingsDao.save(
                current.copy(
                    refrigerantCapacityAttestationNumber = number,
                    refrigerantCapacityAttestationIsDemo = false,
                    refrigerantCapacityAttestationPendingSync = true,
                ),
            )
            flushPendingIfNeeded()
            Result.success(Unit)
        }

    suspend fun flushPendingIfNeeded(): Boolean =
        withContext(Dispatchers.IO) {
            val current = settingsDao.getSettingsOnce() ?: return@withContext false
            if (!current.refrigerantCapacityAttestationPendingSync) return@withContext false
            val number = current.refrigerantCapacityAttestationNumber?.trim().orEmpty()
            if (number.isBlank()) return@withContext false
            try {
                api.setRefrigerantCapacityAttestation(
                    SetRefrigerantCapacityAttestationBody(number = number),
                )
                settingsDao.save(
                    current.copy(refrigerantCapacityAttestationPendingSync = false),
                )
                true
            } catch (e: Exception) {
                Log.w(TAG, "flush pending capacity attestation failed", e)
                false
            }
        }
}
