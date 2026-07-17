package re.savio.mobile.ui.refonte

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import re.savio.mobile.ui.theme.SavioRefonte
import re.savio.mobile.ui.theme.ViolettTokens

private val ChevronColor = Color(0xFFC2CBD6)

@Composable
fun SavioInfoIconBox(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 40.dp,
) {
    Box(
        modifier =
            modifier
                .size(size)
                .background(ViolettTokens.Faint, RoundedCornerShape(13.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = ViolettTokens.Accent,
            modifier = Modifier.size(size * 0.5f),
        )
    }
}

@Composable
fun SavioInfoLinkRow(
    icon: ImageVector,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 15.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SavioInfoIconBox(icon = icon)
        Text(
            text = value,
            fontSize = 15.5.sp,
            fontWeight = FontWeight.Medium,
            color = SavioRefonte.Link,
            modifier = Modifier.weight(1f),
        )
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = ChevronColor,
            modifier = Modifier.size(18.dp),
        )
    }
}

@Composable
fun SavioHistoryRow(
    typeLabel: String,
    dateLabel: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    typeCode: String? = null,
    isLast: Boolean = false,
    expanded: Boolean = false,
    expandedContent: (@Composable ColumnScope.() -> Unit)? = null,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClick)
                    .padding(horizontal = 15.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SavioTypeBadge(
                typeLabel = typeLabel,
                typeCode = typeCode,
                size = 36.dp,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = typeLabel,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = SavioRefonte.Ink,
                )
                Text(
                    text = dateLabel,
                    fontSize = 12.5.sp,
                    color = SavioRefonte.Muted,
                    modifier = Modifier.padding(top = 1.dp),
                )
            }
            if (isLast) {
                Text(
                    text = "Dernière",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = SavioRefonte.StatusDoneFg,
                    modifier =
                        Modifier
                            .background(SavioRefonte.StatusDoneBg, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                )
            }
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = ChevronColor,
                modifier = Modifier.size(18.dp),
            )
        }
        if (expanded) {
            expandedContent?.invoke(this)
        }
    }
}

@Composable
fun SavioHistoryShowMoreRow(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 13.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "···",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = SavioRefonte.Muted,
        )
    }
}
