package re.melchior.saviomobile.data.remote.dto

import com.google.gson.annotations.SerializedName

data class MeResponseDto(
    @SerializedName("user")
    val user: UserDto,
    @SerializedName("societe")
    val societe: SocieteDto?,
    @SerializedName("societes")
    val societes: List<SocieteDto>?
)

data class SocieteDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("slug")
    val slug: String,
    @SerializedName("isActive")
    val isActive: Boolean = true
)