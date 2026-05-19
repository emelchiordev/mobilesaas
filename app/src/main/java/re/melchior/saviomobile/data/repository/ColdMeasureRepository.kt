package re.melchior.saviomobile.data.repository

import re.melchior.saviomobile.data.local.dao.ColdMeasureDao
import re.melchior.saviomobile.data.local.entity.ColdMeasureEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ColdMeasureRepository @Inject constructor(
    private val dao: ColdMeasureDao,
) {

    suspend fun existsPersisted(
        interventionId: String,
        equipmentId: String,
    ): Boolean =
        dao.getByInterventionAndEquipment(interventionId, equipmentId) != null

    suspend fun get(
        interventionId: String,
        equipmentId: String,
    ): ColdMeasureEntity? =
        dao.getByInterventionAndEquipment(interventionId, equipmentId)

    fun newDraft(
        interventionId: String,
        equipmentId: String,
    ): ColdMeasureEntity =
        ColdMeasureEntity(
            id = java.util.UUID.randomUUID().toString(),
            interventionId = interventionId,
            equipmentId = equipmentId,
            isDirty = false,
            updatedAt = "",
        )

    suspend fun save(entity: ColdMeasureEntity) {
        dao.upsert(
            entity.copy(
                updatedAt = java.time.Instant.now().toString(),
                isDirty = true,
            )
        )
    }

    suspend fun getDirty(): List<ColdMeasureEntity> = dao.getDirty()

    suspend fun markClean(id: String) = dao.markClean(id)
}
