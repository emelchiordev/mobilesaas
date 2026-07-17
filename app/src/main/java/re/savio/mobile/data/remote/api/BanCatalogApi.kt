package re.savio.mobile.data.remote.api

import re.savio.mobile.data.remote.dto.CatalogSyncResponseDto
import re.savio.mobile.data.remote.dto.NoticeUrlResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface BanCatalogApi {
    @GET("sync/catalog")
    suspend fun syncCatalog(
        @Query("since") since: String?,
    ): CatalogSyncResponseDto

    @GET("sync/equipment/{id}/notice-url")
    suspend fun getNoticeDownloadUrl(
        @Path("id") id: String,
    ): NoticeUrlResponseDto
}
