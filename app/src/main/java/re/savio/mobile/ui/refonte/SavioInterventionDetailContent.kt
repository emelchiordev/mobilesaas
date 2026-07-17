package re.savio.mobile.ui.refonte

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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import re.savio.mobile.data.local.entity.InterventionEntity
import re.savio.mobile.ui.theme.SavioInterventionColors
import re.savio.mobile.ui.theme.SavioRefonte
import re.savio.mobile.ui.theme.ViolettTokens
import re.savio.mobile.util.UnitEnergySummaryItem
import java.util.Locale

private val ChevronColor = Color(0xFFC2CBD6)

@Composable
fun SavioViolettAvatar(
    initials: String,
    modifier: Modifier = Modifier,
    size: Dp = 52.dp,
    gradient: Boolean = true,
) {
    Box(
        modifier =
            modifier
                .size(size)
                .clip(CircleShape)
                .then(
                    if (gradient) {
                        Modifier.background(SavioRefonte.PrimaryGradient, CircleShape)
                    } else {
                        Modifier.background(ViolettTokens.Tint, CircleShape)
                    },
                ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initials,
            fontSize = if (size >= 50.dp) 18.sp else 16.sp,
            fontWeight = FontWeight.Medium,
            color = if (gradient) Color.White else ViolettTokens.Accent,
        )
    }
}

@Composable
fun SavioInterventionDetailContent(
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
        SavioInterventionClientCard(
            intervention = intervention,
            energyBadges = energyBadges,
            onClientClick = onClientClick,
        )
        SavioInterventionAddressCard(
            intervention = intervention,
            onNavigateClick = onNavigateClick,
            onCallClick = onCallClick,
        )
        if (!intervention.notes.isNullOrBlank()) {
            SavioInterventionNotesCard(notes = intervention.notes)
        }
    }
}

@Composable
internal fun SavioInterventionNotesCard(notes: String) {
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

@Composable
internal fun SavioInterventionClientCard(
    intervention: InterventionEntity,
    onClientClick: (String) -> Unit,
    energyBadges: List<UnitEnergySummaryItem> = emptyList(),
) {
    val customerId = intervention.customerId
    val clientName =
        listOfNotNull(intervention.customerFirstName, intervention.customerLastName)
            .joinToString(" ")
            .trim()
            .ifBlank { "Non renseigné" }

    SavioRefonteCard(contentPadding = false) {
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
                    .padding(horizontal = 15.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            SavioViolettAvatar(
                initials =
                    interventionDetailInitials(
                        intervention.customerFirstName,
                        intervention.customerLastName,
                    ),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Client",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Normal,
                    color = SavioRefonte.Muted,
                )
                Text(
                    text = clientName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = SavioRefonte.Ink,
                )
                if (energyBadges.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    UnitEnergyBadges(items = energyBadges)
                }
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
internal fun SavioInterventionAddressCard(
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

    SavioRefonteCard(contentPadding = false) {
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

@Composable
internal fun SavioInterventionClientAddressCard(
    intervention: InterventionEntity,
    onClientClick: (String) -> Unit,
    onNavigateClick: () -> Unit,
    onCallClick: (String) -> Unit,
    energyBadges: List<UnitEnergySummaryItem> = emptyList(),
) {
    SavioInterventionDetailContent(
        intervention = intervention,
        onClientClick = onClientClick,
        onNavigateClick = onNavigateClick,
        onCallClick = onCallClick,
        energyBadges = energyBadges,
    )
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
