package re.melchior.saviomobile.data.repository

import com.google.gson.JsonParser
import re.melchior.saviomobile.data.local.database.TokenDataStore
import re.melchior.saviomobile.data.remote.api.AuthApi
import re.melchior.saviomobile.data.remote.dto.LoginRequestDto
import re.melchior.saviomobile.data.remote.dto.LoginResponseDto
import re.melchior.saviomobile.data.remote.dto.MeResponseDto
import re.melchior.saviomobile.data.remote.dto.RegisterRequestDto
import re.melchior.saviomobile.data.remote.dto.ResendRegistrationEmailRequestDto
import re.melchior.saviomobile.data.remote.dto.SocieteDto
import re.melchior.saviomobile.util.SavioSlugify
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

sealed class AuthResult {
    object Success : AuthResult()

    data class ChooseSociete(val societes: List<SocieteDto>) : AuthResult()

    data class Error(val message: String) : AuthResult()
}

sealed class RegisterResult {
    /** Session complète ou token prêt — parcours onboarding affiché ensuite. */
    object SuccessContinueOnboarding : RegisterResult()

    /** Backend SaaS typique : pas de JWT tant que l’e-mail n’est pas confirmé. */
    data class PendingEmailConfirmation(val message: String) : RegisterResult()

    data class ChooseSociete(val societes: List<SocieteDto>) : RegisterResult()

    data class Error(val message: String) : RegisterResult()
}

sealed class ResendRegistrationEmailResult {
    data class Success(val message: String) : ResendRegistrationEmailResult()

    data class Error(val message: String) : ResendRegistrationEmailResult()
}

sealed class ActivateAccountResult {
    /** JWT persisté, session prête (ou choix société à faire). */
    data object Authenticated : ActivateAccountResult()

    data class ChooseSociete(val societes: List<SocieteDto>) : ActivateAccountResult()

    data class Error(val message: String) : ActivateAccountResult()
}

@Singleton
class AuthRepository @Inject constructor(
    private val authApi: AuthApi,
    private val tokenDataStore: TokenDataStore,
) {

    suspend fun login(email: String, password: String): AuthResult {
        return try {
            val loginResponse =
                authApi.login(
                    LoginRequestDto(email = email, password = password),
                )
            finalizeSessionAfterToken(loginResponse, markOnboardingCompleted = true)
        } catch (e: Exception) {
            tokenDataStore.clearSession()
            AuthResult.Error(message = humanReadableApiError(e))
        }
    }

    suspend fun register(
        companyName: String,
        email: String,
        password: String,
        contactFullName: String? = null,
    ): RegisterResult {
        return try {
            val slug = SavioSlugify.fromCompanyName(companyName)
            val nomTrimmed = contactFullName?.trim()?.takeIf { it.length >= 2 }
            val resp =
                authApi.register(
                    RegisterRequestDto(
                        nomSociete = companyName.trim(),
                        slug = slug,
                        email = email.trim(),
                        password = password,
                        nom = nomTrimmed,
                    ),
                )

            val token = resp.resolvedAccessToken()
            val userFromResp = resp.user

            if (!token.isNullOrBlank()) {
                val loginLike =
                    if (userFromResp != null) {
                        LoginResponseDto(accessToken = token, user = userFromResp)
                    } else {
                        tokenDataStore.saveTokenOnly(token)
                        val me = authApi.me()
                        LoginResponseDto(accessToken = token, user = me.user)
                    }
                when (
                    val auth =
                        finalizeSessionAfterToken(
                            loginLike,
                            markOnboardingCompleted = false,
                        )
                ) {
                    AuthResult.Success -> RegisterResult.SuccessContinueOnboarding
                    is AuthResult.ChooseSociete ->
                        RegisterResult.ChooseSociete(auth.societes)
                    is AuthResult.Error ->
                        RegisterResult.Error(auth.message)
                }
            } else {
                val msg = resp.message?.trim().takeUnless { it.isNullOrBlank() }
                if (msg != null) {
                    RegisterResult.PendingEmailConfirmation(msg)
                } else {
                    RegisterResult.Error("Réponse inscription invalide.")
                }
            }
        } catch (e: Exception) {
            tokenDataStore.clearSession()
            RegisterResult.Error(humanReadableApiError(e))
        }
    }

    suspend fun resendRegistrationEmail(email: String): ResendRegistrationEmailResult {
        return try {
            val resp =
                authApi.resendRegistrationEmail(
                    ResendRegistrationEmailRequestDto(email = email.trim()),
                )
            val msg =
                resp.message?.trim().takeUnless { it.isNullOrBlank() }
                    ?: "Si cette adresse correspond à un compte en attente, un e-mail vient d’être renvoyé."
            ResendRegistrationEmailResult.Success(msg)
        } catch (e: Exception) {
            ResendRegistrationEmailResult.Error(humanReadableApiError(e))
        }
    }

    suspend fun activateAccount(token: String): ActivateAccountResult {
        return try {
            val response = authApi.activateAccount(token.trim())
            if (response.isSuccessful) {
                val body = response.body()
                val jwt = body?.accessToken?.trim().orEmpty()
                if (body != null && jwt.isNotEmpty()) {
                    when (val r = finalizeSessionAfterToken(body, markOnboardingCompleted = true)) {
                        AuthResult.Success -> ActivateAccountResult.Authenticated
                        is AuthResult.ChooseSociete ->
                            ActivateAccountResult.ChooseSociete(r.societes)
                        is AuthResult.Error -> ActivateAccountResult.Error(r.message)
                    }
                } else {
                    ActivateAccountResult.Error("Réponse d’activation invalide.")
                }
            } else {
                val raw = response.errorBody()?.string().orEmpty()
                ActivateAccountResult.Error(parseActivateErrorMessage(raw))
            }
        } catch (e: Exception) {
            tokenDataStore.clearSession()
            ActivateAccountResult.Error(humanReadableApiError(e))
        }
    }

    suspend fun selectSociete(societe: SocieteDto, isRegistrationFlow: Boolean = false) {
        tokenDataStore.saveSession(
            accessToken = tokenDataStore.getAccessToken() ?: "",
            societeSlug = societe.slug,
            societeName = societe.name,
            userId = tokenDataStore.getUserId() ?: "",
            userEmail = tokenDataStore.getUserEmail() ?: "",
            userFullName = tokenDataStore.getUserFullName(),
        )
        tokenDataStore.setOnboardingCompleted(!isRegistrationFlow)
    }

    suspend fun logout() {
        tokenDataStore.clearSession()
    }

    /**
     * Après réception d’un JWT : hydrate `/mobile/me` et persiste la session si une société est résoluble.
     *
     * @param markOnboardingCompleted **true** après login plein succès ; **false** après inscription (wizard).
     */
    private suspend fun finalizeSessionAfterToken(
        loginResponse: LoginResponseDto,
        markOnboardingCompleted: Boolean,
    ): AuthResult {
        tokenDataStore.saveTokenOnly(loginResponse.accessToken)

        val meResponse = authApi.me()

        return when {
            meResponse.societe != null -> {
                val s = meResponse.societe
                tokenDataStore.saveSession(
                    accessToken = loginResponse.accessToken,
                    societeSlug = s.slug,
                    societeName = s.name,
                    userId = loginResponse.user.id,
                    userEmail = loginResponse.user.email,
                    userFullName = loginResponse.user.fullName,
                )
                tokenDataStore.setOnboardingCompleted(markOnboardingCompleted)
                AuthResult.Success
            }

            else -> {
                val societes = meResponse.societes.orEmpty()
                when {
                    societes.size == 1 -> {
                        val societe = societes.first()
                        tokenDataStore.saveSession(
                            accessToken = loginResponse.accessToken,
                            societeSlug = societe.slug,
                            societeName = societe.name,
                            userId = loginResponse.user.id,
                            userEmail = loginResponse.user.email,
                            userFullName = loginResponse.user.fullName,
                        )
                        tokenDataStore.setOnboardingCompleted(markOnboardingCompleted)
                        AuthResult.Success
                    }

                    societes.isEmpty() ->
                        AuthResult.Error("Aucune société associée à ce compte.")

                    else ->
                        AuthResult.ChooseSociete(societes)
                }
            }
        }
    }

    private fun parseActivateErrorMessage(raw: String): String {
        if (raw.isBlank()) return "Activation impossible."
        runCatching {
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
        return "Activation impossible."
    }

    /**
     * Parse minimal des erreurs Nest (`message` string ou tableau).
     */
    private fun humanReadableApiError(e: Throwable): String {
        if (e is HttpException) {
            runCatching {
                val raw = e.response()?.errorBody()?.string().orEmpty()
                if (raw.isBlank()) return@runCatching
                val root = JsonParser.parseString(raw).asJsonObject
                val msgEl = root.get("message") ?: return@runCatching
                when {
                    msgEl.isJsonPrimitive ->
                        msgEl.asString.trim().takeIf { it.isNotEmpty() }?.let {
                            return it
                        }

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
