package re.melchior.saviomobile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "invoice_lines")
data class InvoiceLineEntity(
    @PrimaryKey val id: String,
    val invoiceId: String,
    val type: String,
    val label: String,
    val reference: String?,
    val quantity: Double,
    val unitPriceHt: Double,
    val vatRate: Double,
    val discountPercent: Double,
    val totalHt: Double,
    val billingType: String,
    val isTextBlock: Boolean,
    val order: Int
)
