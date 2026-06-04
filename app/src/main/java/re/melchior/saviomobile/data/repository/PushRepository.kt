package re.melchior.saviomobile.data.repository

import android.provider.Settings
import android.content.Context
import androidx.room.withTransaction
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import re.melchior.saviomobile.data.local.dao.ColdMeasureDao
import re.melchior.saviomobile.data.local.dao.EquipmentDao
import re.melchior.saviomobile.data.local.dao.InterventionActualTypeDao
import re.melchior.saviomobile.data.local.dao.InterventionDao
import re.melchior.saviomobile.data.local.database.SavioDatabase
import re.melchior.saviomobile.data.local.dao.SettingsDao
import re.melchior.saviomobile.data.local.dao.InvoiceDao
import re.melchior.saviomobile.data.local.dao.InvoiceLineDao
import re.melchior.saviomobile.data.local.dao.AttestationVePointControleDao
import re.melchior.saviomobile.data.local.dao.PendingOperationDao
import re.melchior.saviomobile.data.local.dao.PendingUpdateDao
import re.melchior.saviomobile.data.local.entity.AttestationVeEntity
import re.melchior.saviomobile.data.local.entity.AttestationVePointControleEntity
import re.melchior.saviomobile.data.local.entity.ColdMeasureEntity
import re.melchior.saviomobile.data.local.entity.InterventionEntity
import re.melchior.saviomobile.data.local.entity.MeasureEntity
import re.melchior.saviomobile.data.local.entity.PacMeasureEntity
import re.melchior.saviomobile.data.local.entity.PendingOperationEntity
import re.melchior.saviomobile.data.local.entity.PendingUpdateEntity
import re.melchior.saviomobile.data.remote.api.PushApi
import re.melchior.saviomobile.data.remote.dto.PushOperationDto
import re.melchior.saviomobile.data.remote.dto.PushRequestDto
import re.melchior.saviomobile.data.remote.dto.PushResultDto
import retrofit2.HttpException
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

sealed class PushResult {
    data class Success(
        val conflictInterventionIds: Set<String> = emptySet(),
    ) : PushResult()

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
    private val database: SavioDatabase,
    private val pushApi: PushApi,
    private val interventionDao: InterventionDao,
    private val interventionActualTypeDao: InterventionActualTypeDao,
    private val equipmentDao: EquipmentDao,
    private val coldMeasureDao: ColdMeasureDao,
    private val invoiceRepository: InvoiceRepository,
    private val invoiceDao: InvoiceDao,
    private val invoiceLineDao: InvoiceLineDao,
    private val pendingUpdateDao: PendingUpdateDao,
    private val pendingOperationDao: PendingOperationDao,
    private val coldMeasureRepository: ColdMeasureRepository,
    private val measureRepository: MeasureRepository,
    private val pacMeasureRepository: PacMeasureRepository,
    private val attestationVeRepository: AttestationVeRepository,
    private val attestationVePointControleDao: AttestationVePointControleDao,
    private val anomalyDraftRepository: AnomalyDraftRepository,
    private val settingsDao: SettingsDao,
    @ApplicationContext private val context: Context
) {

    private val _conflictEvents =
        MutableSharedFlow<ConflictEvent>(extraBufferCapacity = 64)
    val conflictEvents: SharedFlow<ConflictEvent> = _conflictEvents.asSharedFlow()

    private val pushMutex = Mutex()

    companion object {
        private const val LOG_TAG = "SavioPush"
        private val VERSIONED_PUSH_TYPES = setOf(
            "START_INTERVENTION",
            "CLOSE_INTERVENTION",
            "COMPLETE_INTERVENTION",
            "UPDATE_INTERVENTION",
            "ADD_SIGNATURE",
        )

    }

    private data class PushApplyContext(
        val pendingOps: List<PendingOperationEntity>,
        val pendingUpdates: List<PendingUpdateEntity>,
        val dirtyColdMeasures: List<ColdMeasureEntity>,
        val dirtyMeasures: List<MeasureEntity>,
        val dirtyPacMeasures: List<PacMeasureEntity>,
        val dirtyAttestations: List<AttestationVeEntity>,
    )

    private data class TierPushOutcome(
        val tier: Int,
        val results: List<PushResultDto>,
    )

    suspend fun push(): PushResult {
        // Toujours logger en premier : si ce message n’apparaît pas, `push()` n’est pas appelé
        // (autre écran, exception avant l’appel, worker arrêté faute de slug/token, mauvais processus Logcat).
        android.util.Log.i(LOG_TAG, "push() invoqué (mutex verrouillé=${pushMutex.isLocked})")
        if (pushMutex.isLocked) {
            android.util.Log.w(LOG_TAG, "push ignoré: une synchronisation push est déjà en cours (mutex)")
            return PushResult.NothingToPush
        }
        return pushMutex.withLock {
            try {
                android.util.Log.i(LOG_TAG, "push() — début (construction de la file)")
                val pendingInterventions = interventionDao.getPendingSyncOnce()
                val pushableInterventionIds = pendingInterventions.map { it.id }.toSet()

                if (pushableInterventionIds.isEmpty()) {
                    android.util.Log.i(
                        LOG_TAG,
                        "rien à pousser : aucune intervention clôturée (COMPLETED) en attente",
                    )
                    return PushResult.NothingToPush
                }

                ensureCreateEquipmentPendingOps(pendingInterventions)

                val pendingUpdates =
                    pendingUpdateDao.getPendingOnce().filter { it.type != "SUBMIT_INVOICE_FULL" }
                val pendingOps =
                    pendingOperationDao.getPending().filter { op ->
                        op.interventionId in pushableInterventionIds
                    }
                val dirtyColdMeasures =
                    coldMeasureRepository.getDirty().filter { measure ->
                        measure.interventionId in pushableInterventionIds
                    }
                val dirtyMeasures =
                    measureRepository.getDirty().filter { measure ->
                        measure.interventionId in pushableInterventionIds
                    }
                val dirtyPacMeasures =
                    pacMeasureRepository.getDirty().filter { measure ->
                        measure.interventionId in pushableInterventionIds
                    }
                val dirtyAttestations =
                    attestationVeRepository.getDirty().filter { attestation ->
                        attestation.interventionId in pushableInterventionIds
                    }

                if (pendingInterventions.isEmpty() &&
                    pendingUpdates.isEmpty() &&
                    pendingOps.isEmpty() &&
                    dirtyColdMeasures.isEmpty() &&
                    dirtyMeasures.isEmpty() &&
                    dirtyPacMeasures.isEmpty() &&
                    dirtyAttestations.isEmpty()
                ) {
                    android.util.Log.i(
                        LOG_TAG,
                        "rien à pousser (interventions=${pendingInterventions.size}, " +
                            "pendingUpdates=${pendingUpdates.size}, pendingOps=${pendingOps.size}, " +
                            "coldMeasures=${dirtyColdMeasures.size}, measures=${dirtyMeasures.size}, " +
                            "pacMeasures=${dirtyPacMeasures.size}, " +
                            "attestations=${dirtyAttestations.size})",
                    )
                    return PushResult.NothingToPush
                }

                android.util.Log.i(
                    LOG_TAG,
                    "file locale: interventions=${pendingInterventions.size}, " +
                        "pendingUpdates=${pendingUpdates.size}, pendingOps=${pendingOps.size}, " +
                        "coldMeasures=${dirtyColdMeasures.size}, measures=${dirtyMeasures.size}, " +
                        "pacMeasures=${dirtyPacMeasures.size}, " +
                        "attestations=${dirtyAttestations.size}",
                )
    
                val startOps = mutableListOf<PushOperationDto>()
                val closeOps = mutableListOf<PushOperationDto>()

                pendingInterventions.forEach { intervention ->
                    intervention.startedAt?.let { startedAt ->
                        startOps.add(
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
                        closeOps.add(
                            buildCloseInterventionOperation(
                                intervention = intervention,
                                completedAt = completedAt,
                                coldMeasures =
                                    dirtyColdMeasures.filter {
                                        it.interventionId == intervention.id
                                    },
                                measures =
                                    dirtyMeasures.filter {
                                        it.interventionId == intervention.id
                                    },
                                pacMeasures =
                                    dirtyPacMeasures.filter {
                                        it.interventionId == intervention.id
                                    },
                                attestations =
                                    dirtyAttestations.filter {
                                        it.interventionId == intervention.id
                                    },
                            ),
                        )
                    }
                }

                val operations = mutableListOf<PushOperationDto>()

                operations.addAll(startOps)

                val gson = com.google.gson.Gson()
                pendingUpdates.forEach { update ->
                    val operationId =
                        if (update.id.startsWith("op-")) update.id
                        else "op-update-${update.id}"
                    val payload =
                        gson.fromJson(update.payload, Map::class.java) as Map<String, Any?>
                    operations.add(
                        PushOperationDto(
                            id = operationId,
                            type = update.type,
                            occurredAt = update.occurredAt,
                            payload = payload,
                        ),
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

                operations.addAll(closeOps)

                if (operations.isEmpty()) {
                    android.util.Log.w(
                        LOG_TAG,
                        "données marquées « en attente » mais aucune opération construite (vérif startedAt/completedAt, etc.)",
                    )
                    return PushResult.NothingToPush
                }

                val isClosurePush = operations.any { it.type == "CLOSE_INTERVENTION" }

                val applyContext = PushApplyContext(
                    pendingOps = pendingOps,
                    pendingUpdates = pendingUpdates,
                    dirtyColdMeasures = dirtyColdMeasures,
                    dirtyMeasures = dirtyMeasures,
                    dirtyPacMeasures = dirtyPacMeasures,
                    dirtyAttestations = dirtyAttestations,
                )

                val tiers = PushOperationOrdering.partitionIntoTiers(operations)
                val versionByIntervention = mutableMapOf<String, Int>()
                val allConflictIds = mutableSetOf<String>()
                val tierOutcomes = mutableListOf<TierPushOutcome>()

                val deviceId = Settings.Secure.getString(
                    context.contentResolver,
                    Settings.Secure.ANDROID_ID,
                ) ?: "unknown-device"

                android.util.Log.i(
                    LOG_TAG,
                    "envoi POST api/mobile/sync/push: ${operations.size} op(s) en ${tiers.size} vague(s), " +
                        "closure=$isClosurePush, deviceId=$deviceId",
                )
                logPushDiagnostics(operations, tiers)

                for ((tier, tierOps) in tiers) {
                    val tierOpsForSend =
                        if (isClosurePush && tier == 3) {
                            refreshCloseOperationsForClosureTier(
                                tierOps = tierOps,
                                pendingInterventions = pendingInterventions,
                                pushableInterventionIds = pushableInterventionIds,
                            )
                        } else {
                            tierOps
                        }
                    val operationsToSend =
                        attachClientKnownVersions(tierOpsForSend, versionByIntervention)

                    operationsToSend.forEachIndexed { index, op ->
                        android.util.Log.i(
                            LOG_TAG,
                            "vague P$tier op[$index] id=${op.id} type=${op.type} occurredAt=${op.occurredAt} " +
                                "clientKnownVersion=${op.clientKnownVersion}",
                        )
                    }

                    val response = pushApi.push(
                        PushRequestDto(
                            deviceId = deviceId,
                            pushedAt = Instant.now().toString(),
                            operations = operationsToSend,
                        ),
                    )

                    val allOk = response.results.all { it.status == "ok" }

                    android.util.Log.i(
                        LOG_TAG,
                        "réponse vague P$tier: applied=${response.applied} failed=${response.failed} " +
                            "appliedAt=${response.appliedAt} allOk=$allOk",
                    )
                    response.results.forEach { r ->
                        android.util.Log.i(
                            LOG_TAG,
                            "  → ${r.operationId} status=${r.status} reason=${r.reason} " +
                                "conflictType=${r.conflictType} message=${r.message}",
                        )
                    }

                    tierOutcomes.add(TierPushOutcome(tier, response.results))

                    if (!isClosurePush) {
                        applyPushResults(applyContext, response.results, tier)

                        val hardFailures =
                            response.results.filter { PushOperationOrdering.isHardPushFailure(it.status) }
                        if (hardFailures.isNotEmpty() && !allOk) {
                            android.util.Log.w(
                                LOG_TAG,
                                "push partiel ou rejet/conflit vague P$tier — résolution auto prévue",
                            )
                        }

                        if (tier == 1 && hardFailures.isNotEmpty()) {
                            val first = hardFailures.first()
                            val msg = first.message ?: first.reason
                                ?: "Échec synchronisation (créations — priorité 1)"
                            android.util.Log.e(LOG_TAG, "arrêt push après vague P1: $msg")
                            return PushResult.Error(msg)
                        }

                        allConflictIds.addAll(applyAutoResolvableConflicts(response.results))
                    } else {
                        val closureFailure = findClosurePushFailure(response.results)
                        if (closureFailure != null) {
                            val msg = closureFailure.message ?: closureFailure.reason
                                ?: "Synchronisation incomplète (clôture)"
                            android.util.Log.e(
                                LOG_TAG,
                                "clôture transactionnelle annulée vague P$tier: $msg",
                            )
                            return PushResult.Error(msg)
                        }
                        if (tier == 1) {
                            applyEquipmentIdRemapsFromTierResults(
                                response.results,
                                applyContext.pendingOps,
                            )
                        }
                    }
                }

                if (isClosurePush) {
                    val allResults = tierOutcomes.flatMap { it.results }
                    if (!allResults.all { it.status == "ok" }) {
                        val bad = allResults.first { it.status != "ok" }
                        return PushResult.Error(
                            bad.message ?: bad.reason
                                ?: "Synchronisation incomplète. Vérifiez votre connexion et réessayez.",
                        )
                    }
                    database.withTransaction {
                        applyAllPushResults(applyContext, tierOutcomes)
                        pendingUpdateDao.deleteSynced()
                    }
                    tierOutcomes.forEach { outcome ->
                        allConflictIds.addAll(applyAutoResolvableConflicts(outcome.results))
                    }
                } else {
                    pendingUpdateDao.deleteSynced()
                }

                android.util.Log.i(
                    LOG_TAG,
                    "push() terminé, retour Success (conflicts=${allConflictIds.size}, closure=$isClosurePush)",
                )
                PushResult.Success(conflictInterventionIds = allConflictIds)
            } catch (e: Exception) {
                android.util.Log.e(LOG_TAG, "push exception: ${e.javaClass.simpleName}: ${e.message}", e)
                if (e is HttpException) {
                    val code = e.code()
                    val body = try {
                        e.response()?.errorBody()?.string()?.take(4000)
                    } catch (_: Exception) {
                        null
                    }
                    android.util.Log.e(LOG_TAG, "HTTP $code corps erreur: $body")
                }
                PushResult.Error(e.message ?: "Erreur de synchronisation")
            }
        }
    }

    /**
     * Migration one-shot : anciens SUBMIT_INVOICE_FULL → facture PENDING, purge de la file.
     */
    suspend fun migrateLegacyPendingInvoiceSubmit() {
        val gson = com.google.gson.Gson()
        val legacy = pendingUpdateDao.getPendingOnce().filter { it.type == "SUBMIT_INVOICE_FULL" }
        if (legacy.isEmpty()) return
        for (update in legacy) {
            val interventionId =
                invoiceDao.getById(update.targetId)?.interventionId?.trim().orEmpty()
                    .ifBlank {
                        runCatching {
                            @Suppress("UNCHECKED_CAST")
                            val payload =
                                gson.fromJson(update.payload, Map::class.java)
                                    as Map<String, Any?>
                            (payload["interventionId"] as? String)?.trim().orEmpty()
                        }.getOrDefault("")
                    }
            if (interventionId.isNotEmpty()) {
                invoiceDao.getByInterventionId(interventionId)?.id?.let {
                    invoiceDao.markAsPending(it)
                }
            } else {
                invoiceDao.markAsPending(update.targetId)
            }
        }
        pendingUpdateDao.deletePendingSubmitInvoiceFull()
        android.util.Log.i(
            LOG_TAG,
            "migration: ${legacy.size} SUBMIT_INVOICE_FULL retirés (CLOSE_INTERVENTION)",
        )
    }

    private suspend fun buildCloseInterventionOperation(
        intervention: InterventionEntity,
        completedAt: String,
        coldMeasures: List<ColdMeasureEntity>,
        measures: List<MeasureEntity>,
        pacMeasures: List<PacMeasureEntity>,
        attestations: List<AttestationVeEntity>,
    ): PushOperationDto {
        val storedActualTypes =
            interventionActualTypeDao.getActualTypesForInterventionOnce(intervention.id)
        val actualTypeIds =
            if (storedActualTypes.isNotEmpty()) {
                storedActualTypes.map { it.interventionTypeId }
            } else {
                listOfNotNull(intervention.actualTypeId).filter { it.isNotBlank() }
            }
        val techId = settingsDao.getSettingsOnce()?.technicianId.orEmpty()
        val payload = mutableMapOf<String, Any?>(
            "interventionId" to intervention.id,
            "report" to (intervention.report ?: ""),
            "completedAt" to completedAt,
            "actualTypeIds" to actualTypeIds,
            "actualTypeId" to (actualTypeIds.firstOrNull() ?: ""),
        )
        if (coldMeasures.isNotEmpty()) {
            payload["coldMeasures"] = coldMeasures.map { buildColdMeasurePayload(it) }
        }
        if (measures.isNotEmpty()) {
            payload["measures"] = measures.map { buildMeasurePayload(it) }
        }
        if (pacMeasures.isNotEmpty()) {
            payload["pacMeasures"] = pacMeasures.map { buildPacMeasurePayload(it) }
        }
        if (attestations.isNotEmpty()) {
            payload["attestations"] =
                attestations.map { attestation ->
                    val points =
                        attestationVePointControleDao.getByAttestationOnce(
                            attestation.interventionId,
                            attestation.equipmentOrder,
                            attestation.type,
                        )
                    buildAttestationPayload(attestation, points)
                }
        }
        when (
            val invoiceBuilt =
                invoiceRepository.buildInvoicePayloadForClosure(
                    interventionId = intervention.id,
                    unitId = intervention.unitId,
                    technicianId = techId,
                )
        ) {
            is SubmitInvoiceFullPayloadResult.Ok ->
                payload["invoice"] = invoiceBuilt.payload
            is SubmitInvoiceFullPayloadResult.HamonMissing ->
                error(
                    "Signature Hamon introuvable — repassez par la signature du devis avant de clôturer.",
                )
            SubmitInvoiceFullPayloadResult.InvoiceNotFound -> Unit
        }
        return PushOperationDto(
            id = "op-close-${intervention.id}",
            type = "CLOSE_INTERVENTION",
            occurredAt = completedAt,
            payload = payload,
        )
    }

    private suspend fun applyInvoiceCloseResult(
        interventionId: String,
        data: Map<String, Any?>?,
    ) {
        if (data == null) return
        val localInvoice = invoiceDao.getByInterventionId(interventionId) ?: return
        val serverId = data["invoiceId"] as? String
        val serverNumber = data["invoiceNumber"] as? String
        val serverStatus = data["invoiceStatus"] as? String
        if (serverId != null) {
            if (serverId != localInvoice.id) {
                invoiceLineDao.reassignToInvoice(localInvoice.id, serverId)
            }
            invoiceDao.updateServerData(
                localId = localInvoice.id,
                serverId = serverId,
                number = serverNumber,
                status = serverStatus ?: localInvoice.status,
                acceptedAt = data["acceptedAt"] as? String,
                invoicedAt = data["invoicedAt"] as? String,
                paidAt = data["paidAt"] as? String,
                devisSignatureUrl = data["devisSignatureUrl"] as? String,
                hamonSignatureUrl = data["hamonSignatureUrl"] as? String,
                hamonRequested = (data["hamonRequested"] as? Boolean) == true,
            )
        } else {
            invoiceDao.markAsSynced(localInvoice.id)
        }
    }

    private fun buildColdMeasurePayload(measure: ColdMeasureEntity): Map<String, Any?> =
        buildMap {
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

    private fun buildPacMeasurePayload(
        m: PacMeasureEntity,
    ): Map<String, Any?> = buildMap {
        put("interventionId", m.interventionId)
        put("equipmentOrder", m.equipmentOrder)
        put("pacVentilation", m.pacVentilation.ifBlank { null })
        put("pacNetail", m.pacNetail.ifBlank { null })
        put("pacVerail", m.pacVerail.ifBlank { null })
        put("pacFiltre", m.pacFiltre.ifBlank { null })
        put("pacFuite", m.pacFuite.ifBlank { null })
        put("pacEvac", m.pacEvac.ifBlank { null })
        put("pacPression1", m.pacPression1.ifBlank { null })
        put("pacPression2", m.pacPression2.toDoubleOrNull())
        put("pacGlycol1", m.pacGlycol1.ifBlank { null })
        put("pacGlycol2", m.pacGlycol2.toDoubleOrNull())
        put("pacTenStat", m.pacTenStat.toDoubleOrNull())
        put("pacTenDyna", m.pacTenDyna.toDoubleOrNull())
        put("pacIntensite", m.pacIntensite.toDoubleOrNull())
        put("pacResserage1", m.pacResserage1.ifBlank { null })
        put("pacResserage2", m.pacResserage2.ifBlank { null })
        put("pacInterieure", m.pacInterieure.toDoubleOrNull())
        put("pacExterieure", m.pacExterieure.toDoubleOrNull())
        put("pacDepart", m.pacDepart.toDoubleOrNull())
        put("pacRetour", m.pacRetour.toDoubleOrNull())
        put("pacDeltaT", m.pacDeltaT.toDoubleOrNull())
        put("pacHiver", m.pacHiver.toDoubleOrNull())
        put("pacAppoint", m.pacAppoint.toDoubleOrNull())
        put("pacConfort", m.pacConfort.toDoubleOrNull())
        put("pacNonChauf", m.pacNonChauf.toDoubleOrNull())
        put("pacEcsConsigne", m.pacEcsConsigne.toDoubleOrNull())
        put("pacEcs", m.pacEcs.toDoubleOrNull())
        put("pacManometreBp", m.pacManometreBp.toDoubleOrNull())
        put("pacManometreHp", m.pacManometreHp.toDoubleOrNull())
        put("pacDegivrage", m.pacDegivrage.ifBlank { null })
        put("pacInversion", m.pacInversion.ifBlank { null })
        put("pacHFonct", m.pacHFonct.toIntOrNull())
        put("pacHComp1", m.pacHComp1.toIntOrNull())
        put("pacHVenti", m.pacHVenti.toIntOrNull())
        put("pacNbDemarr", m.pacNbDemarr.toIntOrNull())
        put("pacHAppoint1", m.pacHAppoint1.toIntOrNull())
        put("pacHAppoint2", m.pacHAppoint2.toIntOrNull())
        put("pacAlarme1", m.pacAlarme1.ifBlank { null })
        put("pacAlarme2", m.pacAlarme2.ifBlank { null })
        put("pacBlocage1", m.pacBlocage1.ifBlank { null })
        put("pacBlocage2", m.pacBlocage2.ifBlank { null })
        put("pacReleve", m.pacReleve.toDoubleOrNull())
        put("pacRem1", m.pacRem1.ifBlank { null })
    }

    private fun buildMeasurePayload(
        measure: MeasureEntity,
    ): Map<String, Any?> = buildMap {
        put("interventionId", measure.interventionId)
        put("equipmentOrder", measure.equipmentOrder)
        put("co", measure.co)
        put("coamb", measure.coamb)
        put("co2", measure.co2)
        put("o2", measure.o2)
        put("tair", measure.tair)
        put("temfu", measure.temfu)
        put("rend", measure.rend)
        put("nox", measure.nox)
        put("eta", measure.eta)
        put("thpa", measure.thpa)
        put("no", measure.no)
        put("no2", measure.no2)
        put("o2ven", measure.o2ven)
        put("condilu", measure.condilu)
        put("tgaz", measure.tgaz)
        put("ta", measure.ta)
        put("pregas", measure.pregas)
        put("prega", measure.prega)
        put("pregn", measure.pregn)
        put("pregm", measure.pregm)
        put("puisgaz", measure.puisgaz)
        put("debga", measure.debga)
        put("temec", measure.temec)
        put("temef", measure.temef)
        put("delta", measure.delta)
        put("debio", measure.debio)
        put("debfuel", measure.debfuel)
        put("prefp", measure.prefp)
        put("puisfuel", measure.puisfuel)
        put("pulve", measure.pulve)
        put("spot", measure.spot)
        put("testdsc", measure.testdsc)
        put("remplacond", measure.remplacond)
        put("templagigleur", measure.templagigleur)
        put("remplapoly", measure.remplapoly)
        put("etaventil", measure.etaventil)
        put("ctranode", measure.ctranode)
        put("ctrextvmc", measure.ctrextvmc)
        put("suie1", measure.suie1)
        put("suie2", measure.suie2)
        put("suie3", measure.suie3)
        put("residhuil", measure.residhuil)
        put("opaci", measure.opaci)
        put("ionis", measure.ionis)
        put("pgevg", measure.pgevg)
        put("pgepg", measure.pgepg)
        put("depre", measure.depre)
        put("depr2", measure.depr2)
        put("gican", measure.gican)
        put("gicle", measure.gicle)
        put("pabs", measure.pabs)
        put("perte", measure.perte)
        put("ppm", measure.ppm)
        put("obser", measure.obser)
    }

    private fun buildAttestationPayload(
        a: AttestationVeEntity,
        points: List<AttestationVePointControleEntity>,
    ): Map<String, Any?> = buildMap {
        put("interventionId", a.interventionId)
        put("equipmentOrder", a.equipmentOrder)
        put("type", a.type)
        put("appareilMesure", a.appareilMesure.ifBlank { null })
        put("appareilMesureTension", a.appareilMesureTension.ifBlank { null })
        put("appareilMesureGenerateur", a.appareilMesureGenerateur.ifBlank { null })
        put("defautsCorriges", a.defautsCorriges.ifBlank { null })
        put("recommandationUsage", a.recommandationUsage.ifBlank { null })
        put("recommandationAmeliorations", a.recommandationAmeliorations.ifBlank { null })
        put("recommandationRemplacement", a.recommandationRemplacement.ifBlank { null })
        put("commentaire", a.commentaire.ifBlank { null })
        put("nomPersonnePresente", a.nomPersonnePresente.ifBlank { null })
        put("remarquesHydraulique", a.remarquesHydraulique.ifBlank { null })
        put("remarquesRegulation", a.remarquesRegulation.ifBlank { null })
        put("remarquesGenerateur", a.remarquesGenerateur.ifBlank { null })
        put("co", a.co.toDoubleOrNull())
        put("coConduitPpm", a.coConduitPpm)
        put("tempFumees", a.tempFumees.toDoubleOrNull())
        put("tempAmbiante", a.tempAmbiante.toDoubleOrNull())
        put("co2Fumees", a.co2Fumees.toDoubleOrNull())
        put("o2Fumees", a.o2Fumees.toDoubleOrNull())
        put("rendementEvalue", a.rendementEvalue.toDoubleOrNull())
        put("noxEmissions", a.noxEmissions.toDoubleOrNull())
        put("classeEnergetique", a.classeEnergetique.ifBlank { null })
        put("indiceNoircissement", a.indiceNoircissement.toDoubleOrNull())
        put("pressionGicleur", a.pressionGicleur.toDoubleOrNull())
        put("emissionsPoussieres", a.emissionsPoussieres.toDoubleOrNull())
        put("emissionsCov", a.emissionsCov.toDoubleOrNull())
        put("tExterieurChauf", a.tExterieurChauf.toDoubleOrNull())
        put("tExterieurRefroid", a.tExterieurRefroid.toDoubleOrNull())
        put("tInterieurChauf", a.tInterieurChauf.toDoubleOrNull())
        put("tInterieurRefroid", a.tInterieurRefroid.toDoubleOrNull())
        put("tensionStatique", a.tensionStatique.toDoubleOrNull())
        put("tensionDynamique", a.tensionDynamique.toDoubleOrNull())
        put("fluideRef", a.fluideRef.ifBlank { null })
        put("chargeTotale", a.chargeTotale.toDoubleOrNull())
        put("pressionBp", a.pressionBp.toDoubleOrNull())
        put("pressionHp", a.pressionHp.toDoubleOrNull())
        put("bruleurMarque", a.bruleurMarque)
        put("bruleurModele", a.bruleurModele)
        put("bruleurSerialNumber", a.bruleurSerialNumber)
        put("bruleurCommissioningDate", a.bruleurCommissioningDate)
        put("bruleurPuissanceKw", a.bruleurPuissanceKw)
        put("bruleurEquipmentOrder", a.bruleurEquipmentOrder)
        put(
            "points",
            points.map { p ->
                mapOf("cle" to p.cle, "resultat" to p.resultat)
            },
        )
    }

    private suspend fun applyPushResults(
        ctx: PushApplyContext,
        results: List<PushResultDto>,
        tier: Int,
    ) {
        results.forEach { result ->
            when (result.status) {
                "ok" -> applyOkPushResult(ctx, result)
                "skipped" -> if (tier == 3) applySkippedPushResult(result)
                else -> Unit
            }
        }
    }

    private suspend fun applyOkPushResult(
        ctx: PushApplyContext,
        result: PushResultDto,
    ) {
        val pendingOp = ctx.pendingOps.find { it.id == result.operationId }
        if (pendingOp?.type == "CREATE_EQUIPMENT" || pendingOp?.type == "REPLACE_EQUIPMENT") {
            remapEquipmentIdFromPushResult(result, pendingOp)
        }
        pendingOp?.let {
            if (it.type == "CREATE_ANOMALY") {
                anomalyDraftRepository.markSynced(it.id)
            }
            pendingOperationDao.updateStatus(it.id, "sent")
        }
        ctx.dirtyColdMeasures.find { it.id == result.operationId }?.let {
            coldMeasureRepository.markClean(it.id)
        }
        ctx.dirtyMeasures.find {
            result.operationId == it.interventionId + "_measure_" + it.equipmentOrder
        }?.let {
            measureRepository.markClean(it.interventionId, it.equipmentOrder)
        }
        ctx.dirtyPacMeasures.find {
            result.operationId == it.interventionId + "_pac_measure_" + it.equipmentOrder
        }?.let {
            pacMeasureRepository.markClean(it.interventionId, it.equipmentOrder)
        }
        ctx.dirtyAttestations.find {
            result.operationId ==
                it.interventionId + "_attestation_" + it.equipmentOrder + "_" + it.type
        }?.let {
            attestationVeRepository.markClean(it.interventionId, it.equipmentOrder, it.type)
        }

        when {
            result.operationId.startsWith("op-close-") -> {
                val interventionId = result.operationId.removePrefix("op-close-")
                interventionDao.markAsSynced(interventionId)
                ctx.dirtyColdMeasures
                    .filter { it.interventionId == interventionId }
                    .forEach { coldMeasureRepository.markClean(it.id) }
                ctx.dirtyMeasures
                    .filter { it.interventionId == interventionId }
                    .forEach {
                        measureRepository.markClean(it.interventionId, it.equipmentOrder)
                    }
                ctx.dirtyPacMeasures
                    .filter { it.interventionId == interventionId }
                    .forEach {
                        pacMeasureRepository.markClean(it.interventionId, it.equipmentOrder)
                    }
                ctx.dirtyAttestations
                    .filter { it.interventionId == interventionId }
                    .forEach {
                        attestationVeRepository.markClean(
                            it.interventionId,
                            it.equipmentOrder,
                            it.type,
                        )
                    }
                applyInvoiceCloseResult(interventionId, result.resultPayload())
            }
            result.operationId.startsWith("op-complete-") -> {
                val interventionId = result.operationId.removePrefix("op-complete-")
                interventionDao.markAsSynced(interventionId)
            }
            result.operationId.startsWith("op-update-") -> {
                val updateId = result.operationId.removePrefix("op-update-")
                pendingUpdateDao.markAsSynced(updateId)
            }
            result.operationId.startsWith("op-update-invoice-") -> {
                val targetInvoiceId =
                    ctx.pendingUpdates.find { it.id == result.operationId }?.targetId
                result.resultPayload()?.let { data ->
                    val serverStatus = data["invoiceStatus"] as? String
                    val serverId = data["invoiceId"] as? String
                    val resolvedId = targetInvoiceId ?: serverId
                    if (serverStatus != null && resolvedId != null) {
                        invoiceDao.getById(resolvedId)?.let { inv ->
                            invoiceDao.updateLifecycle(
                                id = inv.id,
                                status = serverStatus,
                                acceptedAt = data["acceptedAt"] as? String ?: inv.acceptedAt,
                                invoicedAt = data["invoicedAt"] as? String ?: inv.invoicedAt,
                                paidAt = data["paidAt"] as? String ?: inv.paidAt,
                                devisSignatureUrl =
                                    data["devisSignatureUrl"] as? String ?: inv.devisSignatureUrl,
                                hamonSignatureUrl =
                                    data["hamonSignatureUrl"] as? String ?: inv.hamonSignatureUrl,
                                hamonRequested =
                                    (data["hamonRequested"] as? Boolean) ?: inv.hamonRequested,
                                updatedAt = Instant.now().toString(),
                            )
                        }
                    }
                }
                pendingUpdateDao.markAsSynced(result.operationId)
            }
        }
    }

    private suspend fun applyAllPushResults(
        ctx: PushApplyContext,
        tierOutcomes: List<TierPushOutcome>,
    ) {
        tierOutcomes.forEach { outcome ->
            applyPushResults(ctx, outcome.results, outcome.tier)
        }
    }

    private suspend fun ensureCreateEquipmentPendingOps(
        pendingInterventions: List<InterventionEntity>,
    ) {
        val gson = Gson()
        val now = Instant.now().toString()
        for (intervention in pendingInterventions) {
            val interventionId = intervention.id
            val awaitingServerSync = intervention.syncStatus == "COMPLETED"
            val equipments = equipmentDao.getEquipmentsByInterventionOnce(interventionId)
            for (equipment in equipments) {
                val existingCreate =
                    pendingOperationDao.getByIdAndType(equipment.id, "CREATE_EQUIPMENT")
                val existingReplace =
                    pendingOperationDao.getByIdAndType(equipment.id, "REPLACE_EQUIPMENT")

                if (
                    PushCreateEquipmentRepair.shouldRequeueSentEquipmentOp(
                        existingCreate?.status,
                        awaitingServerSync,
                    )
                ) {
                    pendingOperationDao.updateStatus(existingCreate!!.id, "pending")
                    android.util.Log.i(
                        LOG_TAG,
                        "re-queue CREATE_EQUIPMENT equipmentId=${equipment.id} intervention=$interventionId",
                    )
                }
                if (
                    PushCreateEquipmentRepair.shouldRequeueSentEquipmentOp(
                        existingReplace?.status,
                        awaitingServerSync,
                    )
                ) {
                    pendingOperationDao.updateStatus(existingReplace!!.id, "pending")
                    android.util.Log.i(
                        LOG_TAG,
                        "re-queue REPLACE_EQUIPMENT equipmentId=${equipment.id} intervention=$interventionId",
                    )
                }

                val hasCreatePending =
                    PushCreateEquipmentRepair.isPendingOpStatus(existingCreate?.status.orEmpty())
                val hasReplacePending =
                    PushCreateEquipmentRepair.isPendingOpStatus(existingReplace?.status.orEmpty())
                val hasCreateOrReplaceAnyStatus =
                    existingCreate != null || existingReplace != null
                if (
                    !PushCreateEquipmentRepair.shouldRepairCreateEquipment(
                        equipment.order,
                        hasCreatePending,
                        hasReplacePending,
                        hasCreateOrReplaceAnyStatus,
                    )
                ) {
                    continue
                }
                val op = PendingOperationEntity(
                    id = equipment.id,
                    type = "CREATE_EQUIPMENT",
                    payload = gson.toJson(PushCreateEquipmentRepair.buildPayloadFromEntity(equipment)),
                    occurredAt = now,
                    interventionId = interventionId,
                    status = "pending",
                    createdAt = now,
                )
                pendingOperationDao.insert(op)
                android.util.Log.i(
                    LOG_TAG,
                    "réparation CREATE_EQUIPMENT manquant equipmentId=${equipment.id} intervention=$interventionId",
                )
            }
        }
    }

    private fun logPushDiagnostics(
        operations: List<PushOperationDto>,
        tiers: List<Pair<Int, List<PushOperationDto>>>,
    ) {
        operations.filter { it.type == "CLOSE_INTERVENTION" }.forEach { closeOp ->
            @Suppress("UNCHECKED_CAST")
            val coldMeasures =
                closeOp.payload["coldMeasures"] as? List<Map<String, Any?>> ?: emptyList()
            val equipmentIds =
                coldMeasures.mapNotNull { row -> row["equipmentId"] as? String }
            android.util.Log.i(
                LOG_TAG,
                "diagnostic CLOSE id=${closeOp.id} coldMeasures=${coldMeasures.size} " +
                    "equipmentIds=$equipmentIds",
            )
        }
        tiers.firstOrNull { it.first == 1 }?.second?.let { tier1Ops ->
            val createIds =
                tier1Ops
                    .filter { it.type == "CREATE_EQUIPMENT" }
                    .map { it.id }
            android.util.Log.i(
                LOG_TAG,
                "diagnostic vague P1: CREATE_EQUIPMENT count=${createIds.size} ids=$createIds",
            )
        }
    }

    private suspend fun refreshCloseOperationsForClosureTier(
        tierOps: List<PushOperationDto>,
        pendingInterventions: List<InterventionEntity>,
        pushableInterventionIds: Set<String>,
    ): List<PushOperationDto> {
        val withoutClose = tierOps.filter { it.type != "CLOSE_INTERVENTION" }
        val dirtyColdMeasures =
            coldMeasureRepository.getDirty().filter { it.interventionId in pushableInterventionIds }
        val dirtyMeasures =
            measureRepository.getDirty().filter { it.interventionId in pushableInterventionIds }
        val dirtyPacMeasures =
            pacMeasureRepository.getDirty().filter { it.interventionId in pushableInterventionIds }
        val dirtyAttestations =
            attestationVeRepository.getDirty().filter { it.interventionId in pushableInterventionIds }
        val closeOps = pendingInterventions.mapNotNull { intervention ->
            val completedAt = intervention.completedAt ?: return@mapNotNull null
            buildCloseInterventionOperation(
                intervention = intervention,
                completedAt = completedAt,
                coldMeasures = dirtyColdMeasures.filter { it.interventionId == intervention.id },
                measures = dirtyMeasures.filter { it.interventionId == intervention.id },
                pacMeasures = dirtyPacMeasures.filter { it.interventionId == intervention.id },
                attestations = dirtyAttestations.filter { it.interventionId == intervention.id },
            )
        }
        return withoutClose + closeOps
    }

    private suspend fun applyEquipmentIdRemapsFromTierResults(
        results: List<PushResultDto>,
        pendingOps: List<PendingOperationEntity>,
    ) {
        results.filter { it.status == "ok" }.forEach { result ->
            val pendingOp = pendingOps.find { it.id == result.operationId } ?: return@forEach
            if (pendingOp.type == "CREATE_EQUIPMENT" || pendingOp.type == "REPLACE_EQUIPMENT") {
                remapEquipmentIdFromPushResult(result, pendingOp)
            }
        }
    }

    private suspend fun remapEquipmentIdFromPushResult(
        result: PushResultDto,
        pendingOp: PendingOperationEntity,
    ) {
        val data = result.resultPayload() ?: return
        val serverId =
            (data["equipmentId"] as? String)?.takeIf { it.isNotBlank() }
                ?: (data["id"] as? String)?.takeIf { it.isNotBlank() }
                ?: return
        val localId = pendingOp.id
        if (serverId == localId) return
        val interventionId = pendingOp.interventionId
        equipmentDao.updateEquipmentId(interventionId, localId, serverId)
        coldMeasureDao.updateEquipmentId(interventionId, localId, serverId)
        remapEquipmentIdInPendingPayloads(interventionId, localId, serverId)
        android.util.Log.i(
            LOG_TAG,
            "equipmentId remappé $localId → $serverId (intervention=$interventionId)",
        )
    }

    private suspend fun remapEquipmentIdInPendingPayloads(
        interventionId: String,
        oldId: String,
        newId: String,
    ) {
        val gson = Gson()
        pendingOperationDao.getPendingByInterventionIdOnce(interventionId).forEach { op ->
            @Suppress("UNCHECKED_CAST")
            val map =
                gson.fromJson(op.payload, Map::class.java) as? Map<String, Any?> ?: return@forEach
            val mutable = map.toMutableMap()
            var changed = false
            if (mutable["equipmentId"] == oldId) {
                mutable["equipmentId"] = newId
                changed = true
            }
            if (mutable["parentEquipmentId"] == oldId) {
                mutable["parentEquipmentId"] = newId
                changed = true
            }
            if (changed) {
                pendingOperationDao.updatePayload(op.id, gson.toJson(mutable))
            }
        }
    }

    private fun findClosurePushFailure(results: List<PushResultDto>): PushResultDto? =
        results.firstOrNull { PushOperationOrdering.isClosurePushFailureStatus(it.status) }

    private suspend fun applySkippedPushResult(result: PushResultDto) {
        val interventionId =
            conflictInterventionId(result)
                ?: (result.resultPayload()?.get("interventionId") as? String)
                ?: return
        interventionDao.setSyncStatus(interventionId, "SKIPPED")
        android.util.Log.w(
            LOG_TAG,
            "op ${result.operationId} skipped → intervention $interventionId marquée SKIPPED",
        )
    }

    /** Attache `clientKnownVersion` et simule les bumps serveur entre vagues ordonnées. */
    private suspend fun attachClientKnownVersions(
        ops: List<PushOperationDto>,
        versionByIntervention: MutableMap<String, Int>,
    ): List<PushOperationDto> =
        ops.map { op ->
            if (op.type !in VERSIONED_PUSH_TYPES) return@map op
            val interventionId = op.payload["interventionId"] as? String ?: return@map op
            val version = versionByIntervention.getOrPut(interventionId) {
                interventionDao.getInterventionByIdOnce(interventionId)?.version?.coerceAtLeast(1) ?: 1
            }
            versionByIntervention[interventionId] = version + 1
            op.copy(clientKnownVersion = version)
        }

    private suspend fun applyAutoResolvableConflicts(results: List<PushResultDto>): Set<String> {
        val ids = mutableSetOf<String>()
        results.forEach { result ->
            val interventionId = conflictInterventionId(result) ?: return@forEach
            when {
                result.status == "rejected" && result.reason == "IMMUTABLE" -> {
                    interventionDao.setSyncStatus(interventionId, "CONFLICT_IMMUTABLE")
                    interventionDao.markLocalChanges(interventionId, false)
                    ids.add(interventionId)
                    val attempts =
                        interventionDao.getInterventionByIdOnce(interventionId)?.conflictResolveAttempts ?: 0
                    re.melchior.saviomobile.observability.SavioSyncSentry.onConflictImmutable(
                        interventionId,
                        attempts,
                    )
                    android.util.Log.w(
                        LOG_TAG,
                        "CONFLICT_IMMUTABLE intervention=$interventionId",
                    )
                }

                result.status == "conflict" &&
                    (result.conflictType == "VERSION_MISMATCH" || result.reason == "VERSION_MISMATCH") -> {
                    interventionDao.setSyncStatus(interventionId, "CONFLICT_VERSION")
                    interventionDao.markLocalChanges(interventionId, false)
                    ids.add(interventionId)
                    val clientVersion =
                        (result.serverData?.get("version") as? Number)?.toInt()
                            ?: interventionDao.getInterventionByIdOnce(interventionId)?.version
                    re.melchior.saviomobile.observability.SavioSyncSentry.onConflictVersion(
                        interventionId,
                        clientVersion,
                    )
                    android.util.Log.w(
                        LOG_TAG,
                        "CONFLICT_VERSION intervention=$interventionId",
                    )
                }

                result.status == "conflict" -> {
                    interventionDao.markAsConflict(interventionId)
                    if (result.operationId.startsWith("op-complete-")) {
                        _conflictEvents.emit(
                            ConflictEvent(
                                interventionId = interventionId,
                                conflictType = result.conflictType ?: "UNKNOWN",
                                message = result.message
                                    ?: result.reason
                                    ?: "Conflit détecté.",
                            ),
                        )
                    }
                }
            }
        }
        return ids
    }

    private fun conflictInterventionId(result: PushResultDto): String? {
        return when {
            result.operationId.startsWith("op-complete-") ->
                result.operationId.removePrefix("op-complete-")
            result.operationId.startsWith("op-start-") ->
                result.operationId.removePrefix("op-start-")
            result.status == "rejected" || result.status == "conflict" ->
                result.serverData?.get("interventionId") as? String
            else -> result.serverData?.get("interventionId") as? String
        }
    }
}
