package re.savio.mobile.ui.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import re.savio.mobile.ui.refonte.SavioRefonteFab
import re.savio.mobile.ui.theme.SavioDimens
import re.savio.mobile.ui.theme.SavioPalette
import re.savio.mobile.ui.theme.SavioRefonte
import re.savio.mobile.ui.theme.SavioType
import re.savio.mobile.ui.theme.ViolettTokens
import re.savio.mobile.ui.theme.useSavioRefonteUi

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
    centerFab: (@Composable () -> Unit)? = null,
) {
    val refonte = useSavioRefonteUi()
    if (refonte) {
        ViolettBottomNavBar(
            items = items,
            selectedIndex = selectedIndex,
            onSelect = onSelect,
            modifier = modifier,
            centerFab = centerFab,
        )
        return
    }

    val barColor = MaterialTheme.colorScheme.background
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .background(barColor),
    ) {
        androidx.compose.material3.HorizontalDivider(
            thickness = SavioDimens.BorderThin,
            color = MaterialTheme.colorScheme.outline,
        )
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(SavioDimens.BottomNavHeight),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                items.forEachIndexed { index, item ->
                    LegacyBottomNavTab(
                        item = item,
                        selected = index == selectedIndex,
                        onClick = { onSelect(index) },
                        activeColor = MaterialTheme.colorScheme.primary,
                        inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
        Spacer(modifier = Modifier.navigationBarsPadding())
    }
}

@Composable
private fun ViolettBottomNavBar(
    items: List<BottomNavItem>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    centerFab: (@Composable () -> Unit)? = null,
) {
    val fabOverflow = 26.dp
    val insertFab = centerFab != null && items.size == 2
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = SavioDimens.BottomNavBottomInset)
                .navigationBarsPadding(),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .then(
                        if (insertFab) {
                            Modifier.padding(top = fabOverflow)
                        } else {
                            Modifier
                        },
                    ),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(SavioDimens.BottomNavHeight)
                        .shadow(12.dp, RoundedCornerShape(SavioDimens.RadiusNav), clip = false)
                        .clip(RoundedCornerShape(SavioDimens.RadiusNav))
                        .background(ViolettTokens.Nav)
                        .padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (insertFab) {
                    ViolettNavTab(
                        item = items[0],
                        selected = selectedIndex == 0,
                        onClick = { onSelect(0) },
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(modifier = Modifier.width(80.dp))
                    ViolettNavTab(
                        item = items[1],
                        selected = selectedIndex == 1,
                        onClick = { onSelect(1) },
                        modifier = Modifier.weight(1f),
                    )
                } else {
                    items.forEachIndexed { index, item ->
                        ViolettNavTab(
                            item = item,
                            selected = index == selectedIndex,
                            onClick = { onSelect(index) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
            if (insertFab) {
                Box(
                    modifier =
                        Modifier
                            .align(Alignment.TopCenter)
                            .offset(y = -fabOverflow),
                    contentAlignment = Alignment.Center,
                ) {
                    centerFab?.invoke()
                }
            }
        }
    }
}

@Composable
private fun ViolettNavTab(
    item: BottomNavItem,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier =
            modifier
                .clickable(onClick = onClick)
                .padding(vertical = 6.dp),
    ) {
        if (selected) {
            Box(
                modifier =
                    Modifier
                        .width(26.dp)
                        .height(3.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(ViolettTokens.PrimaryGradientSoft),
            )
        } else {
            Spacer(modifier = Modifier.height(3.dp))
        }
        Spacer(modifier = Modifier.height(2.dp))
        Icon(
            imageVector = item.icon,
            contentDescription = item.label,
            tint = if (selected) Color.White else ViolettTokens.NavInk,
            modifier = Modifier.size(22.dp),
        )
        Text(
            text = item.label,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
            color = if (selected) Color.White else ViolettTokens.NavInk,
        )
    }
}

@Composable
private fun LegacyBottomNavTab(
    item: BottomNavItem,
    selected: Boolean,
    onClick: () -> Unit,
    activeColor: Color,
    inactiveColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier =
            modifier
                .clickable(onClick = onClick)
                .padding(vertical = SavioDimens.SpaceXS),
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.label,
            tint = if (selected) activeColor else inactiveColor,
            modifier = Modifier.size(24.dp),
        )
        Text(
            text = item.label,
            style = SavioType.NavLabel,
            color = if (selected) activeColor else inactiveColor,
        )
    }
}

@Composable
fun BottomNavBarWithFab(
    items: List<BottomNavItem>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    onFabClick: () -> Unit,
    modifier: Modifier = Modifier,
    fabContentDescription: String = "Nouvelle intervention",
) {
    BottomNavBar(
        items = items,
        selectedIndex = selectedIndex,
        onSelect = onSelect,
        modifier = modifier,
        centerFab = {
            SavioRefonteFab(
                onClick = onFabClick,
                contentDescription = fabContentDescription,
            )
        },
    )
}
