package re.melchior.saviomobile.ui.refonte

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import re.melchior.saviomobile.ui.theme.SavioRefonte

@Composable
fun SavioRefonteTabRow(
    tabs: List<String>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .background(Color.White),
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            tabs.forEachIndexed { index, label ->
                val selected = index == selectedIndex
                Box(
                    modifier =
                        Modifier
                            .weight(1f)
                            .clickable { onTabSelected(index) }
                            .padding(top = 15.dp, bottom = 13.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = label,
                            fontSize = 14.5.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                            color = if (selected) SavioRefonte.Navy else SavioRefonte.Muted,
                        )
                        if (selected) {
                            Box(
                                modifier =
                                    Modifier
                                        .padding(top = 10.dp)
                                        .fillMaxWidth(0.62f)
                                        .height(3.dp)
                                        .background(SavioRefonte.Navy, androidx.compose.foundation.shape.RoundedCornerShape(3.dp)),
                            )
                        }
                    }
                }
            }
        }
        HorizontalDivider(color = SavioRefonte.Line, thickness = 1.dp)
    }
}
