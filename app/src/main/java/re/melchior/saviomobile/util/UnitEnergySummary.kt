package re.melchior.saviomobile.util

import java.util.Locale

data class UnitEnergySummaryItem(
    val code: String,
    val label: String,
)

data class UnitEnergyEquipmentInput(
    val energyCode: String? = null,
    val energyLabel: String? = null,
    val parentEquipmentId: String? = null,
    val status: String? = null,
)

object UnitEnergySummary {
    private val ENERGY_SORT_ORDER =
        listOf(
            "GAZ",
            "FIOUL",
            "ELEC",
            "ELECTRICITE",
            "BOIS",
            "PAC",
            "CLIM",
            "PROPANE",
            "GRANULE",
        )

    fun compute(equipments: List<UnitEnergyEquipmentInput>): List<UnitEnergySummaryItem> {
        if (equipments.isEmpty()) return emptyList()

        val byCode = linkedMapOf<String, UnitEnergySummaryItem>()
        for (eq in equipments) {
            if (!isActive(eq)) continue
            if (!isRoot(eq)) continue
            val rawCode = pickEnergyCode(eq) ?: continue
            val code = normalizeEnergyCode(rawCode)
            if (code.isBlank() || byCode.containsKey(code)) continue
            byCode[code] =
                UnitEnergySummaryItem(
                    code = code,
                    label = shortEnergyLabel(code, eq.energyLabel),
                )
        }

        return byCode.values.sortedWith(
            compareBy<UnitEnergySummaryItem> { energySortRank(it.code) }
                .thenBy { it.label },
        )
    }

    private fun normalizeEnergyCode(raw: String): String =
        raw.trim().uppercase(Locale.ROOT).replace(Regex("\\s+"), "_")

    private fun energySortRank(code: String): Int {
        val up = normalizeEnergyCode(code)
        ENERGY_SORT_ORDER.forEachIndexed { index, key ->
            if (up == key || up.contains(key)) return index
        }
        return ENERGY_SORT_ORDER.size
    }

    private fun shortLabelFromCode(code: String): String? {
        val up = normalizeEnergyCode(code)
        return when {
            up.contains("GAZ") -> "Gaz"
            up.contains("FIOUL") -> "Fioul"
            up.contains("ELECTRIC") || up == "ELEC" -> "Élec"
            up.contains("BOIS") -> "Bois"
            up.contains("PAC") -> "PAC"
            up.contains("CLIM") -> "Clim"
            up.contains("PROPANE") -> "Propane"
            up.contains("GRANUL") -> "Granulés"
            else -> null
        }
    }

    private fun shortEnergyLabel(code: String, energyLabel: String?): String {
        val label = energyLabel?.trim().orEmpty()
        if (label.isNotEmpty()) {
            val parts = label.split(Regex("\\s+")).filter { it.isNotBlank() }
            if (parts.size > 1) {
                val first = parts.first()
                return first.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.FRANCE) else it.toString() }
            }
        }
        shortLabelFromCode(code)?.let { return it }
        if (label.isNotEmpty()) {
            return label.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.FRANCE) else it.toString() }
        }
        return code.trim()
    }

    private fun pickEnergyCode(eq: UnitEnergyEquipmentInput): String? {
        val code = eq.energyCode?.trim().orEmpty()
        if (code.isNotEmpty()) return code
        val label = eq.energyLabel?.trim().orEmpty()
        return label.takeIf { it.isNotEmpty() }
    }

    private fun isRoot(eq: UnitEnergyEquipmentInput): Boolean =
        eq.parentEquipmentId.isNullOrBlank()

    private fun isActive(eq: UnitEnergyEquipmentInput): Boolean =
        eq.status?.trim()?.lowercase(Locale.ROOT) != "replaced"
}
