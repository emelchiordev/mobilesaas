package re.savio.mobile.data.remote.api

import retrofit2.http.Body
import retrofit2.http.PATCH

data class SetRefrigerantCapacityAttestationBody(
    val number: String,
)

interface CompanySettingsApi {
    @PATCH("api/company-settings/refrigerant-capacity-attestation")
    suspend fun setRefrigerantCapacityAttestation(
        @Body body: SetRefrigerantCapacityAttestationBody,
    )
}
