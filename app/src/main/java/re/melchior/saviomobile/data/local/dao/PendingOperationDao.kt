package re.melchior.saviomobile.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import re.melchior.saviomobile.data.local.entity.PendingOperationEntity

@Dao
interface PendingOperationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(op: PendingOperationEntity)

    @Query("SELECT * FROM pending_operations WHERE status = 'pending' ORDER BY createdAt ASC")
    suspend fun getPending(): List<PendingOperationEntity>

    @Query("UPDATE pending_operations SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: String)

    @Query("UPDATE pending_operations SET payload = :payload WHERE id = :id")
    suspend fun updatePayload(id: String, payload: String)

    @Query(
        """
        SELECT * FROM pending_operations
        WHERE interventionId = :interventionId AND status = 'pending'
        """,
    )
    suspend fun getPendingByInterventionIdOnce(interventionId: String): List<PendingOperationEntity>

    @Query("SELECT COUNT(*) FROM pending_operations WHERE status = 'pending'")
    fun getPendingCount(): Flow<Int>

    @Query(
        """
        SELECT * FROM pending_operations 
        WHERE interventionId = :interventionId 
        AND status = 'pending'
        """,
    )
    fun getPendingByInterventionId(
        interventionId: String,
    ): Flow<List<PendingOperationEntity>>

    @Query("SELECT * FROM pending_operations WHERE id = :id AND type = :type LIMIT 1")
    suspend fun getByIdAndType(id: String, type: String): PendingOperationEntity?

    @Query(
        """
        SELECT * FROM pending_operations
        WHERE interventionId = :interventionId
        AND type = :type
        """,
    )
    suspend fun getByInterventionIdAndType(
        interventionId: String,
        type: String,
    ): List<PendingOperationEntity>

    @Query(
        """
        SELECT * FROM pending_operations
        WHERE interventionId = :interventionId
        AND type IN (:types)
        """,
    )
    suspend fun getByInterventionIdAndTypes(
        interventionId: String,
        types: List<String>,
    ): List<PendingOperationEntity>

    @Query("DELETE FROM pending_operations WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query(
        """
        DELETE FROM pending_operations
        WHERE interventionId = :interventionId
        AND type = :type
        AND status = 'pending'
        """,
    )
    suspend fun deletePendingByInterventionAndType(interventionId: String, type: String)

    @Query("DELETE FROM pending_operations WHERE interventionId = :interventionId")
    suspend fun deleteByInterventionId(interventionId: String)
}
