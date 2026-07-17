package re.savio.mobile.data.remote.api

import re.savio.mobile.data.remote.dto.CreateRefrigerantWastePartnerBody
import re.savio.mobile.data.remote.dto.RefrigerantWastePartnerDto
import re.savio.mobile.data.remote.dto.RefrigerantWastePartnersForMobileResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface RefrigerantWastePartnerApi {
    @GET("api/refrigerant-waste-partners/for-mobile")
    suspend fun getForMobile(
        @Query("since") since: String? = null,
    ): RefrigerantWastePartnersForMobileResponseDto

    @POST("api/refrigerant-waste-partners")
    suspend fun create(
        @Body body: CreateRefrigerantWastePartnerBody,
    ): RefrigerantWastePartnerDto
}
