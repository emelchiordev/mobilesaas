package re.savio.mobile.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class VeScopeTest {

    @Test
    fun chaudiereGaz_mapsToChGaz_notEcs() {
        val equipment = attestableFrom(
            id = "eq-1",
            typeCode = "CHAUDIERE",
            energyCode = "GAZ",
            hybridePacEquipmentId = null,
        )
        assertEquals(VeScope.CH_GAZ, resolveVeScope(equipment))
        assertEquals("GAZ", suggestedAttestationType(equipment))
        assertNull(suggestedEcsControlType(equipment))
    }

    @Test
    fun chauffeEauGaz_mapsToEcs_notGazAttestation() {
        val equipment = attestableFrom(
            id = "eq-ce",
            typeCode = "CHAUFFE EAU",
            energyCode = "GAZ NAT",
            hybridePacEquipmentId = null,
            sousType = "accumulation",
        )
        assertEquals(VeScope.ECS_ACCUMULATION, resolveVeScope(equipment))
        assertNull(suggestedAttestationType(equipment))
        assertEquals("ECS", suggestedEcsControlType(equipment))
    }

    @Test
    fun chauffeEauInstant_notAttestable() {
        val equipment = attestableFrom(
            id = "eq-inst",
            typeCode = "CHAUFFE EAU",
            energyCode = "GAZ NAT",
            hybridePacEquipmentId = null,
            sousType = "instantane",
        )
        assertNull(resolveVeScope(equipment))
        assertNull(suggestedAttestationType(equipment))
        assertNull(suggestedEcsControlType(equipment))
    }

    @Test
    fun ballonEcsElec_mapsToEcsControl() {
        val equipment = attestableFrom(
            id = "eq-ballon",
            typeCode = "BALLON ECS",
            energyCode = "ELEC",
            hybridePacEquipmentId = null,
        )
        assertNull(suggestedAttestationType(equipment))
        assertEquals("ECS", suggestedEcsControlType(equipment))
    }
}
