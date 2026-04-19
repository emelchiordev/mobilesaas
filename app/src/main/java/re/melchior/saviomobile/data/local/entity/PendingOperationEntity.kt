package re.melchior.saviomobile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_operations")
data class PendingOperationEntity(
    @PrimaryKey val id: String, // UUID local
    val type: String, // CREATE_EQUIPMENT, REPLACE_EQUIPMENT
    val payload: String, // JSON sérialisé
    val occurredAt: String, // ISO 8601
    val interventionId: String, // pour retrouver le contexte
    val status: String = "pending", // pending, sent, failed
    val createdAt: String,
)
