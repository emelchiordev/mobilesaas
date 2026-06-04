package re.melchior.saviomobile.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import re.melchior.saviomobile.data.local.entity.AnomalyTypeEntity

@Dao
interface AnomalyTypeDao {

    @Query("SELECT * FROM anomaly_types WHERE isActive = 1 ORDER BY code ASC")
    suspend fun getAllActiveOnce(): List<AnomalyTypeEntity>

    @Query("DELETE FROM anomaly_types")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(types: List<AnomalyTypeEntity>)
}
