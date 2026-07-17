package re.savio.mobile.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "quote_photos",
    indices = [
        Index("invoiceId"),
        Index("syncStatus"),
    ],
)
data class QuotePhotoEntity(
    @PrimaryKey val id: String,
    val invoiceId: String,
    val localPath: String,
    val remoteId: String? = null,
    val remoteThumbnailUrl: String? = null,
    val syncStatus: String = "PENDING",
    val errorMessage: String? = null,
    val takenAt: Long = System.currentTimeMillis(),
    val deletedLocally: Boolean = false,
)
