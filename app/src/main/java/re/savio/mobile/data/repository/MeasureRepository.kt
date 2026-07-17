package re.savio.mobile.data.repository

import re.savio.mobile.data.local.dao.MeasureDao
import re.savio.mobile.data.local.entity.MeasureEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MeasureRepository @Inject constructor(
    private val measureDao: MeasureDao,
) {

    suspend fun getMeasure(
        interventionId: String,
        equipmentOrder: Int,
    ): MeasureEntity? = measureDao.getByInterventionAndOrder(
        interventionId,
        equipmentOrder,
    )

    suspend fun saveMeasure(measure: MeasureEntity) =
        measureDao.upsert(measure)

    suspend fun getDirty(): List<MeasureEntity> =
        measureDao.getDirty()

    suspend fun markClean(
        interventionId: String,
        equipmentOrder: Int,
    ) = measureDao.markClean(interventionId, equipmentOrder)

    suspend fun deleteByInterventionId(interventionId: String) =
        measureDao.deleteByInterventionId(interventionId)
}
