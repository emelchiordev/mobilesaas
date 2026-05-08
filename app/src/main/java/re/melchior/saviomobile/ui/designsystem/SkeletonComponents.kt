package re.melchior.saviomobile.ui.designsystem

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import re.melchior.saviomobile.ui.component.SavioShimmerLine
import re.melchior.saviomobile.ui.component.rememberSavioShimmerBrush
import re.melchior.saviomobile.ui.theme.SavioPalette
import re.melchior.saviomobile.ui.theme.SavioDimens

/** Simule une [SectionCard] avec plusieurs lignes shimmer. */
@Composable
fun SkeletonCard(
    rows: Int = 3,
    modifier: Modifier = Modifier,
) {
    val brush = rememberSavioShimmerBrush()
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(SavioDimens.RadiusLG),
        color = SavioPalette.BackgroundCard,
        border = BorderStroke(SavioDimens.BorderThin, SavioPalette.BorderDefault),
        shadowElevation = 0.dp,
        tonalElevation = 0.dp,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(SavioDimens.SpaceLG),
            verticalArrangement = Arrangement.spacedBy(SavioDimens.SpaceSM),
        ) {
            repeat(rows) {
                SavioShimmerLine(heightDp = 14f, brush = brush)
                SavioShimmerLine(
                    modifier = Modifier.fillMaxWidth(0.55f),
                    heightDp = 12f,
                    brush = brush,
                )
                Spacer(modifier = Modifier.height(SavioDimens.SpaceXS))
            }
        }
    }
}

/** Une ligne de liste type équipement. */
@Composable
fun SkeletonListItem(modifier: Modifier = Modifier) {
    val brush = rememberSavioShimmerBrush()
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(vertical = SavioDimens.SpaceMD),
        verticalArrangement = Arrangement.spacedBy(SavioDimens.SpaceSM),
    ) {
        SavioShimmerLine(heightDp = 14f, brush = brush)
        SavioShimmerLine(
            modifier = Modifier.fillMaxWidth(0.45f),
            heightDp = 12f,
            brush = brush,
        )
    }
}
