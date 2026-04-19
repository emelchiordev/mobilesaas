package re.melchior.saviomobile.ui.screen.tournee

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import re.melchior.saviomobile.R

@Composable
fun TourneeCardTablet(
    intervention: InterventionItem,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val accentColor = when (intervention.status) {
        "completed" -> colorResource(R.color.accent_done)
        "in_progress" -> colorResource(R.color.accent_inprog)
        else -> colorResource(R.color.accent_todo)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isSelected) colorResource(R.color.savio_primary_light)
                else colorResource(R.color.card_bg)
            )
            .clickable(onClick = onClick)
            .alpha(if (intervention.isCompleted) 0.5f else 1f),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .width(3.dp)
                .height(64.dp)
                .background(accentColor)
        )
        Column(
            Modifier
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .weight(1f)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = intervention.time,
                    fontSize = 11.sp,
                    color = colorResource(R.color.badge_neutral_text).copy(alpha = 0.65f)
                )
                if (intervention.status == "in_progress" && intervention.elapsedTime.isNotBlank()) {
                    Text(
                        text = intervention.elapsedTime,
                        fontSize = 10.sp,
                        color = colorResource(R.color.badge_inprog_text),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Text(
                text = intervention.clientName,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = colorResource(R.color.badge_neutral_text)
            )
            Text(
                text = intervention.address,
                fontSize = 11.sp,
                color = colorResource(R.color.badge_neutral_text).copy(alpha = 0.55f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (isSelected) {
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = colorResource(R.color.savio_primary),
                modifier = Modifier.padding(end = 8.dp).size(16.dp)
            )
        }
    }
    HorizontalDivider(
        thickness = 0.5.dp,
        color = colorResource(R.color.badge_neutral_bg)
    )
}
