package re.savio.mobile.data.remote.api

import re.savio.mobile.data.remote.dto.CreateTenantClientRequestDto
import re.savio.mobile.data.remote.dto.CreateTenantClientResponseDto
import retrofit2.http.Body
import retrofit2.http.POST

/** POST /api/clients — façade tenant (client + logement + occupation). */
interface ClientApi {
    @POST("api/clients")
    suspend fun createClient(@Body body: CreateTenantClientRequestDto): CreateTenantClientResponseDto
}
