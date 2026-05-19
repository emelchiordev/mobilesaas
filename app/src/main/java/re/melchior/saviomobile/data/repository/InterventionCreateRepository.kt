package re.melchior.saviomobile.data.repository

import com.google.gson.JsonParser
import re.melchior.saviomobile.data.remote.api.CustomerSearchApi
import re.melchior.saviomobile.data.remote.api.InterventionApi
import re.melchior.saviomobile.data.remote.dto.CreateInterventionRequestDto
import re.melchior.saviomobile.data.remote.dto.CustomerSearchRowDto
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton

sealed class CreateInterventionOutcome {
    data class Success(val interventionId: String) : CreateInterventionOutcome()
    data object NetworkError : CreateInterventionOutcome()
    data class ServerError(val message: String) : CreateInterventionOutcome()
}

sealed class CustomerSearchOutcome {
    data class Success(val items: List<CustomerSearchRowDto>) : CustomerSearchOutcome()
    data object NetworkError : CustomerSearchOutcome()
    data class Error(val message: String) : CustomerSearchOutcome()
}

@Singleton
class InterventionCreateRepository @Inject constructor(
    private val customerSearchApi: CustomerSearchApi,
    private val interventionApi: InterventionApi,
) {
    suspend fun searchCustomers(query: String, limit: Int = 10): CustomerSearchOutcome {
        return try {
            val page = customerSearchApi.search(query = query, limit = limit)
            CustomerSearchOutcome.Success(page.items)
        } catch (e: Exception) {
            if (isNetworkFailure(e)) CustomerSearchOutcome.NetworkError
            else CustomerSearchOutcome.Error(humanReadableApiError(e))
        }
    }

    suspend fun createIntervention(body: CreateInterventionRequestDto): CreateInterventionOutcome {
        return try {
            val response = interventionApi.create(body)
            CreateInterventionOutcome.Success(response.id)
        } catch (e: Exception) {
            if (isNetworkFailure(e)) CreateInterventionOutcome.NetworkError
            else CreateInterventionOutcome.ServerError(humanReadableApiError(e))
        }
    }

    private fun isNetworkFailure(e: Throwable): Boolean =
        e is IOException ||
            e is UnknownHostException ||
            e is SocketTimeoutException ||
            (e is HttpException && e.code() in setOf(502, 503, 504))

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
