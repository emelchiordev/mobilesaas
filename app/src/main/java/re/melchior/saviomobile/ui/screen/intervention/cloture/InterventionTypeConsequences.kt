package re.melchior.saviomobile.ui.screen.intervention.cloture

import re.melchior.saviomobile.data.local.entity.EquipmentEntity
import re.melchior.saviomobile.data.local.entity.InterventionEntity
import re.melchior.saviomobile.data.remote.dto.InterventionTypeDto
import re.melchior.saviomobile.util.isEquipmentReplaced
import re.melchior.saviomobile.util.attestableFrom

/** Agrégation des flags comportementaux sur les types cochés à la clôture. */
fun List<InterventionTypeDto>.anyVeType(): Boolean = any { it.isVeType }

fun List<InterventionTypeDto>.anyEquipmentSetup(): Boolean = any { it.triggerEquipmentSetup }

fun List<InterventionTypeDto>.anyRamonageType(): Boolean = any { it.isRamonageType }

fun List<InterventionTypeDto>.anyRequiresClientSignature(): Boolean = any { it.requireClientSignature }

fun List<InterventionTypeDto>.anyRequiresReport(): Boolean = any { it.requireReport }

/** Résout le type prévu (back-office) depuis le référentiel clôture. */
fun resolvePlannedType(
    intervention: InterventionEntity?,
    closeTypes: List<InterventionTypeDto>,
): InterventionTypeDto? {
    if (intervention == null || closeTypes.isEmpty()) return null
    intervention.interventionTypeId?.let { id ->
        closeTypes.find { it.id == id }?.let { return it }
    }
    return closeTypes.find { it.code == intervention.typeCode }
}

/** VE planifiée mais un autre type coché à la clôture. */
fun isPlannedVeNotSelected(
    plannedType: InterventionTypeDto?,
    selectedTypes: List<InterventionTypeDto>,
): Boolean =
    plannedType?.isVeType == true &&
        selectedTypes.isNotEmpty() &&
        !selectedTypes.anyVeType()

fun isPlannedVeChanged(
    plannedType: InterventionTypeDto?,
    selectedTypes: List<InterventionTypeDto>,
): Boolean = isPlannedVeNotSelected(plannedType, selectedTypes)

/** Appareils racine actifs sans date de mise en service renseignée. */
fun countEquipmentsMissingCommissioning(roots: List<EquipmentEntity>): Int =
    roots.count { eq ->
        !isEquipmentReplaced(
            attestableFrom(
                id = eq.id,
                typeCode = eq.typeCode,
                energyCode = eq.energyCode,
                hybridePacEquipmentId = null,
            ),
        ) && eq.installDate.isNullOrBlank()
    }

/** Bloque la clôture tant que la MES est cochée sans date sur chaque appareil actif. */
fun blocksClosureForMissingCommissioning(
    selectedTypes: List<InterventionTypeDto>,
    rootEquipments: List<EquipmentEntity>,
): Boolean =
    selectedTypes.anyEquipmentSetup() && countEquipmentsMissingCommissioning(rootEquipments) > 0

fun resolveDefaultCloseType(
    intervention: InterventionEntity,
    types: List<InterventionTypeDto>,
): InterventionTypeDto {
    intervention.interventionTypeId?.let { id ->
        types.find { it.id == id }?.let { return it }
    }
    types.find { it.code == intervention.typeCode }?.let { return it }
    return types.first()
}
