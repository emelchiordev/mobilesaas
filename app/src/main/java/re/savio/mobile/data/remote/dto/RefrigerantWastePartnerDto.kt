package re.savio.mobile.data.remote.dto

import com.google.gson.annotations.SerializedName
import re.savio.mobile.data.local.objectbox.RefrigerantWastePartnerBox

data class RefrigerantWastePartnersForMobileResponseDto(
    @SerializedName("partners") val partners: List<RefrigerantWastePartnerDto>,
    @SerializedName("total") val total: Int,
    @SerializedName("syncedAt") val syncedAt: String,
)

data class RefrigerantWastePartnerDto(
    @SerializedName("id") val id: String,
    @SerializedName("kind") val kind: String,
    @SerializedName("label") val label: String,
    @SerializedName("siret") val siret: String,
    @SerializedName("addressLine1") val addressLine1: String? = null,
    @SerializedName("addressLine2") val addressLine2: String? = null,
    @SerializedName("postalCode") val postalCode: String? = null,
    @SerializedName("city") val city: String? = null,
    @SerializedName("isDefault") val isDefault: Boolean = false,
    @SerializedName("updatedAt") val updatedAt: String,
)

data class CreateRefrigerantWastePartnerBody(
    @SerializedName("kind") val kind: String,
    @SerializedName("label") val label: String,
    @SerializedName("siret") val siret: String,
    @SerializedName("addressLine1") val addressLine1: String? = null,
    @SerializedName("addressLine2") val addressLine2: String? = null,
    @SerializedName("postalCode") val postalCode: String? = null,
    @SerializedName("city") val city: String? = null,
    @SerializedName("isDefault") val isDefault: Boolean? = null,
)

fun RefrigerantWastePartnerDto.toBoxEntity(
    existing: RefrigerantWastePartnerBox? = null,
): RefrigerantWastePartnerBox {
    val target = existing ?: RefrigerantWastePartnerBox()
    target.id = id
    target.kind = kind
    target.label = label
    target.siret = siret
    target.addressLine1 = addressLine1
    target.addressLine2 = addressLine2
    target.postalCode = postalCode
    target.city = city
    target.isDefault = isDefault
    target.updatedAt = updatedAt
    return target
}

fun RefrigerantWastePartnerBox.formatVilleForCerfa(): String {
    return listOf(postalCode?.trim().orEmpty(), city?.trim().orEmpty())
        .filter { it.isNotBlank() }
        .joinToString(" ")
}
