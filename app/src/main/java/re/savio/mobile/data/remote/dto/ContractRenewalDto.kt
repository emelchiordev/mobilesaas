package re.savio.mobile.data.remote.dto

import com.google.gson.annotations.SerializedName

data class EquipmentContractStatusDto(
    @SerializedName("status") val status: String,
    @SerializedName("anniversaryDate") val anniversaryDate: String? = null,
    @SerializedName("coverageEndDate") val coverageEndDate: String? = null,
    @SerializedName("contractLineId") val contractLineId: String? = null,
    @SerializedName("billingMode") val billingMode: String? = null,
    @SerializedName("renewable") val renewable: Boolean = false,
    @SerializedName("priceTtc") val priceTtc: String? = null,
)

data class CreateRenewalInvoiceRequestDto(
    @SerializedName("interventionId") val interventionId: String,
    @SerializedName("createdFrom") val createdFrom: String = "mobile",
)

data class CreateRenewalInvoiceResponseDto(
    @SerializedName("invoiceId") val invoiceId: String,
)
