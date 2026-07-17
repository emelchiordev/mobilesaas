package re.savio.mobile.data.remote.api

import re.savio.mobile.data.remote.dto.CustomerSearchPageDto
import retrofit2.http.GET
import retrofit2.http.Query

interface CustomerSearchApi {
    @GET("api/customers/search")
    suspend fun search(
        @Query("q") query: String,
        @Query("limit") limit: Int = 10,
    ): CustomerSearchPageDto
}
