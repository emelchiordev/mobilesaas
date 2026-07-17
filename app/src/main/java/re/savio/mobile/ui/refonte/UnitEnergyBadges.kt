package re.savio.mobile.ui.refonte

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import re.savio.mobile.ui.theme.SavioRefonte
import re.savio.mobile.ui.theme.ViolettTokens
import re.savio.mobile.util.UnitEnergySummaryItem

private fun badgeColors(code: String): Pair<Color, Color> {
    val raw = code.lowercase()
    return if (Regex("gaz|fioul|propane|bois|granul").containsMatchIn(raw)) {
        Color(0xFFFFF3E8) to Color(0xFFB45309)
    } else if (Regex("élec|elec|pac|thermo|clim").containsMatchIn(raw)) {
        SavioRefonte.Navy.copy(alpha = 0.1f) to SavioRefonte.Navy
    } else {
        Color(0xFFF1F3F6) to SavioRefonte.Muted
    }
}

@Composable
fun UnitEnergyBadges(
    items: List<UnitEnergySummaryItem>,
    modifier: Modifier = Modifier,
    muted: Boolean = false,
) {
    if (items.isEmpty()) return
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        items.forEach { item ->
            val (bg, fg) =
                if (muted) {
                    ViolettTokens.Faint to ViolettTokens.Muted
                } else {
                    badgeColors(item.code)
                }
            Text(
                text = item.label,
                modifier =
                    Modifier
                        .background(bg, RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = fg,
            )
        }
    }
}
