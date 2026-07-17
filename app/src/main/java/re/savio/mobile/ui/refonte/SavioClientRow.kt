package re.savio.mobile.ui.refonte

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
import re.savio.mobile.ui.screen.intervention.cloture.VeHintUrgency
import re.savio.mobile.ui.theme.ViolettTokens

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
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(
                            if (incomplete) ViolettTokens.WarnTagBg else ViolettTokens.Tint,
                        ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = initials,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (incomplete) ViolettTokens.WarnTagFg else ViolettTokens.Accent,
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = ViolettTokens.Ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                subtitle?.let {
                    Text(
                        text = it,
                        fontSize = 13.sp,
                        color = ViolettTokens.Muted,
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
                        color = ViolettTokens.StatusLiveFg,
                        modifier =
                            Modifier
                                .padding(top = 6.dp)
                                .background(ViolettTokens.WarnTagBg, RoundedCornerShape(7.dp))
                                .padding(horizontal = 9.dp, vertical = 3.dp),
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
                tint = ViolettTokens.FaintInk,
                modifier = Modifier.size(20.dp),
            )
        }
        if (showDivider) {
            HorizontalDivider(
                color = ViolettTokens.Line,
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
        VeHintUrgency.NEUTRAL, null -> ViolettTokens.Muted
    }
