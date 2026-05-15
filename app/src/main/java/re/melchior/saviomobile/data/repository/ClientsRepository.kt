package re.melchior.saviomobile.data.repository

import com.google.gson.JsonParser
import re.melchior.saviomobile.data.remote.api.ClientApi
import re.melchior.saviomobile.data.remote.dto.CreateTenantClientRequestDto
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

sealed class CreateClientResult {
    data object Success : CreateClientResult()

    data class Error(val message: String) : CreateClientResult()
}

@Singleton
class ClientsRepository @Inject constructor(
    private val clientApi: ClientApi,
) {
    suspend fun createClient(body: CreateTenantClientRequestDto): CreateClientResult {
        return try {
            clientApi.createClient(body)
            CreateClientResult.Success
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
