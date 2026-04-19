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
    suspend fun deleteByInterventionId(interventionId: String)

    @Query(
        """
        DELETE FROM equipments 
        WHERE interventionId IN (
            SELECT id FROM interventions 
            WHERE scheduledAt < :date 
            AND status = 'completed'
            AND syncStatus = 'SYNCED'
        )
        """,
    )
    suspend fun deleteOlderThan(date: String)

    @Query(
        """
        DELETE FROM equipments 
        WHERE interventionId IN (
            SELECT id FROM interventions 
            WHERE scheduledAt LIKE :date || '%' 
            AND syncStatus = 'SYNCED' 
            AND id NOT IN (:keepIds)
        )
        """,
    )
    suspend fun deleteEquipmentsForSyncedInterventionsNotInKeepList(date: String, keepIds: List<String>)

    @Query(
        """
        DELETE FROM equipments 
        WHERE interventionId IN (
            SELECT id FROM interventions 
            WHERE scheduledAt LIKE :date || '%' 
            AND syncStatus = 'SYNCED'
        )
        """,
    )
    suspend fun deleteEquipmentsForAllSyncedInterventionsOnDate(date: String)

    @Query("SELECT * FROM equipments WHERE id = :id LIMIT 1")
    fun getEquipmentById(id: String): Flow<EquipmentEntity?>

    @Query("DELETE FROM equipments WHERE id = :id AND interventionId = :interventionId")
    suspend fun deleteById(id: String, interventionId: String)

    @Query(
        """
        UPDATE equipments
        SET typeCode = 'replaced'
        WHERE id = :equipmentId
        AND interventionId = :interventionId
        """,
    )
    suspend fun markAsReplaced(equipmentId: String, interventionId: String)
}