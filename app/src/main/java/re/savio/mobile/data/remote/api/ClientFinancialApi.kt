package re.savio.mobile.data.remote.api

import re.savio.mobile.data.remote.dto.ClientFinancialDocumentsPageDto
import re.savio.mobile.data.remote.dto.ClientFinancialSummaryResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ClientFinancialApi {
    @GET("api/clients/{clientId}/financial-summary")
    suspend fun getFinancialSummary(
        @Path("clientId") clientId: String,
    ): ClientFinancialSummaryResponseDto

    @GET("api/clients/{clientId}/financial-documents")
    suspend fun getFinancialDocuments(
        @Path("clientId") clientId: String,
        @Query("type") type: String,
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int,
    ): ClientFinancialDocumentsPageDto
}
