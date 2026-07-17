package re.savio.mobile.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.ZoneId

class ScheduledAtFormatTest {

    private val paris = ZoneId.of("Europe/Paris")

    @Test
    fun formatCompletedAtDisplay_frenchDateAndTime() {
        val iso = "2026-06-22T05:39:00Z"
        assertEquals("22/06/2026 à 07h39", formatCompletedAtDisplay(iso, paris))
    }
}
