package re.melchior.saviomobile.data.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import re.melchior.saviomobile.data.local.dao.CatalogEquipmentDao
import re.melchior.saviomobile.data.local.dao.CatalogNomenclatureDao
import re.melchior.saviomobile.data.local.entity.CatalogNomenclatureEntity
import re.melchior.saviomobile.data.local.entity.CatalogEquipmentEntity
import re.melchior.saviomobile.data.local.entity.CatalogEquipmentSearchRow
import re.melchior.saviomobile.data.remote.api.BanCatalogApi
import re.melchior.saviomobile.data.remote.dto.toEntity
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CatalogSyncRepository @Inject constructor(
    private val api: BanCatalogApi,
    private val nomenclatureDao: CatalogNomenclatureDao,
    private val equipmentDao: CatalogEquipmentDao,
    @ApplicationContext private val context: Context,
) {

    private val prefs = context.getSharedPreferences("catalog_sync", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_LAST_SYNC = "last_sync_at"
        private const val SYNC_INTERVAL_MS = 24 * 60 * 60 * 1000L
    }

    suspend fun syncIfNeeded() {
        val lastSync = prefs.getLong(KEY_LAST_SYNC, 0L)
        if (System.currentTimeMillis() - lastSync < SYNC_INTERVAL_MS) return
        sync()
    }

    suspend fun sync() {
        val lastSync = prefs.getLong(KEY_LAST_SYNC, 0L)
        val since = if (lastSync == 0L) {
            null
        } else {
            Instant.ofEpochMilli(lastSync).toString()
        }

        val response = api.syncCatalog(since = since)

        nomenclatureDao.upsertAll(response.nomenclature.map { it.toEntity() })
        equipmentDao.upsertAll(response.equipment.map { it.toEntity() })

        val syncedAt = Instant.parse(response.syncedAt).toEpochMilli()
        prefs.edit().putLong(KEY_LAST_SYNC, syncedAt).apply()
    }

    fun getLastSyncMillis(): Long = prefs.getLong(KEY_LAST_SYNC, 0L)

    suspend fun getEquipmentById(id: String): CatalogEquipmentEntity? =
        equipmentDao.getById(id)

    suspend fun getNomenclatureById(id: String): CatalogNomenclatureEntity? =
        nomenclatureDao.getById(id)

    suspend fun getNoticeDownloadUrl(catalogEquipmentId: String): String? {
        return try {
            api.getNoticeDownloadUrl(catalogEquipmentId).downloadUrl
        } catch (e: Exception) {
            null
        }
    }

    fun getBrands() = nomenclatureDao.getByDomain("BRAND")

    fun getEnergies() = nomenclatureDao.getByDomain("ENERGY")

    fun getEquipmentTypes() = nomenclatureDao.getByDomain("EQUIPMENT_TYPE")

    fun searchEquipment(
        q: String,
        brandId: String?,
        typeId: String?,
        energyId: String?,
    ): Flow<List<CatalogEquipmentSearchRow>> =
        equipmentDao.search(q, brandId, typeId, energyId)
}
