package re.savio.mobile.ui.screen.intervention.cerfa

import java.util.Locale

/**
 * GWP (AR4 / valeurs admin SAVIO) — aligné sur `react/src/constants/fluides.ts`.
 * Tonnage CO₂e (t) = charge_kg × GWP / 1000.
 */
object FluideGwp {
    val BY_FLUIDE: Map<String, Int> =
        mapOf(
            "R32" to 675,
            "R454B" to 467,
            "R513A" to 631,
            "R1234yf" to 4,
            "R1234ze" to 6,
            "R290" to 3,
            "R600a" to 3,
            "R744" to 1,
            "R717" to 0,
            "R410A" to 2088,
            "R134a" to 1430,
            "R407C" to 1774,
            "R448A" to 1273,
            "R449A" to 1282,
            "R22" to 1810,
            "R404A" to 3922,
            "R507A" to 3985,
        )

    fun gwpFor(fluide: String): Int? {
        val key = fluide.trim()
        if (key.isEmpty()) return null
        BY_FLUIDE[key]?.let { return it }
        return BY_FLUIDE.entries.firstOrNull { it.key.equals(key, ignoreCase = true) }?.value
    }

    fun hasKnownGwp(fluide: String): Boolean = gwpFor(fluide) != null

    /**
     * @return tonnage en t CO₂e formaté, ou null si fluide/charge invalides.
     */
    fun computeTonnageCo2e(fluide: String, chargeKg: String): String? {
        val gwp = gwpFor(fluide) ?: return null
        val charge = chargeKg.replace(",", ".").toDoubleOrNull() ?: return null
        if (charge < 0.0) return null
        val tonnes = charge * gwp / 1000.0
        return formatTonnage(tonnes)
    }

    fun formatTonnage(tonnes: Double): String {
        if (tonnes == 0.0) return "0"
        return if (tonnes == tonnes.toLong().toDouble()) {
            tonnes.toLong().toString()
        } else {
            // Jusqu’à 3 décimales, sans zéros inutiles
            "%.3f".format(Locale.US, tonnes).trimEnd('0').trimEnd('.')
        }
    }
}
