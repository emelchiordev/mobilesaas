package re.savio.mobile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "invoice_payments")
data class InvoicePaymentEntity(
    @PrimaryKey val id: String,
    val invoiceId: String,
    val amount: Double,
    val paymentMethodCode: String, // cash, card, check, transfer
    val paidAt: String,
)
