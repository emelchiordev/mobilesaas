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
        val token = runBlocking { tokenDataStore.accessToken.first() }
        val slug = runBlocking { tokenDataStore.societeSlug.first() }

        val request = chain.request().newBuilder().apply {
            token?.let { addHeader("Authorization", "Bearer $it") }
            // N'envoie le slug que s'il est non vide
            if (!slug.isNullOrBlank()) {
                addHeader("x-societe-slug", slug)
            }
        }.build()

        return chain.proceed(request)
    }
}