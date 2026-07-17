package re.savio.mobile.data.repository

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import io.objectbox.query.QueryBuilder.StringOrder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import re.savio.mobile.data.local.objectbox.ObjectBoxStore
import re.savio.mobile.data.local.objectbox.RefrigerantWastePartnerBox
import re.savio.mobile.data.local.objectbox.RefrigerantWastePartnerBox_
import re.savio.mobile.data.remote.api.RefrigerantWastePartnerApi
import re.savio.mobile.data.remote.dto.CreateRefrigerantWastePartnerBody
import re.savio.mobile.data.remote.dto.toBoxEntity
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RefrigerantWastePartnerSyncRepository @Inject constructor(
    private val api: RefrigerantWastePartnerApi,
    @ApplicationContext context: Context,
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "refrigerant_waste_partners_sync"
        private const val KEY_LAST_SYNC = "refrigerant_waste_partners_last_sync"
        private const val SYNC_INTERVAL_MS = 4 * 60 * 60 * 1000L
        private const val TAG = "WastePartnerSync"
        const val KIND_TRANSPORTER = "transporter"
        const val KIND_DESTINATION = "destination"
    }

    suspend fun syncIfNeeded(): Boolean {
        val lastSync = prefs.getString(KEY_LAST_SYNC, null)
        val lastSyncMs = lastSync?.let {
            runCatching { Instant.parse(it).toEpochMilli() }.getOrDefault(0L)
        } ?: 0L
        if (System.currentTimeMillis() - lastSyncMs < SYNC_INTERVAL_MS) {
            return false
        }
        return sync()
    }

    suspend fun sync(force: Boolean = false): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val since = if (force) null else prefs.getString(KEY_LAST_SYNC, null)
                val response = api.getForMobile(since = since)
                val box = ObjectBoxStore.refrigerantWastePartnersBox()
                if (since == null) {
                    box.removeAll()
                }
                val existingById = box.all.associateBy { it.id }
                val entities = response.partners.map { dto ->
                    dto.toBoxEntity(existingById[dto.id])
                }
                box.put(entities)
                prefs.edit()
                    .putString(KEY_LAST_SYNC, response.syncedAt)
                    .apply()
                Log.d(TAG, "Sync OK: ${entities.size} partenaire(s)")
                true
            } catch (e: Exception) {
                Log.e(TAG, "Sync failed", e)
                false
            }
        }
    }

    fun listByKind(kind: String): List<RefrigerantWastePartnerBox> {
        if (kind.isBlank()) return emptyList()
        val box = ObjectBoxStore.refrigerantWastePartnersBox()
        return box.query(
            RefrigerantWastePartnerBox_.kind.equal(kind, StringOrder.CASE_SENSITIVE),
        )
            .order(RefrigerantWastePartnerBox_.label)
            .build()
            .find()
            .sortedWith(
                compareByDescending<RefrigerantWastePartnerBox> { it.isDefault }
                    .thenBy { it.label.lowercase() },
            )
    }

    fun findById(id: String): RefrigerantWastePartnerBox? {
        if (id.isBlank()) return null
        val box = ObjectBoxStore.refrigerantWastePartnersBox()
        return box.query(
            RefrigerantWastePartnerBox_.id.equal(id, StringOrder.CASE_SENSITIVE),
        ).build().findFirst()
    }

    suspend fun createOnTheFly(
        kind: String,
        label: String,
        siret: String,
        addressLine1: String?,
        addressLine2: String?,
        postalCode: String?,
        city: String?,
    ): Result<RefrigerantWastePartnerBox> {
        return withContext(Dispatchers.IO) {
            try {
                val dto = api.create(
                    CreateRefrigerantWastePartnerBody(
                        kind = kind,
                        label = label.trim(),
                        siret = siret.trim(),
                        addressLine1 = addressLine1?.trim()?.takeIf { it.isNotEmpty() },
                        addressLine2 = addressLine2?.trim()?.takeIf { it.isNotEmpty() },
                        postalCode = postalCode?.trim()?.takeIf { it.isNotEmpty() },
                        city = city?.trim()?.takeIf { it.isNotEmpty() },
                        isDefault = false,
                    ),
                )
                val box = ObjectBoxStore.refrigerantWastePartnersBox()
                val entity = dto.toBoxEntity()
                box.put(entity)
                Result.success(entity)
            } catch (e: Exception) {
                Log.e(TAG, "createOnTheFly failed", e)
                Result.failure(e)
            }
        }
    }
}
