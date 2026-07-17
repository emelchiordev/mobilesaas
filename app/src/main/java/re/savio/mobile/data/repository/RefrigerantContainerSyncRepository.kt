package re.savio.mobile.data.repository

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import io.objectbox.query.QueryBuilder.StringOrder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import re.savio.mobile.data.local.objectbox.ObjectBoxStore
import re.savio.mobile.data.local.objectbox.RefrigerantContainerBox
import re.savio.mobile.data.local.objectbox.RefrigerantContainerBox_
import re.savio.mobile.data.remote.api.RefrigerantContainerApi
import re.savio.mobile.data.remote.dto.CreateRefrigerantContainerBody
import re.savio.mobile.data.remote.dto.toBoxEntity
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RefrigerantContainerSyncRepository @Inject constructor(
    private val api: RefrigerantContainerApi,
    @ApplicationContext context: Context,
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "refrigerant_containers_sync"
        private const val KEY_LAST_SYNC = "refrigerant_containers_last_sync"
        private const val SYNC_INTERVAL_MS = 4 * 60 * 60 * 1000L
        private const val TAG = "RefrigerantContainerSync"
        const val STATUS_WITH_TECHNICIAN = "with_technician"
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
                val box = ObjectBoxStore.refrigerantContainersBox()
                val existingById = box.all.associateBy { it.id }
                val entities = response.containers.map { dto ->
                    dto.toBoxEntity(existingById[dto.id])
                }
                box.put(entities)
                prefs.edit()
                    .putString(KEY_LAST_SYNC, response.syncedAt)
                    .apply()
                Log.d(TAG, "Sync OK: ${entities.size} contenant(s)")
                true
            } catch (e: Exception) {
                Log.e(TAG, "Sync failed", e)
                false
            }
        }
    }

    fun listForTechnician(
        technicianId: String?,
        containerKind: String,
        fluidType: String? = null,
    ): List<RefrigerantContainerBox> {
        if (technicianId.isNullOrBlank()) return emptyList()
        val box = ObjectBoxStore.refrigerantContainersBox()
        var condition = RefrigerantContainerBox_.status.equal(
            STATUS_WITH_TECHNICIAN,
            StringOrder.CASE_SENSITIVE,
        ).and(
            RefrigerantContainerBox_.holderTechnicianId.equal(
                technicianId,
                StringOrder.CASE_SENSITIVE,
            ),
        ).and(
            RefrigerantContainerBox_.containerKind.equal(
                containerKind,
                StringOrder.CASE_SENSITIVE,
            ),
        )
        if (!fluidType.isNullOrBlank()) {
            val normalized = fluidType.trim().uppercase()
            condition = condition.and(
                RefrigerantContainerBox_.fluidType.equal(
                    normalized,
                    StringOrder.CASE_SENSITIVE,
                ),
            )
        }
        return box.query(condition)
            .order(RefrigerantContainerBox_.containerIdentifier)
            .build()
            .find()
    }

    fun findById(id: String): RefrigerantContainerBox? {
        if (id.isBlank()) return null
        val box = ObjectBoxStore.refrigerantContainersBox()
        return box.query(
            RefrigerantContainerBox_.id.equal(id, StringOrder.CASE_SENSITIVE),
        ).build().findFirst()
    }

    suspend fun createOnTheFly(
        identifier: String,
        fluidType: String,
        capacityKg: Double,
        containerKind: String,
        technicianId: String,
    ): Result<RefrigerantContainerBox> {
        return withContext(Dispatchers.IO) {
            try {
                val dto = api.create(
                    CreateRefrigerantContainerBody(
                        containerIdentifier = identifier.trim(),
                        containerKind = containerKind,
                        fluidType = fluidType.trim().uppercase(),
                        capacityKg = capacityKg,
                        status = STATUS_WITH_TECHNICIAN,
                        holderTechnicianId = technicianId,
                    ),
                )
                val box = ObjectBoxStore.refrigerantContainersBox()
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
