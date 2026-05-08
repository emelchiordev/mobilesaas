package re.melchior.saviomobile.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import re.melchior.saviomobile.data.local.entity.EquipmentEntity

@Dao
interface EquipmentDao {

    @Query(
        """
        SELECT * FROM equipments 
        WHERE interventionId = :interventionId
        ORDER BY `order` ASC, id ASC
        """,
    )
    fun getEquipmentsByIntervention(interventionId: String): Flow<List<EquipmentEntity>>

    @Query(
        """
        SELECT * FROM equipments 
        WHERE interventionId = :interventionId
        ORDER BY `order` ASC, id ASC
        """,
    )
    suspend fun getEquipmentsByInterventionOnce(interventionId: String): List<EquipmentEntity>

    @Query(
        """
        SELECT * FROM equipments 
        WHERE unitId = :unitId
        AND (typeCode != 'replaced' OR typeCode IS NULL)
        ORDER BY `order` ASC, id ASC
        """,
    )
    suspend fun getEquipmentsByUnitId(unitId: String): List<EquipmentEntity>

    @Query("SELECT MAX(`order`) FROM equipments WHERE unitId = :unitId")
    suspend fun getMaxOrderForUnit(unitId: String): Int?

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
            AND syncStatus = 'SYNCED'
            AND status NOT IN ('in_progress', 'pending_validation', 'completed')
            AND isChantier = 0
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
            AND status NOT IN ('completed', 'pending_validation')
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
            AND status NOT IN ('completed', 'pending_validation')
        )
        """,
    )
    suspend fun deleteEquipmentsForAllSyncedInterventionsOnDate(date: String)

    @Query("SELECT * FROM equipments WHERE id = :serverId LIMIT 1")
    fun getEquipmentByServerId(serverId: String): Flow<EquipmentEntity?>

    @Query(
        """
        SELECT * FROM equipments
        WHERE interventionId = :interventionId
        AND id = :serverId
        LIMIT 1
        """,
    )
    fun getEquipmentByInterventionAndServerId(
        interventionId: String,
        serverId: String,
    ): Flow<EquipmentEntity?>

    @Query("SELECT * FROM equipments WHERE id = :serverId LIMIT 1")
    suspend fun getEquipmentByServerIdOnce(serverId: String): EquipmentEntity?

    @Query(
        """
        SELECT * FROM equipments 
        WHERE interventionId = :interventionId 
        AND `order` = :order 
        LIMIT 1
        """,
    )
    suspend fun getEquipmentByInterventionAndOrder(
        interventionId: String,
        order: Int,
    ): EquipmentEntity?

    @Query(
        """
        DELETE FROM equipments
        WHERE interventionId = :interventionId
        AND `order` NOT IN (:keepOrders)
        """,
    )
    suspend fun deleteEquipmentsNotInList(
        interventionId: String,
        keepOrders: List<Int>,
    )

    @Query(
        """
        DELETE FROM equipments 
        WHERE interventionId = :interventionId 
        AND `order` = :order
        """,
    )
    suspend fun deleteByInterventionAndOrder(interventionId: String, order: Int)

    @Query(
        """
        UPDATE equipments
        SET typeCode = 'replaced'
        WHERE interventionId = :interventionId
        AND `order` = :order
        """,
    )
    suspend fun markAsReplaced(interventionId: String, order: Int)

    @Query(
        """
        UPDATE equipments
        SET typeCode = NULL
        WHERE interventionId = :interventionId
        AND `order` = :order
        AND typeCode = 'replaced'
        """,
    )
    suspend fun restoreTypeCode(interventionId: String, order: Int)
}
