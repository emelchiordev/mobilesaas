package re.savio.mobile.data.remote.api

import re.savio.mobile.data.remote.dto.PushRequestDto
import re.savio.mobile.data.remote.dto.PushResponseDto
import retrofit2.http.Body
import retrofit2.http.POST

interface PushApi {

    @POST("api/mobile/sync/push")
    suspend fun push(
        @Body request: PushRequestDto
    ): PushResponseDto
}