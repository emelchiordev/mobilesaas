package re.melchior.saviomobile.data.remote.api

import re.melchior.saviomobile.data.remote.dto.InvoiceDto
import re.melchior.saviomobile.data.remote.dto.SearchRefResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface InvoiceApi {

    @GET("api/invoices")
    suspend fun getInvoiceByIntervention(
        @Query("interventionId") interventionId: String
    ): List<InvoiceDto>

    @GET("api/invoices/search-ref")
    suspend fun searchRef(
        @Query("q") query: String
    ): SearchRefResponseDto

    @GET("api/invoices/{id}")
    suspend fun getInvoice(
        @Path("id") id: String
    ): InvoiceDto
}
