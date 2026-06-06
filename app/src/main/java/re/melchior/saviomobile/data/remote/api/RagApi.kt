package re.melchior.saviomobile.data.remote.api

import re.melchior.saviomobile.data.remote.dto.RagSearchRequestDto
import re.melchior.saviomobile.data.remote.dto.RagSearchResponseDto
import re.melchior.saviomobile.data.remote.dto.RagSynthesizeRequestDto
import re.melchior.saviomobile.data.remote.dto.RagSynthesizeResponseDto
import retrofit2.http.Body
import retrofit2.http.POST

interface RagApi {
    @POST("api/rag/search")
    suspend fun search(@Body body: RagSearchRequestDto): RagSearchResponseDto

    @POST("api/rag/synthesize")
    suspend fun synthesize(@Body body: RagSynthesizeRequestDto): RagSynthesizeResponseDto
}
