package re.melchior.saviomobile.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import re.melchior.saviomobile.data.local.entity.MeasureEntity

@Dao
interface MeasureDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(measure: MeasureEntity)

    @Query(
        """
        SELECT * FROM measures 
        WHERE interventionId = :interventionId 
        AND equipmentOrder = :equipmentOrder
        LIMIT 1
        """,
    )
    suspend fun getByInterventionAndOrder(
        interventionId: String,
        equipmentOrder: Int,
    ): MeasureEntity?

    @Query(
        """
        SELECT * FROM measures 
        WHERE interventionId = :interventionId
        """,
    )
    suspend fun getByIntervention(
        interventionId: String,
    ): List<MeasureEntity>

    @Query(
        """
        SELECT * FROM measures 
        WHERE is_dirty = 1
        """,
    )
    suspend fun getDirty(): List<MeasureEntity>

    @Query(
        """
        UPDATE measures SET is_dirty = 0 
        WHERE interventionId = :interventionId 
        AND equipmentOrder = :equipmentOrder
        """,
    )
    suspend fun markClean(
        interventionId: String,
        equipmentOrder: Int,
    )

    @Query(
        """
        DELETE FROM measures 
        WHERE interventionId = :interventionId
        """,
    )
    suspend fun deleteByInterventionId(interventionId: String)
}
