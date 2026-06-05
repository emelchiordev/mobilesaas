package re.melchior.saviomobile.ui.refonte

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import re.melchior.saviomobile.data.local.entity.InterventionEntity
import re.melchior.saviomobile.ui.screen.tournee.InterventionItem
import re.melchior.saviomobile.ui.screen.tournee.conflictBannerText
import re.melchior.saviomobile.ui.screen.tournee.displayTypeLabel
import re.melchior.saviomobile.ui.theme.SavioDimens
import re.melchior.saviomobile.ui.theme.SavioRefonte

@Composable
fun SavioPlanningCard(
    item: InterventionItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    typeCode: String? = null,
) {
    val borderColor = if (selected) SavioRefonte.Navy else SavioRefonte.Line
    val borderWidth = if (selected) 1.5.dp else 1.dp
    val addressParts = item.address.split(", ", limit = 2)
    val street = addressParts.getOrElse(0) { item.address }
    val city = addressParts.getOrElse(1) { "" }

    Surface(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(SavioDimens.RadiusCard),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(borderWidth, borderColor),
        shadowElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 15.dp, vertical = 14.dp),
            verticalAlignment = Alignment.Top,
        ) {
            SavioTypeBadge(typeLabel = item.typeLabel, typeCode = typeCode, size = 46.dp)
            Spacer(modifier = Modifier.width(13.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Schedule,
                            contentDescription = null,
                            tint = SavioRefonte.Navy,
                            modifier = Modifier.size(15.dp),
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = item.time,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = SavioRefonte.Navy,
                        )
                    }
                    SavioStatusPill(status = item.status, syncStatus = item.syncStatus)
                }
                Spacer(modifier = Modifier.height(7.dp))
                Text(
                    text = item.typeLabel,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = SavioRefonte.Ink,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    letterSpacing = (-0.01).sp,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.clientName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = androidx.compose.ui.graphics.Color(0xFF46505F),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(5.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = null,
                        tint = androidx.compose.ui.graphics.Color(0xFFB3BDCA),
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text =
                            if (city.isNotBlank()) {
                                "$street · $city"
                            } else {
                                street
                            },
                        fontSize = 12.5.sp,
                        color = SavioRefonte.Muted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                item.conflictBannerText?.let { banner ->
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = banner,
                        fontSize = 11.sp,
                        color = SavioRefonte.StatusLiveFg,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }
}

@Composable
fun SavioPlanningCard(
    intervention: InterventionEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
) {
    val item =
        InterventionItem(
            id = intervention.id,
            time = re.melchior.saviomobile.util.formatScheduledAtTime(intervention.scheduledAt),
            clientName =
                listOfNotNull(intervention.customerFirstName, intervention.customerLastName)
                    .joinToString(" ")
                    .trim()
                    .ifEmpty { "—" },
            address =
                listOf(intervention.unitStreet, "${intervention.unitPostalCode} ${intervention.unitCity}")
                    .joinToString(", "),
            status = intervention.status,
            syncStatus = intervention.syncStatus,
            typeLabel = intervention.displayTypeLabel(),
            isCompleted = intervention.status == "completed" && intervention.syncStatus == "SYNCED",
            elapsedTime = "",
            conflictResolveAttempts = intervention.conflictResolveAttempts,
            conflictBannerText = intervention.conflictBannerText(),
        )
    SavioPlanningCard(
        item = item,
        onClick = onClick,
        modifier = modifier,
        selected = selected,
        typeCode = intervention.typeCode,
    )
}
