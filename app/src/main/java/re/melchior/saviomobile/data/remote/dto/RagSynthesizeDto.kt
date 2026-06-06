package re.melchior.saviomobile.data.remote.dto

import com.google.gson.annotations.SerializedName

data class RagSynthesizeResultItemDto(
    @SerializedName("observations") val observations: String,
    @SerializedName("interventionType") val interventionType: String,
)

data class RagSynthesizeRequestDto(
    @SerializedName("query") val query: String,
    @SerializedName("marque") val marque: String? = null,
    @SerializedName("modele") val modele: String? = null,
    @SerializedName("results") val results: List<RagSynthesizeResultItemDto>,
)

data class RagSynthesizeResponseDto(
    @SerializedName("causes") val causes: List<String>,
    @SerializedName("verification") val verification: List<String>,
    @SerializedName("solutionFrequente") val solutionFrequente: String,
    @SerializedName("fallbackUsed") val fallbackUsed: Boolean? = null,
)
