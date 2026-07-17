package re.savio.mobile.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded

/**
 * Résultat de recherche catalogue avec libellés issus des jointures sur [CatalogNomenclatureEntity].
 */
data class CatalogEquipmentSearchRow(
    @Embedded
    val equipment: CatalogEquipmentEntity,
    @ColumnInfo(name = "brandLabel") val brandLabel: String?,
    @ColumnInfo(name = "brandCode") val brandCode: String?,
    @ColumnInfo(name = "typeLabel") val typeLabel: String?,
    @ColumnInfo(name = "typeCode") val typeCode: String?,
    @ColumnInfo(name = "energyLabel") val energyLabel: String?,
    @ColumnInfo(name = "energyCode") val energyCode: String?,
)
