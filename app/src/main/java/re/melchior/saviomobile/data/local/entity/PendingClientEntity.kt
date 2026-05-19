package re.melchior.saviomobile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "pending_clients")
data class PendingClientEntity(
    @PrimaryKey val localId: String = UUID.randomUUID().toString(),
    val firstName: String,
    val lastName: String,
    val civility: String?,
    val phone: String?,
    val email: String?,
    val address: String,
    val addressComplement: String?,
    val city: String,
    val zipCode: String,
    val lat: Double?,
    val lng: Double?,
    val unitType: String,
    val unitCategory: String?,
    val floor: String?,
    val syncStatus: String = "PENDING",
    val remoteId: String? = null,
    val remoteUnitId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
)
