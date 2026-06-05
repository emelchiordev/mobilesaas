package re.melchior.saviomobile.ui.refonte

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import re.melchior.saviomobile.ui.theme.SavioDimens
import re.melchior.saviomobile.ui.theme.SavioRefonte

private val RowLineColor = Color(0xFFEDF1F6)

@Composable
fun SavioRefonteCard(
    modifier: Modifier = Modifier,
    contentPadding: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(SavioDimens.RadiusCard),
        color = Color.White,
        border = BorderStroke(1.dp, SavioRefonte.Line),
        shadowElevation = 1.dp,
        tonalElevation = 0.dp,
    ) {
        Column(
            modifier =
                if (contentPadding) {
                    Modifier.padding(vertical = 3.dp)
                } else {
                    Modifier
                },
            content = content,
        )
    }
}

@Composable
fun SavioRowLine(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 15.dp),
        thickness = 1.dp,
        color = RowLineColor,
    )
}
