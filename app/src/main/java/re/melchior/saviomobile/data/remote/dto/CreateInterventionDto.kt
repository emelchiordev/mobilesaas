package re.melchior.saviomobile.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CreateInterventionRequestDto(
    @SerializedName("unitId") val unitId: String,
    @SerializedName("technicianId") val technicianId: String?,
    @SerializedName("interventionTypeId") val interventionTypeId: String,
    @SerializedName("scheduledAt") val scheduledAt: String,
    @SerializedName("notes") val notes: String?,
)

data class CreateInterventionResponseDto(
    @SerializedName("id") val id: String,
)
