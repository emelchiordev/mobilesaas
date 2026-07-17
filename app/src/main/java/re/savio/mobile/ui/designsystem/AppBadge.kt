package re.savio.mobile.ui.designsystem

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import re.savio.mobile.ui.theme.SavioDimens
import re.savio.mobile.ui.theme.SavioPalette
import re.savio.mobile.ui.theme.SavioType

enum class AppBadgeStyle {
    Primary,
    Warning,
    Success,
    Error,
    Info,
}

@Composable
fun AppBadge(
    text: String,
    modifier: Modifier = Modifier,
    style: AppBadgeStyle = AppBadgeStyle.Primary,
) {
    val palette: Pair<Color, Color> =
        when (style) {
            AppBadgeStyle.Primary ->
                SavioPalette.PrimaryLight to SavioPalette.Primary
            AppBadgeStyle.Warning ->
                SavioPalette.WarningLight to SavioPalette.Warning
            AppBadgeStyle.Success ->
                SavioPalette.SuccessLight to SavioPalette.Success
            AppBadgeStyle.Error ->
                SavioPalette.ErrorLight to SavioPalette.Error
            AppBadgeStyle.Info ->
                SavioPalette.InfoLight to SavioPalette.Info
        }
    val bg = palette.first
    val fg = palette.second
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(SavioDimens.RadiusBadge),
        color = bg,
        shadowElevation = 0.dp,
        tonalElevation = 0.dp,
    ) {
        Text(
            text = text,
            modifier =
                Modifier.padding(
                    horizontal = 10.dp,
                    vertical = 4.dp,
                ),
            style = SavioType.Label,
            color = fg,
        )
    }
}
