package re.melchior.saviomobile.data.repository

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import re.melchior.saviomobile.data.remote.api.BanAddressSearchApi
import re.melchior.saviomobile.data.remote.dto.BanAddressSuggestionDto
import re.melchior.saviomobile.data.remote.dto.toBanAddressPick
import javax.inject.Inject
import javax.inject.Singleton

data class BanAddressPick(
    val label: String,
    val street: String,
    val postalCode: String,
    val city: String,
    val latitude: Double?,
    val longitude: Double?,
)

@Singleton
class BanAddressSearchRepository @Inject constructor(
    private val api: BanAddressSearchApi,
) {
    private val gson = Gson()
    suspend fun search(q: String): List<BanAddressPick> {
        val trimmed = q.trim()
        if (trimmed.length < 2) return emptyList()
        return searchResult(trimmed).getOrElse { emptyList() }
    }

    /** Succès (liste éventuellement vide) ou échec réseau / HTTP / parse. */
    suspend fun searchResult(q: String): Result<List<BanAddressPick>> {
        val trimmed = q.trim()
        if (trimmed.length < 2) return Result.success(emptyList())
        return runCatching {
            val root = api.search(trimmed, 10)
            parseBanSearchRoot(root)
        }
    }

    private fun parseBanSearchRoot(root: JsonElement): List<BanAddressPick> {
        when {
            root.isJsonArray -> return root.asJsonArray.mapNotNull { parseBanItem(it) }
            root.isJsonObject -> {
                val o = root.asJsonObject
                for (key in listOf("results", "data", "features", "adresses", "items", "rows", "suggestions")) {
                    val el = o.get(key) ?: continue
                    if (el.isJsonArray) {
                        return el.asJsonArray.mapNotNull { parseBanItem(it) }
                    }
                }
            }
        }
        return emptyList()
    }

    private fun parseBanItem(el: JsonElement): BanAddressPick? {
        if (!el.isJsonObject) return null
        runCatching {
            gson.fromJson(el, BanAddressSuggestionDto::class.java)?.toBanAddressPick()
        }.getOrNull()?.let { return it }
        val o = el.asJsonObject
        if (o.has("properties") && o.has("geometry")) {
            parseGeoJsonFeature(o)?.let { return it }
        }
        return parseFlatAddress(o)
    }

    /** GeoJSON Point : `coordinates[0]` = longitude, `coordinates[1]` = latitude. */
    private fun latLngFromGeometry(o: JsonObject): Pair<Double, Double>? {
        val geom = o.getAsJsonObject("geometry") ?: return null
        val coords = geom.get("coordinates")?.takeIf { it.isJsonArray }?.asJsonArray ?: return null
        if (coords.size() < 2) return null
        return runCatching {
            val lng = coords[0].asDouble
            val lat = coords[1].asDouble
            Pair(lat, lng)
        }.getOrNull()
    }

    private fun parseGeoJsonFeature(feature: JsonObject): BanAddressPick? {
        val props = feature.getAsJsonObject("properties")
        val label = stringProp(props, "label", "name", "libelle", "formatted_address") ?: return null
        val geom = feature.getAsJsonObject("geometry") ?: return null
        val coords = geom.get("coordinates")?.takeIf { it.isJsonArray }?.asJsonArray ?: return null
        if (coords.size() < 2) return null
        val lng = coords[0].asDouble
        val lat = coords[1].asDouble
        val street =
            stringProp(props, "street", "name", "label")?.ifBlank { label } ?: label
        val city =
            stringProp(props, "ville", "city", "commune", "nom_commune", "nomCommune", "town") ?: ""
        val postal =
            stringProp(props, "codePostal", "code_postal", "postcode", "postalCode", "zipcode", "cp")
                ?: ""
        return BanAddressPick(
            label = label,
            street = street.trim().ifEmpty { label },
            postalCode = postal.trim(),
            city = city.trim().ifEmpty { "—" },
            latitude = lat,
            longitude = lng,
        )
    }

    private fun parseFlatAddress(o: JsonObject): BanAddressPick? {
        val label =
            stringProp(
                o,
                "label",
                "libelle",
                "libellé",
                "formatted_address",
                "adresse",
                "fullAddress",
                "text",
            ) ?: return null
        val street =
            stringProp(o, "street", "rue", "voie", "address", "streetLine", "street_line", "name")
                ?: label
        val city =
            stringProp(o, "ville", "city", "commune", "town", "nom_commune", "nomCommune", "locality")
                ?: ""
        val postal =
            stringProp(o, "codePostal", "code_postal", "postcode", "postalCode", "zipCode", "zipcode", "cp")
                ?: ""
        val geomLatLng = latLngFromGeometry(o)
        val lat = doubleProp(o, "latitude", "lat", "y") ?: geomLatLng?.first
        val lng = doubleProp(o, "longitude", "lon", "lng", "x") ?: geomLatLng?.second
        return BanAddressPick(
            label = label,
            street = street.trim().ifEmpty { label },
            postalCode = postal.trim(),
            city = city.trim().ifEmpty { "—" },
            latitude = lat,
            longitude = lng,
        )
    }

    private fun stringProp(o: JsonObject, vararg keys: String): String? {
        for (k in keys) {
            val v = o.get(k) ?: continue
            if (v.isJsonPrimitive && (v as JsonPrimitive).isString) {
                val s = v.asString.trim()
                if (s.isNotEmpty()) return s
            }
        }
        return null
    }

    private fun doubleProp(o: JsonObject, vararg keys: String): Double? {
        for (k in keys) {
            val v = o.get(k) ?: continue
            when {
                v.isJsonPrimitive && v.asJsonPrimitive.isNumber ->
                    return v.asDouble

                v.isJsonPrimitive && v.asJsonPrimitive.isString ->
                    v.asString.trim().replace(',', '.').toDoubleOrNull()?.let { return it }
            }
        }
        return null
    }
}
