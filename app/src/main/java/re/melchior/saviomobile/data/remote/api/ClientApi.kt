package re.melchior.saviomobile.data.remote.api

import com.google.gson.JsonElement
import re.melchior.saviomobile.data.remote.dto.CreateTenantClientRequestDto
import retrofit2.http.Body
import retrofit2.http.POST

/** POST /api/clients — façade tenant (client + logement + occupation). */
interface ClientApi {
    @POST("api/clients")
    suspend fun createClient(@Body body: CreateTenantClientRequestDto): JsonElement
}
