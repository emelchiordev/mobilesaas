package re.savio.mobile.data.remote.api

import re.savio.mobile.data.remote.dto.MobilePendingInterventionRequestDto
import re.savio.mobile.data.remote.dto.MobilePendingInterventionResponseDto
import re.savio.mobile.data.remote.dto.PullResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface SyncApi {

    @GET("api/mobile/sync/pull")
    suspend fun pull(
        @Query("date") date: String,
        @Header("If-Modified-Since") ifModifiedSince: String? = null
    ): PullResponseDto

    @POST("api/mobile/interventions/pending")
    suspend fun postPendingIntervention(
        @Body body: MobilePendingInterventionRequestDto,
    ): MobilePendingInterventionResponseDto
}