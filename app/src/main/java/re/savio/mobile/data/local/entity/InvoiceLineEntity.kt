package re.savio.mobile.data.local.entity

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
    val order: Int,
    val natureLigne: String? = null,
    val tenantArticleId: String? = null,
    val photoIncluded: Boolean = false,
    val hasLinePhoto: Boolean = false,
    val hasArticlePhoto: Boolean = false,
    val hasEffectivePhoto: Boolean = false,
    val photoSource: String? = null,
    val photoThumbnailUrl: String? = null,
)
