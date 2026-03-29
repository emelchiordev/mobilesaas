package re.melchior.saviomobile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_updates")
data class PendingUpdateEntity(
    @PrimaryKey
    val id: String,
    val type: String,        // UPDATE_CUSTOMER | UPDATE_UNIT_ACCESS
    val targetId: String,    // customerId ou unitId
    val payload: String,     // JSON sérialisé
    val occurredAt: String,
    val syncStatus: String = "PENDING" // PENDING | SYNCED
)