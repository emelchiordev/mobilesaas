package re.savio.mobile.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "client_financial_summary",
    indices = [Index("clientId")],
)
data class ClientFinancialSummaryEntity(
    @PrimaryKey val documentId: String,
    val clientId: String,
    val documentType: String,
    val number: String,
    val title: String?,
    val emittedAt: String,
    val statusCode: String,
    val statusLabel: String,
    val statusTone: String,
    val totalTtc: Double,
    val syncedAt: Long,
)
