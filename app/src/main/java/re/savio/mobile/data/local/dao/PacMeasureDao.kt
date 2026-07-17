package re.savio.mobile.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import re.savio.mobile.data.local.entity.PacMeasureEntity

@Dao
interface PacMeasureDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: PacMeasureEntity)

    @Query(
        """
        SELECT * FROM pac_measures
        WHERE interventionId = :interventionId
        AND equipmentOrder = :equipmentOrder
        LIMIT 1
        """,
    )
    suspend fun getByInterventionAndOrder(
        interventionId: String,
        equipmentOrder: Int,
    ): PacMeasureEntity?

    @Query("SELECT * FROM pac_measures WHERE is_dirty = 1")
    suspend fun getDirty(): List<PacMeasureEntity>

    @Query(
        """
        UPDATE pac_measures SET is_dirty = 0
        WHERE interventionId = :interventionId
        AND equipmentOrder = :equipmentOrder
        """,
    )
    suspend fun markClean(
        interventionId: String,
        equipmentOrder: Int,
    )

    @Query("DELETE FROM pac_measures WHERE interventionId = :interventionId")
    suspend fun deleteByInterventionId(interventionId: String)

    @Query("SELECT COUNT(*) FROM pac_measures WHERE interventionId = :interventionId")
    suspend fun countForIntervention(interventionId: String): Int

    @Query("SELECT * FROM pac_measures WHERE interventionId = :interventionId")
    fun observeByInterventionId(interventionId: String): Flow<List<PacMeasureEntity>>
}
