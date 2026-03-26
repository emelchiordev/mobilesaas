
package re.melchior.saviomobile.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import re.melchior.saviomobile.data.local.database.TokenDataStore
import re.melchior.saviomobile.data.repository.PushRepository
import re.melchior.saviomobile.data.repository.PushResult
import re.melchior.saviomobile.data.repository.SyncRepository
import java.time.LocalDate
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val pushRepository: PushRepository,
    private val tokenDataStore: TokenDataStore
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val slug = tokenDataStore.societeSlug.first()
            if (slug.isNullOrBlank()) {
                android.util.Log.w("SyncWorker", "Pas de slug — sync annulée")
                return Result.success()
            }

            val token = tokenDataStore.accessToken.first()
            if (token.isNullOrBlank()) {
                android.util.Log.w("SyncWorker", "Pas de token — sync annulée")
                return Result.success()
            }

            // Push uniquement
            val pushResult = pushRepository.push()
            android.util.Log.d("SyncWorker", "Push result: $pushResult")

            when (pushResult) {
                is PushResult.Error -> Result.retry()
                else -> Result.success()
            }

        } catch (e: Exception) {
            android.util.Log.e("SyncWorker", "Sync error: ${e.message}", e)
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "SavioSyncWorker"
    }
}