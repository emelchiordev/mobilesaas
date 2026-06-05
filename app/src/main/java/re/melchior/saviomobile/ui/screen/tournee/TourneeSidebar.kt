package re.melchior.saviomobile.ui.screen.tournee

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.material3.MaterialTheme
import re.melchior.saviomobile.data.local.entity.PendingInterventionEntity

@Composable
fun TourneeSidebar(
    modifier: Modifier,
    interventions: List<InterventionItem>,
    pendingCreating: List<PendingInterventionEntity> = emptyList(),
    selectedId: String?,
    onSelect: (String) -> Unit,
    onPendingCreatingClick: () -> Unit = {},
) {
    LazyColumn(
        modifier
            .fillMaxHeight()
            .fillMaxWidth(),
    ) {
        items(
            items = interventions,
            key = { it.id }
        ) { intervention ->
            TourneeCardTablet(
                intervention = intervention,
                isSelected = intervention.id == selectedId,
                onClick = { onSelect(intervention.id) }
            )
        }
        items(
            items = pendingCreating,
            key = { "pending-${it.localId}" },
        ) { pending ->
            PendingCreatingInterventionCard(
                pending = pending,
                onClick = onPendingCreatingClick,
            )
        }
    }
}
