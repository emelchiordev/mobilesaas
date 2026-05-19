package re.melchior.saviomobile.data.remote.dto

import com.google.gson.annotations.SerializedName
import re.melchior.saviomobile.data.repository.BanAddressPick

/**
 * Élément renvoyé par `GET /api/addresses/autocomplete` (proxy BAN / Codepo).
 */
data class BanAddressSuggestionDto(
    val id: String? = null,
    val numero: String? = null,
    @SerializedName("typeVoie")
    val typeVoie: String? = null,
    @SerializedName("nomVoie")
    val nomVoie: String? = null,
    @SerializedName("codePostal")
    val codePostal: String? = null,
    val ville: String? = null,
    @SerializedName("nomCommune")
    val nomCommune: String? = null,
    val lat: String? = null,
    val lon: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val label: String? = null,
    val score: Double? = null,
)

fun BanAddressSuggestionDto.toBanAddressPick(): BanAddressPick? {
    val streetLine =
        listOfNotNull(
            numero?.trim()?.takeIf { it.isNotEmpty() },
            typeVoie?.trim()?.takeIf { it.isNotEmpty() },
            nomVoie?.trim()?.takeIf { it.isNotEmpty() },
        ).joinToString(" ")
            .trim()

    val postal = codePostal?.trim().orEmpty()
    val cityName =
        ville?.trim()?.takeIf { it.isNotEmpty() }
            ?: nomCommune?.trim().orEmpty()

    val displayLabel =
        label?.trim()?.takeIf { it.isNotEmpty() }
            ?: listOfNotNull(
                streetLine.ifEmpty { null },
                listOf(postal, cityName)
                    .filter { it.isNotEmpty() }
                    .joinToString(" ")
                    .ifEmpty { null },
            ).joinToString(" ")
                .ifEmpty { null }

    if (displayLabel.isNullOrBlank() && streetLine.isBlank() && postal.isBlank() && cityName.isBlank()) {
        return null
    }

    val latValue =
        latitude
            ?: lat?.trim()?.replace(',', '.')?.toDoubleOrNull()
    val lonValue =
        longitude
            ?: lon?.trim()?.replace(',', '.')?.toDoubleOrNull()

    return BanAddressPick(
        label = displayLabel ?: streetLine.ifEmpty { postal },
        street = streetLine.ifEmpty { displayLabel ?: postal },
        postalCode = postal,
        city = cityName.ifEmpty { "—" },
        latitude = latValue,
        longitude = lonValue,
    )
}
