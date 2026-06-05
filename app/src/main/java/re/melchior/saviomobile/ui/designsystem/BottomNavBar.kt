package re.melchior.saviomobile.ui.designsystem

import androidx.compose.material3.MaterialTheme

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import re.melchior.saviomobile.ui.theme.SavioPalette
import re.melchior.saviomobile.ui.theme.SavioDimens
import re.melchior.saviomobile.ui.theme.SavioRefonte
import re.melchior.saviomobile.ui.theme.SavioType
import re.melchior.saviomobile.ui.theme.useSavioRefonteUi

data class BottomNavItem(
    val icon: ImageVector,
    val label: String,
)

@Composable
fun BottomNavBar(
    items: List<BottomNavItem>,
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
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                items.forEachIndexed { index, item ->
                    val selected = index == selectedIndex
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier =
                            Modifier
                                .weight(1f)
                                .clickable { onSelect(index) }
                                .padding(vertical = SavioDimens.SpaceXS),
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            tint =
                                if (selected) {
                                    if (refonte) SavioRefonte.OrangeAction else SavioPalette.Accent
                                } else {
                                    if (refonte) SavioRefonte.NavInactive else MaterialTheme.colorScheme.onSurfaceVariant
                                },
                            modifier = Modifier.size(24.dp),
                        )
                        Text(
                            text = item.label,
                            style = SavioType.NavLabel,
                            color =
                                if (selected) {
                                    if (refonte) SavioRefonte.OrangeAction else SavioPalette.Accent
                                } else {
                                    if (refonte) SavioRefonte.NavInactive else MaterialTheme.colorScheme.onSurfaceVariant
                                },
                        )
                    }
                }
            }
        }
    }
}
