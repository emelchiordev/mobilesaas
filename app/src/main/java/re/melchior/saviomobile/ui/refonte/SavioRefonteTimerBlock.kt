package re.melchior.saviomobile.ui.refonte

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import re.melchior.saviomobile.ui.theme.SavioDimens
import re.melchior.saviomobile.ui.theme.SavioRefonte

private val TimerBorder = Color(0xFFF3E2C2)
private val TimerLabel = Color(0xFFB5710A)
private val TimerDotGlow = Color(0x38EC971F)

@Composable
fun SavioRefonteTimerBlock(
    elapsedLabel: String,
    startTimeLabel: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(SavioDimens.RadiusCard),
        color = SavioRefonte.StatusLiveBg,
        border = BorderStroke(1.dp, TimerBorder),
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier =
                    Modifier
                        .size(11.dp)
                        .drawBehind {
                            drawCircle(
                                color = TimerDotGlow,
                                radius = size.minDimension * 1.9f,
                                center = Offset(size.width / 2f, size.height / 2f),
                            )
                        }
                        .clip(CircleShape)
                        .background(SavioRefonte.OrangeAction),
            )
            Column(modifier = Modifier.weight(1f).padding(start = 14.dp)) {
                Text(
                    text = "En cours · démarrée à $startTimeLabel",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TimerLabel,
                )
                Text(
                    text = elapsedLabel,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = SavioRefonte.Ink,
                    letterSpacing = 0.01.sp,
                    modifier = Modifier.padding(top = 1.dp),
                )
            }
        }
    }
}
