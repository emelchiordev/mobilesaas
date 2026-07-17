package re.savio.mobile.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import re.savio.mobile.data.local.entity.CivilityOptionEntity
import re.savio.mobile.data.local.entity.EnergyTypeEntity
import re.savio.mobile.data.local.entity.EquipmentTypeEntity
import re.savio.mobile.data.local.entity.InterventionTypeEntity
import re.savio.mobile.data.local.entity.UnitTypeEntity

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

    @Query("SELECT * FROM unit_types ORDER BY category ASC, label ASC")
    fun getUnitTypes(): Flow<List<UnitTypeEntity>>

    @Query("SELECT * FROM unit_types ORDER BY category ASC, label ASC")
    suspend fun getUnitTypesOnce(): List<UnitTypeEntity>

    @Query("DELETE FROM unit_types")
    suspend fun deleteAllUnitTypes()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUnitTypes(types: List<UnitTypeEntity>)

    @Query("SELECT * FROM civility_options ORDER BY label ASC")
    fun getCivilityOptions(): Flow<List<CivilityOptionEntity>>

    @Query("SELECT * FROM civility_options ORDER BY label ASC")
    suspend fun getCivilityOptionsOnce(): List<CivilityOptionEntity>

    @Query("DELETE FROM civility_options")
    suspend fun deleteAllCivilityOptions()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCivilityOptions(options: List<CivilityOptionEntity>)
}