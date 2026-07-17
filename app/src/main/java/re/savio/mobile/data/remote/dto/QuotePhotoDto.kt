package re.savio.mobile.data.remote.dto

import com.google.gson.annotations.SerializedName

data class QuotePhotoDto(
    @SerializedName("id") val id: String,
    @SerializedName("thumbnailUrl") val thumbnailUrl: String,
    @SerializedName("displayUrl") val displayUrl: String,
    @SerializedName("createdAt") val createdAt: String,
)
