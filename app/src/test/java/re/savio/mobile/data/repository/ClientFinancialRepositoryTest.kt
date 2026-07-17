package re.savio.mobile.data.repository

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import re.savio.mobile.data.local.dao.ClientFinancialSummaryDao
import re.savio.mobile.data.remote.api.ClientFinancialApi
import re.savio.mobile.data.remote.dto.ClientFinancialDocumentDto
import re.savio.mobile.data.remote.dto.ClientFinancialSummaryResponseDto

class ClientFinancialRepositoryTest {

    private val dao = mockk<ClientFinancialSummaryDao>(relaxed = true)
    private val api = mockk<ClientFinancialApi>()
    private lateinit var repository: ClientFinancialRepository

    @Before
    fun setUp() {
        repository = ClientFinancialRepository(dao, api)
    }

    @Test
    fun replaceFromSync_deletesThenInserts() = runTest {
        val docs = listOf(
            ClientFinancialDocumentDto(
                documentId = "doc-1",
                documentType = "devis",
                number = "D0001",
                title = null,
                emittedAt = "2026-03-01",
                statusCode = "pending_approval",
                statusLabel = "Envoyé",
                statusTone = "info",
                totalTtc = 100.0,
            ),
        )

        repository.replaceFromSync("cust-1", docs, "2026-07-15T07:32:00Z")

        coVerify(exactly = 1) { dao.deleteByClientId("cust-1") }
        coVerify(exactly = 1) {
            dao.insertAll(
                match { rows ->
                    rows.size == 1 &&
                        rows[0].clientId == "cust-1" &&
                        rows[0].documentId == "doc-1"
                },
            )
        }
    }

    @Test
    fun replaceFromSync_emptyDocumentsOnlyDeletes() = runTest {
        repository.replaceFromSync("cust-1", emptyList(), "2026-07-15T07:32:00Z")

        coVerify(exactly = 1) { dao.deleteByClientId("cust-1") }
        coVerify(exactly = 0) { dao.insertAll(any()) }
    }

    @Test
    fun refreshSummary_replacesFromApi() = runTest {
        coEvery { api.getFinancialSummary("cust-1") } returns ClientFinancialSummaryResponseDto(
            documents = emptyList(),
            financialSummarySyncedAt = "2026-07-15T07:32:00Z",
        )

        repository.refreshSummary("cust-1")

        coVerify { dao.deleteByClientId("cust-1") }
    }
}
