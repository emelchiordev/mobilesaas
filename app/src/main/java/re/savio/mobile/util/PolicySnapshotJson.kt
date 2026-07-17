package re.savio.mobile.util

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import re.savio.mobile.data.local.entity.SettingsEntity
import re.savio.mobile.data.remote.dto.PolicySnapshotDto

private val gson = Gson()

fun settingsToTechnicianPolicy(settings: SettingsEntity): TechnicianPolicy {
    val overridesType = object : TypeToken<Map<String, String>>() {}.type
    val overrides: Map<String, String> =
        runCatching {
            gson.fromJson<Map<String, String>>(settings.policyOverridesJson, overridesType)
        }.getOrDefault(emptyMap())
    return TechnicianPolicy(profile = settings.profile, policyOverrides = overrides)
}

fun settingsToResolvedPolicies(settings: SettingsEntity): ResolvedPolicies =
    ResolvedPolicies(
        invoices = settings.resolvedInvoices,
        planning = settings.resolvedPlanning,
        planningEdit = settings.resolvedPlanningEdit,
        fieldModifications = settings.resolvedFieldModifications,
        closing = settings.resolvedClosing,
    )

fun interventionPolicySnapshotToJson(snapshot: InterventionPolicySnapshot): String =
    gson.toJson(
        mapOf(
            "invoices" to snapshot.invoices,
            "planning" to snapshot.planning,
            "fieldModifications" to snapshot.fieldModifications,
            "closing" to snapshot.closing,
        ),
    )

fun parseInterventionPolicySnapshot(json: String?): InterventionPolicySnapshot? {
    if (json.isNullOrBlank()) return null
    return runCatching {
        val map = gson.fromJson(json, object : TypeToken<Map<String, String>>() {}.type) as Map<String, String>
        InterventionPolicySnapshot(
            invoices = map["invoices"] ?: return null,
            planning = map["planning"] ?: return null,
            fieldModifications = map["fieldModifications"] ?: return null,
            closing = map["closing"] ?: return null,
        )
    }.getOrNull()
}

fun policySnapshotDtoToJson(dto: PolicySnapshotDto?): String? {
    if (dto == null) return null
    val invoices = dto.invoices ?: return null
    val planning = dto.planning ?: return null
    val fieldModifications = dto.fieldModifications ?: return null
    val closing = dto.closing ?: return null
    return interventionPolicySnapshotToJson(
        InterventionPolicySnapshot(
            invoices = invoices,
            planning = planning,
            fieldModifications = fieldModifications,
            closing = closing,
        ),
    )
}

fun policyOverridesToJson(overrides: Map<String, String>?): String =
    gson.toJson(overrides ?: emptyMap<String, String>())
