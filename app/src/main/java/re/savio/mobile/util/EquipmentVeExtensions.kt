package re.savio.mobile.util

import com.google.gson.JsonParser
import re.savio.mobile.data.local.entity.EquipmentEntity

fun sousTypeFromAttrsJson(attrsJson: String?): String? {
    if (attrsJson.isNullOrBlank()) return null
    return try {
        val obj = JsonParser.parseString(attrsJson).asJsonObject
        obj.get("sous_type")?.asString?.trim()?.lowercase()?.takeIf { it.isNotEmpty() }
    } catch (_: Exception) {
        null
    }
}

fun EquipmentEntity.toAttestableInput(): AttestableEquipmentInput =
    attestableFrom(
        id = id,
        typeCode = typeCode,
        energyCode = energyCode,
        hybridePacEquipmentId = hybridePacEquipmentId,
        sousType = sousTypeFromAttrsJson(attrsJson),
    )
