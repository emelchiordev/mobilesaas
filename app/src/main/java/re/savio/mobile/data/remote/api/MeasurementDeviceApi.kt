package re.savio.mobile.data.remote.api

import re.savio.mobile.data.remote.dto.CreateMeasurementDeviceBody
import re.savio.mobile.data.remote.dto.MeasurementDeviceDto
import re.savio.mobile.data.remote.dto.MeasurementDevicesForMobileResponseDto
import re.savio.mobile.data.remote.dto.RememberCerfaMeasurementDeviceBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query

interface MeasurementDeviceApi {
    @GET("api/measurement-devices/for-mobile")
    suspend fun getForMobile(
        @Query("since") since: String? = null,
    ): MeasurementDevicesForMobileResponseDto

    @POST("api/measurement-devices/mobile")
    suspend fun createFromMobile(
        @Body body: CreateMeasurementDeviceBody,
    ): MeasurementDeviceDto

    @POST("api/measurement-devices/remember-cerfa")
    suspend fun rememberCerfa(
        @Body body: RememberCerfaMeasurementDeviceBody,
    )
}
