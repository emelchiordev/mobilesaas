package re.savio.mobile.ui.screen.intervention.cerfa

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import re.savio.mobile.data.local.entity.ColdMeasureEntity

class CerfaFroidUiTest {

    private fun base() =
        ColdMeasureEntity(id = "1", interventionId = "i", equipmentId = "e")

    @Test
    fun tabs_humanTitleDominant_cerfaRefSecondary() {
        val fuites = CERFA_FROID_TABS.first { it.title == "Fuites" }
        assertEquals("Fuites", fuites.title)
        assertEquals("7–9", fuites.cerfaRef)
        val bordereau = CERFA_FROID_TABS.first { it.title == "Bordereau" }
        assertEquals("Bordereau", bordereau.title)
        assertEquals("13–14", bordereau.cerfaRef)
    }

    @Test
    fun floatingLabelConvention_autoFieldsDetectedByLabel() {
        assertTrue(isCerfaAutoCalculatedField("Total fluide réinjecté (auto)"))
        assertTrue(isCerfaAutoCalculatedField("Total fluide récupéré (auto)"))
        assertFalse(isCerfaAutoCalculatedField("Fluide chargé — vapeur (kg)"))
        assertFalse(isCerfaAutoCalculatedField("Localisation fuite 1"))
    }

    @Test
    fun leakBlocks_hiddenWhenNoLeaks() {
        assertTrue(visibleLeakBlockIndices(leaksEnabled = false, blockCount = 3).isEmpty())
    }

    @Test
    fun leakBlocks_defaultShowsOnlyFirst() {
        assertEquals(listOf(1), visibleLeakBlockIndices(leaksEnabled = true, blockCount = 1))
        assertEquals(
            1,
            initialLeakBlockCount(
                fuiteloc1 = "",
                fuiterep1 = "",
                fuiteloc2 = "",
                fuiterep2 = "",
                fuiteloc3 = "",
                fuiterep3 = "",
            ),
        )
    }

    @Test
    fun leakBlocks_2and3OnlyWhenDataOrExplicitCount() {
        assertEquals(listOf(1, 2), visibleLeakBlockIndices(leaksEnabled = true, blockCount = 2))
        assertEquals(listOf(1, 2, 3), visibleLeakBlockIndices(leaksEnabled = true, blockCount = 3))
        assertEquals(
            2,
            initialLeakBlockCount(
                fuiteloc1 = "compresseur",
                fuiterep1 = "",
                fuiteloc2 = "vanne",
                fuiterep2 = "",
                fuiteloc3 = "",
                fuiterep3 = "",
            ),
        )
        assertEquals(
            3,
            initialLeakBlockCount(
                fuiteloc1 = "a",
                fuiterep1 = "",
                fuiteloc2 = "",
                fuiterep2 = "",
                fuiteloc3 = "c",
                fuiterep3 = "",
            ),
        )
    }

    @Test
    fun tabFillState_fluidesMissingWhenContainerRequired() {
        val state = base().copy(fluidrein = "1.5", supplyContainerId = "")
        assertEquals(CerfaTabFillState.Missing, cerfaTabFillState(4, state))
        assertEquals(
            CerfaTabFillState.Done,
            cerfaTabFillState(4, state.copy(supplyContainerId = "c1")),
        )
    }

    @Test
    fun tabFillState_equipementDoneWhenFrigoSet() {
        assertEquals(CerfaTabFillState.Todo, cerfaTabFillState(0, base()))
        assertEquals(CerfaTabFillState.Done, cerfaTabFillState(0, base().copy(frigo = "R32")))
    }

    @Test
    fun tabFillState_natureDoneWhenMinterOn() {
        assertEquals(CerfaTabFillState.Todo, cerfaTabFillState(1, base()))
        assertEquals(CerfaTabFillState.Done, cerfaTabFillState(1, base().copy(minter2 = "O")))
    }

    @Test
    fun leaksEnabled_fromPasfuiteFlag() {
        assertTrue(cerfaLeaksEnabled("N"))
        assertFalse(cerfaLeaksEnabled("O"))
        assertFalse(cerfaLeaksEnabled(""))
    }

    @Test
    fun recommendedFrequency_standardByTonnage() {
        assertEquals(12, recommendedFrequencyMonths("7.2", auto = false))
        assertEquals(6, recommendedFrequencyMonths("100", auto = false))
        assertEquals(3, recommendedFrequencyMonths("600", auto = false))
    }

    @Test
    fun recommendedFrequency_autoByTonnage() {
        assertEquals(24, recommendedFrequencyMonths("7.2", auto = true))
        assertEquals(12, recommendedFrequencyMonths("100", auto = true))
        assertEquals(6, recommendedFrequencyMonths("600", auto = true))
    }

    @Test
    fun frequencyMode_fromFreqaOrFreqs() {
        assertEquals(CerfaFrequencyMode.Standard, cerfaFrequencyMode(base()))
        assertEquals(
            CerfaFrequencyMode.Auto,
            cerfaFrequencyMode(base().copy(freqa1 = "O")),
        )
        assertEquals(
            CerfaFrequencyMode.Standard,
            cerfaFrequencyMode(base().copy(freqs2 = "O")),
        )
    }

    @Test
    fun selectedFrequencyMonths_readsActiveFlag() {
        assertEquals(null, selectedFrequencyMonths(base()))
        assertEquals(12, selectedFrequencyMonths(base().copy(freqs1 = "O")))
        assertEquals(24, selectedFrequencyMonths(base().copy(freqa1 = "O")))
        assertEquals(6, selectedFrequencyMonths(base().copy(freqa3 = "O")))
    }

    @Test
    fun frequencyOptions_autoVsStandard() {
        assertEquals(listOf(24, 12, 6), frequencyOptions(auto = true).map { it.first })
        assertEquals(listOf(12, 6, 3), frequencyOptions(auto = false).map { it.first })
    }

    @Test
    fun countMissingRequiredTabs() {
        assertEquals(0, countMissingRequiredTabs(base()))
        assertEquals(1, countMissingRequiredTabs(base().copy(fluidrein = "2", supplyContainerId = "")))
    }
}
