package re.savio.mobile.data.remote.dto

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
    @SerializedName("interventionDate") val interventionDate: String? = null,
    @SerializedName("establishedDocuments") val establishedDocuments: List<String>? = null,
    @SerializedName("structuredFacts") val structuredFacts: List<String>? = null,
    @SerializedName("technicalFactsCount") val technicalFactsCount: Int? = null,
)

data class GenerateReportEnqueueResponseDto(
    @SerializedName("jobId") val jobId: String,
    @SerializedName("status") val status: String,
)

data class ReportStatusResponseDto(
    @SerializedName("status") val status: String?,
    @SerializedName("report") val report: String? = null,
    @SerializedName("fallback_used") val fallbackUsed: Boolean? = null,
)
