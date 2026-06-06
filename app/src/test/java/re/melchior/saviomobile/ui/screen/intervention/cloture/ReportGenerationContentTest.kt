package re.melchior.saviomobile.ui.screen.intervention.cloture

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import re.melchior.saviomobile.data.local.entity.InstallationCheckEntity

class ReportGenerationContentTest {

    @Test
    fun refusesEquipmentOnlyContext() {
        val result =
            assessReportGenerationContent(
                technicianNotes = null,
                invoiceLines = emptyList(),
                anomalyDrafts = emptyList(),
                attestations = emptyList(),
                measures = emptyList(),
                pacMeasures = emptyList(),
                coldMeasureCount = 0,
                installationCheck = null,
            )
        assertFalse(result.canGenerate)
        assertEquals(0, result.technicalFactsCount)
    }

    @Test
    fun acceptsInstallationCheckTurbidity() {
        val result =
            assessReportGenerationContent(
                technicianNotes = null,
                invoiceLines = emptyList(),
                anomalyDrafts = emptyList(),
                attestations = emptyList(),
                measures = emptyList(),
                pacMeasures = emptyList(),
                coldMeasureCount = 0,
                installationCheck =
                    InstallationCheckEntity(
                        interventionId = "i1",
                        turbidityTested = true,
                        turbidityNtu = "15",
                        turbidityState = "embouee",
                    ),
            )
        assertTrue(result.canGenerate)
        assertTrue(result.technicalFactsCount > 0)
    }

    @Test
    fun acceptsTechnicianObservations() {
        val result =
            assessReportGenerationContent(
                technicianNotes = "Fuite réparée",
                invoiceLines = emptyList(),
                anomalyDrafts = emptyList(),
                attestations = emptyList(),
                measures = emptyList(),
                pacMeasures = emptyList(),
                coldMeasureCount = 0,
                installationCheck = null,
            )
        assertTrue(result.canGenerate)
        assertTrue(result.hasExplicitContent)
    }
}
