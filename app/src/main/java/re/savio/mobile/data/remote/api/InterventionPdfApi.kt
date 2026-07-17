package re.savio.mobile.data.remote.api

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming

interface InterventionPdfApi {

    @Streaming
    @GET("api/interventions/{id}/pac-measures/pdf")
    suspend fun downloadPacMeasuresPdf(
        @Path("id") interventionId: String,
        @Query("equipmentOrder") equipmentOrder: Int,
    ): ResponseBody
}
