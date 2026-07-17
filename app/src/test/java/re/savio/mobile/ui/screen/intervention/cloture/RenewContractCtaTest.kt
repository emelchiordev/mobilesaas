package re.savio.mobile.ui.screen.intervention.cloture

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RenewContractCtaTest {

    @Test
    fun visibleWhenRenewableAndVeTypeSelected() {
        assertTrue(shouldShowRenewContractCta(renewable = true, hasVeTypeSelected = true))
    }

    @Test
    fun hiddenWhenNotRenewable() {
        assertFalse(shouldShowRenewContractCta(renewable = false, hasVeTypeSelected = true))
    }

    @Test
    fun hiddenWhenNoVeTypeSelected() {
        assertFalse(shouldShowRenewContractCta(renewable = true, hasVeTypeSelected = false))
    }
}
