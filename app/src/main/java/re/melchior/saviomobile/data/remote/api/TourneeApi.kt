package re.melchior.saviomobile.data.remote.api

import re.melchior.saviomobile.data.remote.dto.InterventionTypeDto
import retrofit2.http.GET
import retrofit2.http.Query

interface TourneeApi {

    @GET("api/intervention-types")
    suspend fun getInterventionTypesForClose(
        @Query("showOnClose") showOnClose: Boolean = true
    ): List<InterventionTypeDto>
}
