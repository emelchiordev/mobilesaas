package re.savio.mobile.data.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import re.savio.mobile.data.local.dao.InterventionDao
import re.savio.mobile.data.local.dao.PendingClientDao
import re.savio.mobile.data.local.dao.PendingInterventionDao
import re.savio.mobile.data.local.dao.PendingOperationDao
import re.savio.mobile.data.local.dao.PendingUpdateDao
import re.savio.mobile.data.local.dao.PhotoDao
import re.savio.mobile.data.local.dao.QuotePhotoDao
import re.savio.mobile.data.local.dao.SettingsDao
import re.savio.mobile.data.local.database.TokenDataStore
import re.savio.mobile.ui.utils.NetworkUtils
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

sealed class OutgoingFlushResult {
    data object Success : OutgoingFlushResult()
    data object NothingToFlush : OutgoingFlushResult()
    data class Blocked(
        val message: String,
        val offline: Boolean,
        val unsynced: UnsyncedLocalWorkSummary,
    ) : OutgoingFlushResult()
}

/**
 * Approximate dirty queue size for flush-failure warning copy.
 */
data class UnsyncedLocalWorkSummary(
    val closedInterventionsPendingPush: Int = 0,
    val pendingOfflineInterventions: Int = 0,
    val pendingOperations: Int = 0,
    val pendingUpdates: Int = 0,
    val pendingClients: Int = 0,
    val pendingPhotos: Int = 0,
    val pendingQuotePhotos: Int = 0,
    val dirtyForms: Int = 0,
) {
    val totalItems: Int
        get() =
            closedInterventionsPendingPush +
                pendingOfflineInterventions +
                pendingOperations +
                pendingUpdates +
                pendingClients +
                pendingPhotos +
                pendingQuotePhotos +
                dirtyForms

    /** Interventions-ish count for user-facing copy (closures + offline creates). */
    val interventionLikeCount: Int
        get() = closedInterventionsPendingPush + pendingOfflineInterventions
}

/**
 * Ensures outgoing-account local mutations are pushed before any wipe.
 */
@Singleton
class OutgoingAccountFlushGate @Inject constructor(
    @ApplicationContext private val context: Context,
    private val tokenDataStore: TokenDataStore,
    private val interventionDao: InterventionDao,
    private val pendingOperationDao: PendingOperationDao,
    private val pendingUpdateDao: PendingUpdateDao,
    private val pendingInterventionDao: PendingInterventionDao,
    private val pendingClientDao: PendingClientDao,
    private val photoDao: PhotoDao,
    private val quotePhotoDao: QuotePhotoDao,
    private val coldMeasureRepository: ColdMeasureRepository,
    private val measureRepository: MeasureRepository,
    private val pacMeasureRepository: PacMeasureRepository,
    private val attestationVeRepository: AttestationVeRepository,
    private val installationCheckRepository: InstallationCheckRepository,
    private val settingsDao: SettingsDao,
    private val mobileSyncOrchestrator: MobileSyncOrchestrator,
) {

    suspend fun hasLocalTenantData(): Boolean {
        if (interventionDao.countAll() > 0) return true
        if (settingsDao.getSettingsOnce() != null) return true
        if (pendingClientDao.getPending().isNotEmpty()) return true
        if (pendingInterventionDao.listPendingToSync().isNotEmpty()) return true
        return false
    }

    suspend fun countUnsyncedLocalWork(): UnsyncedLocalWorkSummary {
        val dirtyForms =
            coldMeasureRepository.getDirty().size +
                measureRepository.getDirty().size +
                pacMeasureRepository.getDirty().size +
                attestationVeRepository.getDirty().size +
                installationCheckRepository.getDirty().size
        return UnsyncedLocalWorkSummary(
            closedInterventionsPendingPush = interventionDao.getPendingSyncOnce().size,
            pendingOfflineInterventions = pendingInterventionDao.listPendingToSync().size,
            pendingOperations = pendingOperationDao.getPending().size,
            pendingUpdates = pendingUpdateDao.getPendingOnce().size,
            pendingClients = pendingClientDao.getPending().size,
            pendingPhotos =
                photoDao.getPendingUploadPhotos().size + photoDao.getPendingDeletePhotos().size,
            pendingQuotePhotos =
                quotePhotoDao.getPendingUploads().size + quotePhotoDao.getPendingDeletes().size,
            dirtyForms = dirtyForms,
        )
    }

    suspend fun hasUnsyncedLocalWork(): Boolean = countUnsyncedLocalWork().totalItems > 0

    /**
     * Flush outgoing account. Caller must keep outgoing session active in TokenDataStore.
     */
    suspend fun flushOutgoingAccount(outgoingDisplayName: String?): OutgoingFlushResult {
        val unsynced = countUnsyncedLocalWork()
        if (unsynced.totalItems == 0) {
            return OutgoingFlushResult.NothingToFlush
        }
        // No outgoing JWT: calling sync would 401 and (historically) wipe pending switch.
        if (tokenDataStore.getAccessToken().isNullOrBlank()) {
            return OutgoingFlushResult.Blocked(
                message = blockedMessage(outgoingDisplayName, offline = false),
                offline = false,
                unsynced = unsynced,
            )
        }
        if (!NetworkUtils.isOnline(context)) {
            return OutgoingFlushResult.Blocked(
                message = blockedMessage(outgoingDisplayName, offline = true),
                offline = true,
                unsynced = unsynced,
            )
        }
        return try {
            val result = mobileSyncOrchestrator.runFullSync(
                pullDate = LocalDate.now(),
                pullForce = false,
            )
            when (val push = result.pushResult) {
                is PushResult.Error ->
                    OutgoingFlushResult.Blocked(
                        message = push.message.ifBlank {
                            blockedMessage(outgoingDisplayName, offline = false)
                        },
                        offline = false,
                        unsynced = countUnsyncedLocalWork(),
                    )
                else -> {
                    val still = countUnsyncedLocalWork()
                    if (still.totalItems > 0) {
                        OutgoingFlushResult.Blocked(
                            message = blockedMessage(outgoingDisplayName, offline = false),
                            offline = false,
                            unsynced = still,
                        )
                    } else {
                        OutgoingFlushResult.Success
                    }
                }
            }
        } catch (e: Exception) {
            OutgoingFlushResult.Blocked(
                message = e.message?.takeIf { it.isNotBlank() }
                    ?: blockedMessage(outgoingDisplayName, offline = false),
                offline = false,
                unsynced = countUnsyncedLocalWork(),
            )
        }
    }

    fun blockedMessage(outgoingDisplayName: String?, offline: Boolean): String {
        val who = outgoingDisplayName?.takeIf { it.isNotBlank() } ?: "l'utilisateur précédent"
        return if (offline) {
            "Des données de $who ne sont pas encore synchronisées — connectez-vous avant de changer de compte."
        } else {
            "Des données de $who n'ont pas pu être synchronisées — réessayez avec une connexion stable avant de changer de compte."
        }
    }
}
