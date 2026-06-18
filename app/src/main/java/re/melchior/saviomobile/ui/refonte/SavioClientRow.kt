package re.melchior.saviomobile.ui.refonte

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import re.melchior.saviomobile.ui.screen.intervention.cloture.VeHintUrgency
import re.melchior.saviomobile.ui.theme.SavioRefonte

@Composable
fun SavioClientRow(
    name: String,
    subtitle: String?,
    initials: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    phone: String? = null,
    tag: String? = null,
    veHint: String? = null,
    veHintUrgency: VeHintUrgency? = null,
    incomplete: Boolean = false,
    showDivider: Boolean = true,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .alpha(if (incomplete) 0.92f else 1f)
                    .clickable(onClick = onClick)
                    .padding(horizontal = 15.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier =
                    Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            if (incomplete) Color(0xFFFBF0DC) else SavioRefonte.Tint,
                        ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = initials,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (incomplete) Color(0xFFC0820F) else SavioRefonte.Navy,
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = SavioRefonte.Ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                subtitle?.let {
                    Text(
                        text = it,
                        fontSize = 13.sp,
                        color = SavioRefonte.Muted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
                phone?.takeIf { it.isNotBlank() }?.let {
                    Text(
                        text = it,
                        fontSize = 12.5.sp,
                        color = Color(0xFFA6B0BD),
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
                tag?.let {
                    Text(
                        text = it,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SavioRefonte.StatusLiveFg,
                        modifier =
                            Modifier
                                .padding(top = 6.dp)
                                .background(Color(0xFFFBEFD7), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                    )
                }
                veHint?.let { hint ->
                    Text(
                        text = hint,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = veHintColor(veHintUrgency),
                        modifier = Modifier.padding(top = if (tag != null) 4.dp else 6.dp),
                    )
                }
            }
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = Color(0xFFC2CBD6),
                modifier = Modifier.size(20.dp),
            )
        }
        if (showDivider) {
            HorizontalDivider(
                color = Color(0xFFEDF1F6),
                thickness = 1.dp,
                modifier = Modifier.padding(horizontal = 15.dp),
            )
        }
    }
}

private fun veHintColor(urgency: VeHintUrgency?): Color =
    when (urgency) {
        VeHintUrgency.OK -> Color(0xFF1F7A4C)
        VeHintUrgency.SOON -> Color(0xFFB7791F)
        VeHintUrgency.OVERDUE -> Color(0xFFC0392B)
        VeHintUrgency.NEUTRAL, null -> SavioRefonte.Muted
    }
