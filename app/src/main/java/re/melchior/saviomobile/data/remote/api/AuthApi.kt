package re.melchior.saviomobile.data.remote.api

import re.melchior.saviomobile.data.remote.dto.LoginRequestDto
import re.melchior.saviomobile.data.remote.dto.LoginResponseDto
import re.melchior.saviomobile.data.remote.dto.MeResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApi {

    @POST("api/auth/login/body")
    suspend fun login(
        @Body request: LoginRequestDto
    ): LoginResponseDto

    @GET("api/mobile/me")
    suspend fun me(): MeResponseDto
}