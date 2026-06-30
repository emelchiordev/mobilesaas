package re.melchior.saviomobile.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import re.melchior.saviomobile.data.local.entity.InterventionActualTypeEntity

@Dao
interface InterventionActualTypeDao {

    @Query("SELECT * FROM intervention_actual_types WHERE interventionId = :interventionId ORDER BY `order`")
    fun getActualTypesForIntervention(interventionId: String): Flow<List<InterventionActualTypeEntity>>

    @Query("SELECT * FROM intervention_actual_types WHERE interventionId = :interventionId ORDER BY `order`")
    suspend fun getActualTypesForInterventionOnce(interventionId: String): List<InterventionActualTypeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(types: List<InterventionActualTypeEntity>)

    @Query("DELETE FROM intervention_actual_types WHERE interventionId = :interventionId")
    suspend fun deleteForIntervention(interventionId: String)

    /** Avant suppression bulk des interventions SYNCED ouvertes du jour (pull vide). */
    @Query(
        """
        DELETE FROM intervention_actual_types
        WHERE interventionId IN (
            SELECT id FROM interventions
            WHERE scheduledAt >= :startIso AND scheduledAt < :endIso
            AND syncStatus = 'SYNCED'
            AND status NOT IN ('completed', 'pending_validation')
        )
        """,
    )
    suspend fun deleteForSyncedOpenInterventionsOnDate(startIso: String, endIso: String)

    /** Avant suppression bulk des interventions SYNCED absentes du pull. */
    @Query(
        """
        DELETE FROM intervention_actual_types
        WHERE interventionId IN (
            SELECT id FROM interventions
            WHERE scheduledAt >= :startIso AND scheduledAt < :endIso
            AND syncStatus = 'SYNCED'
            AND status NOT IN ('completed', 'pending_validation')
            AND id NOT IN (:keepIds)
        )
        """,
    )
    suspend fun deleteForSyncedOpenOnDateNotInKeepList(
        startIso: String,
        endIso: String,
        keepIds: List<String>,
    )

    /** Avant [InterventionDao.deleteOlderThan]. */
    @Query(
        """
        DELETE FROM intervention_actual_types
        WHERE interventionId IN (
            SELECT id FROM interventions
            WHERE scheduledAt < :beforeIso
            AND status NOT IN ('completed', 'pending_validation')
        )
        """,
    )
    suspend fun deleteOlderThanForOpenInterventions(beforeIso: String)
}
