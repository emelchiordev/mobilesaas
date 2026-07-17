package re.savio.mobile.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import re.savio.mobile.data.local.entity.AnomalyDraftEntity

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

    @Query(
        """
        SELECT COUNT(*) FROM anomaly_draft
        WHERE interventionId = :interventionId
        AND anomalyTypeCode = :code
        """,
    )
    suspend fun countByInterventionAndCode(interventionId: String, code: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(draft: AnomalyDraftEntity)

    @Query("UPDATE anomaly_draft SET syncStatus = :status WHERE localId = :localId")
    suspend fun updateSyncStatus(localId: String, status: String)

    @Query("UPDATE anomaly_draft SET corrected = :corrected WHERE localId = :localId")
    suspend fun updateCorrected(localId: String, corrected: Boolean)

    @Query("DELETE FROM anomaly_draft WHERE localId = :localId")
    suspend fun deleteByLocalId(localId: String)

    @Query("DELETE FROM anomaly_draft WHERE interventionId = :interventionId")
    suspend fun deleteByInterventionId(interventionId: String)
}
