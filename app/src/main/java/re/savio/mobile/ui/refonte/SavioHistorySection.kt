package re.savio.mobile.ui.refonte

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import re.savio.mobile.ui.theme.SavioRefonte

@Composable
fun SavioHistoryFilterChips(
    filters: List<String>,
    selected: String?,
    onSelect: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        filters.forEach { filter ->
            val isAllFilter = filter == "Tout" || filter == "Tous"
            val active = selected == filter || (isAllFilter && (selected == null || selected == "Tout" || selected == "Tous"))
            Text(
                text = filter,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (active) Color.White else Color(0xFF5A6573),
                modifier =
                    Modifier
                        .background(
                            if (active) SavioRefonte.Navy else Color.White,
                            RoundedCornerShape(999.dp),
                        )
                        .then(
                            if (!active) {
                                Modifier.border(1.dp, SavioRefonte.Line, RoundedCornerShape(999.dp))
                            } else {
                                Modifier
                            },
                        )
                        .clickable {
                            onSelect(if (isAllFilter) null else filter)
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
            )
        }
    }
}

@Composable
fun SavioHistoryMonthHeader(
    monthLabel: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = monthLabel.uppercase(),
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = SavioRefonte.Muted,
        letterSpacing = 0.6.sp,
        modifier = modifier.padding(top = 2.dp, bottom = 2.dp),
    )
}

@Composable
fun SavioHistoryEntryRow(
    title: String,
    subtitle: String,
    badge: String? = null,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth().padding(vertical = 10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = SavioRefonte.Ink,
                modifier = Modifier.weight(1f),
            )
            badge?.let {
                Text(
                    text = it,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SavioRefonte.StatusPlanFg,
                    modifier =
                        Modifier
                            .background(SavioRefonte.StatusPlanBg, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                )
            }
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = subtitle,
            fontSize = 12.sp,
            color = SavioRefonte.Muted,
        )
    }
}
