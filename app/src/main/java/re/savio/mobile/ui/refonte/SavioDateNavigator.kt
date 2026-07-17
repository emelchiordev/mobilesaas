@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package re.savio.mobile.ui.refonte

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import re.savio.mobile.ui.theme.ViolettTokens
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
private fun SavioDateNavButton(
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = ViolettTokens.Surface,
        shadowElevation = 3.dp,
        modifier = Modifier.size(40.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = ViolettTokens.Accent,
            modifier = Modifier.padding(10.dp),
        )
    }
}

@Composable
fun SavioDateNavigator(
    selectedDate: LocalDate,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    modifier: Modifier = Modifier,
    isRefreshing: Boolean = false,
    subtitle: String? = null,
    showDateLabel: Boolean = false,
) {
    val formatter = DateTimeFormatter.ofPattern("EEEE d MMMM", Locale.FRENCH)
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 22.dp).padding(top = 2.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        SavioDateNavButton(
            onClick = onPreviousDay,
            icon = Icons.Filled.ChevronLeft,
            contentDescription = "Jour précédent",
        )
        Column(
            modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (showDateLabel) {
                Text(
                    text = selectedDate.format(formatter).replaceFirstChar { it.uppercase() },
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = ViolettTokens.Ink,
                )
            }
            subtitle?.let {
                Text(
                    text = it,
                    fontSize = if (showDateLabel) 13.sp else 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = ViolettTokens.Muted,
                    modifier = Modifier.padding(top = if (showDateLabel) 2.dp else 0.dp),
                    textAlign = TextAlign.Center,
                )
            }
            if (isRefreshing) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(top = 4.dp).size(14.dp),
                    strokeWidth = 2.dp,
                    color = ViolettTokens.Accent,
                )
            }
        }
        SavioDateNavButton(
            onClick = onNextDay,
            icon = Icons.Filled.ChevronRight,
            contentDescription = "Jour suivant",
        )
    }
}
