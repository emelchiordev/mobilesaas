package re.savio.mobile.util

import org.junit.Assert.assertEquals
import org.junit.Test
import re.savio.mobile.data.local.entity.InstallationCheckEntity
import java.time.LocalDate

class GasPipeValidityTest {

    private val today = LocalDate.of(2026, 6, 4)

    @Test
    fun none_whenTypeNotSet() {
        assertEquals(
            GasPipeValidityStatus.None,
            evaluateGasPipeValidity(null, "2025-01-01", today),
        )
    }

    @Test
    fun none_whenDateEmpty() {
        assertEquals(
            GasPipeValidityStatus.None,
            evaluateGasPipeValidity("souple", "", today),
        )
    }

    @Test
    fun expired_whenBeforeToday() {
        assertEquals(
            GasPipeValidityStatus.Expired,
            evaluateGasPipeValidity("souple", "2026-06-03", today),
        )
    }

    @Test
    fun expiringSoon_onToday() {
        assertEquals(
            GasPipeValidityStatus.ExpiringSoon,
            evaluateGasPipeValidity("souple", "2026-06-04", today),
        )
    }

    @Test
    fun expiringSoon_atPlus30() {
        assertEquals(
            GasPipeValidityStatus.ExpiringSoon,
            evaluateGasPipeValidity("souple", "2026-07-04", today),
        )
    }

    @Test
    fun ok_afterPlus30() {
        assertEquals(
            GasPipeValidityStatus.Ok,
            evaluateGasPipeValidity("souple", "2026-07-05", today),
        )
    }

    @Test
    fun closureWarning_whenExpiredWithoutDraft() {
        val check =
            InstallationCheckEntity(
                interventionId = "i1",
                gasPipeType = "souple",
                gasPipeValidityDate = "2026-06-03",
            )
        assertEquals(
            true,
            isGasPipeExpiredWithoutAnomaly(check, hasGasPipeAnomalyDraft = false, today = today),
        )
    }

    @Test
    fun noClosureWarning_whenDraftExists() {
        val check =
            InstallationCheckEntity(
                interventionId = "i1",
                gasPipeType = "souple",
                gasPipeValidityDate = "2026-06-03",
            )
        assertEquals(
            false,
            isGasPipeExpiredWithoutAnomaly(check, hasGasPipeAnomalyDraft = true, today = today),
        )
    }
}
