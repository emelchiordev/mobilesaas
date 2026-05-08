package re.melchior.saviomobile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "attestation_ve_point_controle")
data class AttestationVePointControleEntity(
    @PrimaryKey
    val id: String,
    val attestationId: String,
    val interventionId: String,
    val equipmentOrder: Int,
    val type: String,
    val cle: String,
    val resultat: String = "",
)
