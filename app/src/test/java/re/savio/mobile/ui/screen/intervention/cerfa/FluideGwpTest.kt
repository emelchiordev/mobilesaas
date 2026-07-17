package re.savio.mobile.ui.screen.intervention.cerfa

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FluideGwpTest {

    @Test
    fun gwpFor_knownFluids() {
        assertEquals(675, FluideGwp.gwpFor("R32"))
        assertEquals(2088, FluideGwp.gwpFor("R410A"))
        assertEquals(1430, FluideGwp.gwpFor("r134a"))
        assertEquals(0, FluideGwp.gwpFor("R717"))
    }

    @Test
    fun gwpFor_unknown() {
        assertNull(FluideGwp.gwpFor(""))
        assertNull(FluideGwp.gwpFor("R999"))
        assertFalse(FluideGwp.hasKnownGwp("XYZ"))
        assertTrue(FluideGwp.hasKnownGwp("R32"))
    }

    @Test
    fun computeTonnage_r32_10kg() {
        // 10 × 675 / 1000 = 6.75
        assertEquals("6.75", FluideGwp.computeTonnageCo2e("R32", "10"))
    }

    @Test
    fun computeTonnage_r410a_commaCharge() {
        // 5,5 × 2088 / 1000 = 11.484
        assertEquals("11.484", FluideGwp.computeTonnageCo2e("R410A", "5,5"))
    }

    @Test
    fun computeTonnage_integerResult() {
        // 1000 × 1 / 1000 = 1
        assertEquals("1", FluideGwp.computeTonnageCo2e("R744", "1000"))
    }

    @Test
    fun computeTonnage_missingInputs() {
        assertNull(FluideGwp.computeTonnageCo2e("R32", ""))
        assertNull(FluideGwp.computeTonnageCo2e("", "10"))
        assertNull(FluideGwp.computeTonnageCo2e("R999", "10"))
    }
}
