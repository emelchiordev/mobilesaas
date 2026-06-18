package re.melchior.saviomobile.util

interface AttestableEquipmentInput {
    val id: String
    val typeCode: String?
    val energyCode: String?
    val status: String?
    val hybridePacEquipmentId: String?
}

fun hybridePacEquipmentIdFrom(attrsHybridePacId: String?): String? {
    val s = attrsHybridePacId?.trim().orEmpty()
    return s.ifEmpty { null }
}

data class SimpleAttestableEquipment(
    override val id: String,
    override val typeCode: String?,
    override val energyCode: String?,
    override val status: String? = null,
    override val hybridePacEquipmentId: String?,
) : AttestableEquipmentInput

fun attestableFrom(
    id: String,
    typeCode: String?,
    energyCode: String?,
    status: String? = null,
    hybridePacEquipmentId: String?,
): AttestableEquipmentInput =
    SimpleAttestableEquipment(
        id = id,
        typeCode = typeCode,
        energyCode = energyCode,
        status = status,
        hybridePacEquipmentId = hybridePacEquipmentId,
    )

fun isEquipmentReplaced(equipment: AttestableEquipmentInput): Boolean {
    if (equipment.status?.trim()?.lowercase() == "replaced") return true
    return equipment.typeCode?.trim()?.lowercase() == "replaced"
}

fun isBruleurEquipment(equipment: AttestableEquipmentInput): Boolean =
    equipment.typeCode?.trim()?.uppercase() == "BRULEUR"

fun isPacTypeCode(typeCode: String): Boolean {
    val tc = typeCode.trim().uppercase()
    return tc == "PAC" || tc == "PAC A/E" || tc == "PAC A/A"
}

fun isPartOfHybrideAsPac(
    equipment: AttestableEquipmentInput,
    unitEquipments: List<AttestableEquipmentInput>,
): Boolean {
    val typeCodeNorm = equipment.typeCode?.trim()?.uppercase().orEmpty()
    if (!isPacTypeCode(typeCodeNorm)) return false
    return unitEquipments.any { eq ->
        val tc = eq.typeCode?.trim()?.uppercase().orEmpty()
        val hy = hybridePacEquipmentIdFrom(eq.hybridePacEquipmentId)
        tc == "CHAUDIERE" && hy != null && hy == equipment.id
    }
}

/** Même heuristique que `ve-attestable-equipment.util.ts` (suggestedAttestationType). */
fun suggestedAttestationType(equipment: AttestableEquipmentInput): String? {
    val tc = equipment.typeCode?.uppercase().orEmpty()
    val ec = equipment.energyCode?.uppercase().orEmpty()
    val isHybride = hybridePacEquipmentIdFrom(equipment.hybridePacEquipmentId) != null

    if (tc == "CHAUDIERE" && isHybride) {
        return if (ec.contains("FIOUL") || ec.contains("FUEL")) "PAC_HYBRIDE_FIOUL" else "PAC_HYBRIDE_GAZ"
    }
    if (tc == "CHAUDIERE") {
        if (ec.contains("FIOUL") || ec.contains("FUEL") || tc.contains("FIOUL")) return "FIOUL"
        if (ec.contains("GAZ") || tc.contains("GAZ")) return "GAZ"
        return null
    }
    if (tc.contains("PAC") && (tc.contains("HYBRIDE") || ec.contains("GAZ")) && ec.contains("GAZ")) {
        return "PAC_HYBRIDE_GAZ"
    }
    if (
        tc.contains("PAC") &&
        (tc.contains("HYBRIDE") || ec.contains("FIOUL") || ec.contains("FUEL")) &&
        (ec.contains("FIOUL") || ec.contains("FUEL"))
    ) {
        return "PAC_HYBRIDE_FIOUL"
    }
    if (tc.contains("PAC") || tc.contains("THERMODYNAMIQUE")) return "PAC"
    if (ec.contains("GAZ") || tc.contains("GAZ")) return "GAZ"
    if (ec.contains("FIOUL") || ec.contains("FUEL") || tc.contains("FIOUL")) return "FIOUL"
    if (ec.contains("BOIS") || tc.contains("BOIS")) return "BOIS"
    return null
}

fun isAttestable(
    equipment: AttestableEquipmentInput,
    unitEquipments: List<AttestableEquipmentInput>,
): Boolean {
    if (isEquipmentReplaced(equipment)) return false
    if (isBruleurEquipment(equipment)) return false
    if (isPartOfHybrideAsPac(equipment, unitEquipments)) return false
    return suggestedAttestationType(equipment) != null
}

fun attestationAnchorEquipmentId(
    equipment: AttestableEquipmentInput,
    unitEquipments: List<AttestableEquipmentInput>,
): String? {
    if (!isAttestable(equipment, unitEquipments)) return null
    return equipment.id
}
