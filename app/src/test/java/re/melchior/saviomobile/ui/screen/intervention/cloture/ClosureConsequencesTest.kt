package re.melchior.saviomobile.ui.screen.intervention.cloture

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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

  @Test
  fun casA_veWithAttestation() {
    val list = computeClosureConsequences(
        selectedTypes = listOf(veType),
        plannedTypeCode = "VE",
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
        plannedTypeCode = "VE",
        hasLocalAttestationVe = false,
        updatesRequireValidation = false,
    )
    assertTrue(list.any { it.isWarning && it.text.contains("non remplie") })
  }

  @Test
  fun casD_plannedVeButNotSelected() {
    val list = computeClosureConsequences(
        selectedTypes = listOf(maintType),
        plannedTypeCode = "VE",
        hasLocalAttestationVe = false,
        updatesRequireValidation = false,
    )
    assertTrue(list.any { it.isWarning && it.text.contains("ne sera pas mise à jour") })
  }

  @Test
  fun validationBureau() {
    val list = computeClosureConsequences(
        selectedTypes = listOf(maintType),
        plannedTypeCode = "MAINT",
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
  fun nextVe_overdue() {
    val display = nextVeDisplay(
        LocalDate.of(2020, 1, 1),
        today = LocalDate.of(2026, 5, 19),
    )
    assertEquals(VeDueUrgency.OVERDUE, display.urgency)
  }
}
