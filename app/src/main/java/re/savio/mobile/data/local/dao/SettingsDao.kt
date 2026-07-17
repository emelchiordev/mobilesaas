package re.savio.mobile.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import re.savio.mobile.data.local.entity.SettingsEntity

@Dao
interface SettingsDao {

    @Query("SELECT * FROM settings WHERE id = 1")
    fun getSettings(): Flow<SettingsEntity?>

    @Query("SELECT * FROM settings WHERE id = 1")
    suspend fun getSettingsOnce(): SettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(settings: SettingsEntity)

    @Query("UPDATE settings SET lastPulledAt = :pulledAt WHERE id = 1")
    suspend fun updateLastPulledAt(pulledAt: String)

    @Query("UPDATE settings SET demoOnboardingState = :state WHERE id = 1")
    suspend fun updateDemoOnboardingState(state: String)
}