package re.savio.mobile.util

import org.junit.Assert.assertEquals
import org.junit.Test

class UnitEnergySummaryTest {
    @Test
    fun `unionne GAZ et ELEC depuis les racines actives`() {
        val result =
            UnitEnergySummary.compute(
                listOf(
                    UnitEnergyEquipmentInput(energyCode = "GAZ_NATUREL", energyLabel = "Gaz naturel"),
                    UnitEnergyEquipmentInput(energyCode = "ELECTRICITE", energyLabel = "Électricité"),
                ),
            )
        assertEquals(listOf("GAZ_NATUREL", "ELECTRICITE"), result.map { it.code })
        assertEquals(listOf("Gaz", "Élec"), result.map { it.label })
    }

    @Test
    fun `deduplique et ignore les enfants`() {
        val result =
            UnitEnergySummary.compute(
                listOf(
                    UnitEnergyEquipmentInput(energyCode = "GAZ"),
                    UnitEnergyEquipmentInput(energyCode = "GAZ"),
                    UnitEnergyEquipmentInput(energyCode = "GAZ", parentEquipmentId = "parent-1"),
                ),
            )
        assertEquals(1, result.size)
        assertEquals("GAZ", result.first().code)
    }

    @Test
    fun `ignore les appareils remplaces et sans energie`() {
        val result =
            UnitEnergySummary.compute(
                listOf(
                    UnitEnergyEquipmentInput(energyCode = "GAZ", status = "replaced"),
                    UnitEnergyEquipmentInput(),
                ),
            )
        assertEquals(emptyList<UnitEnergySummaryItem>(), result)
    }
}
