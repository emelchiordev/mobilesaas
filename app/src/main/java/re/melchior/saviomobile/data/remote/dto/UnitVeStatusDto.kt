package re.melchior.saviomobile.data.remote.dto

import com.google.gson.annotations.SerializedName

data class UnitVeStatusDto(
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("lastVeCompletedAt")
    val lastVeCompletedAt: String? = null,
    @SerializedName("nextVePrevisionalMonth")
    val nextVePrevisionalMonth: String? = null,
    @SerializedName("hasOverdueContractVisit")
    val hasOverdueContractVisit: Boolean = false,
    @SerializedName("coverageUnavailable")
    val coverageUnavailable: Boolean? = null,
    @SerializedName("coverageAttested")
    val coverageAttested: Int? = null,
    @SerializedName("coverageExpected")
    val coverageExpected: Int? = null,
    @SerializedName("coverageComplete")
    val coverageComplete: Boolean? = null,
)
