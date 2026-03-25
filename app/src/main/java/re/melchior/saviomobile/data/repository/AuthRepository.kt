package re.melchior.saviomobile.data.repository

import re.melchior.saviomobile.data.local.database.TokenDataStore
import re.melchior.saviomobile.data.remote.api.AuthApi
import re.melchior.saviomobile.data.remote.dto.LoginRequestDto
import re.melchior.saviomobile.data.remote.dto.SocieteDto
import javax.inject.Inject
import javax.inject.Singleton

sealed class AuthResult {
    object Success : AuthResult()
    data class ChooseSociete(val societes: List<SocieteDto>) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

@Singleton
class AuthRepository @Inject constructor(
    private val authApi: AuthApi,
    private val tokenDataStore: TokenDataStore
) {

    suspend fun login(email: String, password: String): AuthResult {
        return try {
            // Login — récupère le token
            val loginResponse = authApi.login(
                LoginRequestDto(email = email, password = password)
            )

            // Sauvegarder UNIQUEMENT le token provisoire
            // sans déclencher isLoggedIn = true
            tokenDataStore.saveTokenOnly(loginResponse.accessToken)

            // Récupérer le contexte société
            val meResponse = authApi.me()

            if (meResponse.societe != null) {
                // Sauvegarder la session complète → isLoggedIn = true
                tokenDataStore.saveSession(
                    accessToken = loginResponse.accessToken,
                    societeSlug = meResponse.societe.slug,
                    societeName = meResponse.societe.name,
                    userId = loginResponse.user.id,
                    userEmail = loginResponse.user.email,
                    userFullName = loginResponse.user.fullName
                )
                AuthResult.Success
            } else {
                val societes = meResponse.societes.orEmpty()
                if (societes.size == 1) {
                    val societe = societes.first()
                    tokenDataStore.saveSession(
                        accessToken = loginResponse.accessToken,
                        societeSlug = societe.slug,
                        societeName = societe.name,
                        userId = loginResponse.user.id,
                        userEmail = loginResponse.user.email,
                        userFullName = loginResponse.user.fullName
                    )
                    AuthResult.Success
                } else {
                    AuthResult.ChooseSociete(societes)
                }
            }

        } catch (e: Exception) {
            tokenDataStore.clearSession()
            AuthResult.Error(message = e.message ?: "Erreur de connexion")
        }
    }

    suspend fun selectSociete(societe: SocieteDto) {
        tokenDataStore.saveSession(
            accessToken = tokenDataStore.getAccessToken() ?: "",
            societeSlug = societe.slug,
            societeName = societe.name,
            userId = tokenDataStore.getUserId() ?: "",
            userEmail = tokenDataStore.getUserEmail() ?: "",
            userFullName = tokenDataStore.getUserFullName()
        )
    }

    suspend fun logout() {
        tokenDataStore.clearSession()
    }
}