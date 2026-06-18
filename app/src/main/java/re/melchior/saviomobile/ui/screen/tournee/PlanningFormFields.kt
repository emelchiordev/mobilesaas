package re.melchior.saviomobile.ui.screen.tournee

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import re.melchior.saviomobile.util.InterventionTimeSlot
import re.melchior.saviomobile.util.MobilePlanningPermission
import re.melchior.saviomobile.util.formatPlanningLabel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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
    allowDateEdit: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val showDateField = permission == MobilePlanningPermission.FULL_EDIT || allowDateEdit
    val dateDisplayLabel = remember(dateText) {
        runCatching { LocalDate.parse(dateText) }
            .map { it.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) }
            .getOrDefault(dateText)
    }

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

        if (showDateField) {
            OutlinedTextField(
                value = dateDisplayLabel,
                onValueChange = {},
                readOnly = true,
                label = { Text("Date") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val currentDate = runCatching { LocalDate.parse(dateText) }
                            .getOrDefault(LocalDate.now())
                        DatePickerDialog(
                            context,
                            { _, year, month, day ->
                                val picked = LocalDate.of(year, month + 1, day)
                                onDateTextChange(picked.toString())
                            },
                            currentDate.year,
                            currentDate.monthValue - 1,
                            currentDate.dayOfMonth,
                        ).show()
                    },
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
