package re.melchior.saviomobile.data.repository

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import io.objectbox.query.QueryBuilder.StringOrder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import re.melchior.saviomobile.data.local.objectbox.ObjectBoxStore
import re.melchior.saviomobile.data.local.objectbox.TenantArticleBox
import re.melchior.saviomobile.data.local.objectbox.TenantArticleBox_
import re.melchior.saviomobile.data.remote.api.TenantArticleApi
import re.melchior.saviomobile.data.remote.dto.toBoxEntity
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TenantArticleSyncRepository @Inject constructor(
    private val api: TenantArticleApi,
    @ApplicationContext context: Context,
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "tenant_articles_sync"
        private const val KEY_LAST_SYNC = "tenant_articles_last_sync"
        private const val SYNC_INTERVAL_MS = 24 * 60 * 60 * 1000L
        private const val TAG = "TenantArticleSync"
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
                val box = ObjectBoxStore.articlesBox()
                val existingById = box.all.associateBy { it.id }
                val entities = response.articles.map { dto ->
                    dto.toBoxEntity(existingById[dto.id])
                }
                box.put(entities)
                prefs.edit()
                    .putString(KEY_LAST_SYNC, response.syncedAt)
                    .apply()
                Log.d(TAG, "Sync OK: ${entities.size} article(s), total tenant=${response.total}")
                true
            } catch (e: Exception) {
                Log.e(TAG, "Sync failed", e)
                false
            }
        }
    }

    fun search(q: String, limit: Int = 20): List<TenantArticleBox> {
        if (q.length < 2) return emptyList()
        val box = ObjectBoxStore.articlesBox()
        val trimmed = q.trim()
        return box.query(
            TenantArticleBox_.actif.equal(true)
                .and(
                    TenantArticleBox_.designation.contains(trimmed, StringOrder.CASE_INSENSITIVE)
                        .or(TenantArticleBox_.reference.contains(trimmed, StringOrder.CASE_INSENSITIVE))
                        .or(
                            TenantArticleBox_.referenceInterne.contains(
                                trimmed,
                                StringOrder.CASE_INSENSITIVE,
                            ),
                        ),
                ),
        )
            .build()
            .find(0, limit.toLong())
    }
}
