package re.melchior.saviomobile.data.remote.interceptor

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import re.melchior.saviomobile.data.local.database.TokenDataStore
import javax.inject.Inject
import javax.inject.Singleton
@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenDataStore: TokenDataStore
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        // Ne pas ajouter les headers auth sur les URLs externes (Scaleway S3)
        if (isExternalUrl(request.url.host)) {
            return chain.proceed(request)
        }

        val token = runBlocking { tokenDataStore.accessToken.first() }
        val slug = runBlocking { tokenDataStore.societeSlug.first() }

        val authenticatedRequest = request.newBuilder().apply {
            token?.let { addHeader("Authorization", "Bearer $it") }
            if (!slug.isNullOrBlank()) {
                addHeader("x-societe-slug", slug)
            }
        }.build()

        return chain.proceed(authenticatedRequest)
    }

    private fun isExternalUrl(host: String): Boolean {
        return host.endsWith(".scw.cloud") // Scaleway
    }
}