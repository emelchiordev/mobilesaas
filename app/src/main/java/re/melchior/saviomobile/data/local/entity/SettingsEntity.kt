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
    val requireInvoiceValidation: Boolean = false,
    val updatesRequireValidation: Boolean = false,
    val mobilePlanningPermission: String = "LIMITED_EDIT",
    val blockMobileFollowUpResolve: Boolean = false,
    val profile: String = "ARTISAN_SOLO",
    val policyOverridesJson: String = "{}",
    val resolvedInvoices: String = "AUTO_ISSUE",
    val resolvedPlanning: String = "FREE",
    val resolvedPlanningEdit: String = "FULL_EDIT",
    val resolvedFieldModifications: String = "AUTO_APPLY",
    val resolvedClosing: String = "TECH_CAN_CLOSE",
)