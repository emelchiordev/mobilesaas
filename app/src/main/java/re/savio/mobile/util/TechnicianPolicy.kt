package re.savio.mobile.util

object UserProfile {
    const val ARTISAN_SOLO = "ARTISAN_SOLO"
    const val TECHNICIEN_EQUIPE = "TECHNICIEN_EQUIPE"
    const val BACKOFFICE = "BACKOFFICE"
}

object InvoicePolicy {
    const val AUTO_ISSUE = "AUTO_ISSUE"
    const val REQUIRES_VALIDATION = "REQUIRES_VALIDATION"
}

object PlanningPolicy {
    const val FREE = "FREE"
    const val CONTROLLED = "CONTROLLED"
}

object FieldModificationPolicy {
    const val AUTO_APPLY = "AUTO_APPLY"
    const val REQUIRES_VALIDATION = "REQUIRES_VALIDATION"
}

object ClosingPolicy {
    const val TECH_CAN_CLOSE = "TECH_CAN_CLOSE"
    const val BUREAU_ONLY = "BUREAU_ONLY"
}

object MobilePlanningPermissionPolicy {
    const val READ_ONLY = "READ_ONLY"
    const val LIMITED_EDIT = "LIMITED_EDIT"
    const val FULL_EDIT = "FULL_EDIT"
}

data class TechnicianPolicy(
    val profile: String,
    val policyOverrides: Map<String, String> = emptyMap(),
)

data class ResolvedPolicies(
    val invoices: String,
    val planning: String,
    val planningEdit: String,
    val fieldModifications: String,
    val closing: String,
)

data class InterventionPolicySnapshot(
    val invoices: String,
    val planning: String,
    val fieldModifications: String,
    val closing: String,
)

private val DEFAULT_POLICIES: Map<String, ResolvedPolicies> = mapOf(
    UserProfile.ARTISAN_SOLO to ResolvedPolicies(
        invoices = InvoicePolicy.AUTO_ISSUE,
        planning = PlanningPolicy.FREE,
        planningEdit = MobilePlanningPermissionPolicy.FULL_EDIT,
        fieldModifications = FieldModificationPolicy.AUTO_APPLY,
        closing = ClosingPolicy.TECH_CAN_CLOSE,
    ),
    UserProfile.TECHNICIEN_EQUIPE to ResolvedPolicies(
        invoices = InvoicePolicy.REQUIRES_VALIDATION,
        planning = PlanningPolicy.CONTROLLED,
        planningEdit = MobilePlanningPermissionPolicy.LIMITED_EDIT,
        fieldModifications = FieldModificationPolicy.REQUIRES_VALIDATION,
        closing = ClosingPolicy.BUREAU_ONLY,
    ),
    UserProfile.BACKOFFICE to ResolvedPolicies(
        invoices = InvoicePolicy.AUTO_ISSUE,
        planning = PlanningPolicy.CONTROLLED,
        planningEdit = MobilePlanningPermissionPolicy.FULL_EDIT,
        fieldModifications = FieldModificationPolicy.AUTO_APPLY,
        closing = ClosingPolicy.TECH_CAN_CLOSE,
    ),
)

private val VALID_DOMAIN_VALUES: Map<String, Set<String>> = mapOf(
    "invoices" to setOf(InvoicePolicy.AUTO_ISSUE, InvoicePolicy.REQUIRES_VALIDATION),
    "planning" to setOf(PlanningPolicy.FREE, PlanningPolicy.CONTROLLED),
    "planningEdit" to setOf(
        MobilePlanningPermissionPolicy.READ_ONLY,
        MobilePlanningPermissionPolicy.LIMITED_EDIT,
        MobilePlanningPermissionPolicy.FULL_EDIT,
    ),
    "fieldModifications" to setOf(
        FieldModificationPolicy.AUTO_APPLY,
        FieldModificationPolicy.REQUIRES_VALIDATION,
    ),
    "closing" to setOf(ClosingPolicy.TECH_CAN_CLOSE, ClosingPolicy.BUREAU_ONLY),
)

fun resolvePolicy(policy: TechnicianPolicy, domain: String): String {
    val override = policy.policyOverrides[domain]
    if (override != null && VALID_DOMAIN_VALUES[domain]?.contains(override) == true) {
        return override
    }
    val defaults =
        DEFAULT_POLICIES[policy.profile] ?: DEFAULT_POLICIES[UserProfile.ARTISAN_SOLO]!!
    return when (domain) {
        "invoices" -> defaults.invoices
        "planning" -> defaults.planning
        "planningEdit" -> defaults.planningEdit
        "fieldModifications" -> defaults.fieldModifications
        "closing" -> defaults.closing
        else -> defaults.invoices
    }
}

fun resolveAllPolicies(policy: TechnicianPolicy): ResolvedPolicies =
    ResolvedPolicies(
        invoices = resolvePolicy(policy, "invoices"),
        planning = resolvePolicy(policy, "planning"),
        planningEdit = resolvePolicy(policy, "planningEdit"),
        fieldModifications = resolvePolicy(policy, "fieldModifications"),
        closing = resolvePolicy(policy, "closing"),
    )

fun buildInterventionPolicySnapshot(resolved: ResolvedPolicies): InterventionPolicySnapshot =
    InterventionPolicySnapshot(
        invoices = resolved.invoices,
        planning = resolved.planning,
        fieldModifications = resolved.fieldModifications,
        closing = resolved.closing,
    )

fun requiresInvoiceValidation(snapshot: InterventionPolicySnapshot): Boolean =
    snapshot.invoices == InvoicePolicy.REQUIRES_VALIDATION
