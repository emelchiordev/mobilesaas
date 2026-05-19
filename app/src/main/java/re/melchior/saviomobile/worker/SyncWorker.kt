
package re.melchior.saviomobile.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import re.melchior.saviomobile.R
import re.melchior.saviomobile.data.local.database.TokenDataStore
import re.melchior.saviomobile.data.repository.ConflictEvent
import re.melchior.saviomobile.data.repository.MobileSyncOrchestrator
import re.melchior.saviomobile.data.repository.PushResult
import re.melchior.saviomobile.data.repository.SyncResult

private const val SAVIO_PUSH_LOG = "SavioPush"

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val mobileSyncOrchestrator: MobileSyncOrchestrator,
    private val tokenDataStore: TokenDataStore,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            android.util.Log.i(SAVIO_PUSH_LOG, "SyncWorker.doWork() démarré")
            val slug = tokenDataStore.societeSlug.first()
            if (slug.isNullOrBlank()) {
                android.util.Log.w(SAVIO_PUSH_LOG, "sync annulée: pas de slug société")
                return Result.success()
            }

            val token = tokenDataStore.accessToken.first()
            if (token.isNullOrBlank()) {
                android.util.Log.w(SAVIO_PUSH_LOG, "sync annulée: pas de jeton JWT")
                return Result.success()
            }

            var notifiedConflicts = 0
            val runResult =
                mobileSyncOrchestrator.runFullSync(
                    onPushConflict = { conflict ->
                        if (notifiedConflicts < 10) {
                            showConflictNotification(conflict)
                            notifiedConflicts++
                        }
                    },
                )

            android.util.Log.i(
                SAVIO_PUSH_LOG,
                "SyncWorker terminé push=${runResult.pushResult} pull=${runResult.pullResult}",
            )

            when {
                runResult.pushResult is PushResult.Error -> Result.retry()
                runResult.pullResult is SyncResult.Error -> Result.retry()
                else -> Result.success()
            }
        } catch (e: Exception) {
            android.util.Log.e(SAVIO_PUSH_LOG, "SyncWorker exception: ${e.message}", e)
            Result.retry()
        }
    }

    private fun showConflictNotification(conflict: ConflictEvent) {
        ensureConflictChannel()
        val notification =
            NotificationCompat.Builder(applicationContext, CHANNEL_ID_CONFLICTS)
                .setSmallIcon(R.drawable.ic_warning)
                .setContentTitle("Conflit de synchronisation")
                .setContentText(conflict.message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build()

        NotificationManagerCompat.from(applicationContext)
            .notify(conflict.interventionId.hashCode(), notification)
    }

    private fun ensureConflictChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(
                    CHANNEL_ID_CONFLICTS,
                    "Conflits de synchronisation",
                    NotificationManager.IMPORTANCE_HIGH,
                ).apply {
                    description = "Alertes lorsque la synchronisation détecte un conflit"
                }
            applicationContext.getSystemService(NotificationManager::class.java)
                ?.createNotificationChannel(channel)
        }
    }

    companion object {
        const val WORK_NAME = "SavioSyncWorker"
        const val ONE_TIME_WORK_NAME = "SavioSyncOneTime"
        private const val CHANNEL_ID_CONFLICTS = "sync_conflicts"

        /** Push + photos + signatures ; survit à la navigation (WorkManager). */
        fun enqueueNow(workManager: WorkManager) {
            val request =
                OneTimeWorkRequestBuilder<SyncWorker>()
                    .setConstraints(
                        Constraints.Builder()
                            .setRequiredNetworkType(NetworkType.CONNECTED)
                            .build(),
                    )
                    .build()
            workManager.enqueueUniqueWork(
                ONE_TIME_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                request,
            )
            android.util.Log.i(SAVIO_PUSH_LOG, "SyncWorker one-shot enqueued (REPLACE)")
        }
    }
}
