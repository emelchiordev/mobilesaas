package re.melchior.saviomobile.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import re.melchior.saviomobile.data.local.entity.PhotoEntity

@Dao
interface PhotoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: PhotoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhotos(photos: List<PhotoEntity>)

    // Photos visibles pour une intervention (non supprimées)
    @Query("""
        SELECT * FROM photos 
        WHERE interventionId = :interventionId 
        AND deletedLocally = 0
        ORDER BY takenAt ASC
    """)
    fun getPhotosForIntervention(interventionId: String): Flow<List<PhotoEntity>>

    // Photos en attente d'upload
    @Query("SELECT * FROM photos WHERE syncStatus IN ('PENDING', 'ERROR')")
    suspend fun getPendingUploadPhotos(): List<PhotoEntity>

    // Photos en attente de suppression distante
    @Query("""
        SELECT * FROM photos 
        WHERE syncStatus = 'PENDING_DELETE'
        AND remoteKey IS NOT NULL
    """)
    suspend fun getPendingDeletePhotos(): List<PhotoEntity>

    // Mise à jour sync status après upload réussi
    @Query("""
        UPDATE photos 
        SET syncStatus = 'SYNCED',
            remoteKey = :remoteKey,
            remoteUrl = :remoteUrl,
            errorMessage = NULL
        WHERE id = :id
    """)
    suspend fun markAsSynced(id: String, remoteKey: String, remoteUrl: String?)

    // Mise à jour en erreur
    @Query("""
        UPDATE photos 
        SET syncStatus = 'ERROR',
            errorMessage = :errorMessage
        WHERE id = :id
    """)
    suspend fun markAsError(id: String, errorMessage: String)

    // Soft delete — si déjà synced, marque pour suppression distante
    @Query("""
        UPDATE photos 
        SET deletedLocally = 1,
            syncStatus = CASE 
                WHEN syncStatus = 'SYNCED' THEN 'PENDING_DELETE'
                ELSE 'PENDING_DELETE'
            END
        WHERE id = :id
    """)
    suspend fun softDelete(id: String)

    // Suppression physique après suppression distante confirmée
    @Query("DELETE FROM photos WHERE id = :id")
    suspend fun hardDelete(id: String)

    // Suppression physique de toutes les photos locales d'une intervention
    // (ex. si intervention supprimée)
    @Query("DELETE FROM photos WHERE interventionId = :interventionId")
    suspend fun deleteAllForIntervention(interventionId: String)

    // Compte les photos non synced (badge indicateur)
    @Query("""
        SELECT COUNT(*) FROM photos 
        WHERE syncStatus IN ('PENDING', 'ERROR')
        AND deletedLocally = 0
    """)
    fun getPendingCount(): Flow<Int>

    // Une photo par son id
    @Query("SELECT * FROM photos WHERE id = :id")
    suspend fun getPhotoById(id: String): PhotoEntity?
}