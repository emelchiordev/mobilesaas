package re.savio.mobile.data.repository

import com.google.gson.JsonParser
import re.savio.mobile.data.remote.api.ClientApi
import re.savio.mobile.data.remote.dto.CreateTenantClientRequestDto
import re.savio.mobile.data.remote.dto.CreateTenantClientResponseDto
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

sealed class CreateClientResult {
    data class Success(val response: CreateTenantClientResponseDto) : CreateClientResult()

    data class Error(val message: String) : CreateClientResult()
}

@Singleton
class ClientsRepository @Inject constructor(
    private val clientApi: ClientApi,
) {
    suspend fun createClient(body: CreateTenantClientRequestDto): CreateClientResult {
        return try {
            val response = clientApi.createClient(body)
            CreateClientResult.Success(response)
        } catch (e: Exception) {
            CreateClientResult.Error(humanReadableApiError(e))
        }
    }

    private fun humanReadableApiError(e: Throwable): String {
        if (e is HttpException) {
            runCatching {
                val raw = e.response()?.errorBody()?.string().orEmpty()
                if (raw.isBlank()) return@runCatching
                val root = JsonParser.parseString(raw).asJsonObject
                val msgEl = root.get("message") ?: return@runCatching
                when {
                    msgEl.isJsonPrimitive ->
                        msgEl.asString.trim().takeIf { it.isNotEmpty() }?.let { return it }

                    msgEl.isJsonArray -> {
                        val joined =
                            msgEl.asJsonArray.joinToString(" ") { je ->
                                je.asJsonPrimitive.asString.trim()
                            }
                        if (joined.isNotBlank()) return joined
                    }
                }
            }
            return "Erreur ${e.code()}"
        }
        return e.message ?: "Erreur réseau"
    }
}
