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
    private val tokenDataStore: TokenDataStore,
    private val authEventBus: AuthEventBus
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

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

        val response = chain.proceed(authenticatedRequest)

        // ← ajouté
        if (response.code == 401) {
            runBlocking {
                tokenDataStore.clearSession()
                authEventBus.emit(AuthEvent.Unauthorized)
            }
        }

        return response
    }

    private fun isExternalUrl(host: String): Boolean {
        return host.endsWith(".scw.cloud")
    }
}