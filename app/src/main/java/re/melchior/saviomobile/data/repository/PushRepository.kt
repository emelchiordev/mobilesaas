package re.melchior.saviomobile.data.repository

import android.provider.Settings
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import re.melchior.saviomobile.data.local.dao.InterventionActualTypeDao
import re.melchior.saviomobile.data.local.dao.InterventionDao
import re.melchior.saviomobile.data.local.dao.InvoiceDao
import re.melchior.saviomobile.data.local.dao.InvoiceLineDao
import re.melchior.saviomobile.data.local.dao.PendingUpdateDao
import re.melchior.saviomobile.data.remote.api.PushApi
import re.melchior.saviomobile.data.remote.dto.PushOperationDto
import re.melchior.saviomobile.data.remote.dto.PushRequestDto
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

sealed class PushResult {
    object Success : PushResult()
    object NothingToPush : PushResult()
    data class Error(val message: String) : PushResult()
}

@Singleton
class PushRepository @Inject constructor(
    private val pushApi: PushApi,
    private val interventionDao: InterventionDao,
    private val interventionActualTypeDao: InterventionActualTypeDao,
    private val invoiceDao: InvoiceDao,
    private val invoiceLineDao: InvoiceLineDao,
    private val pendingUpdateDao: PendingUpdateDao,
    @ApplicationContext private val context: Context
) {

    suspend fun push(): PushResult {
        return try {
            val pendingInterventions = interventionDao.getPendingSyncOnce()
            val pendingUpdates = pendingUpdateDao.getPendingOnce()

            if (pendingInterventions.isEmpty() && pendingUpdates.isEmpty()) {
                return PushResult.NothingToPush
            }

            val operations = mutableListOf<PushOperationDto>()

            pendingInterventions.forEach { intervention ->
                intervention.startedAt?.let { startedAt ->
                    operations.add(
                        PushOperationDto(
                            id = "op-start-${intervention.id}",
                            type = "START_INTERVENTION",
                            occurredAt = startedAt,
                            payload = mapOf(
                                "interventionId" to intervention.id,
                                "startedAt" to startedAt
                            )
                        )
                    )
                }
                intervention.completedAt?.let { completedAt ->
                    val storedActualTypes =
                        interventionActualTypeDao.getActualTypesForInterventionOnce(intervention.id)
                    val actualTypeIds = if (storedActualTypes.isNotEmpty()) {
                        storedActualTypes.map { it.interventionTypeId }
                    } else {
                        listOfNotNull(intervention.actualTypeId).filter { it.isNotBlank() }
                    }
                    operations.add(
                        PushOperationDto(
                            id = "op-complete-${intervention.id}",
                            type = "COMPLETE_INTERVENTION",
                            occurredAt = completedAt,
                            payload = mapOf(
                                "interventionId" to intervention.id,
                                "report" to (intervention.report ?: ""),
                                "completedAt" to completedAt,
                                "actualTypeIds" to actualTypeIds,
                                "actualTypeId" to (actualTypeIds.firstOrNull() ?: "")
                            )
                        )
                    )
                }
            }

            pendingUpdates.forEach { update ->
                val payload = com.google.gson.Gson()
                    .fromJson(update.payload, Map::class.java) as Map<String, Any?>
                val operationId =
                    if (update.id.startsWith("op-")) update.id else "op-update-${update.id}"
                operations.add(
                    PushOperationDto(
                        id = operationId,
                        type = update.type,
                        occurredAt = update.occurredAt,
                        payload = payload
                    )
                )
            }

            if (operations.isEmpty()) return PushResult.NothingToPush

            val deviceId = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ANDROID_ID
            ) ?: "unknown-device"

            val response = pushApi.push(
                PushRequestDto(
                    deviceId = deviceId,
                    pushedAt = Instant.now().toString(),
                    operations = operations
                )
            )

            response.results.forEach { result ->
                when {
                    result.operationId.startsWith("op-start-") -> {
                        if (result.status == "ok") {
                            result.operationId.removePrefix("op-start-")
                        }
                    }

                    result.operationId.startsWith("op-complete-") -> {
                        val interventionId = result.operationId.removePrefix("op-complete-")
                        when (result.status) {
                            "ok" -> interventionDao.markAsSynced(interventionId)
                            "conflict", "rejected" -> interventionDao.markAsConflict(interventionId)
                        }
                    }

                    result.operationId.startsWith("op-update-") -> {
                        val updateId = result.operationId.removePrefix("op-update-")
                        if (result.status == "ok") {
                            pendingUpdateDao.markAsSynced(updateId)
                        }
                    }

                    result.operationId.startsWith("op-submit-invoice-") -> {
                        if (result.status == "ok") {
                            val localInvoiceId =
                                result.operationId.removePrefix("op-submit-invoice-")
                            result.serverData?.let { data ->
                                val serverId = data["invoiceId"] as? String
                                val serverNumber = data["invoiceNumber"] as? String
                                val serverStatus = data["invoiceStatus"] as? String
                                if (serverId != null) {
                                    if (serverId != localInvoiceId) {
                                        invoiceLineDao.reassignToInvoice(
                                            localInvoiceId,
                                            serverId
                                        )
                                    }
                                    invoiceDao.updateServerData(
                                        localId = localInvoiceId,
                                        serverId = serverId,
                                        number = serverNumber,
                                        status = serverStatus ?: "pending_validation"
                                    )
                                }
                            }
                            pendingUpdateDao.markAsSynced(result.operationId)
                        }
                    }
                }
            }

            pendingUpdateDao.deleteSynced()

            PushResult.Success
        } catch (e: Exception) {
            PushResult.Error(e.message ?: "Erreur de synchronisation")
        }
    }
}
