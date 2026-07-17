package re.savio.mobile.ui.refonte

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import re.savio.mobile.data.local.entity.InterventionEntity
import re.savio.mobile.ui.screen.tournee.displayTypeLabel
import re.savio.mobile.ui.theme.ViolettTokens
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun SavioActiveInterventionHeader(
    intervention: InterventionEntity,
    onCloseClick: () -> Unit,
    onClotureClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .shadow(8.dp, RoundedCornerShape(bottomStart = 26.dp, bottomEnd = 26.dp))
                .clip(RoundedCornerShape(bottomStart = 26.dp, bottomEnd = 26.dp))
                .background(ViolettTokens.PrimaryGradient),
    ) {
        Column(Modifier.statusBarsPadding()) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(start = 18.dp, end = 14.dp, top = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(9.dp),
                ) {
                    Icon(
                        imageVector = savioTypeIcon(intervention.displayTypeLabel(), intervention.typeCode),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp),
                    )
                    Text(
                        text = intervention.displayTypeLabel(),
                        color = Color.White,
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 2,
                    )
                }
                androidx.compose.material3.Button(
                    onClick = onClotureClick,
                    colors =
                        androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = Color.White.copy(alpha = 0.2f),
                            contentColor = Color.White,
                        ),
                    contentPadding =
                        androidx.compose.foundation.layout.PaddingValues(
                            horizontal = 18.dp,
                            vertical = 9.dp,
                        ),
                    shape = RoundedCornerShape(999.dp),
                    elevation = androidx.compose.material3.ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                ) {
                    Text(text = "Clôturer", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
                IconButton(onClick = onCloseClick, modifier = Modifier.size(40.dp)) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Quitter l'intervention",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
            Text(
                text = activeInterventionHeaderSubtitle(intervention),
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 18.dp),
            )
            if (intervention.syncStatus == "CONFLICT") {
                Surface(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp)
                            .padding(bottom = 12.dp),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.92f),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            Icons.Filled.Warning,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                        )
                        Text(
                            text = "Conflit de synchronisation",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            }
        }
    }
}

internal fun activeInterventionHeaderSubtitle(intervention: InterventionEntity): String {
    val scheduledAt = intervention.scheduledAt
    if (scheduledAt.isNullOrBlank()) return "—"
    return try {
        val zdt = Instant.parse(scheduledAt).atZone(ZoneId.systemDefault())
        val date =
            zdt.format(DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.FRENCH))
                .replaceFirstChar { it.uppercase() }
        val time = zdt.format(DateTimeFormatter.ofPattern("HH:mm", Locale.FRENCH))
        "$date · $time"
    } catch (_: Exception) {
        scheduledAt.take(16)
    }
}
