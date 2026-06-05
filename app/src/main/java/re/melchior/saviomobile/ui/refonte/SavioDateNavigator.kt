@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package re.melchior.saviomobile.ui.refonte

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import re.melchior.saviomobile.ui.theme.SavioRefonte
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
        shape = RoundedCornerShape(12.dp),
        color = androidx.compose.ui.graphics.Color.White,
        border = BorderStroke(1.dp, SavioRefonte.Line),
        shadowElevation = 1.dp,
        modifier = Modifier.size(38.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = SavioRefonte.Navy,
            modifier = Modifier.padding(9.dp),
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
) {
    val formatter = DateTimeFormatter.ofPattern("EEEE d MMMM", Locale.FRENCH)
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 18.dp).padding(top = 15.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        SavioDateNavButton(
            onClick = onPreviousDay,
            icon = Icons.Filled.ChevronLeft,
            contentDescription = "Jour précédent",
        )
        Column(
            modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = selectedDate.format(formatter).replaceFirstChar { it.uppercase() },
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = SavioRefonte.Ink,
            )
            subtitle?.let {
                Text(
                    text = it,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = SavioRefonte.Muted,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            if (isRefreshing) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(top = 4.dp).size(14.dp),
                    strokeWidth = 2.dp,
                    color = SavioRefonte.Navy,
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
