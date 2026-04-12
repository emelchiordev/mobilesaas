package re.melchior.saviomobile.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import re.melchior.saviomobile.data.local.entity.InvoiceEntity

@Dao
interface InvoiceDao {

    @Query("SELECT * FROM invoices WHERE interventionId = :interventionId LIMIT 1")
    suspend fun getByInterventionId(interventionId: String): InvoiceEntity?

    @Query("SELECT * FROM invoices WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): InvoiceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(invoice: InvoiceEntity)

    @Update
    suspend fun update(invoice: InvoiceEntity)

    @Query("UPDATE invoices SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: String)

    @Query("UPDATE invoices SET syncStatus = 'synced' WHERE id = :id")
    suspend fun markAsSynced(id: String)

    @Query(
        """
        UPDATE invoices
        SET id = :serverId,
            number = :number,
            status = :status,
            syncStatus = 'synced'
        WHERE id = :localId
        """
    )
    suspend fun updateServerData(
        localId: String,
        serverId: String,
        number: String?,
        status: String
    )
}
