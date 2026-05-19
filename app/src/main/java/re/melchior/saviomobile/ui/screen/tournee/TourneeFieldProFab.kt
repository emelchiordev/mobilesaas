package re.melchior.saviomobile.ui.screen.tournee

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import re.melchior.saviomobile.ui.theme.SavioPalette

private val FieldProCardBg = Color(0xFF1C2030)
private val FieldProAccent = Color(0xFFF5A623)
private val FieldProScrim = Color(0xFF000000).copy(alpha = 0.6f)

@Composable
fun TourneeFieldProFab(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    pendingOfflineInterventionCount: Int,
    modifier: Modifier = Modifier,
) {
    BadgedBox(
        modifier = modifier,
        badge = {
            if (pendingOfflineInterventionCount > 0) {
                Badge(
                    containerColor = SavioPalette.Accent,
                    contentColor = SavioPalette.OnAccent,
                ) {
                    Text(pendingOfflineInterventionCount.toString())
                }
            }
        },
    ) {
        FloatingActionButton(
            onClick = { onExpandedChange(!expanded) },
            containerColor = Color(0xFFF5A623),
            contentColor = Color(0xFF000000),
        ) {
            Crossfade(expanded, label = "fabAddClose") { open ->
                Icon(
                    imageVector = if (open) Icons.Filled.Close else Icons.Filled.Add,
                    contentDescription = if (open) "Fermer le menu" else "Actions",
                    modifier = Modifier.size(if (open) 22.dp else 24.dp),
                )
            }
        }
    }
}

@Composable
fun TourneeFieldProFabMenuOverlay(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onCreateClient: () -> Unit,
    onNewIntervention: () -> Unit,
    modifier: Modifier = Modifier,
    /** Décalage au-dessus du bas de la zone contenu (Scaffold applique déjà l’inset FAB). */
    menuBottomPadding: Dp = 16.dp,
) {
    AnimatedVisibility(
        visible = expanded,
        enter = fadeIn(animationSpec = tween(180)),
        exit = fadeOut(animationSpec = tween(120)),
        modifier = modifier.fillMaxSize(),
    ) {
        Box(Modifier.fillMaxSize()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(FieldProScrim)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = onDismiss,
                    ),
            )
            Column(
                modifier =
                    Modifier
                        .align(Alignment.BottomEnd)
                        .navigationBarsPadding()
                        .padding(end = 20.dp, bottom = menuBottomPadding)
                        .widthIn(min = 268.dp, max = 360.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.End,
            ) {
                AnimatedVisibility(
                    visible = expanded,
                    enter =
                        slideInVertically(
                            animationSpec = tween(320, easing = FastOutSlowInEasing),
                            initialOffsetY = { it },
                        ) + fadeIn(tween(280, delayMillis = 0)),
                    exit =
                        slideOutVertically(
                            animationSpec = tween(200, easing = FastOutSlowInEasing),
                            targetOffsetY = { it / 2 },
                        ) + fadeOut(tween(160)),
                ) {
                    FieldProFabMenuCard(
                        label = "Nouveau client",
                        icon = Icons.Filled.PersonAdd,
                        onClick = {
                            onDismiss()
                            onCreateClient()
                        },
                    )
                }
                AnimatedVisibility(
                    visible = expanded,
                    enter =
                        slideInVertically(
                            animationSpec = tween(320, easing = FastOutSlowInEasing),
                            initialOffsetY = { it },
                        ) + fadeIn(tween(280, delayMillis = 100)),
                    exit =
                        slideOutVertically(
                            animationSpec = tween(200, delayMillis = 40, easing = FastOutSlowInEasing),
                            targetOffsetY = { it / 2 },
                        ) + fadeOut(tween(160, delayMillis = 40)),
                ) {
                    FieldProFabMenuCard(
                        label = "Nouvelle intervention",
                        icon = Icons.Filled.AddCircle,
                        onClick = {
                            onDismiss()
                            onNewIntervention()
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun FieldProFabMenuCard(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = FieldProCardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier =
                    Modifier
                        .width(4.dp)
                        .fillMaxHeight()
                        .background(FieldProAccent),
            )
            Row(
                modifier =
                    Modifier
                        .weight(1f)
                        .padding(horizontal = 18.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = FieldProAccent,
                    modifier = Modifier.size(26.dp),
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}
