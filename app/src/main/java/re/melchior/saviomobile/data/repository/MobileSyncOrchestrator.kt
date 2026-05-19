package re.melchior.saviomobile.data.repository

import android.util.Log
import io.sentry.Sentry
import io.sentry.SpanStatus
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import re.melchior.saviomobile.data.local.dao.InterventionDao
import re.melchior.saviomobile.observability.SavioSyncSentry
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

data class MobileSyncRunResult(
    val pushResult: PushResult,
    val pullResult: SyncResult?,
)

@Singleton
class MobileSyncOrchestrator @Inject constructor(
    private val pendingClientRepository: PendingClientRepository,
    private val pendingInterventionRepository: PendingInterventionRepository,
    private val pushRepository: PushRepository,
    private val photoSyncRepository: PhotoSyncRepository,
    private val syncRepository: SyncRepository,
    private val interventionDao: InterventionDao,
    private val tenantArticleSyncRepository: TenantArticleSyncRepository,
) {

    /**
     * Pipeline séquentiel :
     * 1 clients pending → 2 interventions offline → 3 push batch
     * → 4 photos → 5 signatures → 6 pull
     */
    suspend fun runFullSync(
        pullDate: LocalDate = LocalDate.now(),
        pullForce: Boolean = false,
        onPushConflict: suspend (ConflictEvent) -> Unit = {},
    ): MobileSyncRunResult {
        SavioSyncSentry.addPipelineBreadcrumb("sync.pipeline.start")
        val transaction = Sentry.startTransaction("sync.pipeline", "sync")
        var pushResult: PushResult = PushResult.Error("sync interrompu")
        var pullResult: SyncResult? = null
        try {
            runPipelineStep(transaction, "sync.clients_pending", "sync.step.clients_pending") {
                pendingClientRepository.syncPendingClients()
            }

            runPipelineStep(transaction, "sync.interventions_pending", "sync.step.interventions_pending") {
                pendingInterventionRepository.syncPendingQueue()
            }

            pushResult = runPipelineStepResult(transaction, "sync.push", "sync.step.push") {
                runStepPush(onPushConflict)
            } ?: PushResult.Error("Push interrompu")

            val skipMedia = pushResult is PushResult.Error
            if (!skipMedia) {
                runPipelineStep(transaction, "sync.photos", "sync.step.photos") {
                    photoSyncRepository.uploadPendingPhotos("orchestrateur")
                }
                runPipelineStep(transaction, "sync.signatures", "sync.step.signatures") {
                    photoSyncRepository.uploadPendingSignatures("orchestrateur")
                }
                runPipelineStep(transaction, "sync.photos_delete", "sync.step.photos") {
                    photoSyncRepository.deletePendingPhotos()
                }
            } else {
                Log.w(TAG, "Étapes médias ignorées (push en erreur)")
            }

            if (pushResult is PushResult.Success && pushResult.conflictInterventionIds.isNotEmpty()) {
                resolvePushConflicts(pullDate, pushResult.conflictInterventionIds)
            }

            val forceFinalPull =
                pullForce ||
                    (pushResult is PushResult.Success && pushResult.conflictInterventionIds.isNotEmpty())
            pullResult =
                runPipelineStepResult(transaction, "sync.pull", "sync.step.pull") {
                    syncRepository.pull(pullDate, force = forceFinalPull)
                }

            runPipelineStep(transaction, "sync.tenant_articles", "sync.step.tenant_articles") {
                tenantArticleSyncRepository.syncIfNeeded()
            }

            SavioSyncSentry.addPipelineBreadcrumb("sync.pipeline.complete")
        } catch (e: Exception) {
            transaction.throwable = e
            transaction.status = SpanStatus.INTERNAL_ERROR
            Sentry.captureException(e)
            throw e
        } finally {
            transaction.finish()
        }
        return MobileSyncRunResult(pushResult = pushResult, pullResult = pullResult)
    }

    private suspend fun resolvePushConflicts(
        pullDate: LocalDate,
        interventionIds: Set<String>,
    ) {
        if (interventionIds.isEmpty()) return
        Log.i(TAG, "Résolution auto conflits push: ${interventionIds.size} intervention(s)")
        repeat(MAX_CONFLICT_RESOLVE_ATTEMPTS) { attempt ->
            syncRepository.pull(pullDate, force = true)
            val remaining =
                interventionIds.filter { id ->
                    val row = interventionDao.getInterventionByIdOnce(id)
                    row?.syncStatus == "CONFLICT_IMMUTABLE" || row?.syncStatus == "CONFLICT_VERSION"
                }
            if (remaining.isEmpty()) {
                Log.i(TAG, "Conflits résolus après ${attempt + 1} pull(s)")
                return
            }
            remaining.forEach { id ->
                interventionDao.incrementConflictResolveAttempts(id)
                val row = interventionDao.getInterventionByIdOnce(id)
                if (row != null && row.conflictResolveAttempts >= MAX_CONFLICT_RESOLVE_ATTEMPTS) {
                    SavioSyncSentry.onConflictUnresolved(
                        interventionId = id,
                        syncStatus = row.syncStatus,
                        attempts = row.conflictResolveAttempts,
                    )
                }
            }
            Log.w(TAG, "Conflits restants après pull ${attempt + 1}: $remaining")
        }
    }

    private suspend fun runStepPush(
        onConflict: suspend (ConflictEvent) -> Unit,
    ): PushResult =
        try {
            coroutineScope {
                val conflictJob = launch {
                    pushRepository.conflictEvents.collect { onConflict(it) }
                }
                yield()
                val result = pushRepository.push()
                conflictJob.cancel()
                result
            }
        } catch (e: Exception) {
            Log.e(TAG, "3-push échoué", e)
            PushResult.Error(e.message ?: "Erreur push")
        }

    private suspend fun runPipelineStep(
        transaction: io.sentry.ITransaction,
        spanOp: String,
        breadcrumb: String,
        block: suspend () -> Unit,
    ) {
        val started = System.currentTimeMillis()
        val span = transaction.startChild(spanOp)
        try {
            block()
            span.finish()
            Log.i(TAG, "Étape $breadcrumb OK")
        } catch (e: Exception) {
            span.throwable = e
            span.status = SpanStatus.INTERNAL_ERROR
            span.finish()
            Log.e(TAG, "Étape $breadcrumb échouée (suite du pipeline si possible)", e)
        } finally {
            SavioSyncSentry.addPipelineBreadcrumb(breadcrumb, System.currentTimeMillis() - started)
        }
    }

    private suspend fun <T> runPipelineStepResult(
        transaction: io.sentry.ITransaction,
        spanOp: String,
        breadcrumb: String,
        block: suspend () -> T,
    ): T? {
        val started = System.currentTimeMillis()
        val span = transaction.startChild(spanOp)
        return try {
            block().also {
                span.finish()
                Log.i(TAG, "Étape $breadcrumb OK")
            }
        } catch (e: Exception) {
            span.throwable = e
            span.status = SpanStatus.INTERNAL_ERROR
            span.finish()
            Log.e(TAG, "Étape $breadcrumb échouée", e)
            null
        } finally {
            SavioSyncSentry.addPipelineBreadcrumb(breadcrumb, System.currentTimeMillis() - started)
        }
    }

    private companion object {
        const val TAG = "MobileSync"
        const val MAX_CONFLICT_RESOLVE_ATTEMPTS = 3
    }
}
