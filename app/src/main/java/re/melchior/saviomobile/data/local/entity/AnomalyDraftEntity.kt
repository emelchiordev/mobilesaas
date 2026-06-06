package re.melchior.saviomobile.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "anomaly_draft",
    indices = [Index(value = ["interventionId"])],
)
data class AnomalyDraftEntity(
    @PrimaryKey val localId: String,
    val interventionId: String,
    val unitId: String,
    val equipmentId: String?,
    val scope: String,
    val anomalyTypeCode: String?,
    val customDescription: String?,
    val reportedAt: String,
    val action: String?,
    @ColumnInfo(defaultValue = "0")
    val corrected: Boolean = false,
    @ColumnInfo(defaultValue = "pending")
    val syncStatus: String = "pending",
)
