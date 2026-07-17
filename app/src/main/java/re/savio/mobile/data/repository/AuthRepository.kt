package re.savio.mobile.data.repository

import com.google.gson.JsonParser
import re.savio.mobile.data.account.AccountSwitchCoordinator
import re.savio.mobile.data.local.database.TokenDataStore
import re.savio.mobile.data.remote.api.AuthApi
import re.savio.mobile.data.remote.dto.LoginRequestDto
import re.savio.mobile.data.remote.dto.LoginResponseDto
import re.savio.mobile.data.remote.dto.RegisterRequestDto
import re.savio.mobile.data.remote.dto.ResendRegistrationEmailRequestDto
import re.savio.mobile.data.remote.dto.SocieteDto
import re.savio.mobile.util.SavioSlugify
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

sealed class AuthResult {
    object Success : AuthResult()

    /** Flush→wipe→pull required; outgoing session still active, pending in TokenDataStore. */
    object NeedsAccountPrep : AuthResult()

    data class ChooseSociete(val societes: List<SocieteDto>) : AuthResult()

    data class Error(val message: String) : AuthResult()
}

sealed class RegisterResult {
    object SuccessContinueOnboarding : RegisterResult()

    object NeedsAccountPrep : RegisterResult()

    data class PendingEmailConfirmation(val message: String) : RegisterResult()

    data class ChooseSociete(val societes: List<SocieteDto>) : RegisterResult()

    data class Error(val message: String) : RegisterResult()
}

sealed class ResendRegistrationEmailResult {
    data class Success(val message: String) : ResendRegistrationEmailResult()

    data class Error(val message: String) : ResendRegistrationEmailResult()
}

sealed class ActivateAccountResult {
    data object Authenticated : ActivateAccountResult()

    data object NeedsAccountPrep : ActivateAccountResult()

    data class ChooseSociete(val societes: List<SocieteDto>) : ActivateAccountResult()

    data class Error(val message: String) : ActivateAccountResult()
}

sealed class LogoutResult {
    data object Success : LogoutResult()

    /** Flush failed — switch state stays FLUSHING; navigate to AccountPrep for retry/override. */
    data object NeedsAccountPrep : LogoutResult()
}

@Singleton
class AuthRepository @Inject constructor(
    private val authApi: AuthApi,
    private val tokenDataStore: TokenDataStore,
    private val accountSwitchCoordinator: AccountSwitchCoordinator,
) {

    suspend fun login(email: String, password: String): AuthResult {
        return try {
            val loginResponse =
                authApi.login(
                    LoginRequestDto(email = email, password = password),
                )
            finalizeSessionAfterToken(loginResponse, markOnboardingCompleted = true)
        } catch (e: Exception) {
            if (tokenDataStore.getAccessToken().isNullOrBlank()) {
                tokenDataStore.clearSession()
            }
            val message =
                if (e is HttpException && e.code() == 401) {
                    "Email ou mot de passe incorrect."
                } else {
                    humanReadableApiError(e)
                }
            AuthResult.Error(message = message)
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
                        val outgoing = captureOutgoingSession()
                        tokenDataStore.saveTokenOnly(token)
                        val me = authApi.me()
                        restoreOutgoingSession(outgoing)
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
                    AuthResult.NeedsAccountPrep -> RegisterResult.NeedsAccountPrep
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
            if (tokenDataStore.getAccessToken().isNullOrBlank()) {
                tokenDataStore.clearSession()
            }
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
                        AuthResult.NeedsAccountPrep -> ActivateAccountResult.NeedsAccountPrep
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
            if (tokenDataStore.getAccessToken().isNullOrBlank()) {
                tokenDataStore.clearSession()
            }
            ActivateAccountResult.Error(humanReadableApiError(e))
        }
    }

    suspend fun selectSociete(societe: SocieteDto, isRegistrationFlow: Boolean = false): AuthResult {
        val accessToken = tokenDataStore.getAccessToken() ?: ""
        val userId = tokenDataStore.getUserId() ?: ""
        val userEmail = tokenDataStore.getUserEmail() ?: ""
        val userFullName = tokenDataStore.getUserFullName()
        val pending = TokenDataStore.PendingSwitchSession(
            accessToken = accessToken,
            societeSlug = societe.slug,
            societeName = societe.name,
            userId = userId,
            userEmail = userEmail,
            userFullName = userFullName,
            onboardingCompleted = !isRegistrationFlow,
        )
        val hasLocal = accountSwitchCoordinator.hasLocalTenantData()
        if (
            accountSwitchCoordinator.requiresAccountPrep(
                newUserId = userId,
                newSocieteSlug = societe.slug,
                hasLocalData = hasLocal,
                currentUserId = tokenDataStore.getUserId(),
                currentSocieteSlug = tokenDataStore.getSocieteSlugStored(),
            )
        ) {
            accountSwitchCoordinator.beginAccountSwitch(
                pending = pending,
                outgoingUserId = tokenDataStore.getUserId(),
                outgoingSocieteSlug = tokenDataStore.getSocieteSlugStored(),
            )
            return AuthResult.NeedsAccountPrep
        }
        tokenDataStore.saveSession(
            accessToken = accessToken,
            societeSlug = societe.slug,
            societeName = societe.name,
            userId = userId,
            userEmail = userEmail,
            userFullName = userFullName,
        )
        tokenDataStore.setOnboardingCompleted(!isRegistrationFlow)
        accountSwitchCoordinator.writeFingerprintForCurrentSession()
        return AuthResult.Success
    }

    suspend fun logout(): LogoutResult {
        accountSwitchCoordinator.beginLogoutWipe()
        return when (val outcome = accountSwitchCoordinator.runOrResumePrep()) {
            is re.savio.mobile.data.account.AccountPrepOutcome.LogoutSuccess ->
                LogoutResult.Success
            is re.savio.mobile.data.account.AccountPrepOutcome.FlushFailed ->
                LogoutResult.NeedsAccountPrep
            else -> LogoutResult.Success
        }
    }

    private suspend fun finalizeSessionAfterToken(
        loginResponse: LoginResponseDto,
        markOnboardingCompleted: Boolean,
    ): AuthResult {
        val outgoing = captureOutgoingSession()

        tokenDataStore.saveTokenOnly(loginResponse.accessToken)
        val meResponse = try {
            authApi.me()
        } catch (e: Exception) {
            restoreOutgoingSession(outgoing)
            throw e
        }

        fun pendingFor(slug: String, name: String) =
            TokenDataStore.PendingSwitchSession(
                accessToken = loginResponse.accessToken,
                societeSlug = slug,
                societeName = name,
                userId = loginResponse.user.id,
                userEmail = loginResponse.user.email,
                userFullName = loginResponse.user.fullName,
                onboardingCompleted = markOnboardingCompleted,
            )

        suspend fun commitOrPrep(slug: String, name: String): AuthResult {
            val hasLocal = accountSwitchCoordinator.hasLocalTenantData()
            val needsPrep =
                accountSwitchCoordinator.requiresAccountPrep(
                    newUserId = loginResponse.user.id,
                    newSocieteSlug = slug,
                    hasLocalData = hasLocal,
                    currentUserId = outgoing?.userId,
                    currentSocieteSlug = outgoing?.societeSlug,
                )
            if (needsPrep) {
                // Keep outgoing session when still present so flush can use it.
                // If the outgoing account is gone (reset tenant, deleted tech), flush will fail
                // and AccountPrep offers retry + confirmed data-loss override.
                restoreOutgoingSession(outgoing)
                accountSwitchCoordinator.beginAccountSwitch(
                    pending = pendingFor(slug, name),
                    outgoingUserId = outgoing?.userId,
                    outgoingSocieteSlug = outgoing?.societeSlug,
                )
                return AuthResult.NeedsAccountPrep
            }
            tokenDataStore.saveSession(
                accessToken = loginResponse.accessToken,
                societeSlug = slug,
                societeName = name,
                userId = loginResponse.user.id,
                userEmail = loginResponse.user.email,
                userFullName = loginResponse.user.fullName,
            )
            tokenDataStore.setOnboardingCompleted(markOnboardingCompleted)
            accountSwitchCoordinator.writeFingerprintForCurrentSession()
            return AuthResult.Success
        }

        return when {
            meResponse.societe != null -> {
                val s = meResponse.societe
                commitOrPrep(s.slug, s.name)
            }

            else -> {
                val societes = meResponse.societes.orEmpty()
                when {
                    societes.size == 1 -> {
                        val societe = societes.first()
                        commitOrPrep(societe.slug, societe.name)
                    }

                    societes.isEmpty() -> {
                        restoreOutgoingSession(outgoing)
                        AuthResult.Error("Aucune société associée à ce compte.")
                    }

                    else -> {
                        tokenDataStore.saveSession(
                            accessToken = loginResponse.accessToken,
                            societeSlug = "",
                            societeName = "",
                            userId = loginResponse.user.id,
                            userEmail = loginResponse.user.email,
                            userFullName = loginResponse.user.fullName,
                        )
                        AuthResult.ChooseSociete(societes)
                    }
                }
            }
        }
    }

    private data class OutgoingSessionSnapshot(
        val accessToken: String,
        val societeSlug: String?,
        val societeName: String?,
        val userId: String?,
        val userEmail: String?,
        val userFullName: String?,
        val onboardingCompleted: Boolean,
    )

    private suspend fun captureOutgoingSession(): OutgoingSessionSnapshot? {
        val token = tokenDataStore.getAccessToken()?.takeIf { it.isNotBlank() } ?: return null
        return OutgoingSessionSnapshot(
            accessToken = token,
            societeSlug = tokenDataStore.getSocieteSlugStored(),
            societeName = tokenDataStore.getSocieteName(),
            userId = tokenDataStore.getUserId(),
            userEmail = tokenDataStore.getUserEmail(),
            userFullName = tokenDataStore.getUserFullName(),
            onboardingCompleted = tokenDataStore.isOnboardingCompletedFirst(),
        )
    }

    private suspend fun restoreOutgoingSession(outgoing: OutgoingSessionSnapshot?) {
        if (outgoing == null) {
            tokenDataStore.clearSession()
            return
        }
        val slug = outgoing.societeSlug
        if (!slug.isNullOrBlank()) {
            tokenDataStore.saveSession(
                accessToken = outgoing.accessToken,
                societeSlug = slug,
                societeName = outgoing.societeName.orEmpty(),
                userId = outgoing.userId.orEmpty(),
                userEmail = outgoing.userEmail.orEmpty(),
                userFullName = outgoing.userFullName,
            )
            tokenDataStore.setOnboardingCompleted(outgoing.onboardingCompleted)
        } else {
            tokenDataStore.saveTokenOnly(outgoing.accessToken)
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
