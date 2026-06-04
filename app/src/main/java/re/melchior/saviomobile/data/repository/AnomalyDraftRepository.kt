package re.melchior.saviomobile.data.repository

import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import re.melchior.saviomobile.data.local.dao.AnomalyDraftDao
import re.melchior.saviomobile.data.local.dao.AnomalyTypeDao
import re.melchior.saviomobile.data.local.dao.PendingOperationDao
import re.melchior.saviomobile.data.local.entity.AnomalyDraftEntity
import re.melchior.saviomobile.data.local.entity.AnomalyTypeEntity
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnomalyDraftRepository @Inject constructor(
    private val anomalyDraftDao: AnomalyDraftDao,
    private val anomalyTypeDao: AnomalyTypeDao,
    private val pendingOperationDao: PendingOperationDao,
) {
    private val gson = Gson()

    fun observeDrafts(interventionId: String): Flow<List<AnomalyDraftEntity>> =
        anomalyDraftDao.observeByIntervention(interventionId)

    suspend fun getCatalogTypesOnce(): List<AnomalyTypeEntity> =
        anomalyTypeDao.getAllActiveOnce()

    suspend fun addDraft(
        interventionId: String,
        unitId: String,
        equipmentId: String?,
        scope: String,
        anomalyTypeCode: String?,
        customDescription: String?,
        action: String?,
    ) {
        anomalyDraftDao.insert(
            AnomalyDraftEntity(
                localId = UUID.randomUUID().toString(),
                interventionId = interventionId,
                unitId = unitId,
                equipmentId = equipmentId,
                scope = scope,
                anomalyTypeCode = anomalyTypeCode,
                customDescription = customDescription?.trim()?.takeIf { it.isNotEmpty() },
                reportedAt = LocalDate.now().toString(),
                action = action?.trim()?.takeIf { it.isNotEmpty() },
                syncStatus = "pending",
            ),
        )
    }

    suspend fun enqueuePendingPushOps(interventionId: String, completedAt: String) {
        val drafts = anomalyDraftDao.getPendingByInterventionOnce(interventionId)
        if (drafts.isEmpty()) return
        val now = Instant.now().toString()
        drafts.forEach { draft ->
            val payload = mapOf(
                "localId" to draft.localId,
                "interventionId" to draft.interventionId,
                "unitId" to draft.unitId,
                "equipmentId" to draft.equipmentId,
                "scope" to draft.scope,
                "anomalyTypeCode" to draft.anomalyTypeCode,
                "customDescription" to draft.customDescription,
                "reportedAt" to draft.reportedAt,
                "action" to draft.action,
                "source" to "intervention",
            )
            pendingOperationDao.insert(
                re.melchior.saviomobile.data.local.entity.PendingOperationEntity(
                    id = draft.localId,
                    type = "CREATE_ANOMALY",
                    payload = gson.toJson(payload),
                    occurredAt = completedAt,
                    interventionId = interventionId,
                    status = "pending",
                    createdAt = now,
                ),
            )
        }
    }

    suspend fun markSynced(localId: String) {
        anomalyDraftDao.updateSyncStatus(localId, "synced")
    }

    suspend fun countDgiForIntervention(interventionId: String): Int {
        val drafts = anomalyDraftDao.getPendingByInterventionOnce(interventionId)
        if (drafts.isEmpty()) return 0
        val typesByCode = anomalyTypeDao.getAllActiveOnce().associateBy { it.code }
        return drafts.count { draft ->
            val code = draft.anomalyTypeCode ?: return@count false
            typesByCode[code]?.level == "dgi"
        }
    }
}
