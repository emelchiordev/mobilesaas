package re.savio.mobile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "catalog_equipment")
data class CatalogEquipmentEntity(
    @PrimaryKey val id: String,
    val model: String,
    val brandId: String,
    val energyId: String,
    val equipmentTypeId: String,
    val powerKw: Double?,
    val maintenanceDurationHours: Double?,
    val referenceConstructeur: String?,
    val noticeUrl: String?,
    val isActive: Boolean,
    val updatedAt: String, // ISO 8601
)
