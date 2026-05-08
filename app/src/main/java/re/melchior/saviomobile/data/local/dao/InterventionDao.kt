package re.melchior.saviomobile.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import re.melchior.saviomobile.data.local.entity.InterventionEntity

@Dao
abstract class InterventionDao {

//    @Transaction
//    suspend fun insertAllSafe(interventions: List<InterventionEntity>) {
//        interventions.forEach { entity ->
//            val existing = getInterventionByIdOnce(entity.id)
//            if (existing == null || existing.syncStatus == "SYNCED") {
//                insertOrReplace(entity)
//            } else if (existing.syncStatus == "COMPLETED" &&
//                entity.status == "completed") {
//                // Le serveur confirme la clôture → passer en SYNCED
//                markAsSynced(entity.id)
//            }
//        }
//    }

    @Query("""
    SELECT * FROM interventions 
    WHERE status = 'completed'
    AND (
        signaturePath LIKE '/data%' 
        OR techSignaturePath LIKE '/data%'
    )
""")
    abstract suspend fun getInterventionsWithLocalSignatures(): List<InterventionEntity>

    @Query("UPDATE interventions SET signaturePath = :signaturePath WHERE id = :id")
    abstract suspend fun updateSignaturePath(id: String, signaturePath: String)

    @Query("UPDATE interventions SET techSignaturePath = :techSignaturePath WHERE id = :id")
    abstract suspend fun updateTechSignaturePath(id: String, techSignaturePath: String)

    @Query("SELECT * FROM interventions WHERE customerId = :customerId LIMIT 1")
    abstract fun getInterventionByCustomerId(customerId: String): Flow<InterventionEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertOrReplace(intervention: InterventionEntity)

    @Query("SELECT * FROM interventions WHERE id = :id LIMIT 1")
    abstract suspend fun getInterventionByIdOnce(id: String): InterventionEntity?

    @Query("SELECT * FROM interventions WHERE scheduledAt LIKE :date || '%' ORDER BY scheduledAt ASC")
    abstract fun getInterventionsByDate(date: String): Flow<List<InterventionEntity>>

    @Query("SELECT * FROM interventions WHERE id = :id")
    abstract fun getInterventionById(id: String): Flow<InterventionEntity?>

    @Query("SELECT * FROM interventions WHERE syncStatus IN ('PENDING', 'COMPLETED') ORDER BY scheduledAt ASC")
    abstract fun getPendingSync(): Flow<List<InterventionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertAll(interventions: List<InterventionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(intervention: InterventionEntity)

    @Update
    abstract suspend fun update(intervention: InterventionEntity)

    @Query("UPDATE interventions SET status = :status, syncStatus = :syncStatus WHERE id = :id")
    abstract suspend fun updateStatus(id: String, status: String, syncStatus: String)

    @Query("UPDATE interventions SET status = 'in_progress', syncStatus = 'IN_PROGRESS', startedAt = :startedAt WHERE id = :id")
    abstract suspend fun markAsInProgress(id: String, startedAt: String)

    @Query(
        """
        UPDATE interventions
        SET status = 'scheduled', syncStatus = 'SYNCED'
        WHERE id != :exceptInterventionId
        AND status = 'in_progress'
        AND isChantier = 0
        """,
    )
    abstract suspend fun resetOtherInProgressToScheduled(exceptInterventionId: String)

    @Query(
        """
        UPDATE interventions
        SET status = 'scheduled', syncStatus = 'SYNCED'
        WHERE status = 'in_progress'
        AND isChantier = 0
        AND startedAt IS NOT NULL
        AND startedAt < :cutoffTime
        """,
    )
    abstract suspend fun resetStaleInProgressToScheduled(cutoffTime: String)

    @Query("""
        UPDATE interventions 
        SET status = 'completed', 
            syncStatus = 'COMPLETED',
            completedAt = :completedAt,
            signaturePath = :signaturePath,
            techSignaturePath = :techSignaturePath,
            actualTypeId = :actualTypeId,
            actualTypeCode = :actualTypeCode,
            actualTypeLabel = :actualTypeLabel
        WHERE id = :id
    """)
    abstract suspend fun completeIntervention(
        id: String,
        completedAt: String,
        signaturePath: String?,
        techSignaturePath: String,
        actualTypeId: String,
        actualTypeCode: String?,
        actualTypeLabel: String?
    )

    @Query("UPDATE interventions SET report = :report WHERE id = :id")
    abstract suspend fun saveReport(id: String, report: String)

    @Query("UPDATE interventions SET syncStatus = 'SYNCED' WHERE id = :id")
    abstract suspend fun markAsSynced(id: String)

    @Query("UPDATE interventions SET syncStatus = 'CONFLICT' WHERE id = :id")
    abstract suspend fun markAsConflict(id: String)

    @Query(
        """
        DELETE FROM interventions
        WHERE scheduledAt < :date
        AND status NOT IN ('completed', 'pending_validation')
        """,
    )
    abstract suspend fun deleteOlderThan(date: String)

    @Query("SELECT * FROM interventions WHERE syncStatus IN ('PENDING', 'COMPLETED') ORDER BY scheduledAt ASC")
    abstract suspend fun getPendingSyncOnce(): List<InterventionEntity>
    /**
     * Supprime les interventions SYNCED du jour encore « ouvertes » côté sync,
     * absentes du pull. Ne touche jamais [completed] ni [pending_validation].
     */
    @Query(
        """
        DELETE FROM interventions 
        WHERE scheduledAt LIKE :date || '%'
        AND syncStatus = 'SYNCED'
        AND status NOT IN ('completed', 'pending_validation')
        AND id NOT IN (:keepIds)
        """,
    )
    abstract suspend fun deleteSyncedOpenForDateNotInKeepList(date: String, keepIds: List<String>)

    /**
     * Cas pull vide : retire du jour les interventions SYNCED ouvertes uniquement
     * (les clôturées locales restent).
     */
    @Query(
        """
        DELETE FROM interventions 
        WHERE scheduledAt LIKE :date || '%'
        AND syncStatus = 'SYNCED'
        AND status NOT IN ('completed', 'pending_validation')
        """,
    )
    abstract suspend fun deleteSyncedOpenInterventionsForDateWhenPullEmpty(date: String)

    @Query("SELECT COUNT(*) FROM interventions WHERE syncStatus IN ('PENDING', 'COMPLETED')")
    abstract fun getPendingSyncCount(): Flow<Int>

    @Query("SELECT * FROM interventions WHERE status = 'in_progress' LIMIT 1")
    abstract suspend fun findFirstInProgress(): InterventionEntity?

    @Query(
        """
        UPDATE interventions SET
            status = 'scheduled',
            syncStatus = 'SYNCED',
            startedAt = NULL,
            report = NULL
        WHERE id = :id
        """
    )
    abstract suspend fun resetToScheduledAfterAbandon(id: String)
}