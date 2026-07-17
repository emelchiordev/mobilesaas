package re.savio.mobile.data.remote.api

import re.savio.mobile.data.remote.dto.CreateInterventionRequestDto
import re.savio.mobile.data.remote.dto.CreateInterventionResponseDto
import re.savio.mobile.data.remote.dto.GenerateReportEnqueueResponseDto
import re.savio.mobile.data.remote.dto.GenerateReportRequestDto
import re.savio.mobile.data.remote.dto.ReportStatusResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface InterventionApi {
    @POST("api/interventions")
    suspend fun create(@Body body: CreateInterventionRequestDto): CreateInterventionResponseDto

    @POST("api/interventions/{id}/generate-report")
    suspend fun generateReport(
        @Path("id") interventionId: String,
        @Body body: GenerateReportRequestDto,
    ): GenerateReportEnqueueResponseDto

    @GET("api/interventions/{id}/report-status")
    suspend fun getReportStatus(
        @Path("id") interventionId: String,
    ): ReportStatusResponseDto
}
