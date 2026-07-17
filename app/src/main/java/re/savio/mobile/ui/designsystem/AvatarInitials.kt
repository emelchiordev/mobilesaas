package re.savio.mobile.ui.designsystem

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import re.savio.mobile.ui.theme.SavioPalette
import re.savio.mobile.ui.theme.SavioType

@Composable
fun AvatarInitials(
    initials: String,
    modifier: Modifier = Modifier,
    size: Dp = 36.dp,
) {
    Surface(
        modifier = modifier.size(size),
        shape = CircleShape,
        color = SavioPalette.PrimaryLight,
        shadowElevation = 0.dp,
        tonalElevation = 0.dp,
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.clip(CircleShape)) {
            Text(
                text = initials.uppercase().take(3),
                style = SavioType.BodySmall.copy(fontSize = 12.sp, fontWeight = FontWeight.Medium),
                color = SavioPalette.Primary,
            )
        }
    }
}
