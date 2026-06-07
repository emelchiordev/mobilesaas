package re.melchior.saviomobile.ui.screen.tournee

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import re.melchior.saviomobile.util.InterventionTimeSlot
import re.melchior.saviomobile.util.MobilePlanningPermission
import re.melchior.saviomobile.util.formatPlanningLabel

@Composable
fun PlanningFormFields(
    timeSlot: InterventionTimeSlot,
    onTimeSlotChange: (InterventionTimeSlot) -> Unit,
    timeText: String,
    onTimeTextChange: (String) -> Unit,
    dateText: String,
    onDateTextChange: (String) -> Unit,
    isUrgent: Boolean,
    onIsUrgentChange: (Boolean) -> Unit,
    permission: MobilePlanningPermission,
    previewScheduledAtIso: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Planning",
            style = MaterialTheme.typography.titleSmall,
        )
        Text(
            text = formatPlanningLabel(timeSlot, previewScheduledAtIso),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            InterventionTimeSlot.entries.forEach { slot ->
                FilterChip(
                    selected = timeSlot == slot,
                    onClick = { onTimeSlotChange(slot) },
                    label = { Text(slot.label) },
                )
            }
        }

        OutlinedTextField(
            value = timeText,
            onValueChange = onTimeTextChange,
            label = { Text("Heure (optionnel)") },
            modifier = Modifier.fillMaxWidth(),
        )

        if (permission == MobilePlanningPermission.FULL_EDIT) {
            OutlinedTextField(
                value = dateText,
                onValueChange = onDateTextChange,
                label = { Text("Date") },
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("Intervention urgente")
            Switch(checked = isUrgent, onCheckedChange = onIsUrgentChange)
        }
    }
}
