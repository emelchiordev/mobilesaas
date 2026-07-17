package re.savio.mobile.data.repository

import com.google.gson.Gson
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import re.savio.mobile.data.local.dao.InstallationCheckDao
import re.savio.mobile.data.local.dao.PendingOperationDao
import re.savio.mobile.data.local.entity.InstallationCheckEntity
import re.savio.mobile.data.local.entity.PendingOperationEntity
import re.savio.mobile.data.remote.dto.InstallationCheckPullDto

@Singleton
class InstallationCheckRepository @Inject constructor(
    private val dao: InstallationCheckDao,
    private val pendingOperationDao: PendingOperationDao,
) {
    private val gson = Gson()

    fun observe(interventionId: String): Flow<InstallationCheckEntity?> =
        dao.observeByInterventionId(interventionId)

    suspend fun get(interventionId: String): InstallationCheckEntity? =
        dao.getByInterventionId(interventionId)

    fun newDraft(interventionId: String): InstallationCheckEntity =
        InstallationCheckEntity(interventionId = interventionId)

    suspend fun upsertLocal(entity: InstallationCheckEntity) {
        val now = Instant.now().toString()
        val updated =
            entity.copy(
                isDirty = true,
                updatedAt = now,
            )
        dao.upsert(updated)
        enqueuePendingPush(updated, now)
    }

    suspend fun getDirty(): List<InstallationCheckEntity> = dao.getDirty()

    suspend fun markClean(interventionId: String) = dao.markClean(interventionId)

    suspend fun mergeFromPull(dto: InstallationCheckPullDto?, interventionId: String) {
        if (dto == null) return
        val local = dao.getByInterventionId(interventionId)
        if (local?.isDirty == true) return
        dao.upsert(dto.toEntity(interventionId))
    }

    private suspend fun enqueuePendingPush(entity: InstallationCheckEntity, occurredAt: String) {
        val opId = pendingOpId(entity.interventionId)
        val payload = buildPayload(entity)
        val existing = pendingOperationDao.getByIdAndType(opId, "SAVE_INSTALLATION_CHECK")
        if (existing == null) {
            pendingOperationDao.insert(
                PendingOperationEntity(
                    id = opId,
                    type = "SAVE_INSTALLATION_CHECK",
                    payload = gson.toJson(payload),
                    occurredAt = occurredAt,
                    interventionId = entity.interventionId,
                    status = "pending",
                    createdAt = occurredAt,
                ),
            )
        } else {
            pendingOperationDao.updatePayload(opId, gson.toJson(payload))
            if (existing.status == "sent") {
                pendingOperationDao.updateStatus(opId, "pending")
            }
        }
    }

    fun buildPayload(entity: InstallationCheckEntity): Map<String, Any?> {
        val map = linkedMapOf<String, Any?>(
            "interventionId" to entity.interventionId,
            "turbidityTested" to entity.turbidityTested,
            "gasPipeReplaced" to entity.gasPipeReplaced,
        )
        if (entity.turbidityTested) {
            entity.turbidityNtu.toDoubleOrNull()?.let { map["turbidityNtu"] = it }
            entity.turbidityState?.takeIf { it.isNotBlank() }?.let { map["turbidityState"] = it }
        }
        entity.gasPipeType?.takeIf { it.isNotBlank() }?.let { map["gasPipeType"] = it }
        entity.gasPipeValidityDate.takeIf { it.isNotBlank() }?.let { map["gasPipeValidityDate"] = it }
        entity.gasTapCompliant?.takeIf { it.isNotBlank() }?.let { map["gasTapCompliant"] = it }
        entity.notes.takeIf { it.isNotBlank() }?.let { map["notes"] = it }
        return map
    }

    private fun InstallationCheckPullDto.toEntity(interventionId: String): InstallationCheckEntity {
        return InstallationCheckEntity(
            interventionId = interventionId,
            turbidityTested = turbidityTested ?: false,
            turbidityNtu = turbidityNtu?.toString().orEmpty(),
            turbidityState = turbidityState,
            gasPipeType = gasPipeType,
            gasPipeValidityDate = gasPipeValidityDate.orEmpty(),
            gasPipeReplaced = gasPipeReplaced ?: false,
            gasTapCompliant = gasTapCompliant,
            notes = notes.orEmpty(),
            updatedAt = updatedAt.orEmpty(),
            isDirty = false,
        )
    }

    companion object {
        fun pendingOpId(interventionId: String): String = "installation-check-$interventionId"
    }
}
