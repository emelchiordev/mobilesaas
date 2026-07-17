package re.savio.mobile.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ClientFinancialDocumentDto(
    @SerializedName("documentId")
    val documentId: String,
    @SerializedName("documentType")
    val documentType: String,
    @SerializedName("number")
    val number: String,
    @SerializedName("title")
    val title: String? = null,
    @SerializedName("emittedAt")
    val emittedAt: String,
    @SerializedName("statusCode")
    val statusCode: String,
    @SerializedName("statusLabel")
    val statusLabel: String,
    @SerializedName("statusTone")
    val statusTone: String,
    @SerializedName("totalTtc")
    val totalTtc: Double,
)

data class ClientFinancialSummaryResponseDto(
    @SerializedName("documents")
    val documents: List<ClientFinancialDocumentDto>,
    @SerializedName("financialSummarySyncedAt")
    val financialSummarySyncedAt: String,
)

data class ClientFinancialDocumentsPageDto(
    @SerializedName("items")
    val items: List<ClientFinancialDocumentDto>,
    @SerializedName("total")
    val total: Int,
    @SerializedName("page")
    val page: Int,
    @SerializedName("pageSize")
    val pageSize: Int,
)
