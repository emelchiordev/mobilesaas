package re.savio.mobile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contract_tariffs")
data class ContractTariffEntity(
    @PrimaryKey val id: String,
    val contractTypeId: String,
    val appliesToType: String,
    val priceHt: Double,
    val vatCategory: String,
    val isActive: Boolean = true,
)
