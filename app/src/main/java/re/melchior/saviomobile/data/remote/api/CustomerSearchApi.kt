package re.melchior.saviomobile.data.remote.api

import re.melchior.saviomobile.data.remote.dto.CustomerSearchPageDto
import retrofit2.http.GET
import retrofit2.http.Query

interface CustomerSearchApi {
    @GET("api/customers/search")
    suspend fun search(
        @Query("q") query: String,
        @Query("limit") limit: Int = 10,
    ): CustomerSearchPageDto
}
