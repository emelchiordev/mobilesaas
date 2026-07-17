package re.savio.mobile.data.repository

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import io.objectbox.query.QueryBuilder.StringOrder
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import re.savio.mobile.data.local.dao.SettingsDao
import re.savio.mobile.data.local.objectbox.MeasurementDeviceBox
import re.savio.mobile.data.local.objectbox.MeasurementDeviceBox_
import re.savio.mobile.data.local.objectbox.ObjectBoxStore
import re.savio.mobile.data.remote.api.MeasurementDeviceApi
import re.savio.mobile.data.remote.dto.CreateMeasurementDeviceBody
import re.savio.mobile.data.remote.dto.RememberCerfaMeasurementDeviceBody
import re.savio.mobile.data.remote.dto.controlDateToIso
import re.savio.mobile.data.remote.dto.hasUseCase
import re.savio.mobile.data.remote.dto.toBoxEntity
import re.savio.mobile.util.UserProfile
import java.time.Instant

@Singleton
class MeasurementDeviceSyncRepository @Inject constructor(
    private val api: MeasurementDeviceApi,
    private val settingsDao: SettingsDao,
    @ApplicationContext context: Context,
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "measurement_devices_sync"
        private const val KEY_LAST_SYNC = "measurement_devices_last_sync"
        private const val KEY_LAST_DEVICE_PREFIX = "last_cerfa_device_"
        private const val SYNC_INTERVAL_MS = 4 * 60 * 60 * 1000L
        private const val TAG = "MeasurementDeviceSync"
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
                val box = ObjectBoxStore.measurementDevicesBox()
                if (since == null) {
                    box.removeAll()
                }
                val existingById = box.all.associateBy { it.id }
                val entities = response.devices.map { dto ->
                    dto.toBoxEntity(existingById[dto.id])
                }
                box.put(entities)
                prefs.edit().putString(KEY_LAST_SYNC, response.syncedAt).apply()
                val techId = settingsDao.getSettingsOnce()?.technicianId
                if (!techId.isNullOrBlank() && !response.lastCerfaMeasurementDeviceId.isNullOrBlank()) {
                    prefs.edit()
                        .putString(KEY_LAST_DEVICE_PREFIX + techId, response.lastCerfaMeasurementDeviceId)
                        .apply()
                }
                Log.d(TAG, "Sync OK: ${entities.size} appareil(s)")
                true
            } catch (e: Exception) {
                Log.e(TAG, "Sync failed", e)
                false
            }
        }
    }

    fun listForCerfa(technicianId: String?, profile: String?): List<MeasurementDeviceBox> {
        val box = ObjectBoxStore.measurementDevicesBox()
        val all = box.all.filter { it.hasUseCase("cerfa") }
        return if (profile == UserProfile.ARTISAN_SOLO || technicianId.isNullOrBlank()) {
            all.sortedWith(compareBy({ it.brand.lowercase() }, { it.model.lowercase() }))
        } else {
            all.filter { it.assignedTechnicianId == technicianId }
                .sortedWith(compareBy({ it.brand.lowercase() }, { it.model.lowercase() }))
        }
    }

    fun findById(id: String): MeasurementDeviceBox? {
        if (id.isBlank()) return null
        return ObjectBoxStore.measurementDevicesBox()
            .query(MeasurementDeviceBox_.id.equal(id, StringOrder.CASE_SENSITIVE))
            .build()
            .findFirst()
    }

    fun getLastCerfaDeviceId(technicianId: String?): String? {
        if (technicianId.isNullOrBlank()) return null
        return prefs.getString(KEY_LAST_DEVICE_PREFIX + technicianId, null)
    }

    fun setLastCerfaDeviceIdLocal(technicianId: String, deviceId: String) {
        prefs.edit().putString(KEY_LAST_DEVICE_PREFIX + technicianId, deviceId).apply()
    }

    suspend fun rememberCerfaDevice(deviceId: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val techId = settingsDao.getSettingsOnce()?.technicianId
                if (!techId.isNullOrBlank()) {
                    setLastCerfaDeviceIdLocal(techId, deviceId)
                }
                api.rememberCerfa(RememberCerfaMeasurementDeviceBody(deviceId = deviceId))
                Result.success(Unit)
            } catch (e: Exception) {
                Log.w(TAG, "rememberCerfa failed (kept local)", e)
                Result.success(Unit)
            }
        }
    }

    suspend fun createOnTheFly(
        brand: String,
        model: String,
        serialNumber: String?,
        lastControlDate: String?,
    ): Result<MeasurementDeviceBox> {
        return withContext(Dispatchers.IO) {
            try {
                val dto = api.createFromMobile(
                    CreateMeasurementDeviceBody(
                        brand = brand.trim(),
                        model = model.trim(),
                        serialNumber = serialNumber?.trim()?.takeIf { it.isNotEmpty() },
                        useCases = listOf("cerfa"),
                        lastControlDate = controlDateToIso(lastControlDate),
                        assignToSelf = true,
                    ),
                )
                val entity = dto.toBoxEntity()
                ObjectBoxStore.measurementDevicesBox().put(entity)
                Result.success(entity)
            } catch (e: Exception) {
                Log.e(TAG, "createOnTheFly failed", e)
                Result.failure(e)
            }
        }
    }
}
