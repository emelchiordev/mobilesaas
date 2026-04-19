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

    @Query("SELECT COUNT(*) FROM pending_operations WHERE status = 'pending'")
    fun getPendingCount(): Flow<Int>

    @Query("SELECT * FROM pending_operations WHERE id = :id AND type = :type LIMIT 1")
    suspend fun getByIdAndType(id: String, type: String): PendingOperationEntity?

    @Query("DELETE FROM pending_operations WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM pending_operations WHERE interventionId = :interventionId")
    suspend fun deleteByInterventionId(interventionId: String)
}
