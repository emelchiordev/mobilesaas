package re.melchior.saviomobile.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import re.melchior.saviomobile.data.local.entity.EquipmentSnapshotEntity

@Dao
interface EquipmentSnapshotDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(snapshots: List<EquipmentSnapshotEntity>)

    @Query("SELECT * FROM equipment_snapshots WHERE interventionId = :interventionId")
    suspend fun getByInterventionId(interventionId: String): List<EquipmentSnapshotEntity>

    @Query("SELECT COUNT(*) FROM equipment_snapshots WHERE interventionId = :interventionId")
    suspend fun countByInterventionId(interventionId: String): Int

    @Query("DELETE FROM equipment_snapshots WHERE interventionId = :interventionId")
    suspend fun deleteByInterventionId(interventionId: String)
}
