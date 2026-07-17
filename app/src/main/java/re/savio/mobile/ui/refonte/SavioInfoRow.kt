package re.savio.mobile.ui.refonte

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import re.savio.mobile.ui.theme.SavioRefonte

@Composable
fun SavioInfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    showDivider: Boolean = true,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 15.dp, vertical = 13.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SavioInfoIconBox(icon = icon)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    fontSize = 12.5.sp,
                    color = SavioRefonte.Muted,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier.padding(bottom = 3.dp),
                )
                Text(
                    text = value,
                    fontSize = 15.5.sp,
                    color = SavioRefonte.Ink,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 22.sp,
                )
            }
        }
        if (showDivider) {
            SavioRowLine()
        }
    }
}

@Composable
fun SavioInfoActionsRow(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 15.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        content()
    }
    Spacer(modifier = Modifier.height(2.dp))
}

@Composable
fun SavioSectionHeader(
    title: String,
    count: Int? = null,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(top = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold,
            color = SavioRefonte.Ink,
        )
        count?.let {
            Text(
                text = it.toString(),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = SavioRefonte.Navy,
                modifier =
                    Modifier
                        .background(SavioRefonte.Tint, RoundedCornerShape(999.dp))
                        .padding(horizontal = 11.dp, vertical = 2.dp),
            )
        }
    }
}

@Composable
fun SavioHistorySectionHeader(
    count: Int,
    modifier: Modifier = Modifier,
) {
    SavioSectionHeader(title = "Historique", count = count, modifier = modifier)
}
