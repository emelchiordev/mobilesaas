package re.savio.mobile.ui.screen.auth

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import re.savio.mobile.ui.theme.SavioDimens
import re.savio.mobile.ui.theme.SavioPalette
import re.savio.mobile.ui.theme.SavioType

@Composable
fun RegisterPendingVerificationContent(
    email: String,
    isResending: Boolean,
    resendFeedback: String?,
    resendError: String?,
    onActivatedAccountClick: () -> Unit,
    onResendClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var entranceDone by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { entranceDone = true }

    val enterScale by animateFloatAsState(
        targetValue = if (entranceDone) 1f else 0.35f,
        animationSpec =
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow,
            ),
        label = "check_enter_scale",
    )

    val infinite = rememberInfiniteTransition(label = "check_pulse")
    val pulseFactor by infinite.animateFloat(
        initialValue = 1f,
        targetValue = 1.065f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(900, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "check_pulse_scale",
    )

    val iconScale = enterScale * pulseFactor

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(SavioPalette.AuthBackground),
    ) {
        TextButton(
            onClick = onBackClick,
            modifier = Modifier.padding(start = 8.dp, top = 8.dp),
        ) {
            Text("Retour", color = SavioPalette.AuthAccent)
        }

        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = SavioDimens.SpaceXL)
                    .padding(bottom = SavioDimens.SpaceXL),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = SavioPalette.AuthAccent,
                modifier =
                    Modifier
                        .graphicsLayer {
                            scaleX = iconScale
                            scaleY = iconScale
                        }
                        .size(112.dp),
            )

            Spacer(modifier = Modifier.height(SavioDimens.SpaceXL))

            Text(
                text = "Bienvenue sur SAVIO 🎉",
                style = MaterialTheme.typography.headlineSmall,
                color = SavioPalette.AuthOnBackground,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(SavioDimens.SpaceMD))

            Text(
                text = "Vérifiez votre boîte mail pour activer votre compte",
                style = SavioType.BodyLarge,
                color = SavioPalette.AuthOnBackground,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(SavioDimens.SpaceLG))

            Text(
                text = email,
                style = SavioType.BodySmall,
                color = SavioPalette.AuthOnMuted,
                textAlign = TextAlign.Center,
            )

            resendFeedback?.let { fb ->
                Spacer(modifier = Modifier.height(SavioDimens.SpaceLG))
                Text(
                    text = fb,
                    style = SavioType.BodySmall,
                    color = SavioPalette.AuthAccent,
                    textAlign = TextAlign.Center,
                )
            }

            resendError?.let { err ->
                Spacer(modifier = Modifier.height(SavioDimens.SpaceMD))
                Text(
                    text = err,
                    style = SavioType.BodySmall,
                    color = SavioPalette.Error,
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(modifier = Modifier.height(SavioDimens.SpaceXL * 2))

            Button(
                onClick = onActivatedAccountClick,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(SavioDimens.AppButtonHeight),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = SavioPalette.AuthAccent,
                        contentColor = SavioPalette.OnAccent,
                    ),
            ) {
                Text(
                    text = "J'ai activé mon compte",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                )
            }

            Spacer(modifier = Modifier.height(SavioDimens.SpaceLG))

            TextButton(
                onClick = onResendClick,
                enabled = !isResending,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (isResending) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = SavioPalette.AuthAccent,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(
                        text = "Renvoyer l'email",
                        color = SavioPalette.AuthOnMuted,
                        style = SavioType.BodySmall,
                    )
                }
            }
        }
    }
}
