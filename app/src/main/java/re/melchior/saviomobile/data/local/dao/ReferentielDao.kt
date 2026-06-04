package re.melchior.saviomobile.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import re.melchior.saviomobile.data.local.entity.EnergyTypeEntity
import re.melchior.saviomobile.data.local.entity.EquipmentTypeEntity
import re.melchior.saviomobile.data.local.entity.InterventionTypeEntity

@Dao
interface ReferentielDao {

    // Intervention types
    @Query("SELECT * FROM intervention_types ORDER BY code ASC")
    fun getAllInterventionTypes(): Flow<List<InterventionTypeEntity>>

    @Query("SELECT * FROM intervention_types WHERE showOnCreate = 1 ORDER BY code ASC")
    fun getCreateInterventionTypes(): Flow<List<InterventionTypeEntity>>

    @Query("SELECT * FROM intervention_types WHERE showOnClose = 1 ORDER BY code ASC")
    suspend fun getCloseTypesOnce(): List<InterventionTypeEntity>

    @Query("DELETE FROM intervention_types")
    suspend fun deleteAllInterventionTypes()

    @Upsert
    suspend fun upsertInterventionTypes(types: List<InterventionTypeEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInterventionTypes(types: List<InterventionTypeEntity>)

    // Equipment types
    @Query("SELECT * FROM equipment_types")
    fun getAllEquipmentTypes(): Flow<List<EquipmentTypeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEquipmentTypes(types: List<EquipmentTypeEntity>)

    // Energy types
    @Query("SELECT * FROM energy_types")
    fun getAllEnergyTypes(): Flow<List<EnergyTypeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEnergyTypes(types: List<EnergyTypeEntity>)
}