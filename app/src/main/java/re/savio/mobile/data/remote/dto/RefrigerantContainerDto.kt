package re.savio.mobile.data.remote.dto

import com.google.gson.annotations.SerializedName
import re.savio.mobile.data.local.objectbox.RefrigerantContainerBox

data class RefrigerantContainersForMobileResponseDto(
    @SerializedName("containers") val containers: List<RefrigerantContainerDto>,
    @SerializedName("total") val total: Int,
    @SerializedName("syncedAt") val syncedAt: String,
)

data class RefrigerantContainerDto(
    @SerializedName("id") val id: String,
    @SerializedName("containerIdentifier") val containerIdentifier: String,
    @SerializedName("containerKind") val containerKind: String,
    @SerializedName("fluidType") val fluidType: String,
    @SerializedName("capacityKg") val capacityKg: String,
    @SerializedName("currentKg") val currentKg: String,
    @SerializedName("status") val status: String,
    @SerializedName("holderTechnicianId") val holderTechnicianId: String? = null,
    @SerializedName("bsffNumber") val bsffNumber: String? = null,
    @SerializedName("updatedAt") val updatedAt: String,
)

data class CreateRefrigerantContainerBody(
    @SerializedName("containerIdentifier") val containerIdentifier: String,
    @SerializedName("containerKind") val containerKind: String,
    @SerializedName("fluidType") val fluidType: String,
    @SerializedName("capacityKg") val capacityKg: Double,
    @SerializedName("status") val status: String? = null,
    @SerializedName("holderTechnicianId") val holderTechnicianId: String? = null,
)

fun RefrigerantContainerDto.toBoxEntity(existing: RefrigerantContainerBox? = null): RefrigerantContainerBox {
    val target = existing ?: RefrigerantContainerBox()
    target.id = id
    target.containerIdentifier = containerIdentifier
    target.containerKind = containerKind
    target.fluidType = fluidType
    target.capacityKg = capacityKg
    target.currentKg = currentKg
    target.status = status
    target.holderTechnicianId = holderTechnicianId
    target.bsffNumber = bsffNumber
    target.updatedAt = updatedAt
    return target
}
