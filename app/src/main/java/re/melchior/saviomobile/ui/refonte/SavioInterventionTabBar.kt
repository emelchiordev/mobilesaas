package re.melchior.saviomobile.ui.refonte

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import re.melchior.saviomobile.ui.theme.SavioDimens
import re.melchior.saviomobile.ui.theme.SavioRefonte
import re.melchior.saviomobile.ui.theme.savioTabSelectedColor
import re.melchior.saviomobile.ui.theme.savioTabUnselectedColor
import re.melchior.saviomobile.ui.theme.useSavioRefonteUi

private val Tab4ActiveIconBg = Color(0xFFFCEFD9)

data class SavioInterventionTabItem(
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
)

@Composable
fun SavioInterventionTabBar(
    tabs: List<SavioInterventionTabItem>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val refonte = useSavioRefonteUi()
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .navigationBarsPadding(),
    ) {
        HorizontalDivider(
            thickness = SavioDimens.BorderThin,
            color = if (refonte) SavioRefonte.Line else MaterialTheme.colorScheme.outline,
        )
        Surface(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(SavioDimens.BottomNavHeight),
            color = if (refonte) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.background,
            shadowElevation = 0.dp,
            tonalElevation = 0.dp,
        ) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = if (refonte) 9.dp else 10.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                tabs.forEachIndexed { index, tab ->
                    SavioInterventionTab(
                        modifier = Modifier.weight(1f),
                        label = tab.label,
                        icon = if (index == selectedIndex) tab.selectedIcon else tab.icon,
                        selected = index == selectedIndex,
                        refonte = refonte,
                        onClick = { onSelect(index) },
                    )
                }
            }
        }
    }
}

@Composable
private fun SavioInterventionTab(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    refonte: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier =
            modifier
                .clickable(onClick = onClick)
                .padding(vertical = SavioDimens.SpaceXS),
    ) {
        if (refonte) {
            Box(
                modifier =
                    Modifier
                        .size(width = 46.dp, height = 30.dp)
                        .then(
                            if (selected) {
                                Modifier.background(Tab4ActiveIconBg, RoundedCornerShape(999.dp))
                            } else {
                                Modifier
                            },
                        ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (selected) SavioRefonte.Navy else SavioRefonte.NavInactive,
                    modifier = Modifier.size(23.dp),
                )
            }
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                color = if (selected) SavioRefonte.Navy else SavioRefonte.NavInactive,
            )
        } else {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) savioTabSelectedColor() else savioTabUnselectedColor(),
                modifier = Modifier.size(24.dp),
            )
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) savioTabSelectedColor() else savioTabUnselectedColor(),
            )
        }
    }
}
