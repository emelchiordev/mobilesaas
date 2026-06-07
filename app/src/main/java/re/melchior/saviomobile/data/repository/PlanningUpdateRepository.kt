package re.melchior.saviomobile.data.repository

import androidx.room.withTransaction
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import re.melchior.saviomobile.data.local.dao.InterventionDao
import re.melchior.saviomobile.data.local.dao.PendingOperationDao
import re.melchior.saviomobile.data.local.dao.SettingsDao
import re.melchior.saviomobile.data.local.database.SavioDatabase
import re.melchior.saviomobile.data.local.entity.PendingOperationEntity
import re.melchior.saviomobile.util.MobilePlanningPermission
import re.melchior.saviomobile.util.parseMobilePlanningPermission
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

sealed class PlanningUpdateOutcome {
    data object Success : PlanningUpdateOutcome()
    data class Error(val message: String) : PlanningUpdateOutcome()
    data object ReadOnly : PlanningUpdateOutcome()
}

@Singleton
class PlanningUpdateRepository @Inject constructor(
    private val database: SavioDatabase,
    private val interventionDao: InterventionDao,
    private val pendingOperationDao: PendingOperationDao,
    private val settingsDao: SettingsDao,
    private val mobileSyncOrchestrator: MobileSyncOrchestrator,
) {
    private val gson = Gson()

    suspend fun currentPermission(): MobilePlanningPermission {
        val settings = settingsDao.getSettingsOnce()
        return parseMobilePlanningPermission(settings?.mobilePlanningPermission)
    }

    suspend fun updatePlanning(
        interventionId: String,
        scheduledAt: String,
        timeSlot: String,
        isUrgent: Boolean,
    ): PlanningUpdateOutcome {
        when (currentPermission()) {
            MobilePlanningPermission.READ_ONLY -> return PlanningUpdateOutcome.ReadOnly
            else -> Unit
        }

        database.withTransaction {
            val existing = interventionDao.getInterventionByIdOnce(interventionId)
                ?: throw IllegalStateException("Intervention introuvable")
            pendingOperationDao.deletePendingByInterventionAndType(interventionId, "UPDATE_INTERVENTION")
            interventionDao.insertOrReplace(
                existing.copy(
                    scheduledAt = scheduledAt,
                    timeSlot = timeSlot,
                    isUrgent = isUrgent,
                    hasLocalChanges = true,
                ),
            )
            pendingOperationDao.insert(
                PendingOperationEntity(
                    id = "op-planning-${UUID.randomUUID()}",
                    type = "UPDATE_INTERVENTION",
                    payload = gson.toJson(
                        mapOf(
                            "interventionId" to interventionId,
                            "scheduledAt" to scheduledAt,
                            "timeSlot" to timeSlot,
                            "isUrgent" to isUrgent,
                        ),
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
            is PushResult.Error -> PlanningUpdateOutcome.Error(pushResult.message)
            else -> PlanningUpdateOutcome.Success
        }
    }
}
