package re.melchior.saviomobile.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import re.melchior.saviomobile.data.local.entity.PendingUpdateEntity

@Dao
interface PendingUpdateDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(update: PendingUpdateEntity)

    @Query("SELECT * FROM pending_updates WHERE syncStatus = 'PENDING' ORDER BY occurredAt ASC")
    suspend fun getPendingOnce(): List<PendingUpdateEntity>

    @Query("SELECT COUNT(*) FROM pending_updates WHERE syncStatus = 'PENDING'")
    fun getPendingCount(): Flow<Int>

    @Query("UPDATE pending_updates SET syncStatus = 'SYNCED' WHERE id = :id")
    suspend fun markAsSynced(id: String)

    @Query("SELECT * FROM pending_updates WHERE targetId = :targetId ORDER BY occurredAt DESC LIMIT 1")
    suspend fun getLatestForTarget(targetId: String): PendingUpdateEntity?

    @Query("DELETE FROM pending_updates WHERE syncStatus = 'SYNCED'")
    suspend fun deleteSynced()

    @Query("DELETE FROM pending_updates WHERE targetId = :targetId")
    suspend fun deleteByTargetId(targetId: String)

    @Query("UPDATE pending_updates SET targetId = :remoteId WHERE targetId = :localId")
    suspend fun remapTargetId(localId: String, remoteId: String)
}