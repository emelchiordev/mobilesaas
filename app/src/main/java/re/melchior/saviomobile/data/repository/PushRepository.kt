package re.melchior.saviomobile.data.repository

import android.provider.Settings
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import re.melchior.saviomobile.data.local.dao.InterventionActualTypeDao
import re.melchior.saviomobile.data.local.dao.InterventionDao
import re.melchior.saviomobile.data.local.dao.InvoiceDao
import re.melchior.saviomobile.data.local.dao.InvoiceLineDao
import re.melchior.saviomobile.data.local.dao.PendingOperationDao
import re.melchior.saviomobile.data.local.dao.PendingUpdateDao
import re.melchior.saviomobile.data.remote.api.PushApi
import re.melchior.saviomobile.data.remote.dto.PushOperationDto
import re.melchior.saviomobile.data.remote.dto.PushRequestDto
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

sealed class PushResult {
    object Success : PushResult()
    object NothingToPush : PushResult()
    data class Error(val message: String) : PushResult()
}

data class ConflictEvent(
    val interventionId: String,
    val conflictType: String,
    val message: String,
)

@Singleton
class PushRepository @Inject constructor(
    private val pushApi: PushApi,
    private val interventionDao: InterventionDao,
    private val interventionActualTypeDao: InterventionActualTypeDao,
    private val invoiceDao: InvoiceDao,
    private val invoiceLineDao: InvoiceLineDao,
    private val pendingUpdateDao: PendingUpdateDao,
    private val pendingOperationDao: PendingOperationDao,
    private val coldMeasureRepository: ColdMeasureRepository,
    @ApplicationContext private val context: Context
) {

    private val _conflictEvents =
        MutableSharedFlow<ConflictEvent>(extraBufferCapacity = 64)
    val conflictEvents: SharedFlow<ConflictEvent> = _conflictEvents.asSharedFlow()

    suspend fun push(): PushResult {
        return try {
            val pendingInterventions = interventionDao.getPendingSyncOnce()
            val pendingUpdates = pendingUpdateDao.getPendingOnce()
            val pendingOps = pendingOperationDao.getPending()
            val dirtyColdMeasures = coldMeasureRepository.getDirty()

            if (pendingInterventions.isEmpty() &&
                pendingUpdates.isEmpty() &&
                pendingOps.isEmpty() &&
                dirtyColdMeasures.isEmpty()
            ) {
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

            pendingOps.forEach { op ->
                val payload = com.google.gson.Gson()
                    .fromJson(op.payload, Map::class.java) as Map<String, Any?>
                operations.add(
                    PushOperationDto(
                        id = op.id,
                        type = op.type,
                        occurredAt = op.occurredAt,
                        payload = payload
                    )
                )
            }

            dirtyColdMeasures.forEach { measure ->
                val payload = buildMap<String, Any?> {
                    put("interventionId", measure.interventionId)
                    put("equipmentId", measure.equipmentId)
                    put("frigo", measure.frigo)
                    put("charg", measure.charg.toDoubleOrNull())
                    put("tonnage", measure.tonnage)
                    put("minter1", measure.minter1)
                    put("minter2", measure.minter2)
                    put("minter3", measure.minter3)
                    put("minter4", measure.minter4)
                    put("minter5", measure.minter5)
                    put("minter6", measure.minter6)
                    put("minter7", measure.minter7)
                    put("minter8", measure.minter8)
                    put("minteraut", measure.minteraut)
                    put("obsern1", measure.obsern1)
                    put("obsern2", measure.obsern2)
                    put("detm1", measure.detm1)
                    put("dett1", measure.dett1)
                    put("detd1", measure.detd1.ifBlank { null })
                    put("autofuite", measure.autofuite)
                    put("qtefri", measure.qtefri.toDoubleOrNull())
                    put("qtefri2", measure.qtefri2.toDoubleOrNull())
                    put("freqs1", measure.freqs1)
                    put("freqs2", measure.freqs2)
                    put("freqs3", measure.freqs3)
                    put("freqa1", measure.freqa1)
                    put("freqa2", measure.freqa2)
                    put("freqa3", measure.freqa3)
                    put("pasfuite", measure.pasfuite)
                    put("fuiteloc1", measure.fuiteloc1)
                    put("fuiterep1", measure.fuiterep1)
                    put("fuiteloc2", measure.fuiteloc2)
                    put("fuiterep2", measure.fuiterep2)
                    put("fuiteloc3", measure.fuiteloc3)
                    put("fuiterep3", measure.fuiterep3)
                    put("fluidrein", measure.fluidrein.toDoubleOrNull())
                    put("fluidecv", measure.fluidecv.toDoubleOrNull())
                    put("fluidecr", measure.fluidecr.toDoubleOrNull())
                    put("fluidecrg", measure.fluidecrg.toDoubleOrNull())
                    put("fluidrecup", measure.fluidrecup.toDoubleOrNull())
                    put("fluidert", measure.fluidert.toDoubleOrNull())
                    put("fluideru", measure.fluideru.toDoubleOrNull())
                    put("fluiderc", measure.fluiderc)
                    put("un1078a", measure.un1078a)
                    put("un1078b", measure.un1078b)
                    put("nomdec", measure.nomdec)
                    put("adres1dec", measure.adres1dec)
                    put("adres2dec", measure.adres2dec)
                    put("villedec", measure.villedec)
                    put("nomtrans", measure.nomtrans)
                    put("adres1trans", measure.adres1trans)
                    put("adres2trans", measure.adres2trans)
                    put("villetrans", measure.villetrans)
                    put("fluidobs", measure.fluidobs)
                    put("fluidobs2", measure.fluidobs2)
                    put("bordeqte", measure.bordeqte)
                    put("bordetrans", measure.bordetrans)
                    put("instatrait", measure.instatrait)
                    put("coderd", measure.coderd)
                    put("qterecep", measure.qterecep.toDoubleOrNull())
                    put("frigo2", measure.frigo2)
                    put("bsff", measure.bsff)
                    put("un3161a", measure.un3161a)
                    put("un3161b", measure.un3161b)
                }
                operations.add(
                    PushOperationDto(
                        id = measure.id,
                        type = "SAVE_COLD_MEASURE",
                        occurredAt = measure.updatedAt.ifBlank {
                            Instant.now().toString()
                        },
                        payload = payload,
                    ),
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

            val allOk = response.results.all {
                it.status == "ok"
            }

            if (allOk) {
                response.results.forEach { result ->
                    pendingOps.find { it.id == result.operationId }?.let {
                        pendingOperationDao.updateStatus(it.id, "sent")
                    }
                    dirtyColdMeasures.find { it.id == result.operationId }?.let {
                        coldMeasureRepository.markClean(it.id)
                    }
                }
                response.results.forEach { result ->
                    if (result.operationId.startsWith("op-complete-")) {
                        val interventionId = result.operationId
                            .removePrefix("op-complete-")
                        interventionDao.markAsSynced(interventionId)
                    }
                }
                response.results.forEach { result ->
                    when {
                        result.operationId.startsWith("op-update-") -> {
                            val updateId = result.operationId.removePrefix("op-update-")
                            pendingUpdateDao.markAsSynced(updateId)
                        }
                        result.operationId.startsWith("op-submit-invoice-") -> {
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
                pendingUpdateDao.deleteSynced()
            } else {
                android.util.Log.w(
                    "PushRepository",
                    "Push partiel ou échoué — opérations conservées pour réessai"
                )
                response.results
                    .filter { it.status == "conflict" }
                    .forEach { result ->
                        if (result.operationId.startsWith("op-complete-")) {
                            val interventionId = result.operationId
                                .removePrefix("op-complete-")
                            interventionDao.markAsConflict(interventionId)
                            _conflictEvents.emit(
                                ConflictEvent(
                                    interventionId = interventionId,
                                    conflictType = result.conflictType ?: "UNKNOWN",
                                    message = result.message
                                        ?: result.reason
                                        ?: "Conflit détecté.",
                                )
                            )
                        }
                    }
            }

            PushResult.Success
        } catch (e: Exception) {
            PushResult.Error(e.message ?: "Erreur de synchronisation")
        }
    }
}
