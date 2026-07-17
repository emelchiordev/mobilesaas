package re.savio.mobile.ui.screen.intervention

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import re.savio.mobile.ui.refonte.SavioEquipActionButtons
import re.savio.mobile.ui.refonte.SavioEquipGroupHeader
import re.savio.mobile.ui.refonte.SavioEquipInfoBlock
import re.savio.mobile.ui.refonte.SavioEquipNavRow
import re.savio.mobile.ui.refonte.SavioEquipSpecRow
import re.savio.mobile.ui.refonte.SavioRefonteCard
import re.savio.mobile.ui.theme.SavioRefonte

@Composable
fun EquipementDetailRefonteContent(
    isReplaced: Boolean,
    isBruleur: Boolean,
    isNew: Boolean,
    hasCatalogNotice: Boolean,
    interventionInProgress: Boolean,
    showMeasures: Boolean,
    showEcsControl: Boolean,
    isPacOrClim: Boolean,
    attestationNavOnPacCard: Boolean,
    attestationNavSubtitle: String,
    isPartOfHybrideAsPac: Boolean,
    hybrideChaudiereLabel: String?,
    serialDisplayValue: String,
    installDisplayValue: String,
    energyDisplayValue: String,
    serialTodo: Boolean,
    installTodo: Boolean,
    energyTodo: Boolean,
    onSerialEditClick: () -> Unit,
    onInstallDateEditClick: () -> Unit,
    onEnergyEditClick: () -> Unit,
    onNoticeClick: () -> Unit,
    onDiagnosticClick: () -> Unit,
    onMeasureClick: () -> Unit,
    onEcsControlClick: () -> Unit,
    onPacMeasureClick: () -> Unit,
    onCerfaClick: () -> Unit,
    onAttestationClick: () -> Unit,
    onCorrect: () -> Unit,
    onReplace: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val todoCount = listOfNotNull(
        serialTodo,
        installTodo,
        if (!isBruleur) energyTodo else null,
    ).count { it }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SavioEquipGroupHeader(
                icon = Icons.Filled.Edit,
                title = "À compléter",
                count = todoCount.takeIf { it > 0 },
                countIsWarning = todoCount > 0,
            )
            SavioRefonteCard {
                SavioEquipSpecRow(
                    icon = Icons.Filled.Edit,
                    label = "N° série",
                    value = serialDisplayValue,
                    isTodo = serialTodo,
                    onActionClick = if (!isReplaced) onSerialEditClick else null,
                    showDivider = false,
                )
                SavioEquipSpecRow(
                    icon = Icons.Filled.CalendarToday,
                    label = "Mise en service",
                    value = installDisplayValue,
                    isTodo = installTodo,
                    onActionClick = if (!isReplaced) onInstallDateEditClick else null,
                    showDivider = true,
                )
                if (!isBruleur) {
                    SavioEquipSpecRow(
                        icon = Icons.Filled.Bolt,
                        label = "Énergie raccordée",
                        value = energyDisplayValue,
                        isTodo = energyTodo,
                        onActionClick = if (!isReplaced) onEnergyEditClick else null,
                        showDivider = true,
                    )
                }
            }
        }

        val showAttestationRow =
            !isReplaced && !isBruleur && !showEcsControl && !isPartOfHybrideAsPac
        val showDocumentsSection =
            hasCatalogNotice ||
                (interventionInProgress && !isReplaced) ||
                (showMeasures && !isReplaced) ||
                (showEcsControl && !isReplaced) ||
                (isPacOrClim && !isReplaced) ||
                showAttestationRow

        if (showDocumentsSection) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SavioEquipGroupHeader(
                    icon = Icons.Filled.Description,
                    title = "Documents & outils",
                )
                SavioRefonteCard {
                    var hasPreviousRow = false
                    if (hasCatalogNotice) {
                        SavioEquipNavRow(
                            icon = Icons.Filled.PictureAsPdf,
                            title = "Notice constructeur",
                            subtitle = "Consulter le PDF",
                            onClick = onNoticeClick,
                            showDivider = hasPreviousRow,
                        )
                        hasPreviousRow = true
                    }
                    if (interventionInProgress && !isReplaced) {
                        SavioEquipNavRow(
                            icon = Icons.Outlined.Search,
                            title = "Aide au diagnostic",
                            subtitle = "Pré-diagnostic guidé · Beta",
                            onClick = onDiagnosticClick,
                            showDivider = hasPreviousRow,
                        )
                        hasPreviousRow = true
                    }
                    if (showMeasures && !isReplaced) {
                        SavioEquipNavRow(
                            icon = Icons.Filled.Analytics,
                            title = "Mesures",
                            subtitle = "Saisir les mesures de combustion",
                            onClick = onMeasureClick,
                            showDivider = hasPreviousRow,
                        )
                        hasPreviousRow = true
                    }
                    if (showEcsControl && !isReplaced) {
                        SavioEquipNavRow(
                            icon = Icons.Filled.WaterDrop,
                            title = "Contrôle ECS",
                            subtitle = "Contrôle chauffe-eau / ballon ECS",
                            onClick = onEcsControlClick,
                            showDivider = hasPreviousRow,
                        )
                        hasPreviousRow = true
                    }
                    if (isPacOrClim && !isReplaced) {
                        SavioEquipNavRow(
                            icon = Icons.Filled.AcUnit,
                            title = "Mesures PAC",
                            subtitle = "Saisie terrain",
                            onClick = onPacMeasureClick,
                            showDivider = hasPreviousRow,
                        )
                        hasPreviousRow = true
                        SavioEquipNavRow(
                            icon = Icons.Filled.Description,
                            title = "CERFA fluides frigorigènes",
                            subtitle = "Remplir ou consulter",
                            onClick = onCerfaClick,
                            showDivider = hasPreviousRow,
                        )
                        hasPreviousRow = true
                        if (attestationNavOnPacCard && showAttestationRow) {
                            SavioEquipNavRow(
                                icon = Icons.Filled.Assignment,
                                title = "Attestation d'entretien",
                                subtitle = attestationNavSubtitle,
                                onClick = onAttestationClick,
                                showDivider = hasPreviousRow,
                            )
                            hasPreviousRow = true
                        }
                    }
                    if (showAttestationRow && !attestationNavOnPacCard) {
                        SavioEquipNavRow(
                            icon = Icons.Filled.Assignment,
                            title = "Attestation d'entretien",
                            subtitle = attestationNavSubtitle,
                            onClick = onAttestationClick,
                            showDivider = hasPreviousRow,
                        )
                    }
                }
            }
        }

        if (isPartOfHybrideAsPac && !hybrideChaudiereLabel.isNullOrBlank()) {
            SavioEquipInfoBlock(
                title = "Système PAC Hybride",
                body = "L'attestation se démarre depuis la chaudière $hybrideChaudiereLabel.",
            )
        }

        if (!isReplaced) {
            SavioEquipActionButtons(
                onCorrect = onCorrect,
                onReplace = onReplace,
            )
            if (isNew) {
                Text(
                    text =
                        "Appareil ajouté pendant cette intervention — " +
                            "supprimez-le si vous voulez l'annuler",
                    fontSize = 12.sp,
                    color = SavioRefonte.Muted,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}
