package re.savio.mobile.data.remote.api

import com.google.gson.JsonElement
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Autocomplétion adresses (proxy BAN côté backend).
 * `GET {BASE_URL}api/addresses/autocomplete?q=&limit=` — JWT via [AuthInterceptor] (même client que les autres API).
 */
interface BanAddressSearchApi {
    @GET("api/addresses/autocomplete")
    suspend fun search(
        @Query("q") q: String,
        @Query("limit") limit: Int,
    ): JsonElement
}
