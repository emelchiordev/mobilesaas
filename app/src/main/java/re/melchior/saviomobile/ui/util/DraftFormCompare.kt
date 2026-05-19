package re.melchior.saviomobile.ui.util

import re.melchior.saviomobile.data.local.entity.AttestationVeEntity
import re.melchior.saviomobile.data.local.entity.ColdMeasureEntity
import re.melchior.saviomobile.data.local.entity.PacMeasureEntity

fun AttestationVeEntity.contentFingerprint(): AttestationVeEntity =
    copy(isDirty = false, updatedAt = "")

fun AttestationVeEntity.hasMeaningfulData(points: Map<String, String>): Boolean {
    if (points.values.any { it.isNotBlank() }) return true
    val f = contentFingerprint()
    val empty = AttestationVeEntity(
        interventionId = interventionId,
        equipmentOrder = equipmentOrder,
        id = "",
        type = type,
    ).contentFingerprint()
    return f != empty
}

fun ColdMeasureEntity.contentFingerprint(): ColdMeasureEntity =
    copy(isDirty = false, updatedAt = "")

fun ColdMeasureEntity.hasMeaningfulCerfaData(): Boolean {
    val blank = ColdMeasureEntity(
        id = "",
        interventionId = interventionId,
        equipmentId = equipmentId,
    ).contentFingerprint()
    return contentFingerprint() != blank
}

fun PacMeasureEntity.contentFingerprint(): PacMeasureEntity =
    copy(isDirty = false, updatedAt = "")

fun PacMeasureEntity.hasMeaningfulPacData(): Boolean {
    val blank = PacMeasureEntity(
        interventionId = interventionId,
        equipmentOrder = equipmentOrder,
    ).contentFingerprint()
    return contentFingerprint() != blank
}
