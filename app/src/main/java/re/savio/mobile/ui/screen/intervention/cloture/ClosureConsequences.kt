package re.savio.mobile.ui.screen.intervention.cloture

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.vector.ImageVector
import re.savio.mobile.data.remote.dto.InterventionTypeDto

data class ClosureConsequence(
    val icon: ImageVector,
    val text: String,
    val isWarning: Boolean = false,
)

internal fun buildClientWillReceiveText(
    hasLocalAttestationVe: Boolean,
    hasLocalCerfaFluides: Boolean,
): String {
    val bullets = mutableListOf("Son rapport d'intervention")
    if (hasLocalAttestationVe) {
        bullets += "Son attestation d'entretien"
    }
    if (hasLocalCerfaFluides) {
        bullets += "Son CERFA fluides frigorigènes"
    }
    return "Le client recevra :\n" + bullets.joinToString("\n") { "• $it" }
}

fun computeClosureConsequences(
    selectedTypes: List<InterventionTypeDto>,
    plannedType: InterventionTypeDto?,
    hasLocalAttestationVe: Boolean,
    updatesRequireValidation: Boolean,
    equipmentsMissingCommissioning: Int = 0,
    gasPipeExpiredWithoutAnomaly: Boolean = false,
    hasLocalCerfaFluides: Boolean = false,
): List<ClosureConsequence> {
    if (selectedTypes.isEmpty()) return emptyList()

    val result = mutableListOf<ClosureConsequence>()
    val hasVeType = selectedTypes.anyVeType()
    val hasEquipmentSetup = selectedTypes.anyEquipmentSetup()

    result +=
        ClosureConsequence(
            icon = Icons.Default.MarkEmailRead,
            text =
                buildClientWillReceiveText(
                    hasLocalAttestationVe = hasLocalAttestationVe && hasVeType,
                    hasLocalCerfaFluides = hasLocalCerfaFluides,
                ),
        )

    if (hasVeType && !hasLocalAttestationVe) {
        result +=
            ClosureConsequence(
                icon = Icons.Default.Warning,
                text = "Attestation d'entretien non remplie",
                isWarning = true,
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
