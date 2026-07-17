package re.savio.mobile.data.remote.dto

import com.google.gson.annotations.SerializedName

/** Réponse minimale GET /api/customers/:id (champs supplémentaires ignorés). */
data class CustomerDetailDto(
    @SerializedName("id") val id: String,
    @SerializedName("firstName") val firstName: String? = null,
    @SerializedName("lastName") val lastName: String? = null,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("notes") val notes: String? = null,
)
