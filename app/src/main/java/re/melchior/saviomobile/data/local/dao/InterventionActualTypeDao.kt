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
}
