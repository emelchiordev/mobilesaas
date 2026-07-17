package re.savio.mobile.ui.component

import org.junit.Assert.assertEquals
import org.junit.Test
import re.savio.mobile.ui.designsystem.AppBadgeStyle

class ClientFinancialToneTest {
    @Test
    fun statusToneToBadgeStyle_mapsKnownTones() {
        assertEquals(AppBadgeStyle.Success, statusToneToBadgeStyle("success"))
        assertEquals(AppBadgeStyle.Warning, statusToneToBadgeStyle("warning"))
        assertEquals(AppBadgeStyle.Error, statusToneToBadgeStyle("danger"))
        assertEquals(AppBadgeStyle.Info, statusToneToBadgeStyle("info"))
        assertEquals(AppBadgeStyle.Primary, statusToneToBadgeStyle("neutral"))
    }
}
