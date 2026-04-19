package re.melchior.saviomobile.ui.screen.tournee

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import re.melchior.saviomobile.R

@Composable
fun SavioTabletTopBar(
    selectedIntervention: InterventionItem?,
    currentDate: String,
    remainingCount: Int,
    pendingSyncCount: Int,
    onSyncCatalog: () -> Unit,
    isCatalogSyncing: Boolean = false,
    onRefresh: () -> Unit,
    isRefreshing: Boolean,
) {
    Box(
        Modifier
            .fillMaxWidth()
            .background(colorResource(R.color.savio_primary))
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(0.28f)) {
                Text(
                    text = "Ma tournée",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "$remainingCount restantes",
                    color = colorResource(R.color.savio_primary_light).copy(alpha = 0.95f),
                    fontSize = 12.sp
                )
            }
            Text(
                text = currentDate,
                color = colorResource(R.color.savio_primary_light),
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .weight(0.44f)
                    .padding(horizontal = 8.dp),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                modifier = Modifier.weight(0.28f),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (pendingSyncCount > 0) {
                    BadgedBox(
                        badge = {
                            Badge(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError,
                            ) { Text(pendingSyncCount.toString()) }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Sync,
                            contentDescription = "Sync en attente",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(Modifier.width(4.dp))
                }
                IconButton(
                    onClick = onSyncCatalog,
                    enabled = !isCatalogSyncing && !isRefreshing,
                ) {
                    if (isCatalogSyncing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.LibraryBooks,
                            contentDescription = "Synchroniser le catalogue",
                            tint = MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                }
                IconButton(
                    onClick = onRefresh,
                    enabled = !isRefreshing,
                ) {
                    if (isRefreshing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Rafraîchir",
                            tint = MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                }
                if (selectedIntervention != null) {
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = selectedIntervention.typeLabel,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.width(120.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    TabletInterventionStatusBadge(intervention = selectedIntervention)
                }
            }
        }
    }
}

@Composable
private fun TabletInterventionStatusBadge(intervention: InterventionItem) {
    val (bg, fg, label) = when {
        intervention.syncStatus == "CONFLICT" ->
            Triple(
                MaterialTheme.colorScheme.error.copy(alpha = 0.2f),
                MaterialTheme.colorScheme.error,
                "Conflit"
            )
        intervention.syncStatus == "COMPLETED" ->
            Triple(
                colorResource(R.color.badge_inprog_bg),
                colorResource(R.color.badge_inprog_text),
                "En attente"
            )
        intervention.syncStatus == "SYNCED" && intervention.status == "completed" ->
            Triple(
                colorResource(R.color.badge_done_bg),
                colorResource(R.color.badge_done_text),
                "Terminée"
            )
        intervention.status == "pending_validation" ->
            Triple(
                colorResource(R.color.badge_inprog_bg),
                colorResource(R.color.badge_inprog_text),
                "À valider"
            )
        intervention.syncStatus == "IN_PROGRESS" || intervention.status == "in_progress" ->
            Triple(
                colorResource(R.color.badge_inprog_bg),
                colorResource(R.color.badge_inprog_text),
                "En cours"
            )
        intervention.status == "scheduled" ->
            Triple(
                colorResource(R.color.badge_neutral_bg),
                colorResource(R.color.savio_primary),
                "Planifiée"
            )
        else ->
            Triple(
                colorResource(R.color.badge_neutral_bg),
                colorResource(R.color.badge_neutral_text),
                intervention.status
            )
    }
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(fg)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = fg,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
