package re.melchior.saviomobile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "invoices")
data class InvoiceEntity(
    @PrimaryKey val id: String,
    val interventionId: String?,
    val status: String,
    val number: String?,
    val totalHt: Double,
    val totalVat: Double,
    val totalTtc: Double,
    val emittedAt: String?,
    val dueAt: String?,
    val customerEmail: String?,
    val notes: String?,
    val syncStatus: String = "synced",
    val createdAt: String,
    val updatedAt: String,
    val acceptedAt: String? = null,
    val invoicedAt: String? = null,
    val paidAt: String? = null,
    val devisSignatureUrl: String? = null,
    val hamonSignatureUrl: String? = null,
    val hamonRequested: Boolean = false,
)
