package re.savio.mobile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_interventions")
data class PendingInterventionEntity(
    @PrimaryKey val localId: String,
    val clientNameFree: String,
    val addressFree: String,
    val city: String?,
    val zipCode: String?,
    val phone: String?,
    val interventionType: String,
    val scheduledAt: Long,
    val notes: String?,
    val syncStatus: String = "PENDING",
    val remoteId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
)
