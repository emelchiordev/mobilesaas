package re.melchior.saviomobile.ui.refonte

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import re.melchior.saviomobile.ui.theme.SavioRefonte
import re.melchior.saviomobile.ui.theme.interventionStatusLabel

enum class SavioStatusPillKind { Plan, Live, Done, Other }

fun savioStatusPillKind(status: String, syncStatus: String): SavioStatusPillKind {
    val label = interventionStatusLabel(status, syncStatus).lowercase()
    return when {
        label.contains("termin") || label.contains("clôturée ailleurs") -> SavioStatusPillKind.Done
        label.contains("cours") || label.contains("validation") || label.contains("attente") ||
            label.contains("sync") -> SavioStatusPillKind.Live
        label.contains("planifi") || label.contains("annul") || label.contains("ignor") ->
            SavioStatusPillKind.Plan
        else -> SavioStatusPillKind.Other
    }
}

@Composable
fun SavioStatusPill(
    label: String,
    kind: SavioStatusPillKind,
    modifier: Modifier = Modifier,
    big: Boolean = false,
) {
    val (bg, fg, dot) =
        when (kind) {
            SavioStatusPillKind.Plan ->
                Triple(SavioRefonte.StatusPlanBg, SavioRefonte.StatusPlanFg, SavioRefonte.StatusPlanDot)
            SavioStatusPillKind.Live ->
                Triple(SavioRefonte.StatusLiveBg, SavioRefonte.StatusLiveFg, SavioRefonte.StatusLiveDot)
            SavioStatusPillKind.Done ->
                Triple(SavioRefonte.StatusDoneBg, SavioRefonte.StatusDoneFg, SavioRefonte.StatusDoneDot)
            SavioStatusPillKind.Other ->
                Triple(SavioRefonte.Tint, SavioRefonte.Muted, SavioRefonte.Muted)
        }
    val vPad = if (big) 6.dp else 4.dp
    val hPad = if (big) 12.dp else 10.dp
    val fontSize = if (big) 13.5.sp else 12.5.sp
    Row(
        modifier =
            modifier
                .clip(RoundedCornerShape(999.dp))
                .background(bg)
                .padding(horizontal = hPad, vertical = vPad),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier =
                Modifier
                    .size(if (big) 7.dp else 6.dp)
                    .clip(CircleShape)
                    .background(dot),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            fontSize = fontSize,
            fontWeight = FontWeight.SemiBold,
            color = fg,
        )
    }
}

@Composable
fun SavioStatusPill(
    status: String,
    syncStatus: String,
    modifier: Modifier = Modifier,
    big: Boolean = false,
) {
    SavioStatusPill(
        label = interventionStatusLabel(status, syncStatus),
        kind = savioStatusPillKind(status, syncStatus),
        modifier = modifier,
        big = big,
    )
}
