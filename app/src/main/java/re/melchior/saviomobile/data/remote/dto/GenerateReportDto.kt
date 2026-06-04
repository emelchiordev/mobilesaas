package re.melchior.saviomobile.data.remote.dto

import com.google.gson.annotations.SerializedName

data class GenerateReportRequestDto(
    @SerializedName("plannedTypeLabel") val plannedTypeLabel: String,
    @SerializedName("actualTypeLabels") val actualTypeLabels: List<String>? = null,
    @SerializedName("customerName") val customerName: String? = null,
    @SerializedName("address") val address: String? = null,
    @SerializedName("equipments") val equipments: List<String>? = null,
    @SerializedName("technicianObservations") val technicianObservations: String? = null,
    @SerializedName("invoiceLines") val invoiceLines: List<String>? = null,
    @SerializedName("anomalySummaries") val anomalySummaries: List<String>? = null,
    @SerializedName("lastVisitSummary") val lastVisitSummary: String? = null,
)

data class GenerateReportResponseDto(
    @SerializedName("report") val report: String,
)
