package re.melchior.saviomobile.data.repository

import re.melchior.saviomobile.data.remote.api.ContractsApi
import re.melchior.saviomobile.data.remote.dto.CreateRenewalInvoiceRequestDto
import javax.inject.Inject
import javax.inject.Singleton

data class RenewalEligibility(
    val contractLineId: String,
    val billingMode: String?,
    val priceTtc: String?,
    val renewable: Boolean,
)

@Singleton
class ContractRenewalRepository @Inject constructor(
    private val contractsApi: ContractsApi,
) {
    suspend fun getRenewalEligibility(equipmentId: String): RenewalEligibility? {
        val status = contractsApi.getEquipmentContractStatus(equipmentId)
        val lineId = status.contractLineId?.trim().orEmpty()
        if (lineId.isEmpty() || !status.renewable) return null
        return RenewalEligibility(
            contractLineId = lineId,
            billingMode = status.billingMode,
            priceTtc = status.priceTtc,
            renewable = status.renewable,
        )
    }

    suspend fun createRenewalInvoice(contractLineId: String, interventionId: String): String {
        val response =
            contractsApi.createRenewalInvoice(
                contractLineId,
                CreateRenewalInvoiceRequestDto(interventionId = interventionId),
            )
        return response.invoiceId
    }
}
