package re.savio.mobile.ui.refonte

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import re.savio.mobile.ui.theme.ViolettTokens

private val TimerDot = Color(0xFF22BFA0)
private val TimerDotGlow = Color(0x4022BFA0)

@Composable
fun SavioRefonteTimerBlock(
    elapsedLabel: String,
    startTimeLabel: String,
    modifier: Modifier = Modifier,
) {
    val pulseTransition = rememberInfiniteTransition(label = "timer-dot-pulse")
    val glowAlpha by pulseTransition.animateFloat(
        initialValue = 0.18f,
        targetValue = 0.42f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "glow-alpha",
    )
    val glowRadiusFactor by pulseTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.25f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "glow-radius",
    )

    SavioRefonteCard(modifier = modifier, contentPadding = false) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(15.dp),
        ) {
            Box(
                modifier =
                    Modifier
                        .size(16.dp)
                        .clip(CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(11.dp)
                            .drawBehind {
                                drawCircle(
                                    color = TimerDotGlow.copy(alpha = glowAlpha),
                                    radius = size.minDimension * 0.55f * glowRadiusFactor,
                                    center = Offset(size.width / 2f, size.height / 2f),
                                )
                            }
                            .clip(CircleShape)
                            .background(TimerDot),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "En cours · démarrée à $startTimeLabel",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Normal,
                    color = ViolettTokens.Muted,
                )
                Text(
                    text = elapsedLabel,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Normal,
                    color = ViolettTokens.Ink,
                    letterSpacing = 0.01.sp,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
    }
}
