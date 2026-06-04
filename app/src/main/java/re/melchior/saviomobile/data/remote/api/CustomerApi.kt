package re.melchior.saviomobile.data.remote.api

import re.melchior.saviomobile.data.remote.dto.CustomerDetailDto
import retrofit2.http.GET
import retrofit2.http.Path

interface CustomerApi {
    @GET("api/customers/{id}")
    suspend fun getCustomer(@Path("id") customerId: String): CustomerDetailDto
}
