package re.melchior.saviomobile.data.remote.dto

import com.google.gson.annotations.SerializedName

data class LoginRequestDto(
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String
)

data class ResendRegistrationEmailRequestDto(
    @SerializedName("email")
    val email: String,
)

data class LoginResponseDto(
    @SerializedName("access_token")
    val accessToken: String,
    @SerializedName("user")
    val user: UserDto,
    @SerializedName("societe")
    val societe: SocieteDto? = null,
    @SerializedName("societes")
    val societes: List<SocieteDto>? = null,
)

data class UserDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("fullName")
    val fullName: String?,
    @SerializedName("clientId")
    val clientId: String? = null,
)

/** Aligné backend SaaS (`nomSociete` + `slug` comme le front web). */
data class RegisterRequestDto(
    @SerializedName("nomSociete")
    val nomSociete: String,
    @SerializedName("slug")
    val slug: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String,
    /** Prénom et nom du créateur — fiche technicien admin (optionnel : sinon nom société côté serveur). */
    @SerializedName("nom")
    val nom: String? = null,
)

/**
 * Réponse inscription : soit message uniquement (e-mail à confirmer), soit JWT comme au login.
 */
data class RegisterResponseDto(
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("access_token")
    val accessTokenSnake: String? = null,
    @SerializedName("accessToken")
    val accessTokenCamel: String? = null,
    @SerializedName("user")
    val user: UserDto? = null,
) {
    fun resolvedAccessToken(): String? =
        accessTokenSnake?.takeIf { it.isNotBlank() } ?: accessTokenCamel?.takeIf { it.isNotBlank() }
}
