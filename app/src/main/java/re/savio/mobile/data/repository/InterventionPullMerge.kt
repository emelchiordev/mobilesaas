package re.savio.mobile.data.repository

import re.savio.mobile.data.local.entity.EquipmentEntity
import re.savio.mobile.data.local.entity.InterventionEntity
import re.savio.mobile.data.remote.dto.EquipmentDto
import re.savio.mobile.data.remote.dto.InterventionDto

/**
 * Fusion pull → Room en préservant une session locale `in_progress`.
 * Ne doit jamais réécraser un démarrage terrain par un statut serveur `scheduled`.
 */
internal fun mergePullInterventionWithLocalLifecycle(
    remote: InterventionEntity,
    existing: InterventionEntity?,
): InterventionEntity {
    val merged = preserveInterventionPolicySnapshot(remote, existing)
    if (existing == null) return merged
    if (!existing.isLocallyActiveSession()) return merged
    return merged.copy(
        status = existing.status,
        syncStatus = existing.syncStatus,
        startedAt = existing.startedAt ?: merged.startedAt,
        hasLocalChanges = existing.hasLocalChanges,
        completedAt = existing.completedAt,
    )
}

internal fun InterventionEntity.isLocallyActiveSession(): Boolean =
    status == "in_progress" &&
        (syncStatus == "IN_PROGRESS" || hasLocalChanges)

internal fun preserveInterventionPolicySnapshot(
    entity: InterventionEntity,
    existing: InterventionEntity?,
): InterventionEntity {
    if (existing?.policySnapshotJson.isNullOrBlank()) return entity
    return entity.copy(policySnapshotJson = existing.policySnapshotJson)
}

internal fun detectInterventionBusinessDrift(
    before: InterventionEntity,
    beforeEquipments: List<EquipmentEntity>,
    remote: InterventionDto?,
): Boolean {
    if (remote == null) return true
    if (normalizeScheduledAt(before.scheduledAt) != normalizeScheduledAt(remote.scheduledAt)) {
        return true
    }
    val remoteSlot = remote.timeSlot?.trim()?.takeIf { it.isNotEmpty() } ?: "matin"
    if (before.timeSlot != remoteSlot) return true
    if (localEquipmentFingerprint(beforeEquipments) != remoteEquipmentFingerprint(remote.equipment)) {
        return true
    }
    return false
}

internal fun localEquipmentFingerprint(equipments: List<EquipmentEntity>): String =
    equipments
        .sortedBy { it.order }
        .joinToString("|") { eq ->
            listOf(
                eq.id,
                eq.order.toString(),
                eq.brand.orEmpty(),
                eq.model.orEmpty(),
                eq.typeCode.orEmpty(),
                eq.energyCode.orEmpty(),
            ).joinToString(":")
        }

internal fun remoteEquipmentFingerprint(equipments: List<EquipmentDto>): String =
    equipments
        .withIndex()
        .sortedBy { (index, eq) -> eq.order ?: (index + 1) }
        .joinToString("|") { (index, eq) ->
            val order = eq.order ?: (index + 1)
            listOf(
                eq.id,
                order.toString(),
                eq.brand.orEmpty(),
                eq.model.orEmpty(),
                eq.typeCode.orEmpty(),
                eq.energyCode.orEmpty(),
            ).joinToString(":")
        }

private fun normalizeScheduledAt(value: String): String =
    value.trim().take(19)
