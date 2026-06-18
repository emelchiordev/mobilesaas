package re.melchior.saviomobile.data.repository

import androidx.room.withTransaction
import com.google.gson.Gson
import re.melchior.saviomobile.data.local.dao.InterventionDao
import re.melchior.saviomobile.data.local.dao.PendingOperationDao
import re.melchior.saviomobile.data.local.dao.SettingsDao
import re.melchior.saviomobile.data.local.database.SavioDatabase
import re.melchior.saviomobile.data.local.entity.PendingOperationEntity
import re.melchior.saviomobile.util.isFollowUpPending
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

sealed class FollowUpResolveOutcome {
    data object Success : FollowUpResolveOutcome()
    data class Error(val message: String) : FollowUpResolveOutcome()
    data object BlockedBySettings : FollowUpResolveOutcome()
    data object NotPending : FollowUpResolveOutcome()
}

@Singleton
class FollowUpResolveRepository @Inject constructor(
    private val database: SavioDatabase,
    private val interventionDao: InterventionDao,
    private val pendingOperationDao: PendingOperationDao,
    private val settingsDao: SettingsDao,
    private val mobileSyncOrchestrator: MobileSyncOrchestrator,
) {
    private val gson = Gson()

    suspend fun resolveFollowUp(
        interventionId: String,
        followUpNote: String? = null,
    ): FollowUpResolveOutcome {
        val settings = settingsDao.getSettingsOnce()
        if (settings?.blockMobileFollowUpResolve == true) {
            return FollowUpResolveOutcome.BlockedBySettings
        }

        val existing = interventionDao.getInterventionByIdOnce(interventionId)
            ?: return FollowUpResolveOutcome.Error("Intervention introuvable")

        if (!isFollowUpPending(existing.followUpStatus, existing.followUpRequired, existing.status)) {
            return FollowUpResolveOutcome.NotPending
        }

        database.withTransaction {
            pendingOperationDao.deletePendingByInterventionAndType(interventionId, "RESOLVE_FOLLOW_UP")
            interventionDao.resolveFollowUp(interventionId, followUpNote?.trim()?.takeIf { it.isNotEmpty() })
            pendingOperationDao.insert(
                PendingOperationEntity(
                    id = "op-resolve-followup-${UUID.randomUUID()}",
                    type = "RESOLVE_FOLLOW_UP",
                    payload = gson.toJson(
                        buildMap {
                            put("interventionId", interventionId)
                            followUpNote?.trim()?.takeIf { it.isNotEmpty() }?.let { put("followUpNote", it) }
                        },
                    ),
                    occurredAt = Instant.now().toString(),
                    interventionId = interventionId,
                    createdAt = Instant.now().toString(),
                ),
            )
        }

        return when (
            val pushResult =
                mobileSyncOrchestrator.runFullSync(
                    pullDate = LocalDate.now(),
                    pullForce = true,
                ).pushResult
        ) {
            is PushResult.Error -> FollowUpResolveOutcome.Error(pushResult.message)
            else -> FollowUpResolveOutcome.Success
        }
    }
}
