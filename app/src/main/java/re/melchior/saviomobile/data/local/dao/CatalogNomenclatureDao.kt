package re.melchior.saviomobile.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import re.melchior.saviomobile.data.local.entity.CatalogNomenclatureEntity

@Dao
interface CatalogNomenclatureDao {

    @Query(
        "SELECT * FROM catalog_nomenclature WHERE domain = :domain AND isActive = 1 ORDER BY label ASC"
    )
    fun getByDomain(domain: String): Flow<List<CatalogNomenclatureEntity>>

    @Query("SELECT * FROM catalog_nomenclature WHERE id = :id")
    suspend fun getById(id: String): CatalogNomenclatureEntity?

    @Upsert
    suspend fun upsertAll(items: List<CatalogNomenclatureEntity>)

    @Query("SELECT MAX(updatedAt) FROM catalog_nomenclature")
    suspend fun getLastUpdatedAt(): String?
}
