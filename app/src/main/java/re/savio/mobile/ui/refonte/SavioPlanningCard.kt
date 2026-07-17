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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import re.savio.mobile.data.local.entity.InterventionEntity
import re.savio.mobile.ui.screen.tournee.FollowUpBadge
import re.savio.mobile.ui.screen.tournee.InterventionItem
import re.savio.mobile.ui.screen.tournee.toInterventionItem
import re.savio.mobile.util.UnitEnergySummaryItem
import re.savio.mobile.ui.theme.SavioDimens
import re.savio.mobile.ui.theme.ViolettTokens

@Composable
fun SavioPlanningCard(
    item: InterventionItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    typeCode: String? = null,
) {
    val addressParts = item.address.split(", ", limit = 2)
    val street = addressParts.getOrElse(0) { item.address }
    val city = addressParts.getOrElse(1) { "" }
    val isDone =
        savioStatusPillKind(item.status, item.syncStatus) == SavioStatusPillKind.Done

    Surface(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(SavioDimens.RadiusCard),
        color = ViolettTokens.Surface,
        shadowElevation = if (selected) 10.dp else 8.dp,
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (isDone) {
                PlanningDoneTypeBadge(size = 52.dp)
            } else {
                SavioTypeBadge(typeLabel = item.typeLabel, typeCode = typeCode, size = 52.dp)
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Schedule,
                            contentDescription = null,
                            tint = ViolettTokens.Ink2,
                            modifier = Modifier.size(15.dp),
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = item.planningLabel,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = ViolettTokens.Ink2,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (item.isUrgent) {
                            Box(
                                modifier =
                                    Modifier
                                        .background(
                                            MaterialTheme.colorScheme.errorContainer,
                                            RoundedCornerShape(20.dp),
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                            ) {
                                Text(
                                    text = "Urgent",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }
                        if (item.followUpPending) {
                            FollowUpBadge()
                        }
                        SavioStatusPill(status = item.status, syncStatus = item.syncStatus)
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = item.typeLabel,
                        modifier = Modifier.weight(1f),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = ViolettTokens.Ink,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    UnitEnergyBadges(items = item.energyBadges, muted = isDone)
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = item.clientName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = ViolettTokens.Ink2,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(5.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = null,
                        tint = ViolettTokens.FaintInk,
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
                        fontSize = 13.sp,
                        color = ViolettTokens.Muted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                item.conflictBannerText?.let { banner ->
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = banner,
                        fontSize = 11.sp,
                        color = ViolettTokens.StatusLiveFg,
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
    energyBadges: List<UnitEnergySummaryItem> = emptyList(),
) {
    SavioPlanningCard(
        item = intervention.toInterventionItem(energyBadges = energyBadges),
        onClick = onClick,
        modifier = modifier,
        selected = selected,
        typeCode = intervention.typeCode,
    )
}

@Composable
private fun PlanningDoneTypeBadge(
    modifier: Modifier = Modifier,
    size: Dp = 52.dp,
) {
    Box(
        modifier =
            modifier
                .size(size)
                .background(ViolettTokens.StatusDoneBg, RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.Check,
            contentDescription = "Terminée",
            tint = ViolettTokens.StatusDoneFg,
            modifier = Modifier.size(size * 0.48f),
        )
    }
}
