package re.melchior.saviomobile.data.remote.api

import re.melchior.saviomobile.data.remote.dto.CreateInterventionRequestDto
import re.melchior.saviomobile.data.remote.dto.CreateInterventionResponseDto
import re.melchior.saviomobile.data.remote.dto.GenerateReportRequestDto
import re.melchior.saviomobile.data.remote.dto.GenerateReportResponseDto
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface InterventionApi {
    @POST("api/interventions")
    suspend fun create(@Body body: CreateInterventionRequestDto): CreateInterventionResponseDto

    @POST("api/interventions/{id}/generate-report")
    suspend fun generateReport(
        @Path("id") interventionId: String,
        @Body body: GenerateReportRequestDto,
    ): GenerateReportResponseDto
}
