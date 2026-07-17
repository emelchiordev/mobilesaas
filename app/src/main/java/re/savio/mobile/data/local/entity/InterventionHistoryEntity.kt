package re.savio.mobile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "intervention_history")
data class InterventionHistoryEntity(
    @PrimaryKey
    val id: String,
    val unitId: String,
    val number: String? = null,
    val scheduledAt: String,
    val completedAt: String? = null,
    val report: String? = null,
    val notes: String? = null,
    val typeCode: String,
    val typeLabel: String,
    val typeColor: String? = null,
    val technicianFirstName: String? = null,
    val technicianLastName: String? = null,
    val completedAsVe: Boolean = false,
    val photoKeys: String? = null // JSON array stocké comme String
)