package re.melchior.saviomobile.data.repository

import re.melchior.saviomobile.data.local.dao.ColdMeasureDao
import re.melchior.saviomobile.data.local.entity.ColdMeasureEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ColdMeasureRepository @Inject constructor(
    private val dao: ColdMeasureDao,
) {

    suspend fun getOrCreate(
        interventionId: String,
        equipmentId: String,
    ): ColdMeasureEntity {
        return dao.getByInterventionAndEquipment(interventionId, equipmentId)
            ?: ColdMeasureEntity(
                id = java.util.UUID.randomUUID().toString(),
                interventionId = interventionId,
                equipmentId = equipmentId,
                updatedAt = java.time.Instant.now().toString(),
            )
    }

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
