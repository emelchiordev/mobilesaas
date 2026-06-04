package re.melchior.saviomobile.data.local.entity

import re.melchior.saviomobile.data.remote.dto.InterventionTypeDto

fun InterventionTypeDto.toEntity(): InterventionTypeEntity {
    val resolvedId = id?.trim().orEmpty().ifBlank { code }
    return InterventionTypeEntity(
        id = resolvedId,
        code = code,
        label = label,
        color = color,
        isVeType = isVeType,
        isRamonageType = isRamonageType,
        isSystem = isSystem,
        showOnCreate = showOnCreate,
        showOnClose = showOnClose,
        requireClientSignature = requireClientSignature,
        requireReport = requireReport,
        triggerEquipmentSetup = triggerEquipmentSetup,
    )
}

fun InterventionTypeEntity.toDto(): InterventionTypeDto =
    InterventionTypeDto(
        id = id,
        code = code,
        label = label,
        color = color,
        isVeType = isVeType,
        isRamonageType = isRamonageType,
        isSystem = isSystem,
        showOnCreate = showOnCreate,
        showOnClose = showOnClose,
        requireClientSignature = requireClientSignature,
        requireReport = requireReport,
        triggerEquipmentSetup = triggerEquipmentSetup,
    )

/** Mapping minimal legacy (referentiels.pull ancien : code, label, color). */
fun InterventionTypeDto.toEntityFromLegacyReferentiel(): InterventionTypeEntity =
    toEntity().copy(
        showOnCreate = true,
        showOnClose = true,
    )
