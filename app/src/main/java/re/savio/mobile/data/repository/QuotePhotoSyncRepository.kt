package re.savio.mobile.data.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import re.savio.mobile.data.local.dao.QuotePhotoDao
import re.savio.mobile.data.local.entity.QuotePhotoEntity
import re.savio.mobile.data.remote.api.InvoiceApi
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuotePhotoSyncRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val quotePhotoDao: QuotePhotoDao,
    private val invoiceApi: InvoiceApi,
) {
    suspend fun addLocalPhoto(invoiceId: String, localPath: String): QuotePhotoEntity =
        withContext(Dispatchers.IO) {
            val entity = QuotePhotoEntity(
                id = UUID.randomUUID().toString(),
                invoiceId = invoiceId,
                localPath = localPath,
            )
            quotePhotoDao.insert(entity)
            entity
        }

    suspend fun syncPending(): Int = withContext(Dispatchers.IO) {
        var synced = 0
        for (photo in quotePhotoDao.getPendingUploads()) {
            val file = File(photo.localPath)
            if (!file.exists()) {
                quotePhotoDao.update(photo.copy(syncStatus = "ERROR", errorMessage = "Fichier local absent"))
                continue
            }
            try {
                val part = MultipartBody.Part.createFormData(
                    "file",
                    file.name,
                    file.asRequestBody("image/jpeg".toMediaTypeOrNull()),
                )
                val remote = invoiceApi.uploadQuotePhoto(photo.invoiceId, part)
                quotePhotoDao.update(
                    photo.copy(
                        remoteId = remote.id,
                        remoteThumbnailUrl = remote.thumbnailUrl,
                        syncStatus = "SYNCED",
                        errorMessage = null,
                    ),
                )
                synced++
            } catch (e: Exception) {
                quotePhotoDao.update(
                    photo.copy(syncStatus = "ERROR", errorMessage = e.message ?: "upload failed"),
                )
            }
        }
        for (photo in quotePhotoDao.getPendingDeletes()) {
            val remoteId = photo.remoteId ?: photo.id
            try {
                invoiceApi.deleteQuotePhoto(photo.invoiceId, remoteId)
                quotePhotoDao.deleteById(photo.id)
                synced++
            } catch (e: Exception) {
                quotePhotoDao.update(
                    photo.copy(syncStatus = "ERROR", errorMessage = e.message ?: "delete failed"),
                )
            }
        }
        synced
    }
}
