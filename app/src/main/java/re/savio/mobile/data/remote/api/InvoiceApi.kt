package re.savio.mobile.data.remote.api

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import re.savio.mobile.data.remote.dto.InvoiceDto
import re.savio.mobile.data.remote.dto.QuotePhotoDto
import re.savio.mobile.data.remote.dto.SearchRefResponseDto
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming

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

    @GET("api/invoices/{id}/quote-photos")
    suspend fun getQuotePhotos(
        @Path("id") invoiceId: String,
    ): List<QuotePhotoDto>

    @Multipart
    @POST("api/invoices/{id}/quote-photos")
    suspend fun uploadQuotePhoto(
        @Path("id") invoiceId: String,
        @Part file: MultipartBody.Part,
    ): QuotePhotoDto

    @DELETE("api/invoices/{id}/quote-photos/{photoId}")
    suspend fun deleteQuotePhoto(
        @Path("id") invoiceId: String,
        @Path("photoId") photoId: String,
    )

    @Streaming
    @GET("api/invoices/{id}/pdf")
    suspend fun downloadInvoicePdf(
        @Path("id") invoiceId: String,
    ): ResponseBody

    @Streaming
    @GET("api/invoices/{id}/pdf-quote")
    suspend fun downloadQuotePdf(
        @Path("id") invoiceId: String,
    ): ResponseBody
}
