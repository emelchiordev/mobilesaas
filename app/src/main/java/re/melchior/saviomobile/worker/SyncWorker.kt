
package re.melchior.saviomobile.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import kotlinx.coroutines.flow.first
import re.melchior.saviomobile.R
import re.melchior.saviomobile.data.local.database.TokenDataStore
import re.melchior.saviomobile.data.repository.ConflictEvent
import re.melchior.saviomobile.data.repository.PhotoSyncRepository
import re.melchior.saviomobile.data.repository.PushRepository
import re.melchior.saviomobile.data.repository.PushResult

private const val SAVIO_PUSH_LOG = "SavioPush"

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val pushRepository: PushRepository,
    private val photoSyncRepository: PhotoSyncRepository, // ← ajouté
    private val tokenDataStore: TokenDataStore
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            android.util.Log.i(SAVIO_PUSH_LOG, "SyncWorker.doWork() démarré")
            val slug = tokenDataStore.societeSlug.first()
            if (slug.isNullOrBlank()) {
                android.util.Log.w(SAVIO_PUSH_LOG, "sync annulée: pas de slug société (tokenDataStore vide ?)")
                android.util.Log.w("SyncWorker", "Pas de slug — sync annulée")
                return Result.success()
            }

            val token = tokenDataStore.accessToken.first()
            if (token.isNullOrBlank()) {
                android.util.Log.w(SAVIO_PUSH_LOG, "sync annulée: pas de jeton JWT (déconnecté ?)")
                android.util.Log.w("SyncWorker", "Pas de token — sync annulée")
                return Result.success()
            }

            android.util.Log.i(SAVIO_PUSH_LOG, "SyncWorker: slug OK, token OK → lancement pushRepository.push()")

            // 1. Push interventions — collecter les conflits en parallèle (SharedFlow)
            val pushResult = coroutineScope {
                val conflictJob = launch {
                    var notified = 0
                    pushRepository.conflictEvents.collect { conflict ->
                        if (notified < 10) {
                            showConflictNotification(conflict)
                            notified++
                        }
                    }
                }
                yield()
                val result = pushRepository.push()
                conflictJob.cancel()
                result
            }
            android.util.Log.i(SAVIO_PUSH_LOG, "SyncWorker: push terminé → $pushResult")
            android.util.Log.d("SyncWorker", "Push result: $pushResult")

            // 2. Upload photos PENDING ← ajouté
            photoSyncRepository.uploadPendingPhotos()
            android.util.Log.d("SyncWorker", "Photos uploadées")

            photoSyncRepository.uploadPendingSignatures()
            android.util.Log.d("SyncWorker", "Signatures uploadées")

            // 3. Suppression photos PENDING_DELETE ← ajouté
            photoSyncRepository.deletePendingPhotos()
            android.util.Log.d("SyncWorker", "Photos supprimées")

            when (pushResult) {
                is PushResult.Error -> Result.retry()
                else -> Result.success()
            }

        } catch (e: Exception) {
            android.util.Log.e(SAVIO_PUSH_LOG, "SyncWorker exception: ${e.message}", e)
            android.util.Log.e("SyncWorker", "Sync error: ${e.message}", e)
            Result.retry()
        }
    }

    private fun showConflictNotification(conflict: ConflictEvent) {
        ensureConflictChannel()
        val notification = NotificationCompat.Builder(
            applicationContext,
            CHANNEL_ID_CONFLICTS
        )
            .setSmallIcon(R.drawable.ic_warning)
            .setContentTitle("Conflit de synchronisation")
            .setContentText(conflict.message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat
            .from(applicationContext)
            .notify(conflict.interventionId.hashCode(), notification)
    }

    private fun ensureConflictChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID_CONFLICTS,
                "Conflits de synchronisation",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alertes lorsque la synchronisation détecte un conflit"
            }
            applicationContext.getSystemService(NotificationManager::class.java)
                ?.createNotificationChannel(channel)
        }
    }

    companion object {
        const val WORK_NAME = "SavioSyncWorker"
        private const val CHANNEL_ID_CONFLICTS = "sync_conflicts"
    }
}