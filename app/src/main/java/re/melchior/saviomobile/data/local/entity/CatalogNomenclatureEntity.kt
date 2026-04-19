package re.melchior.saviomobile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "catalog_nomenclature")
data class CatalogNomenclatureEntity(
    @PrimaryKey val id: String,
    val domain: String,
    val code: String,
    val label: String,
    val isActive: Boolean,
    val updatedAt: String, // ISO 8601
)
