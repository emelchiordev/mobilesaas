package re.savio.mobile.data.remote.api

import re.savio.mobile.data.remote.dto.UnitVeStatusDto
import retrofit2.http.GET
import retrofit2.http.Path

interface UnitsApi {
    @GET("api/units/{id}/ve-status")
    suspend fun getVeStatus(@Path("id") unitId: String): UnitVeStatusDto
}
