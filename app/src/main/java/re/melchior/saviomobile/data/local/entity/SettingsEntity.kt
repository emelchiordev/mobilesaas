package re.melchior.saviomobile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey
    val id: Int = 1, // Une seule ligne
    val allowCreateIntervention: Boolean = false,
    val allowProposal: Boolean = true,
    val lastPulledAt: String? = null,
    val technicianId: String? = null,
    val technicianFirstName: String? = null,
    val technicianLastName: String? = null,
    val updatesRequireValidation: Boolean = false
)