package re.melchior.saviomobile.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import re.melchior.saviomobile.data.local.entity.ContractTariffEntity

@Dao
interface ContractTariffDao {
    @Query("SELECT * FROM contract_tariffs WHERE isActive = 1")
    suspend fun getAllActive(): List<ContractTariffEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(rows: List<ContractTariffEntity>)

    @Query("DELETE FROM contract_tariffs")
    suspend fun clearAll()
}
