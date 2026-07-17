package re.savio.mobile.ui.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import re.savio.mobile.ui.theme.SavioPalette

@Composable
fun rememberSavioShimmerBrush(): Brush {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translate by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1200f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(1200, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
        label = "shimmerTranslate",
    )
    return Brush.linearGradient(
        colors =
            listOf(
                SavioPalette.SkeletonBase,
                SavioPalette.SkeletonHighlight,
                SavioPalette.SkeletonBase,
            ),
        start = Offset(translate - 350f, 0f),
        end = Offset(translate, 180f),
    )
}

@Composable
fun SavioShimmerBox(
    modifier: Modifier,
    shape: Shape = RoundedCornerShape(14.dp),
    brush: Brush = rememberSavioShimmerBrush(),
) {
    Box(
        modifier =
            modifier
                .clip(shape)
                .background(brush),
    )
}

@Composable
fun SavioShimmerLine(
    modifier: Modifier = Modifier,
    heightDp: Float = 12f,
    shape: Shape = RoundedCornerShape(6.dp),
    brush: Brush = rememberSavioShimmerBrush(),
) {
    SavioShimmerBox(
        modifier =
            modifier
                .fillMaxWidth()
                .height(heightDp.dp),
        shape = shape,
        brush = brush,
    )
}

@Composable
fun SavioTourneeListSkeleton(modifier: Modifier = Modifier) {
    val brush = rememberSavioShimmerBrush()
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        repeat(4) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
            ) {
                SavioShimmerBox(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(96.dp),
                    shape = RoundedCornerShape(14.dp),
                    brush = brush,
                )
                Spacer(modifier = Modifier.height(10.dp))
                SavioShimmerLine(heightDp = 14f, brush = brush)
                Spacer(modifier = Modifier.height(8.dp))
                SavioShimmerLine(
                    modifier = Modifier.fillMaxWidth(0.55f),
                    heightDp = 12f,
                    brush = brush,
                )
                Spacer(modifier = Modifier.height(6.dp))
                SavioShimmerLine(
                    modifier = Modifier.fillMaxWidth(0.4f),
                    heightDp = 12f,
                    brush = brush,
                )
            }
        }
    }
}

@Composable
fun SavioInterventionDetailSkeleton(modifier: Modifier = Modifier) {
    val brush = rememberSavioShimmerBrush()
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(16.dp),
    ) {
        SavioShimmerBox(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(140.dp),
            shape = RoundedCornerShape(14.dp),
            brush = brush,
        )
        Spacer(modifier = Modifier.height(16.dp))
        SavioShimmerLine(brush = brush)
        Spacer(modifier = Modifier.height(8.dp))
        SavioShimmerLine(modifier = Modifier.fillMaxWidth(0.7f), brush = brush)
        Spacer(modifier = Modifier.height(8.dp))
        SavioShimmerLine(modifier = Modifier.fillMaxWidth(0.5f), brush = brush)
        Spacer(modifier = Modifier.height(20.dp))
        SavioShimmerBox(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(88.dp),
            shape = RoundedCornerShape(14.dp),
            brush = brush,
        )
    }
}

@Composable
fun SavioPhotosTabSkeleton(modifier: Modifier = Modifier) {
    val brush = rememberSavioShimmerBrush()
    Column(
        modifier = modifier.fillMaxWidth().padding(24.dp),
    ) {
        SavioShimmerLine(heightDp = 16f, brush = brush)
        Spacer(modifier = Modifier.height(10.dp))
        SavioShimmerLine(modifier = Modifier.fillMaxWidth(0.6f), heightDp = 12f, brush = brush)
        Spacer(modifier = Modifier.height(10.dp))
        SavioShimmerLine(modifier = Modifier.fillMaxWidth(0.45f), heightDp = 12f, brush = brush)
        Spacer(modifier = Modifier.height(24.dp))
        SavioShimmerBox(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(120.dp),
            shape = RoundedCornerShape(14.dp),
            brush = brush,
        )
    }
}

@Composable
fun SavioClientDetailSkeleton(modifier: Modifier = Modifier) {
    val brush = rememberSavioShimmerBrush()
    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SavioShimmerBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            shape = RoundedCornerShape(14.dp),
            brush = brush,
        )
        SavioShimmerBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(14.dp),
            brush = brush,
        )
        SavioShimmerBox(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(14.dp),
            brush = brush,
        )
    }
}
