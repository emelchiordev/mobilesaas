package re.savio.mobile.ui.screen.tournee

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import re.savio.mobile.ui.theme.SavioInterventionColors
import re.savio.mobile.util.InterventionTimeSlot
import re.savio.mobile.util.compareInterventionsForPlanning

enum class PlanningSidebarSection(val title: String) {
    A_REVOIR("À revoir"),
    URGENT("Urgent"),
    MATIN("Matin"),
    APRES_MIDI("Après-midi"),
    JOURNEE("Journée"),
}

fun groupInterventionsForPlanningSidebar(
    items: List<InterventionItem>,
    followUpItems: List<InterventionItem> = emptyList(),
): List<Pair<PlanningSidebarSection, List<InterventionItem>>> {
    fun sorted(list: List<InterventionItem>): List<InterventionItem> =
        list.sortedWith { a, b ->
            compareInterventionsForPlanning(a.toPlanningSortKey(), b.toPlanningSortKey())
        }

    val dayIds = items.map { it.id }.toSet()
    val followUpOnly = sorted(
        followUpItems.filter { it.followUpPending && it.id !in dayIds },
    )

    return buildList {
        if (followUpOnly.isNotEmpty()) add(PlanningSidebarSection.A_REVOIR to followUpOnly)

        val urgent = sorted(items.filter { it.isUrgent })
        if (urgent.isNotEmpty()) add(PlanningSidebarSection.URGENT to urgent)

        val matin = sorted(items.filter { !it.isUrgent && it.timeSlot == InterventionTimeSlot.MATIN })
        if (matin.isNotEmpty()) add(PlanningSidebarSection.MATIN to matin)

        val apresMidi =
            sorted(items.filter { !it.isUrgent && it.timeSlot == InterventionTimeSlot.APRES_MIDI })
        if (apresMidi.isNotEmpty()) add(PlanningSidebarSection.APRES_MIDI to apresMidi)

        val journee =
            sorted(items.filter { !it.isUrgent && it.timeSlot == InterventionTimeSlot.JOURNEE })
        if (journee.isNotEmpty()) add(PlanningSidebarSection.JOURNEE to journee)
    }
}

@Composable
fun PlanningSectionHeader(
    section: PlanningSidebarSection,
    count: Int,
    modifier: Modifier = Modifier,
) {
    val (background, foreground) = sectionColors(section)
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .background(background)
                .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Text(
            text = "${section.title} · $count",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = foreground,
        )
    }
}

@Composable
private fun sectionColors(section: PlanningSidebarSection): Pair<Color, Color> =
    when (section) {
        PlanningSidebarSection.A_REVOIR ->
            Color(0xFFFFEDD5) to Color(0xFF9A3412)
        PlanningSidebarSection.URGENT ->
            MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
        PlanningSidebarSection.MATIN ->
            SavioInterventionColors.PlanifBg to SavioInterventionColors.PlanifFg
        PlanningSidebarSection.APRES_MIDI ->
            SavioInterventionColors.EnCoursBg to SavioInterventionColors.EnCoursFg
        PlanningSidebarSection.JOURNEE ->
            SavioInterventionColors.TermineeBg to SavioInterventionColors.TermineeFg
    }
