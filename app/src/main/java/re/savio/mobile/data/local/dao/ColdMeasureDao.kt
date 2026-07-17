package re.savio.mobile.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import re.savio.mobile.data.local.entity.ColdMeasureEntity

@Dao
interface ColdMeasureDao {

    @Query(
        """
        SELECT * FROM cold_measures
        WHERE interventionId = :interventionId AND equipmentId = :equipmentId
        LIMIT 1
        """
    )
    suspend fun getByInterventionAndEquipment(
        interventionId: String,
        equipmentId: String,
    ): ColdMeasureEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ColdMeasureEntity)

    @Query("SELECT * FROM cold_measures WHERE is_dirty = 1")
    suspend fun getDirty(): List<ColdMeasureEntity>

    @Query("UPDATE cold_measures SET is_dirty = 0 WHERE id = :id")
    suspend fun markClean(id: String)

    @Query("DELETE FROM cold_measures WHERE interventionId = :interventionId")
    suspend fun deleteByInterventionId(interventionId: String)

    @Query("SELECT COUNT(*) FROM cold_measures WHERE interventionId = :interventionId")
    suspend fun countForIntervention(interventionId: String): Int

    @Query("SELECT COUNT(*) FROM cold_measures WHERE interventionId = :interventionId")
    fun observeCountForIntervention(interventionId: String): Flow<Int>

    @Query(
        """
        UPDATE cold_measures SET equipmentId = :newEquipmentId
        WHERE interventionId = :interventionId AND equipmentId = :oldEquipmentId
        """,
    )
    suspend fun updateEquipmentId(
        interventionId: String,
        oldEquipmentId: String,
        newEquipmentId: String,
    )
}
