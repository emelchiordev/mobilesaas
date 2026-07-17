package re.savio.mobile.data.remote.api

import re.savio.mobile.data.remote.dto.ScanPlateRequestDto
import re.savio.mobile.data.remote.dto.ScanPlateResultDto
import retrofit2.http.Body
import retrofit2.http.POST

interface EquipmentApi {
    @POST("api/equipments/scan-plate")
    suspend fun scanPlate(@Body body: ScanPlateRequestDto): ScanPlateResultDto
}
