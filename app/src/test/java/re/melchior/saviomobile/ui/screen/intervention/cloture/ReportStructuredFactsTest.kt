package re.melchior.saviomobile.ui.screen.intervention.cloture

import org.junit.Assert.assertTrue
import org.junit.Test
import re.melchior.saviomobile.data.local.entity.InstallationCheckEntity

class ReportStructuredFactsTest {

    @Test
    fun installationBlock_includesTurbidityAndGasPipe() {
        val blocks =
            buildStructuredFactsBlocks(
                equipments = emptyList(),
                attestations = emptyList(),
                measures = emptyList(),
                pacMeasures = emptyList(),
                anomalyDrafts = emptyList(),
                anomalyCatalog = emptyList(),
                invoiceLines = emptyList(),
                installationCheck =
                    InstallationCheckEntity(
                        interventionId = "i1",
                        turbidityTested = true,
                        turbidityNtu = "15",
                        turbidityState = "embouee",
                        gasPipeType = "souple",
                        gasPipeValidityDate = "2020-01-01",
                        gasPipeReplaced = true,
                    ),
            )
        assertTrue(blocks.isNotEmpty())
        val joined = blocks.joinToString("\n")
        assertTrue(joined.contains("Turbidité eau circuit: 15 NTU — Eau embouée."))
        assertTrue(joined.contains("Tuyau gaz cuisine (installation logement) : remplacé."))
        assertTrue(joined.contains("hors validité"))
    }

    @Test
    fun installationBlock_omittedWhenEmpty() {
        val blocks =
            buildStructuredFactsBlocks(
                equipments = emptyList(),
                attestations = emptyList(),
                measures = emptyList(),
                pacMeasures = emptyList(),
                anomalyDrafts = emptyList(),
                anomalyCatalog = emptyList(),
                invoiceLines = emptyList(),
                installationCheck = InstallationCheckEntity(interventionId = "i1"),
            )
        assertTrue(blocks.none { it.startsWith("Installation logement (données générales") })
    }
}
