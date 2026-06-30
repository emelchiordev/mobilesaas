package re.melchior.saviomobile.ui.refonte

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import re.melchior.saviomobile.data.local.entity.InterventionEntity
import re.melchior.saviomobile.ui.screen.tournee.displayTypeLabel
import re.melchior.saviomobile.ui.theme.SavioRefonte
import re.melchior.saviomobile.util.UnitEnergySummaryItem
import re.melchior.saviomobile.util.formatPlanningLabel
import re.melchior.saviomobile.util.parseInterventionTimeSlot

@Composable
fun SavioTabletPlanningDetailContent(
    intervention: InterventionEntity,
    energyBadges: List<UnitEnergySummaryItem>,
    onClientClick: (String) -> Unit,
    onCallClick: (String) -> Unit,
    onNavigateClick: () -> Unit,
    onPrimaryClick: () -> Unit,
    showPrimaryAction: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val primaryLabel =
        if (intervention.status == "scheduled") {
            "Démarrer l'intervention"
        } else {
            "Reprendre l'intervention"
        }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        SavioRefonteCard {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = "Intervention",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SavioRefonte.Muted,
                    )
                    intervention.number?.takeIf { it.isNotBlank() }?.let { number ->
                        Text(
                            text = number,
                            fontSize = 15.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold,
                            color = SavioRefonte.Ink,
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Build,
                            contentDescription = null,
                            tint = SavioRefonte.Navy,
                            modifier = Modifier.height(18.dp),
                        )
                        Text(
                            text = intervention.displayTypeLabel(),
                            fontSize = 14.sp,
                            color = SavioRefonte.Navy,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                    Text(
                        text =
                            formatPlanningLabel(
                                parseInterventionTimeSlot(intervention.timeSlot),
                                intervention.scheduledAt,
                            ),
                        fontSize = 14.sp,
                        color = SavioRefonte.Navy,
                        fontWeight = FontWeight.Medium,
                    )
                    if (energyBadges.isNotEmpty()) {
                        UnitEnergyBadges(items = energyBadges)
                    }
                }
                if (showPrimaryAction) {
                    Button(
                        onClick = onPrimaryClick,
                        modifier =
                            Modifier
                                .height(44.dp),
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = SavioRefonte.Navy,
                                contentColor = Color.White,
                            ),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.height(18.dp),
                        )
                        Text(
                            text = primaryLabel,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(start = 4.dp),
                        )
                    }
                }
            }
        }

        SavioTabletTwoColumnGrid(
            left = {
                SavioInterventionClientBlockCard(
                    intervention = intervention,
                    onClientClick = onClientClick,
                    onCallClick = onCallClick,
                )
            },
            right = {
                SavioInterventionAddressBlockCard(
                    intervention = intervention,
                    onNavigateClick = onNavigateClick,
                    onCallClick = onCallClick,
                )
            },
        )

        if (!intervention.notes.isNullOrBlank()) {
            SavioInterventionNotesCard(notes = intervention.notes)
        }
    }
}
