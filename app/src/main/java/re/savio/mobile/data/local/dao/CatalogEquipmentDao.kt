package re.savio.mobile.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import re.savio.mobile.data.local.entity.CatalogEquipmentEntity
import re.savio.mobile.data.local.entity.CatalogEquipmentSearchRow

@Dao
interface CatalogEquipmentDao {

    @Query(
        """
        SELECT e.*, 
               b.label as brandLabel, b.code as brandCode,
               t.label as typeLabel, t.code as typeCode,
               en.label as energyLabel, en.code as energyCode
        FROM catalog_equipment e
        LEFT JOIN catalog_nomenclature b ON b.id = e.brandId
        LEFT JOIN catalog_nomenclature t ON t.id = e.equipmentTypeId
        LEFT JOIN catalog_nomenclature en ON en.id = e.energyId
        WHERE e.isActive = 1
        AND (:brandId IS NULL OR e.brandId = :brandId)
        AND (:typeId IS NULL OR e.equipmentTypeId = :typeId)
        AND (:energyId IS NULL OR e.energyId = :energyId)
        AND (e.model LIKE '%' || :q || '%' OR :q = '')
        ORDER BY e.model ASC
        LIMIT 50
        """
    )
    fun search(
        q: String,
        brandId: String?,
        typeId: String?,
        energyId: String?,
    ): Flow<List<CatalogEquipmentSearchRow>>

    @Query("SELECT * FROM catalog_equipment WHERE id = :id")
    suspend fun getById(id: String): CatalogEquipmentEntity?

    @Query("SELECT brandId FROM catalog_equipment WHERE id = :catalogEquipmentId LIMIT 1")
    suspend fun getBrandIdForCatalogEquipment(catalogEquipmentId: String): String?

    @Upsert
    suspend fun upsertAll(items: List<CatalogEquipmentEntity>)

    @Query("SELECT MAX(updatedAt) FROM catalog_equipment")
    suspend fun getLastUpdatedAt(): String?
}
