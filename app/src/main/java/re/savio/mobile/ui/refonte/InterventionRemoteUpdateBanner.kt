package re.savio.mobile.ui.refonte

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import re.savio.mobile.ui.theme.ViolettTokens

@Composable
fun InterventionRemoteUpdateBanner(
    visible: Boolean,
    onReload: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!visible) return
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = ViolettTokens.Tint,
        shadowElevation = 0.dp,
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "Les données de cette intervention ont été mises à jour",
                modifier = Modifier.weight(1f),
                fontSize = 12.sp,
                color = ViolettTokens.Ink2,
            )
            Text(
                text = "Recharger",
                modifier = Modifier.clickable(onClick = onReload).padding(start = 12.dp),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = ViolettTokens.Accent,
            )
            Text(
                text = "Fermer",
                modifier = Modifier.clickable(onClick = onDismiss).padding(start = 10.dp),
                fontSize = 12.sp,
                color = ViolettTokens.Muted,
            )
        }
    }
}
