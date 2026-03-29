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

    @Query("SELECT * FROM interventions WHERE customerId = :customerId LIMIT 1")
    abstract fun getInterventionByCustomerId(customerId: String): Flow<InterventionEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertOrReplace(intervention: InterventionEntity)

    @Query("SELECT * FROM interventions WHERE id = :id")
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

    @Query("""
        UPDATE interventions 
        SET status = 'completed', 
            syncStatus = 'COMPLETED',
            completedAt = :completedAt,
            signaturePath = :signaturePath,
            techSignaturePath = :techSignaturePath
        WHERE id = :id
    """)
    abstract suspend fun completeIntervention(
        id: String,
        completedAt: String,
        signaturePath: String,
        techSignaturePath: String
    )

    @Query("UPDATE interventions SET report = :report WHERE id = :id")
    abstract suspend fun saveReport(id: String, report: String)

    @Query("UPDATE interventions SET syncStatus = 'SYNCED' WHERE id = :id")
    abstract suspend fun markAsSynced(id: String)

    @Query("UPDATE interventions SET syncStatus = 'CONFLICT' WHERE id = :id")
    abstract suspend fun markAsConflict(id: String)

    @Query("DELETE FROM interventions WHERE scheduledAt < :date")
    abstract suspend fun deleteOlderThan(date: String)

    @Query("SELECT * FROM interventions WHERE syncStatus IN ('PENDING', 'COMPLETED') ORDER BY scheduledAt ASC")
    abstract suspend fun getPendingSyncOnce(): List<InterventionEntity>
    @Query("""
        DELETE FROM interventions 
        WHERE scheduledAt LIKE :date || '%'
        AND syncStatus = 'SYNCED'
        AND id NOT IN (:keepIds)
    """)
    abstract suspend fun deleteSyncedForDate(date: String, keepIds: List<String>)

    @Query("SELECT COUNT(*) FROM interventions WHERE syncStatus IN ('PENDING', 'COMPLETED')")
    abstract fun getPendingSyncCount(): Flow<Int>
}