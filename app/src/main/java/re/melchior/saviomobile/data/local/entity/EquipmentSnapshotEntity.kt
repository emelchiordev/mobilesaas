package re.melchior.saviomobile.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(
    tableName = "equipment_snapshots",
    primaryKeys = ["equipmentId", "interventionId"],
)
data class EquipmentSnapshotEntity(
    val equipmentId: String,
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
    @ColumnInfo(name = "catalog_brand_id")
    val catalogBrandId: String? = null,
    @ColumnInfo(name = "parent_equipment_id")
    val parentEquipmentId: String? = null,
    @ColumnInfo(name = "order")
    val order: Int? = null,
    val unitId: String? = null,
    @ColumnInfo(name = "evacuation_mode")
    val evacuationMode: String? = null,
    @ColumnInfo(name = "hybride_pac_equipment_id")
    val hybridePacEquipmentId: String? = null,
    @ColumnInfo(name = "created_at")
    val createdAt: String = "",
)
