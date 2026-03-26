package re.melchior.saviomobile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "interventions")
data class InterventionEntity(
    @PrimaryKey
    val id: String,
    val scheduledAt: String,
    val status: String,
    val syncStatus: String = "SYNCED", // SYNCED, PENDING, IN_PROGRESS, COMPLETED, CONFLICT
    val typeCode: String,
    val typeLabel: String,
    val typeColor: String?,
    val signaturePath: String? = null,
    val techSignaturePath: String? = null,

    // Unit
    val unitId: String,
    val unitStreet: String,
    val unitAddressLine2: String?,
    val unitPostalCode: String,
    val unitCity: String,
    val unitFloor: String?,
    val unitDoorCode: String?,
    val unitLatitude: Double?,
    val unitLongitude: Double?,
    // Customer
    val customerId: String?,
    val customerFirstName: String?,
    val customerLastName: String?,
    val customerPhone: String?,
    val customerEmail: String?,
    // Contract
    val contractType: String?,
    val contractRenewalDate: String?,
    val contractTariff: Double?,
    val contractVatRate: Double?,
    // Compte-rendu
    val report: String? = null,
    val completedAt: String? = null,
    val startedAt: String? = null,
    // Meta
    val pulledAt: String,
    val updatedAt: String? = null
)