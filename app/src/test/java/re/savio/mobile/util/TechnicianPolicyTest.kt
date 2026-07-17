package re.savio.mobile.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TechnicianPolicyTest {
    @Test
    fun resolvePolicy_usesProfileDefaultWhenOverridesEmpty() {
        val policy = TechnicianPolicy(profile = UserProfile.ARTISAN_SOLO)
        assertEquals(InvoicePolicy.AUTO_ISSUE, resolvePolicy(policy, "invoices"))
        assertEquals(PlanningPolicy.FREE, resolvePolicy(policy, "planning"))
    }

    @Test
    fun resolvePolicy_prefersOverride() {
        val policy =
            TechnicianPolicy(
                profile = UserProfile.ARTISAN_SOLO,
                policyOverrides = mapOf("invoices" to InvoicePolicy.REQUIRES_VALIDATION),
            )
        assertEquals(InvoicePolicy.REQUIRES_VALIDATION, resolvePolicy(policy, "invoices"))
    }

    @Test
    fun requiresInvoiceValidation_fromSnapshot() {
        val snapshot =
            InterventionPolicySnapshot(
                invoices = InvoicePolicy.REQUIRES_VALIDATION,
                planning = PlanningPolicy.FREE,
                fieldModifications = FieldModificationPolicy.AUTO_APPLY,
                closing = ClosingPolicy.TECH_CAN_CLOSE,
            )
        assertTrue(requiresInvoiceValidation(snapshot))
    }

    @Test
    fun requiresInvoiceValidation_falseWhenAutoIssue() {
        val snapshot =
            InterventionPolicySnapshot(
                invoices = InvoicePolicy.AUTO_ISSUE,
                planning = PlanningPolicy.FREE,
                fieldModifications = FieldModificationPolicy.AUTO_APPLY,
                closing = ClosingPolicy.TECH_CAN_CLOSE,
            )
        assertFalse(requiresInvoiceValidation(snapshot))
    }
}
