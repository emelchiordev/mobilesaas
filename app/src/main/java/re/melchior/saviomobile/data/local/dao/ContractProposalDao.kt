package re.melchior.saviomobile.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import re.melchior.saviomobile.data.local.entity.ContractProposalEntity

@Dao
interface ContractProposalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(row: ContractProposalEntity)

    @Query("SELECT * FROM contract_proposals WHERE synced = 0")
    suspend fun listPendingSync(): List<ContractProposalEntity>

    @Query("UPDATE contract_proposals SET synced = 1 WHERE id = :id")
    suspend fun markSynced(id: String)
}
