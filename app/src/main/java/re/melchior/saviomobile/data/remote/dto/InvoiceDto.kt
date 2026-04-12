package re.melchior.saviomobile.data.remote.dto

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
    @SerializedName("updatedAt") val updatedAt: String
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
    @SerializedName("order") val order: Int
)

data class PrestationDto(
    @SerializedName("type") val type: String,
    @SerializedName("reference") val reference: String,
    @SerializedName("label") val label: String,
    @SerializedName("vatRate") val vatRate: Double,
    @SerializedName("unitPriceHt") val unitPriceHt: Double?
)

data class SearchRefResponseDto(
    @SerializedName("prestations") val prestations: List<PrestationDto>,
    @SerializedName("pieces") val pieces: List<PrestationDto>
)
