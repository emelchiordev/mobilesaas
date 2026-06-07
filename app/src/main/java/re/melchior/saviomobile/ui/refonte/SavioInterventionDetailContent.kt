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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import re.melchior.saviomobile.data.local.entity.InterventionEntity
import re.melchior.saviomobile.ui.theme.SavioInterventionColors
import re.melchior.saviomobile.ui.theme.SavioRefonte
import re.melchior.saviomobile.util.formatPlanningLabel
import re.melchior.saviomobile.util.parseInterventionTimeSlot

import java.util.Locale

private val ChevronColor = Color(0xFFC2CBD6)

@Composable
fun SavioInterventionDetailContent(
    intervention: InterventionEntity,
    onClientClick: (String) -> Unit,
    onCallClick: (String) -> Unit,
    onNavigateClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        SavioInterventionMetaCard(intervention = intervention)
        if (!intervention.notes.isNullOrBlank()) {
            SavioInterventionNotesCard(notes = intervention.notes)
        }
        SavioInterventionClientCard(
            intervention = intervention,
            onClientClick = onClientClick,
        )
        SavioInterventionAddressCard(
            intervention = intervention,
            onNavigateClick = onNavigateClick,
            onCallClick = onCallClick,
        )
    }
}

@Composable
private fun SavioInterventionMetaCard(intervention: InterventionEntity) {
    SavioRefonteCard {
        Column(
            modifier = Modifier.padding(horizontal = 15.dp, vertical = 13.dp),
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
            Text(
                text = formatPlanningLabel(
                    parseInterventionTimeSlot(intervention.timeSlot),
                    intervention.scheduledAt,
                ),
                fontSize = 14.sp,
                color = SavioRefonte.Navy,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun SavioInterventionNotesCard(notes: String) {
    SavioRefonteCard {
        Row(
            modifier = Modifier.padding(horizontal = 15.dp, vertical = 13.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            SavioInfoIconBox(icon = Icons.Filled.Info)
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
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
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

@Composable
private fun SavioInterventionClientCard(
    intervention: InterventionEntity,
    onClientClick: (String) -> Unit,
) {
    val customerId = intervention.customerId
    val clientName =
        listOfNotNull(intervention.customerFirstName, intervention.customerLastName)
            .joinToString(" ")
            .trim()
            .ifBlank { "Non renseigné" }

    SavioRefonteCard {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .then(
                        if (customerId != null) {
                            Modifier.clickable { onClientClick(customerId) }
                        } else {
                            Modifier
                        },
                    )
                    .padding(horizontal = 15.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(13.dp),
        ) {
            Box(
                modifier =
                    Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(SavioRefonte.Tint),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = interventionDetailInitials(intervention.customerFirstName, intervention.customerLastName),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = SavioRefonte.Navy,
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Client",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SavioRefonte.Muted,
                )
                Text(
                    text = clientName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = SavioRefonte.Ink,
                )
            }
            if (customerId != null) {
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = "Fiche client",
                    tint = ChevronColor,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@Composable
private fun SavioInterventionAddressCard(
    intervention: InterventionEntity,
    onNavigateClick: () -> Unit,
    onCallClick: (String) -> Unit,
) {
    val addressValue =
        buildString {
            append(intervention.unitStreet)
            append("\n")
            intervention.unitAddressLine2?.takeIf { it.isNotBlank() }?.let {
                append(it)
                append("\n")
            }
            append("${intervention.unitPostalCode} ${intervention.unitCity}")
            val accessParts = buildList {
                intervention.unitFloor?.takeIf { it.isNotBlank() }?.let { add("Étage $it") }
                intervention.unitDoorCode?.takeIf { it.isNotBlank() }?.let { add("Code $it") }
            }
            if (accessParts.isNotEmpty()) {
                append("\n")
                append(accessParts.joinToString(" · "))
            }
        }

    SavioRefonteCard {
        SavioInfoRow(
            icon = Icons.Filled.LocationOn,
            label = "Adresse",
            value = addressValue,
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
    }
}

private fun interventionDetailInitials(first: String?, last: String?): String {
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
