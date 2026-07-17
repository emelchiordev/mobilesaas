package re.savio.mobile.data.remote.api

import re.savio.mobile.data.remote.dto.CreateRenewalInvoiceRequestDto
import re.savio.mobile.data.remote.dto.CreateRenewalInvoiceResponseDto
import re.savio.mobile.data.remote.dto.EquipmentContractStatusDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ContractsApi {
    @GET("api/contracts/equipment/{equipmentId}/status")
    suspend fun getEquipmentContractStatus(
        @Path("equipmentId") equipmentId: String,
    ): EquipmentContractStatusDto

    @POST("api/contracts/lines/{lineId}/renewal-invoice")
    suspend fun createRenewalInvoice(
        @Path("lineId") lineId: String,
        @Body body: CreateRenewalInvoiceRequestDto,
    ): CreateRenewalInvoiceResponseDto
}
