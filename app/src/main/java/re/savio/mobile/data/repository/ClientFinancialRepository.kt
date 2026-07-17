package re.savio.mobile.data.repository

import kotlinx.coroutines.flow.Flow
import re.savio.mobile.data.local.dao.ClientFinancialSummaryDao
import re.savio.mobile.data.local.entity.ClientFinancialSummaryEntity
import re.savio.mobile.data.remote.api.ClientFinancialApi
import re.savio.mobile.data.remote.dto.ClientFinancialDocumentDto
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClientFinancialRepository @Inject constructor(
    private val dao: ClientFinancialSummaryDao,
    private val api: ClientFinancialApi,
) {
    fun observeByClientId(clientId: String): Flow<List<ClientFinancialSummaryEntity>> =
        dao.observeByClientId(clientId)

    suspend fun replaceFromSync(
        clientId: String,
        documents: List<ClientFinancialDocumentDto>,
        syncedAtIso: String?,
    ) {
        dao.deleteByClientId(clientId)
        if (documents.isEmpty()) return
        val syncedAt = parseSyncedAt(syncedAtIso)
        dao.insertAll(documents.map { it.toEntity(clientId, syncedAt) })
    }

    suspend fun refreshSummary(clientId: String) {
        val response = api.getFinancialSummary(clientId)
        replaceFromSync(clientId, response.documents, response.financialSummarySyncedAt)
    }

    suspend fun fetchDocumentsPage(
        clientId: String,
        type: String,
        page: Int,
        pageSize: Int = 25,
    ) = api.getFinancialDocuments(clientId, type, page, pageSize)

    private fun parseSyncedAt(iso: String?): Long {
        if (iso.isNullOrBlank()) return System.currentTimeMillis()
        return runCatching { Instant.parse(iso).toEpochMilli() }
            .getOrDefault(System.currentTimeMillis())
    }

    private fun ClientFinancialDocumentDto.toEntity(
        clientId: String,
        syncedAt: Long,
    ) = ClientFinancialSummaryEntity(
        documentId = documentId,
        clientId = clientId,
        documentType = documentType,
        number = number,
        title = title,
        emittedAt = emittedAt,
        statusCode = statusCode,
        statusLabel = statusLabel,
        statusTone = statusTone,
        totalTtc = totalTtc,
        syncedAt = syncedAt,
    )
}
