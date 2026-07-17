package re.savio.mobile.data.repository

import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import re.savio.mobile.data.local.dao.AnomalyDraftDao
import re.savio.mobile.data.local.dao.AnomalyTypeDao
import re.savio.mobile.data.local.dao.PendingOperationDao
import re.savio.mobile.data.local.entity.AnomalyDraftEntity
import re.savio.mobile.data.local.entity.AnomalyTypeEntity
import re.savio.mobile.util.GAS_PIPE_EXPIRED_ANOMALY_CODE
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

    suspend fun hasDraftWithCode(interventionId: String, code: String): Boolean =
        anomalyDraftDao.countByInterventionAndCode(interventionId, code) > 0

    suspend fun ensureGasPipeExpiredDraft(interventionId: String, unitId: String): Boolean {
        if (hasDraftWithCode(interventionId, GAS_PIPE_EXPIRED_ANOMALY_CODE)) return false
        addDraft(
            interventionId = interventionId,
            unitId = unitId,
            equipmentId = null,
            scope = "installation",
            anomalyTypeCode = GAS_PIPE_EXPIRED_ANOMALY_CODE,
            customDescription = null,
            action = null,
        )
        return true
    }

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

    suspend fun setCorrected(localId: String, corrected: Boolean) {
        anomalyDraftDao.updateCorrected(localId, corrected)
    }

    suspend fun deleteDraft(localId: String) {
        pendingOperationDao.deleteById(localId)
        anomalyDraftDao.deleteByLocalId(localId)
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
                "corrected" to draft.corrected,
                "source" to "intervention",
            )
            pendingOperationDao.insert(
                re.savio.mobile.data.local.entity.PendingOperationEntity(
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
