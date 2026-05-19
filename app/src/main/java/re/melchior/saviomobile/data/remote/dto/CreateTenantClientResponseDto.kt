package re.melchior.saviomobile.data.remote.dto

import com.google.gson.annotations.SerializedName

/** Réponse POST /api/clients — `id` = UUID customer (remoteId mobile). */
data class CreateTenantClientResponseDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("customerId")
    val customerId: String? = null,
    @SerializedName("unitId")
    val unitId: String,
    @SerializedName("slug")
    val slug: String? = null,
    @SerializedName("firstName")
    val firstName: String? = null,
    @SerializedName("lastName")
    val lastName: String? = null,
    @SerializedName("phone")
    val phone: String? = null,
    @SerializedName("email")
    val email: String? = null,
) {
    fun resolvedCustomerId(): String = customerId?.takeIf { it.isNotBlank() } ?: id
}
