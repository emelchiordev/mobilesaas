package re.savio.mobile.ui.screen.tournee

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import re.savio.mobile.ui.refonte.SavioPlanningCard
import re.savio.mobile.ui.theme.SavioPalette
import re.savio.mobile.ui.theme.SavioUi
import re.savio.mobile.ui.theme.useSavioRefonteUi

@Composable
fun TourneeCardTablet(
    intervention: InterventionItem,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    if (useSavioRefonteUi()) {
        SavioPlanningCard(
            item = intervention,
            onClick = onClick,
            selected = isSelected,
            modifier =
                Modifier
                    .padding(horizontal = 8.dp, vertical = 6.dp)
                    .alpha(if (intervention.isCompleted) 0.65f else 1f),
        )
        return
    }
    val accentColor =
        when (intervention.status) {
            "completed" -> SavioPalette.Success
            "in_progress" -> SavioPalette.Accent
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        }
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(
                    if (isSelected) SavioUi.ChipBackground else MaterialTheme.colorScheme.surface,
                )
                .clickable(onClick = onClick)
                .alpha(if (intervention.isCompleted) 0.5f else 1f),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .width(3.dp)
                .height(64.dp)
                .background(accentColor),
        )
        Column(
            Modifier
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .weight(1f),
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = intervention.planningLabel,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = SavioUi.BusinessAccent,
                )
                if (intervention.isUrgent) {
                    Text(
                        text = "Urgent",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                if (intervention.followUpPending) {
                    FollowUpBadge()
                }
                if (intervention.status == "in_progress" && intervention.elapsedTime.isNotBlank()) {
                    Text(
                        text = intervention.elapsedTime,
                        fontSize = 10.sp,
                        color = SavioUi.BusinessAccent,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
            Text(
                text = intervention.clientName,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = intervention.address,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (isSelected) {
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = SavioUi.BusinessAccent,
                modifier = Modifier.padding(end = 8.dp).size(16.dp),
            )
        }
    }
    HorizontalDivider(
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.outline,
    )
}
