package re.melchior.saviomobile.data.remote.dto

import com.google.gson.annotations.SerializedName

data class PushRequestDto(
    @SerializedName("deviceId")
    val deviceId: String,
    @SerializedName("pushedAt")
    val pushedAt: String,
    @SerializedName("operations")
    val operations: List<PushOperationDto>
)

data class PushOperationDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("type")
    val type: String,
    @SerializedName("occurredAt")
    val occurredAt: String,
    @SerializedName("payload")
    val payload: Map<String, Any?>
)

data class PushResponseDto(
    @SerializedName("appliedAt")
    val appliedAt: String,
    @SerializedName("applied")
    val applied: Int,
    @SerializedName("failed")
    val failed: Int,
    @SerializedName("results")
    val results: List<PushResultDto>
)

data class PushResultDto(
    @SerializedName("operationId")
    val operationId: String,
    @SerializedName("status")
    val status: String,
    @SerializedName("reason")
    val reason: String?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("conflictType")
    val conflictType: String?,
    @SerializedName("serverData")
    val serverData: Map<String, Any?>?
)