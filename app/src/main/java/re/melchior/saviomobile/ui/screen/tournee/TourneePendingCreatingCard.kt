package re.melchior.saviomobile.ui.screen.tournee

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import re.melchior.saviomobile.data.local.entity.PendingInterventionEntity
import re.melchior.saviomobile.ui.theme.SavioPalette
import re.melchior.saviomobile.ui.theme.SavioUi
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun PendingCreatingInterventionCard(
    pending: PendingInterventionEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isError = pending.syncStatus == "ERROR"
    val timeLabel = rememberScheduledTimeLabel(pending.scheduledAt)
    val typeLabel = pending.interventionType.takeIf { it.isNotBlank() }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .alpha(0.85f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(0.5.dp, SavioUi.CardBorder),
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = timeLabel,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = SavioUi.BusinessAccent,
                    )
                    typeLabel?.let { label ->
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(SavioUi.ChipBackground)
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                color = SavioUi.BusinessAccent,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                }
                PendingCreatingStatusBadge(isError = isError)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = pending.clientNameFree,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = pending.addressFree,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (!pending.zipCode.isNullOrBlank() || !pending.city.isNullOrBlank()) {
                Text(
                    text = listOfNotNull(pending.zipCode, pending.city)
                        .joinToString(" ")
                        .trim(),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun PendingCreatingStatusBadge(isError: Boolean) {
    val bg: androidx.compose.ui.graphics.Color
    val fg: androidx.compose.ui.graphics.Color
    val label: String
    val showSpinner: Boolean
    if (isError) {
        bg = MaterialTheme.colorScheme.errorContainer
        fg = MaterialTheme.colorScheme.onErrorContainer
        label = "Échec de création — Contactez votre responsable"
        showSpinner = false
    } else {
        bg = SavioUi.StatusTintBg
        fg = SavioPalette.Accent
        label = "En cours de création"
        showSpinner = true
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            if (showSpinner) {
                CircularProgressIndicator(
                    modifier = Modifier.size(10.dp),
                    strokeWidth = 1.5.dp,
                    color = fg,
                )
                Text(
                    text = label,
                    fontSize = 11.sp,
                    color = fg,
                    fontWeight = FontWeight.Medium,
                )
                AnimatedEllipsis(color = fg)
            } else {
                Text(
                    text = label,
                    fontSize = 10.sp,
                    color = fg,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun AnimatedEllipsis(color: androidx.compose.ui.graphics.Color) {
    val transition = rememberInfiniteTransition(label = "creating_dots")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "creating_dots_phase",
    )
    val dots = ".".repeat(phase.toInt().coerceIn(0, 3))
    Text(
        text = dots,
        fontSize = 11.sp,
        color = color,
        fontWeight = FontWeight.Medium,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PendingCreationInfoSheet(
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
        ) {
            Text(
                text = "Intervention en cours de création",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Cette intervention sera disponible dès que la connexion sera rétablie. Aucune action requise de votre part.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 15.sp,
                lineHeight = 22.sp,
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SavioPalette.Accent,
                    contentColor = SavioPalette.OnAccent,
                ),
            ) {
                Text("Fermer", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun rememberScheduledTimeLabel(scheduledAtMillis: Long): String {
    val formatter = androidx.compose.runtime.remember {
        DateTimeFormatter.ofPattern("HH:mm")
    }
    return androidx.compose.runtime.remember(scheduledAtMillis) {
        Instant.ofEpochMilli(scheduledAtMillis)
            .atZone(ZoneId.systemDefault())
            .format(formatter)
    }
}
