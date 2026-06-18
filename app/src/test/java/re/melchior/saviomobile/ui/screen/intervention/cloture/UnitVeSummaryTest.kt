package re.melchior.saviomobile.ui.screen.intervention.cloture

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import re.melchior.saviomobile.data.local.entity.InterventionHistoryEntity
import re.melchior.saviomobile.util.SimpleAttestableEquipment
import java.time.LocalDate

class UnitVeSummaryTest {

    @Test
    fun formatVeListHint_doneWithNextMonth() {
        val hint =
            formatVeListHint(
                veStatus = "done",
                lastVeCompletedAt = "2025-03-15T10:00:00Z",
                nextVePrevisionalMonth = "2026-06",
                today = LocalDate.of(2026, 3, 1),
            )
        assertNotNull(hint)
        assertEquals("VE · 06/2026", hint!!.text)
        assertEquals(VeHintUrgency.OK, hint.urgency)
    }

    @Test
    fun formatVeListHint_pendingOverdue() {
        val hint =
            formatVeListHint(
                veStatus = "pending",
                lastVeCompletedAt = null,
                nextVePrevisionalMonth = "2025-01",
                displayContractStatus = "sous_contrat",
                today = LocalDate.of(2026, 3, 1),
            )
        assertNotNull(hint)
        assertEquals("VE due", hint!!.text)
        assertEquals(VeHintUrgency.OVERDUE, hint.urgency)
    }

    @Test
    fun formatVeListHint_naWithoutContract_returnsNull() {
        assertNull(
            formatVeListHint(
                veStatus = "na",
                lastVeCompletedAt = null,
                nextVePrevisionalMonth = null,
                displayContractStatus = "hors_contrat",
            ),
        )
    }

    @Test
    fun formatVeListHint_doneWithCoverageSuffix() {
        val hint =
            formatVeListHint(
                veStatus = "done",
                lastVeCompletedAt = "2025-03-15T10:00:00Z",
                nextVePrevisionalMonth = "2026-06",
                coverageUnavailable = false,
                coverageAttested = 2,
                coverageExpected = 3,
                today = LocalDate.of(2026, 3, 1),
            )
        assertNotNull(hint)
        assertEquals("VE · 06/2026 · 2/3", hint!!.text)
    }

    @Test
    fun formatVeCoverageLabel_returnsRatio() {
        assertEquals(
            "2/3 appareils",
            formatVeCoverageLabel(
                VeCoverageSummary(
                    unavailable = false,
                    attested = 2,
                    expected = 3,
                    complete = false,
                ),
            ),
        )
    }

    @Test
    fun formatVeCoverageHint_incompleteCoverage_addsHint() {
        assertEquals(
            "0/1 appareils · attestation manquante ou non rattachée",
            formatVeCoverageHint(
                VeCoverageSummary(
                    unavailable = false,
                    attested = 0,
                    expected = 1,
                    complete = false,
                ),
            ),
        )
    }

    @Test
    fun formatVeCoverageHint_closureProjectedComplete_showsAfterClosureSuffix() {
        assertEquals(
            "1/1 appareils (après clôture)",
            formatVeCoverageHint(
                VeCoverageSummary(
                    unavailable = false,
                    attested = 1,
                    expected = 1,
                    complete = true,
                    closureProjected = true,
                ),
            ),
        )
    }

    @Test
    fun projectClosureCoverage_apiZeroLocalAttestation_projectsOneOfOne() {
        val boiler =
            SimpleAttestableEquipment(
                id = "eq-1",
                typeCode = "CHAUDIERE",
                energyCode = "GAZ",
                hybridePacEquipmentId = null,
            )
        val base =
            VeCoverageSummary(
                unavailable = false,
                attested = 0,
                expected = 1,
                complete = false,
            )
        val projected =
            projectClosureCoverage(
                base = base,
                closureIsVe = true,
                localAttestationEquipmentOrders = setOf(1),
                unitEquipments = listOf(boiler),
                equipmentOrderForInput = { if (it.id == "eq-1") 1 else null },
            )
        assertNotNull(projected)
        assertEquals(1, projected!!.attested)
        assertTrue(projected.complete == true)
        assertTrue(projected.closureProjected)
    }

    @Test
    fun projectClosureCoverage_notVeType_returnsBaseUnchanged() {
        val base =
            VeCoverageSummary(
                unavailable = false,
                attested = 0,
                expected = 1,
                complete = false,
            )
        val result =
            projectClosureCoverage(
                base = base,
                closureIsVe = false,
                localAttestationEquipmentOrders = setOf(1),
                unitEquipments = emptyList(),
                equipmentOrderForInput = { null },
            )
        assertEquals(base, result)
    }

    @Test
    fun projectClosureCoverage_nonAttestableEquipment_doesNotProject() {
        val bruleur =
            SimpleAttestableEquipment(
                id = "br-1",
                typeCode = "BRULEUR",
                energyCode = "GAZ",
                hybridePacEquipmentId = null,
            )
        val base =
            VeCoverageSummary(
                unavailable = false,
                attested = 0,
                expected = 1,
                complete = false,
            )
        val projected =
            projectClosureCoverage(
                base = base,
                closureIsVe = true,
                localAttestationEquipmentOrders = setOf(2),
                unitEquipments = listOf(bruleur),
                equipmentOrderForInput = { if (it.id == "br-1") 2 else null },
            )
        assertEquals(base, projected)
        assertFalse(projected!!.closureProjected)
    }

    @Test
    fun buildUnitVeContextFromLocal_usesCompletedVeHistory() {
        val history =
            InterventionHistoryEntity(
                id = "h1",
                unitId = "u1",
                scheduledAt = "2025-05-01T08:00:00Z",
                completedAt = "2025-06-01T12:00:00Z",
                typeCode = "VE",
                typeLabel = "Visite entretien",
                completedAsVe = true,
                technicianFirstName = "Jean",
            )
        val context = buildUnitVeContextFromLocal(contractSource = null, lastHistory = history)
        assertNotNull(context.lastVe)
        assertEquals(LocalDate.of(2025, 6, 1), context.lastVe!!.completedAt)
        assertEquals("Jean", context.lastVe!!.technicianFirstName)
    }

    @Test
    fun historyEntity_completedAsVeFlagIsPreserved() {
        val entity =
            InterventionHistoryEntity(
                id = "h2",
                unitId = "u1",
                scheduledAt = "2025-01-01T08:00:00Z",
                completedAt = "2025-01-01T00:00:00Z",
                typeCode = "VE",
                typeLabel = "Visite entretien",
                completedAsVe = true,
            )
        assertTrue(entity.completedAsVe)
    }
}
