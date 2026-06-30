package re.melchior.saviomobile.ui.refonte

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import re.melchior.saviomobile.data.local.entity.InterventionEntity
import re.melchior.saviomobile.ui.theme.SavioInterventionColors
import re.melchior.saviomobile.ui.theme.SavioRefonte
import re.melchior.saviomobile.util.UnitEnergySummaryItem
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val ChevronColor = Color(0xFFC2CBD6)

@Composable
fun SavioActiveInterventionDetailBody(
    intervention: InterventionEntity,
    onClientClick: (String) -> Unit,
    onCallClick: (String) -> Unit,
    onNavigateClick: () -> Unit,
    energyBadges: List<UnitEnergySummaryItem> = emptyList(),
    modifier: Modifier = Modifier,
) {
    SavioRefonteCard(modifier = modifier) {
        Column {
            ActiveDetailInfoRow(
                icon = Icons.Filled.CalendarToday,
                title = formatInterventionDate(intervention.scheduledAt),
                subtitle = formatInterventionTime(intervention.scheduledAt),
                showDivider = true,
            )

            if (energyBadges.isNotEmpty()) {
                UnitEnergyBadges(
                    items = energyBadges,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                )
                SavioRowLine()
            }

            intervention.customerId?.let { customerId ->
                ActiveDetailClientRow(
                    intervention = intervention,
                    onClick = { onClientClick(customerId) },
                )
                SavioRowLine()
            }

            ActiveDetailInfoRow(
                icon = Icons.Filled.LocationOn,
                title = intervention.unitStreet,
                subtitle = "${intervention.unitPostalCode} ${intervention.unitCity}",
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
            SavioRowLine()

            intervention.contractType?.let { type ->
                ActiveDetailContractRow(
                    type = type,
                    renewalDate = intervention.contractRenewalDate,
                )
            }

            intervention.notes?.takeIf { it.isNotBlank() }?.let { notes ->
                if (intervention.contractType != null) {
                    SavioRowLine()
                }
                ActiveDetailNotesRow(notes = notes)
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
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        SavioTabletTwoColumnGrid(
            left = {
            SavioRefonteCard {
                Column {
                    Text(
                        text = "Rendez-vous & client",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SavioRefonte.Muted,
                        modifier = Modifier.padding(horizontal = 15.dp, vertical = 12.dp),
                    )
                    SavioRowLine()
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 15.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        SavioInfoIconBox(icon = Icons.Filled.CalendarToday)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = formatInterventionDate(intervention.scheduledAt),
                                fontSize = 15.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = SavioRefonte.Ink,
                            )
                            Text(
                                text = formatInterventionTime(intervention.scheduledAt),
                                fontSize = 13.sp,
                                color = SavioRefonte.Muted,
                                modifier = Modifier.padding(top = 2.dp),
                            )
                        }
                        if (energyBadges.isNotEmpty()) {
                            UnitEnergyBadges(items = energyBadges)
                        }
                    }
                    intervention.customerId?.let { customerId ->
                        SavioRowLine()
                        ActiveDetailClientRow(
                            intervention = intervention,
                            onClick = { onClientClick(customerId) },
                        )
                    }
                    intervention.contractType?.let { type ->
                        SavioRowLine()
                        ActiveDetailContractRow(
                            type = type,
                            renewalDate = intervention.contractRenewalDate,
                        )
                    }
                    intervention.notes?.takeIf { it.isNotBlank() }?.let { notes ->
                        SavioRowLine()
                        ActiveDetailNotesRow(notes = notes)
                    }
                }
            }
            },
            right = {
            SavioRefonteCard {
                Column {
                    Text(
                        text = "Adresse",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SavioRefonte.Muted,
                        modifier = Modifier.padding(horizontal = 15.dp, vertical = 12.dp),
                    )
                    SavioRowLine()
                    ActiveDetailInfoRow(
                        icon = Icons.Filled.LocationOn,
                        title = intervention.unitStreet,
                        subtitle = "${intervention.unitPostalCode} ${intervention.unitCity}",
                        showDivider = false,
                    )
                    SavioInfoActionsRow {
                        SavioGhostButton(
                            text = "Itinéraire",
                            icon = Icons.Filled.Directions,
                            onClick = onNavigateClick,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        intervention.customerPhone?.takeIf { it.isNotBlank() }?.let { phone ->
                            SavioGhostButton(
                                text = "Appeler",
                                icon = Icons.Filled.Call,
                                onClick = { onCallClick(phone) },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
            }
            },
        )
    }
}

@Composable
private fun ActiveDetailInfoRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    showDivider: Boolean,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 15.dp, vertical = 14.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SavioInfoIconBox(icon = icon)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.5.sp,
                fontWeight = FontWeight.Bold,
                color = SavioRefonte.Ink,
            )
            Text(
                text = subtitle,
                fontSize = 13.sp,
                color = SavioRefonte.Muted,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
    if (showDivider) SavioRowLine()
}

@Composable
private fun ActiveDetailClientRow(
    intervention: InterventionEntity,
    onClick: () -> Unit,
) {
    val clientName =
        listOfNotNull(intervention.customerFirstName, intervention.customerLastName)
            .joinToString(" ")
            .trim()
            .ifBlank { "Non renseigné" }

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 15.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(SavioRefonte.Tint),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text =
                    activeDetailInitials(
                        intervention.customerFirstName,
                        intervention.customerLastName,
                    ),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = SavioRefonte.Navy,
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = clientName,
                fontSize = 15.5.sp,
                fontWeight = FontWeight.Bold,
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
}

@Composable
private fun ActiveDetailContractRow(
    type: String,
    renewalDate: String?,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 15.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SavioInfoIconBox(icon = Icons.Filled.Description)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Contrat $type",
                fontSize = 15.5.sp,
                fontWeight = FontWeight.Bold,
                color = SavioRefonte.Ink,
            )
            renewalDate?.let { date ->
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

@Composable
private fun ActiveDetailNotesRow(notes: String) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 15.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        SavioInfoIconBox(icon = Icons.Filled.Description)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Notes dispatcher",
                fontSize = 12.5.sp,
                fontWeight = FontWeight.SemiBold,
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

private fun formatInterventionDate(scheduledAt: String?): String {
    if (scheduledAt.isNullOrBlank()) return "—"
    return try {
        val zdt = Instant.parse(scheduledAt).atZone(ZoneId.systemDefault())
        zdt.format(DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.FRENCH))
            .replaceFirstChar { it.uppercase() }
    } catch (_: Exception) {
        scheduledAt.take(10)
    }
}

private fun formatInterventionTime(scheduledAt: String?): String {
    if (scheduledAt.isNullOrBlank()) return "—"
    return try {
        val zdt = Instant.parse(scheduledAt).atZone(ZoneId.systemDefault())
        zdt.format(DateTimeFormatter.ofPattern("HH:mm", Locale.FRENCH))
    } catch (_: Exception) {
        "—"
    }
}

private fun formatContractDate(date: String?): String {
    if (date.isNullOrBlank()) return "—"
    return try {
        LocalDate.parse(date.take(10))
            .format(DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.FRENCH))
    } catch (_: Exception) {
        date.take(10)
    }
}
