package re.savio.mobile.data.remote.api

import re.savio.mobile.data.remote.dto.CreateRefrigerantContainerBody
import re.savio.mobile.data.remote.dto.RefrigerantContainerDto
import re.savio.mobile.data.remote.dto.RefrigerantContainersForMobileResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface RefrigerantContainerApi {
    @GET("api/refrigerant-containers/for-mobile")
    suspend fun getForMobile(
        @Query("since") since: String? = null,
    ): RefrigerantContainersForMobileResponseDto

    @POST("api/refrigerant-containers")
    suspend fun create(
        @Body body: CreateRefrigerantContainerBody,
    ): RefrigerantContainerDto
}
