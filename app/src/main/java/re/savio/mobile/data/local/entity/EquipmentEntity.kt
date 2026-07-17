package re.savio.mobile.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(
    tableName = "equipments",
    primaryKeys = ["interventionId", "order"],
)
data class EquipmentEntity(
    val interventionId: String,
    @ColumnInfo(name = "order")
    val order: Int,
    /** Identifiant serveur (non unique global). */
    val id: String,
    val unitId: String,
    val brand: String? = null,
    val model: String? = null,
    val typeCode: String? = null,
    val energyCode: String? = null,
    val serialNumber: String? = null,
    val installDate: String? = null,
    val isPrimary: Boolean = false,
    @ColumnInfo(name = "equipment_catalog_id")
    val equipmentCatalogId: String? = null,
    @ColumnInfo(name = "catalog_brand_id")
    val catalogBrandId: String? = null,
    @ColumnInfo(name = "parent_equipment_id")
    val parentEquipmentId: String? = null,
    @ColumnInfo(name = "power_kw")
    val powerKw: String? = null,
    val evacuationMode: String? = null,
    @ColumnInfo(name = "hybride_pac_equipment_id")
    val hybridePacEquipmentId: String? = null,
    /** JSON `equipment.attrs` (pompe, gicleur, …). */
    @ColumnInfo(name = "attrsJson")
    val attrsJson: String? = null,
    val syncStatus: String = "SYNCED",
    val isChantier: Int = 0,
)
