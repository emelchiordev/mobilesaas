package re.melchior.saviomobile.data.remote.api

import re.melchior.saviomobile.data.remote.dto.LoginRequestDto
import re.melchior.saviomobile.data.remote.dto.LoginResponseDto
import re.melchior.saviomobile.data.remote.dto.MeResponseDto
import re.melchior.saviomobile.data.remote.dto.RegisterRequestDto
import re.melchior.saviomobile.data.remote.dto.RegisterResponseDto
import re.melchior.saviomobile.data.remote.dto.ResendRegistrationEmailRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface AuthApi {

    @POST("api/auth/login/body")
    suspend fun login(
        @Body request: LoginRequestDto
    ): LoginResponseDto

    @POST("api/auth/register")
    suspend fun register(
        @Body request: RegisterRequestDto,
    ): RegisterResponseDto

    @POST("api/auth/resend-registration-email")
    suspend fun resendRegistrationEmail(
        @Body body: ResendRegistrationEmailRequestDto,
    ): RegisterResponseDto

    /** Hors préfixe api côté serveur : ${BASE_URL}activate */
    @GET("activate")
    suspend fun activateAccount(@Query("token") token: String): Response<LoginResponseDto>

    @GET("api/mobile/me")
    suspend fun me(): MeResponseDto
}