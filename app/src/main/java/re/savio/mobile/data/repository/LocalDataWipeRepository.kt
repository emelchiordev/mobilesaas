package re.savio.mobile.data.repository

import android.content.Context
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import re.savio.mobile.data.local.database.SavioDatabase
import re.savio.mobile.data.local.objectbox.ObjectBoxStore
import re.savio.mobile.worker.CatalogSyncWorker
import re.savio.mobile.worker.PendingInterventionSyncWorker
import re.savio.mobile.worker.SyncWorker
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Full local wipe for account / société switch. Idempotent: safe to run twice.
 * Does not touch account fingerprint or switch-state prefs.
 */
@Singleton
class LocalDataWipeRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: SavioDatabase,
) {

    suspend fun wipeAllLocalData() = withContext(Dispatchers.IO) {
        cancelSyncWorkers()
        deleteLocalFiles()
        database.clearAllTables()
        clearObjectBox()
        clearSyncPrefs()
    }

    private fun cancelSyncWorkers() {
        val wm = WorkManager.getInstance(context)
        wm.cancelUniqueWork(SyncWorker.WORK_NAME)
        wm.cancelUniqueWork(SyncWorker.ONE_TIME_WORK_NAME)
        wm.cancelUniqueWork(PendingInterventionSyncWorker.UNIQUE_WORK_NAME)
        wm.cancelUniqueWork(CatalogSyncWorker.WORK_NAME)
    }

    private fun deleteLocalFiles() {
        val filesDir = context.filesDir
        File(filesDir, "intervention_photos").deleteRecursively()
        File(filesDir, "signatures").deleteRecursively()
        File(filesDir, "client_documents").deleteRecursively()
        File(filesDir, "pac_fiches").deleteRecursively()
        filesDir.listFiles()
            ?.filter { it.isFile && it.name.startsWith("cerfa_") && it.name.endsWith(".pdf") }
            ?.forEach { it.delete() }
    }

    private fun clearObjectBox() {
        runCatching {
            ObjectBoxStore.articlesBox().removeAll()
            ObjectBoxStore.refrigerantContainersBox().removeAll()
        }
    }

    private fun clearSyncPrefs() {
        listOf("catalog_sync", "tenant_articles_sync", "refrigerant_containers_sync").forEach { name ->
            context.getSharedPreferences(name, Context.MODE_PRIVATE).edit().clear().commit()
        }
    }
}
