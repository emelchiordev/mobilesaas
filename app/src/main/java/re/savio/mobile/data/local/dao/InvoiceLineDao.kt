package re.savio.mobile.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import re.savio.mobile.data.local.entity.InvoiceLineEntity

@Dao
interface InvoiceLineDao {

    @Query("SELECT * FROM invoice_lines WHERE invoiceId = :invoiceId ORDER BY `order` ASC")
    fun getByInvoiceId(invoiceId: String): Flow<List<InvoiceLineEntity>>

    @Query("SELECT * FROM invoice_lines WHERE invoiceId = :invoiceId ORDER BY `order` ASC")
    suspend fun getByInvoiceIdOnce(invoiceId: String): List<InvoiceLineEntity>

    @Query("SELECT * FROM invoice_lines WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): InvoiceLineEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(line: InvoiceLineEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(lines: List<InvoiceLineEntity>)

    @Update
    suspend fun update(line: InvoiceLineEntity)

    @Query("DELETE FROM invoice_lines WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM invoice_lines WHERE invoiceId = :invoiceId")
    suspend fun deleteByInvoiceId(invoiceId: String)

    @Query("UPDATE invoice_lines SET invoiceId = :newInvoiceId WHERE invoiceId = :oldInvoiceId")
    suspend fun reassignToInvoice(oldInvoiceId: String, newInvoiceId: String)
}
