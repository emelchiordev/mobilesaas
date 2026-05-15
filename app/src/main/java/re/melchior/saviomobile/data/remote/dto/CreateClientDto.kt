package re.melchior.saviomobile.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CreateClientOccupancyDto(
    @SerializedName("role") val role: String,
    @SerializedName("startDate") val startDate: String,
    @SerializedName("endDate") val endDate: String? = null,
)

data class CreateClientLogementDto(
    @SerializedName("street") val street: String,
    @SerializedName("postalCode") val postalCode: String,
    @SerializedName("city") val city: String,
    @SerializedName("latitude") val latitude: Double? = null,
    @SerializedName("longitude") val longitude: Double? = null,
    @SerializedName("addressLine2") val addressLine2: String? = null,
    @SerializedName("floor") val floor: String? = null,
    @SerializedName("unitType") val unitType: String? = null,
    @SerializedName("unitCategory") val unitCategory: String? = null,
)

/** POST /api/clients — aligné Nest [CreateCustomerWithLogementDto]. */
data class CreateTenantClientRequestDto(
    @SerializedName("firstName") val firstName: String,
    @SerializedName("lastName") val lastName: String,
    @SerializedName("civility") val civility: String? = null,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("logement") val logement: CreateClientLogementDto,
    @SerializedName("occupancy") val occupancy: CreateClientOccupancyDto,
)

/** Alias produit pour le même payload que [CreateTenantClientRequestDto]. */
typealias CreateClientDto = CreateTenantClientRequestDto
