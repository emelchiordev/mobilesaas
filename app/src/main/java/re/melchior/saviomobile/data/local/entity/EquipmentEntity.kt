package re.melchior.saviomobile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "equipments")
data class EquipmentEntity(
    @PrimaryKey
    val id: String,
    val interventionId: String,
    val brand: String?,
    val model: String?,
    val typeCode: String?,
    val energyCode: String?,
    val serialNumber: String?,
    val installDate: String?,
    val isPrimary: Boolean
)