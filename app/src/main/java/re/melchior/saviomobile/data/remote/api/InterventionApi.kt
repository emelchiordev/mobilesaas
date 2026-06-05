package re.melchior.saviomobile.data.remote.api

import re.melchior.saviomobile.data.remote.dto.CreateInterventionRequestDto
import re.melchior.saviomobile.data.remote.dto.CreateInterventionResponseDto
import re.melchior.saviomobile.data.remote.dto.GenerateReportEnqueueResponseDto
import re.melchior.saviomobile.data.remote.dto.GenerateReportRequestDto
import re.melchior.saviomobile.data.remote.dto.ReportStatusResponseDto
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
