package re.savio.mobile.data.remote.dto

import com.google.gson.annotations.SerializedName

data class InvoiceDto(
    @SerializedName("id") val id: String,
    @SerializedName("interventionId") val interventionId: String?,
    @SerializedName("status") val status: String,
    @SerializedName("number") val number: String?,
    @SerializedName("totalHt") val totalHt: Double,
    @SerializedName("totalVat") val totalVat: Double,
    @SerializedName("totalTtc") val totalTtc: Double,
    @SerializedName("emittedAt") val emittedAt: String?,
    @SerializedName("dueAt") val dueAt: String?,
    @SerializedName("customerEmail") val customerEmail: String?,
    @SerializedName("notes") val notes: String?,
    @SerializedName("lines") val lines: List<InvoiceLineDto>?,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String,
    @SerializedName("acceptedAt") val acceptedAt: String? = null,
    @SerializedName("invoicedAt") val invoicedAt: String? = null,
    @SerializedName("paidAt") val paidAt: String? = null,
    @SerializedName("devisSignatureUrl") val devisSignatureUrl: String? = null,
    @SerializedName("hamonSignatureUrl") val hamonSignatureUrl: String? = null,
    @SerializedName("hamonRequested") val hamonRequested: Boolean = false,
    @SerializedName("type") val type: String? = null,
)

data class InvoiceLineDto(
    @SerializedName("id") val id: String,
    @SerializedName("invoiceId") val invoiceId: String,
    @SerializedName("type") val type: String,
    @SerializedName("label") val label: String,
    @SerializedName("reference") val reference: String?,
    @SerializedName("quantity") val quantity: Double,
    @SerializedName("unitPriceHt") val unitPriceHt: Double,
    @SerializedName("vatRate") val vatRate: Double,
    @SerializedName("discountPercent") val discountPercent: Double,
    @SerializedName("totalHt") val totalHt: Double,
    @SerializedName("billingType") val billingType: String,
    @SerializedName("isTextBlock") val isTextBlock: Boolean,
    @SerializedName("order") val order: Int,
    @SerializedName("natureLigne") val natureLigne: String? = null,
    @SerializedName("tenantArticleId") val tenantArticleId: String? = null,
    @SerializedName("photoIncluded") val photoIncluded: Boolean = false,
    @SerializedName("hasArticlePhoto") val hasArticlePhoto: Boolean = false,
    @SerializedName("hasLinePhoto") val hasLinePhoto: Boolean = false,
    @SerializedName("hasEffectivePhoto") val hasEffectivePhoto: Boolean = false,
    @SerializedName("photoSource") val photoSource: String? = null,
    @SerializedName("photoThumbnailUrl") val photoThumbnailUrl: String? = null,
)

data class PrestationDto(
    @SerializedName("type") val type: String,
    @SerializedName("reference") val reference: String,
    @SerializedName("label") val label: String,
    @SerializedName("vatRate") val vatRate: Double,
    @SerializedName("unitPriceHt") val unitPriceHt: Double?,
    @SerializedName("referenceInterne") val referenceInterne: String? = null,
    @SerializedName("marque") val marque: String? = null,
    @SerializedName("famille") val famille: String? = null,
)

data class SearchRefResponseDto(
    @SerializedName("prestations") val prestations: List<PrestationDto>,
    @SerializedName("pieces") val pieces: List<PrestationDto>,
    @SerializedName("articles") val articles: List<PrestationDto> = emptyList(),
)
