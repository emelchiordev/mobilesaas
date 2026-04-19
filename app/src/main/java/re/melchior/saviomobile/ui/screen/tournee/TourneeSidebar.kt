package re.melchior.saviomobile.ui.screen.tournee

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import re.melchior.saviomobile.R

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
            .background(colorResource(R.color.card_bg))
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
