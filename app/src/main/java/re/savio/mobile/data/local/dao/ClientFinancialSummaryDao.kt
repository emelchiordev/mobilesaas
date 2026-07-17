package re.savio.mobile.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import re.savio.mobile.data.local.entity.ClientFinancialSummaryEntity

@Dao
interface ClientFinancialSummaryDao {
    @Query(
        """
        SELECT * FROM client_financial_summary
        WHERE clientId = :clientId
        ORDER BY emittedAt DESC, documentId ASC
        """,
    )
    fun observeByClientId(clientId: String): Flow<List<ClientFinancialSummaryEntity>>

    @Query("DELETE FROM client_financial_summary WHERE clientId = :clientId")
    suspend fun deleteByClientId(clientId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ClientFinancialSummaryEntity>)
}
