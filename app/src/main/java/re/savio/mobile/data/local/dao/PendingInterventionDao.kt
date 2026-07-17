package re.savio.mobile.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import re.savio.mobile.data.local.entity.PendingInterventionEntity

@Dao
interface PendingInterventionDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(intervention: PendingInterventionEntity)

    @Query("SELECT * FROM pending_interventions ORDER BY createdAt DESC")
    fun getPending(): Flow<List<PendingInterventionEntity>>

    @Query(
        "UPDATE pending_interventions SET syncStatus = :status, remoteId = COALESCE(:remoteId, remoteId) WHERE localId = :localId",
    )
    suspend fun updateSyncStatus(localId: String, status: String, remoteId: String?)

    @Query("SELECT COUNT(*) FROM pending_interventions WHERE syncStatus = 'PENDING'")
    fun countPending(): Flow<Int>

    @Query("SELECT * FROM pending_interventions WHERE syncStatus = 'PENDING' ORDER BY createdAt ASC")
    suspend fun listPendingToSync(): List<PendingInterventionEntity>

    @Query(
        """
        SELECT * FROM pending_interventions
        WHERE syncStatus != 'SYNCED'
        AND scheduledAt >= :dayStartMillis AND scheduledAt < :dayEndMillis
        ORDER BY scheduledAt ASC
        """,
    )
    fun getPendingForDate(dayStartMillis: Long, dayEndMillis: Long): Flow<List<PendingInterventionEntity>>

    @Query(
        """
        SELECT COUNT(*) FROM pending_interventions
        WHERE syncStatus != 'SYNCED'
        AND scheduledAt >= :dayStartMillis AND scheduledAt < :dayEndMillis
        """,
    )
    suspend fun countPendingForDate(dayStartMillis: Long, dayEndMillis: Long): Int
}
