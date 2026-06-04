package re.melchior.saviomobile.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ScanPlateRequestDto(
    @SerializedName("ocrText") val ocrText: String,
)

data class ScanPlateResultDto(
    @SerializedName("brand") val brand: String? = null,
    @SerializedName("model") val model: String? = null,
    @SerializedName("powerKw") val powerKw: Double? = null,
    @SerializedName("refrigerant") val refrigerant: String? = null,
    @SerializedName("manufactureYear") val manufactureYear: Int? = null,
    @SerializedName("serialNumber") val serialNumber: String? = null,
    @SerializedName("typeCode") val typeCode: String? = null,
    @SerializedName("energyCode") val energyCode: String? = null,
    @SerializedName("extraNotes") val extraNotes: String? = null,
)
