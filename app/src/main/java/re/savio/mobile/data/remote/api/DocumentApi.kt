package re.savio.mobile.data.remote.api

import com.google.gson.annotations.SerializedName
import re.savio.mobile.data.remote.dto.CreateDocumentRequestDto
import re.savio.mobile.data.remote.dto.CreateDocumentResponseDto
import re.savio.mobile.data.remote.dto.UploadUrlResponseDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
interface DocumentApi {

    @GET("api/documents/upload-url")
    suspend fun getUploadUrl(
        @Query("interventionId") interventionId: String,
        @Query("unitId") unitId: String,
        @Query("customerId") customerId: String,
        @Query("fileName") fileName: String,
        @Query("contentType") contentType: String,
        @Query("context") context: String? = null
    ): UploadUrlResponseDto

    @POST("api/documents")
    suspend fun createDocument(
        @Body body: CreateDocumentRequestDto
    ): CreateDocumentResponseDto

    @DELETE("api/documents/{id}")
    suspend fun deleteDocument(
        @Path("id") documentId: String
    )

    @GET("api/documents/signed-url")
    suspend fun getSignedUrl(
        @Query("key") key: String
    ): SignedUrlResponseDto

    data class SignedUrlResponseDto(
        @SerializedName("url")
        val url: String
    )
}