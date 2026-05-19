package re.melchior.saviomobile.data.remote.api

import re.melchior.saviomobile.data.remote.dto.CreateInterventionRequestDto
import re.melchior.saviomobile.data.remote.dto.CreateInterventionResponseDto
import retrofit2.http.Body
import retrofit2.http.POST

interface InterventionApi {
    @POST("api/interventions")
    suspend fun create(@Body body: CreateInterventionRequestDto): CreateInterventionResponseDto
}
