package re.savio.mobile.data.remote.interceptor

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Response
import re.savio.mobile.data.local.database.AccountSwitchStateStore
import re.savio.mobile.data.local.database.TokenDataStore
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenDataStore: TokenDataStore,
    private val accountSwitchStateStore: AccountSwitchStateStore,
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

        if (response.code == 401) {
            runBlocking {
                // Wrong password / register failures are 401s — not session expiry.
                if (isCredentialAuthUrl(request.url)) return@runBlocking

                val switching =
                    accountSwitchStateStore.read().state != AccountSwitchStateStore.State.IDLE
                if (switching) {
                    // AccountPrep owns flush/pull failures.
                    return@runBlocking
                }
                // Only kick to Welcome when an authenticated call failed.
                if (token.isNullOrBlank()) return@runBlocking

                tokenDataStore.clearSession()
                authEventBus.emit(AuthEvent.Unauthorized)
            }
        }

        return response
    }

    private fun isExternalUrl(host: String): Boolean {
        return host.endsWith(".scw.cloud")
    }

    /** Login / register / activate — 401 means bad credentials, not expired session. */
    private fun isCredentialAuthUrl(url: HttpUrl): Boolean {
        val path = url.encodedPath
        return path.contains("auth/login") ||
            path.contains("auth/register") ||
            path.contains("auth/resend-registration-email") ||
            path.endsWith("/activate") ||
            path.endsWith("activate")
    }
}