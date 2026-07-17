package re.savio.mobile.worker

import android.content.Context
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
import re.savio.mobile.data.local.database.TokenDataStore
import re.savio.mobile.data.repository.PendingInterventionRepository
import java.io.IOException

@HiltWorker
class PendingInterventionSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val pendingInterventionRepository: PendingInterventionRepository,
    private val tokenDataStore: TokenDataStore,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val slug = tokenDataStore.societeSlug.first()
            val token = tokenDataStore.accessToken.first()
            if (slug.isNullOrBlank() || token.isNullOrBlank()) {
                return Result.success()
            }
            pendingInterventionRepository.syncPendingQueue()
            Result.success()
        } catch (e: IOException) {
            Result.retry()
        } catch (e: Exception) {
            if (runAttemptCount < 5) Result.retry()
            else Result.failure()
        }
    }

    companion object {
        const val UNIQUE_WORK_NAME = "pending_intervention_sync"

        fun enqueue(workManager: WorkManager) {
            val request = OneTimeWorkRequestBuilder<PendingInterventionSyncWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build(),
                )
                .build()
            workManager.enqueueUniqueWork(
                UNIQUE_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                request,
            )
        }
    }
}
