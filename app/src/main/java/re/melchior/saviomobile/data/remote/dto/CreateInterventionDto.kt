package re.melchior.saviomobile.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CreateInterventionRequestDto(
    @SerializedName("unitId") val unitId: String,
    @SerializedName("technicianId") val technicianId: String?,
    @SerializedName("interventionTypeId") val interventionTypeId: String,
    @SerializedName("scheduledAt") val scheduledAt: String,
    @SerializedName("timeSlot") val timeSlot: String? = null,
    @SerializedName("isUrgent") val isUrgent: Boolean? = null,
    @SerializedName("notes") val notes: String?,
    @SerializedName("createdFrom") val createdFrom: String = "mobile",
)

data class CreateInterventionResponseDto(
    @SerializedName("id") val id: String,
)
