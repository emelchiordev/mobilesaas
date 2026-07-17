package re.savio.mobile.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "photos",
    indices = [
        Index("interventionId"),
        Index("syncStatus")
    ]
)
data class PhotoEntity(
    @PrimaryKey
    val id: String,                    // UUID généré localement

    val interventionId: String,        // FK logique vers InterventionEntity
    val unitId: String,                // pour construire la clé S3
    val customerId: String,            // pour construire la clé S3

    val localPath: String,             // chemin absolu fichier sur le device
    val remoteKey: String? = null,     // clé S3 après upload réussi
    val remoteUrl: String? = null,     // URL de téléchargement (optionnel cache)

    val syncStatus: String = "PENDING", // PENDING / SYNCED / ERROR / PENDING_DELETE
    val errorMessage: String? = null,   // message d'erreur si ERROR

    val takenAt: Long = System.currentTimeMillis(),
    val deletedLocally: Boolean = false // soft delete avant sync
)