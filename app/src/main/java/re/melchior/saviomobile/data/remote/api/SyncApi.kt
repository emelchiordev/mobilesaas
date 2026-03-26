package re.melchior.saviomobile.data.remote.api

import re.melchior.saviomobile.data.remote.dto.PullResponseDto
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface SyncApi {

    @GET("api/mobile/sync/pull")
    suspend fun pull(
        @Query("date") date: String,
        @Header("If-Modified-Since") ifModifiedSince: String? = null
    ): PullResponseDto
}