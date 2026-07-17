package re.savio.mobile.data.remote.api

import re.savio.mobile.data.remote.dto.CustomerDetailDto
import retrofit2.http.GET
import retrofit2.http.Path

interface CustomerApi {
    @GET("api/customers/{id}")
    suspend fun getCustomer(@Path("id") customerId: String): CustomerDetailDto
}
