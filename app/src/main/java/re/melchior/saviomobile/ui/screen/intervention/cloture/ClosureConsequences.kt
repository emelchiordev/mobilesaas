package re.melchior.saviomobile.ui.screen.intervention.cloture

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.Warning
import re.melchior.saviomobile.data.remote.dto.InterventionTypeDto

data class ClosureConsequence(
    val icon: ImageVector,
    val text: String,
    val isWarning: Boolean = false,
)

fun computeClosureConsequences(
    selectedTypes: List<InterventionTypeDto>,
    plannedTypeCode: String?,
    hasLocalAttestationVe: Boolean,
    updatesRequireValidation: Boolean,
): List<ClosureConsequence> {
    if (selectedTypes.isEmpty()) return emptyList()

    val result = mutableListOf<ClosureConsequence>()
    val hasVeType = selectedTypes.any { it.isVeType }
    val plannedVeButNotSelected =
        plannedTypeCode == "VE" && selectedTypes.isNotEmpty() && !hasVeType

    if (hasVeType) {
        if (hasLocalAttestationVe) {
            result += ClosureConsequence(
                icon = Icons.Default.MarkEmailRead,
                text = "Le client recevra :\n• Son rapport d'intervention\n• Son attestation d'entretien",
            )
        } else {
            result += ClosureConsequence(
                icon = Icons.Default.MarkEmailRead,
                text = "Le client recevra :\n• Son rapport d'intervention",
            )
            result += ClosureConsequence(
                icon = Icons.Default.Warning,
                text = "Attestation d'entretien non remplie",
                isWarning = true,
            )
        }
    } else {
        result += ClosureConsequence(
            icon = Icons.Default.MarkEmailRead,
            text = "Le client recevra :\n• Son rapport d'intervention",
        )
    }

    if (plannedVeButNotSelected) {
        result += ClosureConsequence(
            icon = Icons.Default.Warning,
            text = "La date du dernier entretien ne sera pas mise à jour",
            isWarning = true,
        )
    }

    if (selectedTypes.any { it.requireClientSignature }) {
        result += ClosureConsequence(
            icon = Icons.Default.Draw,
            text = "Signature du client requise à l'étape suivante",
        )
    }

    if (updatesRequireValidation) {
        result += ClosureConsequence(
            icon = Icons.Default.HourglassTop,
            text = "Le rapport sera envoyé après validation du bureau",
        )
    }

    return result
}
