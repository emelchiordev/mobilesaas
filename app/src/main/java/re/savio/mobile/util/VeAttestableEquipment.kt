package re.savio.mobile.util

interface AttestableEquipmentInput {
    val id: String
    val typeCode: String?
    val energyCode: String?
    val status: String?
    val hybridePacEquipmentId: String?
    val sousType: String?
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
    override val sousType: String? = null,
) : AttestableEquipmentInput

fun attestableFrom(
    id: String,
    typeCode: String?,
    energyCode: String?,
    status: String? = null,
    hybridePacEquipmentId: String?,
    sousType: String? = null,
): AttestableEquipmentInput =
    SimpleAttestableEquipment(
        id = id,
        typeCode = typeCode,
        energyCode = energyCode,
        status = status,
        hybridePacEquipmentId = hybridePacEquipmentId,
        sousType = sousType,
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

fun isEcsControlEquipment(equipment: AttestableEquipmentInput): Boolean =
    resolveVeScope(equipment) == VeScope.ECS_ACCUMULATION

fun suggestedEcsControlType(equipment: AttestableEquipmentInput): String? =
    if (isEcsControlEquipment(equipment)) "ECS" else null

/** Clé attestation légale dérivée du scope VE (`ve-scope.util.ts`). ECS exclu. */
fun suggestedAttestationType(equipment: AttestableEquipmentInput): String? {
    val scope = resolveVeScope(equipment) ?: return null
    if (scope == VeScope.ECS_ACCUMULATION) return null
    return attestationTypeKeyFromScope(scope)
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
