package re.melchior.saviomobile.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import re.melchior.saviomobile.data.local.entity.AnomalyDraftEntity

@Dao
interface AnomalyDraftDao {

    @Query(
        """
        SELECT * FROM anomaly_draft
        WHERE interventionId = :interventionId
        ORDER BY reportedAt ASC, localId ASC
        """,
    )
    fun observeByIntervention(interventionId: String): Flow<List<AnomalyDraftEntity>>

    @Query(
        """
        SELECT * FROM anomaly_draft
        WHERE interventionId = :interventionId
        AND syncStatus = 'pending'
        ORDER BY reportedAt ASC, localId ASC
        """,
    )
    suspend fun getPendingByInterventionOnce(interventionId: String): List<AnomalyDraftEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(draft: AnomalyDraftEntity)

    @Query("UPDATE anomaly_draft SET syncStatus = :status WHERE localId = :localId")
    suspend fun updateSyncStatus(localId: String, status: String)
}
