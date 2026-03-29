package re.melchior.saviomobile.data.repository

import com.google.gson.Gson
import re.melchior.saviomobile.data.local.dao.PendingUpdateDao
import re.melchior.saviomobile.data.local.dao.SettingsDao
import re.melchior.saviomobile.data.local.entity.PendingUpdateEntity
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PendingUpdateRepository @Inject constructor(
    private val pendingUpdateDao: PendingUpdateDao,
    private val settingsDao: SettingsDao
) {
    private val gson = Gson()

    suspend fun updateCustomer(
        customerId: String,
        phone: String? = null,
        email: String? = null,
        notes: String? = null
    ) {
        val payload = buildMap<String, Any?> {
            put("customerId", customerId)
            phone?.let { put("phone", it) }
            email?.let { put("email", it) }
            notes?.let { put("notes", it) }
        }

        pendingUpdateDao.insert(
            PendingUpdateEntity(
                id = UUID.randomUUID().toString(),
                type = "UPDATE_CUSTOMER",
                targetId = customerId,
                payload = gson.toJson(payload),
                occurredAt = Instant.now().toString()
            )
        )
    }

    suspend fun updateUnitAccess(
        unitId: String,
        floor: String? = null,
        doorCode: String? = null,
        addressLine2: String? = null
    ) {
        val payload = buildMap<String, Any?> {
            put("unitId", unitId)
            floor?.let { put("floor", it) }
            doorCode?.let { put("doorCode", it) }
            addressLine2?.let { put("addressLine2", it) }
        }

        pendingUpdateDao.insert(
            PendingUpdateEntity(
                id = UUID.randomUUID().toString(),
                type = "UPDATE_UNIT_ACCESS",
                targetId = unitId,
                payload = gson.toJson(payload),
                occurredAt = Instant.now().toString()
            )
        )
    }

    // Récupère la dernière valeur override pour un champ donné
    suspend fun getLatestCustomerOverride(customerId: String): Map<String, Any?>? {
        val latest = pendingUpdateDao.getLatestForTarget(customerId) ?: return null
        return gson.fromJson(latest.payload, Map::class.java) as? Map<String, Any?>
    }

    suspend fun getLatestUnitOverride(unitId: String): Map<String, Any?>? {
        val latest = pendingUpdateDao.getLatestForTarget(unitId) ?: return null
        return gson.fromJson(latest.payload, Map::class.java) as? Map<String, Any?>
    }

    fun getPendingCount() = pendingUpdateDao.getPendingCount()

    suspend fun getPendingOnce() = pendingUpdateDao.getPendingOnce()

    suspend fun markAsSynced(id: String) = pendingUpdateDao.markAsSynced(id)

    suspend fun deleteSynced() = pendingUpdateDao.deleteSynced()
}