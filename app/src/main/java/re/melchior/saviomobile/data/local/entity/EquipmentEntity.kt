package re.melchior.saviomobile.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(
    tableName = "equipments",
    primaryKeys = ["id", "interventionId"],
)
data class EquipmentEntity(
    val id: String,
    val interventionId: String,
    val brand: String?,
    val model: String?,
    val typeCode: String?,
    val energyCode: String?,
    val serialNumber: String?,
    val installDate: String?,
    val isPrimary: Boolean,
    @ColumnInfo(name = "equipment_catalog_id")
    val equipmentCatalogId: String? = null,
    @ColumnInfo(name = "parent_equipment_id")
    val parentEquipmentId: String? = null,
)
