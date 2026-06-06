package re.melchior.saviomobile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "installation_check")
data class InstallationCheckEntity(
    @PrimaryKey val interventionId: String,
    val turbidityTested: Boolean = false,
    val turbidityNtu: String = "",
    val turbidityState: String? = null,
    val gasPipeType: String? = null,
    val gasPipeValidityDate: String = "",
    val gasPipeReplaced: Boolean = false,
    val gasPipeAnomalyDeclinedDate: String = "",
    val gasTapCompliant: String? = null,
    val notes: String = "",
    val updatedAt: String = "",
    val isDirty: Boolean = false,
)
