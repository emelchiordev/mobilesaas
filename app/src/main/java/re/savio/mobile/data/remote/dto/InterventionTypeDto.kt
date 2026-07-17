package re.savio.mobile.data.remote.dto

import com.google.gson.annotations.SerializedName

data class InterventionTypeDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("code") val code: String,
    @SerializedName("label") val label: String,
    @SerializedName("color") val color: String?,
    @SerializedName("isVeType") val isVeType: Boolean = false,
    @SerializedName("isRamonageType") val isRamonageType: Boolean = false,
    @SerializedName("isSystem") val isSystem: Boolean = false,
    @SerializedName("showOnCreate") val showOnCreate: Boolean = true,
    @SerializedName("showOnClose") val showOnClose: Boolean = true,
    @SerializedName("requireClientSignature") val requireClientSignature: Boolean = true,
    @SerializedName("requireReport") val requireReport: Boolean = true,
    @SerializedName("triggerEquipmentSetup") val triggerEquipmentSetup: Boolean = false,
)

/** Clé stable pour sélection UI (id serveur si présent, sinon code). */
fun InterventionTypeDto.stableKey(): String = id ?: code
