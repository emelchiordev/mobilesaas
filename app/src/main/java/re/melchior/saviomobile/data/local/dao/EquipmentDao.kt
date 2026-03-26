package re.melchior.saviomobile.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import re.melchior.saviomobile.data.local.entity.EquipmentEntity

@Dao
interface EquipmentDao {

    @Query("SELECT * FROM equipments WHERE interventionId = :interventionId ORDER BY isPrimary DESC")
    fun getEquipmentsByIntervention(interventionId: String): Flow<List<EquipmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(equipments: List<EquipmentEntity>)

    @Query("DELETE FROM equipments WHERE interventionId = :interventionId")
    suspend fun deleteByIntervention(interventionId: String)

    @Query("DELETE FROM equipments WHERE interventionId IN (SELECT id FROM interventions WHERE scheduledAt < :date)")
    suspend fun deleteOlderThan(date: String)

    @Query("SELECT * FROM equipments WHERE id = :id")
    fun getEquipmentById(id: String): Flow<EquipmentEntity?>
}