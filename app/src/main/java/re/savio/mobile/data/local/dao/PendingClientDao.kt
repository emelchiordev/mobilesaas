package re.savio.mobile.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import re.savio.mobile.data.local.entity.PendingClientEntity

@Dao
interface PendingClientDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(client: PendingClientEntity)

    @Query("SELECT * FROM pending_clients WHERE syncStatus = 'PENDING' ORDER BY createdAt ASC")
    suspend fun getPending(): List<PendingClientEntity>

    @Query(
        """
        UPDATE pending_clients
        SET syncStatus = :status,
            remoteId = :remoteId,
            remoteUnitId = :remoteUnitId
        WHERE localId = :localId
        """,
    )
    suspend fun updateSyncStatus(
        localId: String,
        status: String,
        remoteId: String?,
        remoteUnitId: String?,
    )

    @Query("SELECT * FROM pending_clients WHERE localId = :localId LIMIT 1")
    suspend fun getByLocalId(localId: String): PendingClientEntity?
}
