package re.savio.mobile.data.remote.dto

import com.google.gson.annotations.SerializedName

data class RagSearchRequestDto(
    @SerializedName("query") val query: String,
    @SerializedName("marque") val marque: String? = null,
    @SerializedName("modele") val modele: String? = null,
    @SerializedName("equipmentType") val equipmentType: String? = null,
    @SerializedName("limit") val limit: Int = 5,
    @SerializedName("excludeInterventionId") val excludeInterventionId: String? = null,
)

data class RagSearchResultDto(
    @SerializedName("marque") val marque: String,
    @SerializedName("modele") val modele: String,
    @SerializedName("date") val date: String,
    @SerializedName("interventionType") val interventionType: String,
    @SerializedName("observations") val observations: String,
    @SerializedName("score") val score: Double,
)

data class RagSearchResponseDto(
    @SerializedName("results") val results: List<RagSearchResultDto>,
)
