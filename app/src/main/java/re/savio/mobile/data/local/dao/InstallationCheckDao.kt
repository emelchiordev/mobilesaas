package re.savio.mobile.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import re.savio.mobile.data.local.entity.InstallationCheckEntity

@Dao
interface InstallationCheckDao {

    @Query("SELECT * FROM installation_check WHERE interventionId = :interventionId LIMIT 1")
    fun observeByInterventionId(interventionId: String): Flow<InstallationCheckEntity?>

    @Query("SELECT * FROM installation_check WHERE interventionId = :interventionId LIMIT 1")
    suspend fun getByInterventionId(interventionId: String): InstallationCheckEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: InstallationCheckEntity)

    @Query("SELECT * FROM installation_check WHERE isDirty = 1")
    suspend fun getDirty(): List<InstallationCheckEntity>

    @Query(
        "UPDATE installation_check SET isDirty = 0 WHERE interventionId = :interventionId",
    )
    suspend fun markClean(interventionId: String)

    @Query("DELETE FROM installation_check WHERE interventionId = :interventionId")
    suspend fun deleteByInterventionId(interventionId: String)
}
