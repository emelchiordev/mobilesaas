package re.melchior.saviomobile.data.remote.api

import re.melchior.saviomobile.data.remote.dto.ArticlesForMobileResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface TenantArticleApi {
    @GET("api/articles/for-mobile")
    suspend fun getForMobile(
        @Query("since") since: String? = null,
    ): ArticlesForMobileResponseDto
}
