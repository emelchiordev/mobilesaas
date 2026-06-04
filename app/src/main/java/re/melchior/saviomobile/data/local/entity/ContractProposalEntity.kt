package re.melchior.saviomobile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contract_proposals")
data class ContractProposalEntity(
    @PrimaryKey val id: String,
    val customerId: String,
    val unitId: String,
    val status: String,
    val payloadJson: String,
    val signatureKey: String? = null,
    val hamonWaivedExecution: Boolean = false,
    val synced: Boolean = false,
    val createdAt: String,
)
