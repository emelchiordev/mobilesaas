package re.savio.mobile.ui.designsystem

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import re.savio.mobile.ui.theme.SavioAccent
import re.savio.mobile.ui.theme.SavioDimens
import re.savio.mobile.ui.theme.SavioRefonte
import re.savio.mobile.ui.theme.SavioType
import re.savio.mobile.ui.theme.useSavioRefonteUi

@Composable
fun SectionCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val refonte = useSavioRefonteUi()
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(if (refonte) SavioDimens.RadiusCard else SavioDimens.RadiusLG),
        color = MaterialTheme.colorScheme.surface,
        border =
            BorderStroke(
                SavioDimens.BorderThin,
                if (refonte) SavioRefonte.Line else MaterialTheme.colorScheme.outline,
            ),
        shadowElevation = 0.dp,
        tonalElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = SavioDimens.SpaceLG),
            content = content,
        )
    }
}

@Composable
fun SectionDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier = modifier.fillMaxWidth(),
        thickness = SavioDimens.BorderThin,
        color = MaterialTheme.colorScheme.outline,
    )
}

@Composable
fun SectionRow(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    leading: (@Composable RowScope.() -> Unit)? = null,
) {
    val rowModifier =
        modifier
            .fillMaxWidth()
            .heightIn(min = SavioDimens.SectionRowMinHeight)
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                },
            )
            .padding(vertical = SavioDimens.SpaceSM)
    Row(
        modifier = rowModifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SavioDimens.SpaceMD),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = SavioAccent,
            modifier = Modifier.size(20.dp),
        )
        leading?.invoke(this)
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = SavioType.Label, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = value, style = SavioType.BodyLarge, color = MaterialTheme.colorScheme.onSurface)
        }
        if (onClick != null) {
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = SavioAccent,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}
