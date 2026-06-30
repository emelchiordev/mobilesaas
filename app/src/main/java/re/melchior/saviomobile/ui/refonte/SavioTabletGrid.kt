package re.melchior.saviomobile.ui.refonte

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun SavioTabletTwoColumnGrid(
    modifier: Modifier = Modifier,
    spacing: Dp = 14.dp,
    left: @Composable () -> Unit,
    right: @Composable () -> Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(spacing),
    ) {
        Box(modifier = Modifier.weight(1f)) {
            left()
        }
        Box(modifier = Modifier.weight(1f)) {
            right()
        }
    }
}
