package re.savio.mobile.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import re.savio.mobile.data.local.entity.InvoiceEntity

@Dao
interface InvoiceDao {

    @Query("SELECT * FROM invoices WHERE interventionId = :interventionId LIMIT 1")
    suspend fun getByInterventionId(interventionId: String): InvoiceEntity?

    @Query("SELECT * FROM invoices WHERE interventionId = :interventionId LIMIT 1")
    fun observeByInterventionId(interventionId: String): Flow<InvoiceEntity?>

    @Query("SELECT * FROM invoices WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): InvoiceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(invoice: InvoiceEntity)

    @Update
    suspend fun update(invoice: InvoiceEntity)

    @Query("UPDATE invoices SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: String)

    @Query(
        """
        UPDATE invoices SET
            status = :status,
            acceptedAt = :acceptedAt,
            invoicedAt = :invoicedAt,
            paidAt = :paidAt,
            devisSignatureUrl = :devisSignatureUrl,
            hamonSignatureUrl = :hamonSignatureUrl,
            hamonRequested = :hamonRequested,
            documentType = :documentType,
            updatedAt = :updatedAt
        WHERE id = :id
        """,
    )
    suspend fun updateLifecycle(
        id: String,
        status: String,
        acceptedAt: String?,
        invoicedAt: String?,
        paidAt: String?,
        devisSignatureUrl: String?,
        hamonSignatureUrl: String?,
        hamonRequested: Boolean,
        documentType: String,
        updatedAt: String,
    )

    @Query("UPDATE invoices SET syncStatus = 'synced' WHERE id = :id")
    suspend fun markAsSynced(id: String)

    @Query("UPDATE invoices SET syncStatus = 'PENDING' WHERE id = :id")
    suspend fun markAsPending(id: String)

    @Query(
        """
        UPDATE invoices
        SET id = :serverId,
            number = :number,
            status = :status,
            acceptedAt = :acceptedAt,
            invoicedAt = :invoicedAt,
            paidAt = :paidAt,
            devisSignatureUrl = :devisSignatureUrl,
            hamonSignatureUrl = :hamonSignatureUrl,
            hamonRequested = :hamonRequested,
            syncStatus = 'synced'
        WHERE id = :localId
        """
    )
    suspend fun updateServerData(
        localId: String,
        serverId: String,
        number: String?,
        status: String,
        acceptedAt: String? = null,
        invoicedAt: String? = null,
        paidAt: String? = null,
        devisSignatureUrl: String? = null,
        hamonSignatureUrl: String? = null,
        hamonRequested: Boolean = false,
    )

    @Query("DELETE FROM invoices WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("UPDATE invoices SET acceptedLinesFingerprint = :fingerprint WHERE id = :id")
    suspend fun updateAcceptedLinesFingerprint(id: String, fingerprint: String?)
}
