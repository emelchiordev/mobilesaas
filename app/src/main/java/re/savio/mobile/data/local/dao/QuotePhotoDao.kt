package re.savio.mobile.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import re.savio.mobile.data.local.entity.QuotePhotoEntity

@Dao
interface QuotePhotoDao {
    @Query("SELECT * FROM quote_photos WHERE invoiceId = :invoiceId AND deletedLocally = 0 ORDER BY takenAt ASC")
    fun getByInvoiceId(invoiceId: String): Flow<List<QuotePhotoEntity>>

    @Query("SELECT * FROM quote_photos WHERE syncStatus IN ('PENDING', 'ERROR') AND deletedLocally = 0")
    suspend fun getPendingUploads(): List<QuotePhotoEntity>

    @Query("SELECT * FROM quote_photos WHERE syncStatus = 'PENDING_DELETE'")
    suspend fun getPendingDeletes(): List<QuotePhotoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(photo: QuotePhotoEntity)

    @Update
    suspend fun update(photo: QuotePhotoEntity)

    @Query("DELETE FROM quote_photos WHERE id = :id")
    suspend fun deleteById(id: String)
}
