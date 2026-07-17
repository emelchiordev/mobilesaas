package re.savio.mobile.data.remote.api

import re.savio.mobile.data.remote.dto.ArticlesForMobileResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface TenantArticleApi {
    @GET("api/articles/for-mobile")
    suspend fun getForMobile(
        @Query("since") since: String? = null,
    ): ArticlesForMobileResponseDto
}
