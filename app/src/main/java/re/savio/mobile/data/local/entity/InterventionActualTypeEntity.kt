package re.savio.mobile.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "intervention_actual_types",
    foreignKeys = [
        ForeignKey(
            entity = InterventionEntity::class,
            parentColumns = ["id"],
            childColumns = ["interventionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("interventionId")]
)
data class InterventionActualTypeEntity(
    @PrimaryKey val id: String,
    val interventionId: String,
    val interventionTypeId: String,
    val code: String,
    val label: String,
    val color: String? = null,
    val isVeType: Boolean = false,
    val order: Int = 1
)
