package re.melchior.saviomobile.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import re.melchior.saviomobile.data.local.entity.InterventionHistoryEntity

@Dao
interface InterventionHistoryDao {

    @Query("SELECT * FROM intervention_history WHERE unitId = :unitId ORDER BY completedAt DESC LIMIT 10")
    suspend fun getHistoryForUnit(unitId: String): List<InterventionHistoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<InterventionHistoryEntity>)

    @Query("DELETE FROM intervention_history WHERE unitId = :unitId")
    suspend fun deleteForUnit(unitId: String)

    @Query("DELETE FROM intervention_history")
    suspend fun deleteAll()
}