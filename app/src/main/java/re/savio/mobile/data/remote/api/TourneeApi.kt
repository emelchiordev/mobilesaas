package re.savio.mobile.data.remote.api

import re.savio.mobile.data.remote.dto.InterventionTypeDto
import retrofit2.http.GET
import retrofit2.http.Query

interface TourneeApi {

    @GET("api/intervention-types")
    suspend fun getInterventionTypesForClose(
        @Query("showOnClose") showOnClose: Boolean = true
    ): List<InterventionTypeDto>
}
