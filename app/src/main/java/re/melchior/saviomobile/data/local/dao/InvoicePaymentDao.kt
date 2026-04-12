package re.melchior.saviomobile.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import re.melchior.saviomobile.data.local.entity.InvoicePaymentEntity

@Dao
interface InvoicePaymentDao {

    @Query("SELECT * FROM invoice_payments WHERE invoiceId = :invoiceId ORDER BY paidAt ASC")
    fun getByInvoiceId(invoiceId: String): Flow<List<InvoicePaymentEntity>>

    @Query("SELECT * FROM invoice_payments WHERE invoiceId = :invoiceId ORDER BY paidAt ASC")
    suspend fun getByInvoiceIdOnce(invoiceId: String): List<InvoicePaymentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(payment: InvoicePaymentEntity)

    @Query("DELETE FROM invoice_payments WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM invoice_payments WHERE invoiceId = :invoiceId")
    suspend fun deleteByInvoiceId(invoiceId: String)
}
