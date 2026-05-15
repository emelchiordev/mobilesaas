package re.melchior.saviomobile.ui.screen.tournee

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import re.melchior.saviomobile.ui.theme.SavioPalette

@Composable
fun TourneeSidebar(
    modifier: Modifier,
    interventions: List<InterventionItem>,
    selectedId: String?,
    onSelect: (String) -> Unit,
) {
    LazyColumn(
        modifier
            .fillMaxHeight()
            .background(SavioPalette.SurfaceCard)
            .fillMaxWidth()
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
    }
}
