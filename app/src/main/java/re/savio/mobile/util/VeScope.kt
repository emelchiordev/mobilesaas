package re.savio.mobile.util

enum class VeFunctionalCategory {
    ECS_STORAGE,
    CH_GENERATOR,
    PAC,
    COMPONENT,
    UNKNOWN,
}

enum class VeScope {
    CH_GAZ,
    CH_FIOUL,
    CH_BOIS,
    PAC,
    PAC_HYB_GAZ,
    PAC_HYB_FIOUL,
    ECS_ACCUMULATION,
}

enum class VeInstallationTab {
    FULL,
    ECS_REDUCED,
    PAC,
}

data class VeProfile(
    val scope: VeScope,
    val attestationTypeKey: String,
    val pdfTemplate: String?,
    val installationTab: VeInstallationTab,
)

private val ECS_STORAGE_TYPE_CODES = setOf(
    "CHAUFFE EAU",
    "BALLON ECS",
    "BALLON",
)

private val COMPONENT_TYPE_CODES = setOf(
    "BRULEUR",
    "UNITE_EXT",
    "UNITE_INT",
    "BALLON_TAMPON",
)

private fun normTypeCode(typeCode: String?): String =
    typeCode?.trim()?.uppercase().orEmpty()

private fun normEnergy(energyCode: String?): String =
    energyCode?.trim()?.uppercase().orEmpty()

fun resolveFunctionalCategory(typeCode: String?): VeFunctionalCategory {
    val tc = normTypeCode(typeCode)
    if (tc.isEmpty()) return VeFunctionalCategory.UNKNOWN
    if (tc in ECS_STORAGE_TYPE_CODES) return VeFunctionalCategory.ECS_STORAGE
    if (tc == "CHAUDIERE") return VeFunctionalCategory.CH_GENERATOR
    if (isPacTypeCode(tc) || tc.contains("PAC") || tc.contains("THERMODYNAMIQUE")) {
        return VeFunctionalCategory.PAC
    }
    if (tc in COMPONENT_TYPE_CODES) return VeFunctionalCategory.COMPONENT
    return VeFunctionalCategory.UNKNOWN
}

private fun sousTypeFrom(equipment: AttestableEquipmentInput): String? {
    val s = equipment.sousType?.trim()?.lowercase().orEmpty()
    return s.ifEmpty { null }
}

/** Même logique que `ve-scope.util.ts`. */
fun resolveVeScope(equipment: AttestableEquipmentInput): VeScope? {
    val tc = normTypeCode(equipment.typeCode)
    val ec = normEnergy(equipment.energyCode)
    when (resolveFunctionalCategory(tc)) {
        VeFunctionalCategory.ECS_STORAGE -> {
            if (sousTypeFrom(equipment) == "instantane") return null
            return VeScope.ECS_ACCUMULATION
        }
        else -> Unit
    }

    val isHybride = hybridePacEquipmentIdFrom(equipment.hybridePacEquipmentId) != null

    if (tc == "CHAUDIERE" && isHybride) {
        return if (ec.contains("FIOUL") || ec.contains("FUEL")) {
            VeScope.PAC_HYB_FIOUL
        } else {
            VeScope.PAC_HYB_GAZ
        }
    }
    if (tc == "CHAUDIERE") {
        if (ec.contains("FIOUL") || ec.contains("FUEL") || tc.contains("FIOUL")) return VeScope.CH_FIOUL
        if (ec.contains("GAZ") || tc.contains("GAZ")) return VeScope.CH_GAZ
        return null
    }
    if (tc.contains("PAC") && (tc.contains("HYBRIDE") || ec.contains("GAZ")) && ec.contains("GAZ")) {
        return VeScope.PAC_HYB_GAZ
    }
    if (
        tc.contains("PAC") &&
        (tc.contains("HYBRIDE") || ec.contains("FIOUL") || ec.contains("FUEL")) &&
        (ec.contains("FIOUL") || ec.contains("FUEL"))
    ) {
        return VeScope.PAC_HYB_FIOUL
    }
    if (tc.contains("PAC") || tc.contains("THERMODYNAMIQUE")) return VeScope.PAC
    if (ec.contains("BOIS") || tc.contains("BOIS")) return VeScope.CH_BOIS
    return null
}

fun attestationTypeKeyFromScope(scope: VeScope): String =
    when (scope) {
        VeScope.CH_GAZ -> "GAZ"
        VeScope.CH_FIOUL -> "FIOUL"
        VeScope.CH_BOIS -> "BOIS"
        VeScope.PAC -> "PAC"
        VeScope.PAC_HYB_GAZ -> "PAC_HYBRIDE_GAZ"
        VeScope.PAC_HYB_FIOUL -> "PAC_HYBRIDE_FIOUL"
        VeScope.ECS_ACCUMULATION -> "ECS"
    }

fun resolveVeProfile(equipment: AttestableEquipmentInput): VeProfile? {
    val scope = resolveVeScope(equipment) ?: return null
    val installationTab = when (scope) {
        VeScope.ECS_ACCUMULATION -> VeInstallationTab.ECS_REDUCED
        VeScope.PAC -> VeInstallationTab.PAC
        else -> VeInstallationTab.FULL
    }
    val pdfTemplate = when (scope) {
        VeScope.CH_GAZ -> "ATT_GAZ3"
        VeScope.CH_FIOUL -> "ATT_FIOUL"
        VeScope.CH_BOIS -> "ATT_BOIS"
        VeScope.PAC -> "ATT_PAC"
        VeScope.PAC_HYB_GAZ -> "ATT_HYB_GAZ"
        VeScope.PAC_HYB_FIOUL -> "ATT_HYB_FIOUL"
        VeScope.ECS_ACCUMULATION -> null
    }
    return VeProfile(
        scope = scope,
        attestationTypeKey = attestationTypeKeyFromScope(scope),
        pdfTemplate = pdfTemplate,
        installationTab = installationTab,
    )
}
