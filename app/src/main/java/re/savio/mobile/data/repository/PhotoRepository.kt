package re.savio.mobile.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import re.savio.mobile.data.local.dao.PhotoDao
import re.savio.mobile.data.local.entity.PhotoEntity
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PhotoRepository @Inject constructor(
    private val photoDao: PhotoDao,
    @ApplicationContext private val context: Context
) {

    companion object {
        private const val JPEG_QUALITY = 80
        private const val PHOTOS_DIR = "intervention_photos"
    }

    /**
     * Sauvegarde une photo prise par CameraX.
     * Compression JPEG 80% puis insertion en base Room.
     */
    suspend fun savePhoto(
        interventionId: String,
        unitId: String,
        customerId: String,
        sourceFile: File
    ): PhotoEntity = withContext(Dispatchers.IO) {

        // Dossier de stockage local
        val photosDir = File(context.filesDir, PHOTOS_DIR).also { it.mkdirs() }
        val destFile = File(photosDir, "${UUID.randomUUID()}.jpg")

        // Compression JPEG
        val bitmap = BitmapFactory.decodeFile(sourceFile.absolutePath)
        FileOutputStream(destFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)
        }
        bitmap.recycle()

        // Supprime le fichier temporaire CameraX
        sourceFile.delete()

        // Création de l'entité
        val photo = PhotoEntity(
            id = UUID.randomUUID().toString(),
            interventionId = interventionId,
            unitId = unitId,
            customerId = customerId,
            localPath = destFile.absolutePath,
            syncStatus = "PENDING",
            takenAt = System.currentTimeMillis()
        )

        photoDao.insertPhoto(photo)
        photo
    }

    /**
     * Photos visibles pour une intervention — Flow pour mise à jour auto UI.
     */
    fun getPhotosForIntervention(interventionId: String): Flow<List<PhotoEntity>> {
        return photoDao.getPhotosForIntervention(interventionId)
    }

    suspend fun deleteAllForIntervention(interventionId: String) = withContext(Dispatchers.IO) {
        val photos = photoDao.getAllForInterventionOnce(interventionId)
        photos.forEach { photo ->
            File(photo.localPath).takeIf { it.exists() }?.delete()
        }
        photoDao.deleteAllForIntervention(interventionId)
    }

    /**
     * Soft delete — supprime localement, marque pour suppression distante si synced.
     */
    suspend fun deletePhoto(photo: PhotoEntity) = withContext(Dispatchers.IO) {
        // Supprime le fichier local
        File(photo.localPath).takeIf { it.exists() }?.delete()

        if (photo.syncStatus == "SYNCED" && photo.remoteKey != null) {
            // Déjà synced → marque pour suppression distante
            photoDao.softDelete(photo.id)
        } else {
            // Jamais synced → suppression physique directe
            photoDao.hardDelete(photo.id)
        }
    }

    /**
     * Photos en attente d'upload — appelé par WorkManager.
     */
    suspend fun getPendingUploadPhotos(): List<PhotoEntity> {
        return photoDao.getPendingUploadPhotos()
    }

    /**
     * Photos en attente de suppression distante — appelé par WorkManager.
     */
    suspend fun getPendingDeletePhotos(): List<PhotoEntity> {
        return photoDao.getPendingDeletePhotos()
    }

    /**
     * Marque une photo comme synchronisée après upload réussi.
     */
    suspend fun markAsSynced(id: String, remoteKey: String, remoteUrl: String?) {
        photoDao.markAsSynced(id, remoteKey, remoteUrl)
    }

    /**
     * Marque une photo en erreur.
     */
    suspend fun markAsError(id: String, errorMessage: String) {
        photoDao.markAsError(id, errorMessage)
    }

    /**
     * Nombre de photos non synced — pour badge UI.
     */
    fun getPendingCount(): Flow<Int> {
        return photoDao.getPendingCount()
    }
}