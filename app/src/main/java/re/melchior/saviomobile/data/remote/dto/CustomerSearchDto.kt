package re.melchior.saviomobile.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CustomerSearchPageDto(
    @SerializedName("items") val items: List<CustomerSearchRowDto> = emptyList(),
    @SerializedName("nextCursor") val nextCursor: String? = null,
)

data class CustomerSearchRowDto(
    @SerializedName("id") val unitId: String,
    @SerializedName("customerId") val customerId: String? = null,
    @SerializedName("customerFirstName") val customerFirstName: String,
    @SerializedName("customerLastName") val customerLastName: String,
    @SerializedName("customerDisplayName") val customerDisplayName: String,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("street") val street: String,
    @SerializedName("city") val city: String,
    @SerializedName("postalCode") val postalCode: String,
) {
    fun resolvedDisplayName(): String =
        customerDisplayName.ifBlank {
            "${customerFirstName} ${customerLastName}".trim()
        }

    fun formattedAddress(): String =
        listOf(street, "$postalCode $city".trim())
            .filter { it.isNotBlank() }
            .joinToString(", ")
}
