package re.melchior.saviomobile.ui.screen.intervention.cloture

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import re.melchior.saviomobile.data.remote.dto.InterventionTypeDto
import java.time.LocalDate

class ClosureConsequencesTest {

  private val veType = InterventionTypeDto(
      code = "VE",
      label = "Visite entretien",
      color = null,
      isVeType = true,
  )

  private val maintType = InterventionTypeDto(
      code = "MAINT",
      label = "Maintenance",
      color = null,
      isVeType = false,
  )

  private val mesType = InterventionTypeDto(
      code = "MES",
      label = "Mise en service",
      color = null,
      triggerEquipmentSetup = true,
  )

  @Test
  fun casA_veWithAttestation() {
    val list = computeClosureConsequences(
        selectedTypes = listOf(veType),
        plannedType = veType,
        hasLocalAttestationVe = true,
        updatesRequireValidation = false,
    )
    assertTrue(list.any { it.text.contains("attestation d'entretien", ignoreCase = true) })
    assertFalse(list.any { it.isWarning && it.text.contains("non remplie") })
  }

  @Test
  fun casB_veWithoutAttestation() {
    val list = computeClosureConsequences(
        selectedTypes = listOf(veType),
        plannedType = veType,
        hasLocalAttestationVe = false,
        updatesRequireValidation = false,
    )
    assertTrue(list.any { it.isWarning && it.text.contains("non remplie") })
  }

  @Test
  fun casD_plannedVeButNotSelected() {
    val list = computeClosureConsequences(
        selectedTypes = listOf(maintType),
        plannedType = veType,
        hasLocalAttestationVe = false,
        updatesRequireValidation = false,
    )
    assertTrue(list.any { it.isWarning && it.text.contains("ne sera pas mise à jour") })
  }

  @Test
  fun mes_missingCommissioningDateWarning() {
    val list = computeClosureConsequences(
        selectedTypes = listOf(mesType),
        plannedType = mesType,
        hasLocalAttestationVe = false,
        updatesRequireValidation = false,
        equipmentsMissingCommissioning = 2,
    )
    assertTrue(list.any { it.isWarning && it.text.contains("renseignez") })
  }

  @Test
  fun mes_allCommissioningDatesOk() {
    val list = computeClosureConsequences(
        selectedTypes = listOf(mesType),
        plannedType = mesType,
        hasLocalAttestationVe = false,
        updatesRequireValidation = false,
        equipmentsMissingCommissioning = 0,
    )
    assertTrue(list.any { it.text.contains("dates de mise en service") })
    assertFalse(list.any { it.isWarning && it.text.contains("manquante") })
  }

  @Test
  fun gasPipeExpiredWithoutAnomalyWarning() {
    val list = computeClosureConsequences(
        selectedTypes = listOf(maintType),
        plannedType = maintType,
        hasLocalAttestationVe = false,
        updatesRequireValidation = false,
        gasPipeExpiredWithoutAnomaly = true,
    )
    assertTrue(list.any { it.isWarning && it.text.contains("anomalie manquante") })
  }

  @Test
  fun validationBureau() {
    val list = computeClosureConsequences(
        selectedTypes = listOf(maintType),
        plannedType = maintType,
        hasLocalAttestationVe = false,
        updatesRequireValidation = true,
    )
    assertTrue(list.any { it.text.contains("validation du bureau") })
  }

    @Test
    fun computeNextVe_fromLastVe() {
        val last = LastVeSummary(LocalDate.of(2025, 3, 12), "Cindy")
        assertEquals(LocalDate.of(2026, 3, 12), computeNextVeDate(last, null))
    }

    @Test
    fun computeNextVe_withoutLastVe_returnsNull() {
        assertNull(computeNextVeDate(null, LocalDate.of(2025, 5, 22)))
    }

  @Test
  fun nextVe_overdue() {
    val display = nextVeDisplay(
        LocalDate.of(2020, 1, 1),
        today = LocalDate.of(2026, 5, 19),
    )
    assertEquals(VeDueUrgency.OVERDUE, display.urgency)
  }
}
