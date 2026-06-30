package re.melchior.saviomobile.ui.screen.intervention.cloture

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.vector.ImageVector
import re.melchior.saviomobile.data.remote.dto.InterventionTypeDto

data class ClosureConsequence(
    val icon: ImageVector,
    val text: String,
    val isWarning: Boolean = false,
)

fun computeClosureConsequences(
    selectedTypes: List<InterventionTypeDto>,
    plannedType: InterventionTypeDto?,
    hasLocalAttestationVe: Boolean,
    updatesRequireValidation: Boolean,
    equipmentsMissingCommissioning: Int = 0,
    gasPipeExpiredWithoutAnomaly: Boolean = false,
): List<ClosureConsequence> {
    if (selectedTypes.isEmpty()) return emptyList()

    val result = mutableListOf<ClosureConsequence>()
    val hasVeType = selectedTypes.anyVeType()
    val hasEquipmentSetup = selectedTypes.anyEquipmentSetup()

    if (hasVeType) {
        if (hasLocalAttestationVe) {
            result +=
                ClosureConsequence(
                    icon = Icons.Default.MarkEmailRead,
                    text = "Le client recevra :\n• Son rapport d'intervention\n• Son attestation d'entretien",
                )
        } else {
            result +=
                ClosureConsequence(
                    icon = Icons.Default.MarkEmailRead,
                    text = "Le client recevra :\n• Son rapport d'intervention",
                )
            result +=
                ClosureConsequence(
                    icon = Icons.Default.Warning,
                    text = "Attestation d'entretien non remplie",
                    isWarning = true,
                )
        }
    } else {
        result +=
            ClosureConsequence(
                icon = Icons.Default.MarkEmailRead,
                text = "Le client recevra :\n• Son rapport d'intervention",
            )
    }

    if (isPlannedVeNotSelected(plannedType, selectedTypes)) {
        result +=
            ClosureConsequence(
                icon = Icons.Default.Warning,
                text = "La date du dernier entretien ne sera pas mise à jour",
                isWarning = true,
            )
    }

    if (hasEquipmentSetup) {
        if (equipmentsMissingCommissioning > 0) {
            val label =
                if (equipmentsMissingCommissioning == 1) {
                    "Date de mise en service manquante sur 1 appareil — renseignez-la avant de continuer"
                } else {
                    "Dates de mise en service manquantes sur $equipmentsMissingCommissioning appareils — renseignez-les avant de continuer"
                }
            result +=
                ClosureConsequence(
                    icon = Icons.Default.Warning,
                    text = label,
                    isWarning = true,
                )
        } else {
            result +=
                ClosureConsequence(
                    icon = Icons.Default.Build,
                    text = "Les dates de mise en service seront enregistrées sur les appareils",
                )
        }
    }

    if (selectedTypes.anyRequiresClientSignature()) {
        result +=
            ClosureConsequence(
                icon = Icons.Default.Draw,
                text = "Signature du client requise à l'étape suivante",
            )
    }

    if (updatesRequireValidation) {
        result +=
            ClosureConsequence(
                icon = Icons.Default.HourglassTop,
                text = "Le rapport sera envoyé après validation du bureau",
            )
    }

    if (gasPipeExpiredWithoutAnomaly) {
        result +=
            ClosureConsequence(
                icon = Icons.Default.Warning,
                text = "Tuyau gaz hors validité — anomalie manquante",
                isWarning = true,
            )
    }

    return result
}
