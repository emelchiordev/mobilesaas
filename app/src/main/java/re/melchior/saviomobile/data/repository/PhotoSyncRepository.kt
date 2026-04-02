package re.melchior.saviomobile.data.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import re.melchior.saviomobile.data.local.dao.PhotoDao
import re.melchior.saviomobile.data.local.database.TokenDataStore
import re.melchior.saviomobile.data.remote.api.DocumentApi
import re.melchior.saviomobile.data.remote.dto.CreateDocumentRequestDto
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PhotoSyncRepository @Inject constructor(
    private val photoDao: PhotoDao,
    private val documentApi: DocumentApi,
    @ApplicationContext private val context: Context
) {
    private val httpClient = OkHttpClient()

    suspend fun uploadPendingPhotos(): Boolean {
        val pending = photoDao.getPendingUploadPhotos()
        if (pending.isEmpty()) return true

        var allSuccess = true

        for (photo in pending) {
            try {
                val file = File(photo.localPath)
                if (!file.exists()) {
                    photoDao.hardDelete(photo.id)
                    continue
                }

                // 1. Demande URL signée
                val uploadUrlResponse = documentApi.getUploadUrl(
                    interventionId = photo.interventionId,
                    unitId = photo.unitId,
                    customerId = photo.customerId, // ← ajoute
                    fileName = "${photo.id}.jpg",
                    contentType = "image/jpeg"
                )

                // 2. Upload direct vers Scaleway
                // ⚠️ PAS via Retrofit — URL signée externe, pas ton backend
                val uploadRequest = Request.Builder()
                    .url(uploadUrlResponse.url)
                    .put(file.asRequestBody("image/jpeg".toMediaType()))
                    .build()

                val uploadResponse = httpClient.newCall(uploadRequest).execute()
                if (!uploadResponse.isSuccessful) {
                    photoDao.markAsError(photo.id, "Upload S3 échoué: ${uploadResponse.code}")
                    allSuccess = false
                    continue
                }

                // 3. Notifie le backend
                documentApi.createDocument(
                    body = CreateDocumentRequestDto(
                        key = uploadUrlResponse.key,
                        fileName = "${photo.id}.jpg",
                        type = "photo",
                        interventionId = photo.interventionId,
                        unitId = photo.unitId,
                        customerId = photo.customerId
                    )
                )

                // 4. Marque synced
                photoDao.markAsSynced(
                    id = photo.id,
                    remoteKey = uploadUrlResponse.key,
                    remoteUrl = null
                )

                android.util.Log.d("PhotoSync", "Photo ${photo.id} uploadée ✓")

            } catch (e: Exception) {
                photoDao.markAsError(photo.id, e.message ?: "Erreur inconnue")
                allSuccess = false
                android.util.Log.e("PhotoSync", "Erreur upload ${photo.id}: ${e.message}")
            }
        }
        return allSuccess
    }

    suspend fun deletePendingPhotos() {
        val pending = photoDao.getPendingDeletePhotos()
        if (pending.isEmpty()) return

        for (photo in pending) {
            try {
                photo.remoteKey?.let {
                    documentApi.deleteDocument(documentId = photo.id)
                }
                photoDao.hardDelete(photo.id)
                android.util.Log.d("PhotoSync", "Photo ${photo.id} supprimée ✓")
            } catch (e: Exception) {
                android.util.Log.e("PhotoSync", "Erreur suppression ${photo.id}: ${e.message}")
            }
        }
    }
}

data class CreateDocumentBody(
    val key: String,
    val fileName: String,
    val type: String,
    val interventionId: String,
    val unitId: String,
    val customerId: String
)