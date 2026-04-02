package re.melchior.saviomobile.data.remote.dto

import com.google.gson.annotations.SerializedName

data class UploadUrlResponseDto(
    @SerializedName("url") val url: String,
    @SerializedName("key") val key: String
)

data class CreateDocumentRequestDto(
    @SerializedName("key") val key: String,
    @SerializedName("fileName") val fileName: String,
    @SerializedName("type") val type: String,
    @SerializedName("interventionId") val interventionId: String,
    @SerializedName("unitId") val unitId: String,
    @SerializedName("customerId") val customerId: String
)

data class CreateDocumentResponseDto(
    @SerializedName("id") val id: String,
    @SerializedName("key") val key: String,
    @SerializedName("fileName") val fileName: String
)