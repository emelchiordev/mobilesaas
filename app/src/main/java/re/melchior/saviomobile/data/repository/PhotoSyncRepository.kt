package re.melchior.saviomobile.data.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import re.melchior.saviomobile.data.local.dao.InterventionDao
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
    private val interventionDao: InterventionDao,
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

    /**
     * Upload signatures PENDING vers Scaleway
     */
    suspend fun uploadPendingSignatures(): Boolean {
        val interventions = interventionDao.getInterventionsWithLocalSignatures()
        android.util.Log.d("PhotoSync", "Signatures à uploader: ${interventions.size}")
        interventions.forEach {
            android.util.Log.d("PhotoSync", "  → ${it.id} signaturePath=${it.signaturePath} techSignaturePath=${it.techSignaturePath}")
        }
        if (interventions.isEmpty()) return true
        var allSuccess = true

        for (intervention in interventions) {
            // Signature client
            intervention.signaturePath?.let { path ->
                if (path.startsWith("/data")) {
                    val file = File(path)
                    if (file.exists()) {
                        try {
                            val uploadUrl = documentApi.getUploadUrl(
                                interventionId = intervention.id,
                                unitId = intervention.unitId,
                                customerId = intervention.customerId ?: return@let,
                                fileName = "sig_client_${intervention.id}.png",
                                contentType = "image/png"
                            )
                            val uploadRequest = Request.Builder()
                                .url(uploadUrl.url)
                                .put(file.asRequestBody("image/png".toMediaType()))
                                .build()
                            val response = withContext(Dispatchers.IO) {
                                httpClient.newCall(uploadRequest).execute()
                            }
                            if (response.isSuccessful) {
                                documentApi.createDocument(
                                    body = CreateDocumentRequestDto(
                                        key = uploadUrl.key,
                                        fileName = "sig_client_${intervention.id}.png",
                                        type = "signature_client",
                                        interventionId = intervention.id,
                                        unitId = intervention.unitId,
                                        customerId = intervention.customerId ?: ""
                                    )
                                )
                                // Met à jour le chemin avec la clé remote
                                interventionDao.updateSignaturePath(
                                    id = intervention.id,
                                    signaturePath = uploadUrl.key
                                )
                                android.util.Log.d("PhotoSync", "Signature client uploadée ✓ ${intervention.id}")
                            }
                        } catch (e: Exception) {
                            // Juste logger — on réessaiera au prochain cycle WorkManager
                            android.util.Log.w("PhotoSync", "Erreur upload  — sera retenté: ${e.message}")
                            allSuccess = false
                            // Ne pas appeler photoDao.markAsError()
                        }
                    }
                }
            }

            // Signature technicien — même logique
            intervention.techSignaturePath?.let { path ->
                if (path.startsWith("/data")) {
                    val file = File(path)
                    if (file.exists()) {
                        try {
                            val uploadUrl = documentApi.getUploadUrl(
                                interventionId = intervention.id,
                                unitId = intervention.unitId,
                                customerId = intervention.customerId ?: return@let,
                                fileName = "sig_tech_${intervention.id}.png",
                                contentType = "image/png",
                                context = "signatures"
                            )
                            val uploadRequest = Request.Builder()
                                .url(uploadUrl.url)
                                .put(file.asRequestBody("image/png".toMediaType()))
                                .build()
                            val response = withContext(Dispatchers.IO) {
                                httpClient.newCall(uploadRequest).execute()
                            }
                            if (response.isSuccessful) {
                                documentApi.createDocument(
                                    body = CreateDocumentRequestDto(
                                        key = uploadUrl.key,
                                        fileName = "sig_tech_${intervention.id}.png",
                                        type = "signature_tech",
                                        interventionId = intervention.id,
                                        unitId = intervention.unitId,
                                        customerId = intervention.customerId ?: ""
                                    )
                                )
                                interventionDao.updateTechSignaturePath(
                                    id = intervention.id,
                                    techSignaturePath = uploadUrl.key
                                )
                                android.util.Log.d("PhotoSync", "Signature tech uploadée ✓ ${intervention.id}")
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("PhotoSync", "Erreur signature client ${intervention.id}", e)
                            allSuccess = false
                        }
                    }
                }
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