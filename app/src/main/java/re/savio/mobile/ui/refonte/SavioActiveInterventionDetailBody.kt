package re.savio.mobile.ui.refonte

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import re.savio.mobile.data.local.entity.InterventionEntity
import re.savio.mobile.ui.theme.SavioInterventionColors
import re.savio.mobile.ui.theme.SavioRefonte
import re.savio.mobile.util.UnitEnergySummaryItem
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val ChevronColor = androidx.compose.ui.graphics.Color(0xFFC2CBD6)

@Composable
fun SavioActiveInterventionDetailBody(
    intervention: InterventionEntity,
    onClientClick: (String) -> Unit,
    onCallClick: (String) -> Unit,
    onNavigateClick: () -> Unit,
    energyBadges: List<UnitEnergySummaryItem> = emptyList(),
    modifier: Modifier = Modifier,
) {
    val customerId = intervention.customerId
    val clientName =
        listOfNotNull(intervention.customerFirstName, intervention.customerLastName)
            .joinToString(" ")
            .trim()
            .ifBlank { "Non renseigné" }

    SavioRefonteCard(modifier = modifier, contentPadding = false) {
        Column {
            customerId?.let { id ->
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clickable { onClientClick(id) }
                            .padding(horizontal = 15.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    SavioViolettAvatar(
                        initials =
                            activeDetailInitials(
                                intervention.customerFirstName,
                                intervention.customerLastName,
                            ),
                        size = 46.dp,
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = clientName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = SavioRefonte.Ink,
                        )
                        intervention.customerPhone?.takeIf { it.isNotBlank() }?.let { phone ->
                            Text(
                                text = phone,
                                fontSize = 13.sp,
                                color = SavioRefonte.Link,
                                modifier = Modifier.padding(top = 2.dp),
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Filled.ChevronRight,
                        contentDescription = "Fiche client",
                        tint = ChevronColor,
                        modifier = Modifier.size(20.dp),
                    )
                }
                SavioRowLine()
            }

            SavioInfoRow(
                icon = Icons.Filled.LocationOn,
                label = "Adresse",
                value =
                    buildString {
                        append(intervention.unitStreet)
                        append("\n")
                        append("${intervention.unitPostalCode} ${intervention.unitCity}")
                    },
                showDivider = false,
            )
            SavioInfoActionsRow {
                SavioGhostButton(
                    text = "Itinéraire",
                    icon = Icons.Filled.Directions,
                    onClick = onNavigateClick,
                    modifier = Modifier.weight(1f),
                )
                intervention.customerPhone?.takeIf { it.isNotBlank() }?.let { phone ->
                    SavioGhostButton(
                        text = "Appeler",
                        icon = Icons.Filled.Call,
                        onClick = { onCallClick(phone) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            intervention.contractType?.let { type ->
                SavioRowLine()
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 15.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(13.dp),
                ) {
                    SavioInfoIconBox(icon = Icons.Filled.Description)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Contrat $type",
                            fontSize = 15.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = SavioRefonte.Ink,
                        )
                        intervention.contractRenewalDate?.let { date ->
                            Text(
                                text = "Échéance ${formatContractDate(date)}",
                                fontSize = 13.sp,
                                color = SavioRefonte.Muted,
                                modifier = Modifier.padding(top = 2.dp),
                            )
                        }
                    }
                    SavioStatusPill(label = "Actif", kind = SavioStatusPillKind.Done)
                }
            }

            intervention.notes?.takeIf { it.isNotBlank() }?.let { notes ->
                SavioRowLine()
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 15.dp, vertical = 13.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    SavioInfoIconBox(icon = Icons.Filled.Info)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Notes dispatcher",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Normal,
                            color = SavioRefonte.Muted,
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            color = SavioInterventionColors.NotesDispatcherBg,
                        ) {
                            Text(
                                text = notes,
                                fontSize = 14.sp,
                                color = SavioInterventionColors.NotesDispatcherText,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(10.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SavioActiveInterventionTabletDetailBody(
    intervention: InterventionEntity,
    onClientClick: (String) -> Unit,
    onCallClick: (String) -> Unit,
    onNavigateClick: () -> Unit,
    energyBadges: List<UnitEnergySummaryItem> = emptyList(),
    modifier: Modifier = Modifier,
) {
    SavioActiveInterventionDetailBody(
        intervention = intervention,
        onClientClick = onClientClick,
        onCallClick = onCallClick,
        onNavigateClick = onNavigateClick,
        energyBadges = energyBadges,
        modifier = modifier,
    )
}

private fun activeDetailInitials(first: String?, last: String?): String {
    val f = first?.trim().orEmpty()
    val l = last?.trim().orEmpty()
    return when {
        f.isNotEmpty() && l.isNotEmpty() ->
            "${f.first().uppercaseChar()}${l.first().uppercaseChar()}"
        f.length >= 2 -> f.take(2).uppercase(Locale.FRANCE)
        f.isNotEmpty() -> f.first().uppercaseChar().toString()
        l.length >= 2 -> l.take(2).uppercase(Locale.FRANCE)
        l.isNotEmpty() -> l.first().uppercaseChar().toString()
        else -> "?"
    }
}

private fun formatContractDate(date: String?): String {
    if (date.isNullOrBlank()) return "—"
    return try {
        LocalDate.parse(date.take(10))
            .format(DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.FRANCE))
    } catch (_: Exception) {
        date.take(10)
    }
}
