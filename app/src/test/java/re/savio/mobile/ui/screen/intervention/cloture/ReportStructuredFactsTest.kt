package re.savio.mobile.ui.screen.intervention.cloture

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import re.savio.mobile.data.local.entity.AttestationVeEntity
import re.savio.mobile.data.local.entity.EquipmentEntity
import re.savio.mobile.data.local.entity.InstallationCheckEntity

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

    @Test
    fun ecsControlBlock_includesMeasuresAndControlPoints() {
        val equipment =
            EquipmentEntity(
                interventionId = "i1",
                order = 1,
                id = "eq-1",
                unitId = "u1",
                typeCode = "CHAUFFE EAU",
                energyCode = "GAZ NAT",
                brand = "Atlantic",
            )
        val attestation =
            AttestationVeEntity(
                interventionId = "i1",
                equipmentOrder = 1,
                id = "att-1",
                type = "ECS",
                appareilMesure = "Testo 327",
                tempAmbiante = "58",
                co = "12",
            )
        val points =
            mapOf(
                "ECS_VENTIL_LOCAL" to "V",
                "SANIT_TEMP_CONSIGNE" to "V",
                "ECS_ETANCHEITE_GAZ" to "N",
            )
        val blocks =
            buildStructuredFactsBlocks(
                equipments = listOf(equipment),
                attestations = listOf(attestation),
                measures = emptyList(),
                pacMeasures = emptyList(),
                anomalyDrafts = emptyList(),
                anomalyCatalog = emptyList(),
                invoiceLines = emptyList(),
                attestationPoints =
                    mapOf(
                        attestationPointsKey(1, "ECS") to points,
                    ),
            )
        val joined = blocks.joinToString("\n")
        assertTrue(joined.contains("Contrôle ECS"))
        assertTrue(joined.contains("Appareil de mesure : Testo 327"))
        assertTrue(joined.contains("T° eau mesurée : 58°C"))
        assertTrue(joined.contains("CO ambiant : 12 ppm"))
        assertTrue(joined.contains("Ventilation du local correcte : Validé"))
        assertTrue(joined.contains("Température de consigne ≥ 60°C : Validé"))
        assertTrue(joined.contains("Étanchéité gaz vérifiée : Non validé"))
        assertFalse(joined.contains("Mesures combustion"))
    }

    @Test
    fun ecsControlBlock_omittedWhenNotFilled() {
        val equipment =
            EquipmentEntity(
                interventionId = "i1",
                order = 1,
                id = "eq-1",
                unitId = "u1",
                typeCode = "CHAUFFE EAU",
                energyCode = "ELEC",
            )
        val attestation =
            AttestationVeEntity(
                interventionId = "i1",
                equipmentOrder = 1,
                id = "att-1",
                type = "ECS",
            )
        val blocks =
            buildStructuredFactsBlocks(
                equipments = listOf(equipment),
                attestations = listOf(attestation),
                measures = emptyList(),
                pacMeasures = emptyList(),
                anomalyDrafts = emptyList(),
                anomalyCatalog = emptyList(),
                invoiceLines = emptyList(),
                attestationPoints = emptyMap(),
            )
        val joined = blocks.joinToString("\n")
        assertFalse(joined.contains("Contrôle ECS"))
    }

    @Test
    fun formatEcsControlBlock_unit() {
        val block =
            formatEcsControlBlock(
                att =
                    AttestationVeEntity(
                        interventionId = "i1",
                        equipmentOrder = 1,
                        id = "att-1",
                        type = "ECS",
                        tempAmbiante = "62",
                    ),
                points = mapOf("SANIT_GROUPE_SECU" to "V"),
                energyCode = "ELEC",
                equipmentTypeCode = "BALLON ECS",
            )
        assertTrue(block!!.contains("Contrôle ECS"))
        assertTrue(block.contains("T° eau mesurée : 62°C"))
        assertTrue(block.contains("Groupe de sécurité fonctionnel : Validé"))
        assertFalse(block.contains("CO ambiant"))
    }
}
