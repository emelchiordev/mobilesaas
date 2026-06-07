package re.melchior.saviomobile.ui.screen.tournee



import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column

import androidx.compose.foundation.layout.fillMaxWidth

import androidx.compose.foundation.layout.padding

import androidx.compose.material3.Button

import androidx.compose.material3.Card

import androidx.compose.material3.CardDefaults

import androidx.compose.material3.MaterialTheme

import androidx.compose.material3.Text

import androidx.compose.runtime.Composable

import androidx.compose.runtime.getValue

import androidx.compose.runtime.mutableStateOf

import androidx.compose.runtime.remember

import androidx.compose.runtime.setValue

import androidx.compose.ui.Modifier

import androidx.compose.ui.unit.dp

import re.melchior.saviomobile.data.local.entity.InterventionEntity

import re.melchior.saviomobile.ui.screen.intervention.create.buildPlanningScheduledAtMillis

import re.melchior.saviomobile.ui.screen.intervention.create.scheduledAtToIso

import re.melchior.saviomobile.util.MobilePlanningPermission

import re.melchior.saviomobile.util.parseInterventionTimeSlot

import java.time.Instant

import java.time.LocalDate

import java.time.ZoneId



@Composable

fun PlanningEditSection(

    intervention: InterventionEntity,

    permission: MobilePlanningPermission,

    isSaving: Boolean,

    onSave: (scheduledAt: String, timeSlot: String, isUrgent: Boolean) -> Unit,

) {

    if (permission == MobilePlanningPermission.READ_ONLY) return

    if (intervention.status !in listOf("scheduled", "in_progress")) return



    var timeSlot by remember(intervention.id) {

        mutableStateOf(parseInterventionTimeSlot(intervention.timeSlot))

    }

    var isUrgent by remember(intervention.id) { mutableStateOf(intervention.isUrgent) }

    var timeText by remember(intervention.id) {

        mutableStateOf(

            runCatching {

                val instant = Instant.parse(intervention.scheduledAt)

                instant.atZone(ZoneId.systemDefault()).toLocalTime().toString().take(5)

            }.getOrDefault("09:00"),

        )

    }

    var dateText by remember(intervention.id) {

        mutableStateOf(

            runCatching {

                val instant = Instant.parse(intervention.scheduledAt)

                instant.atZone(ZoneId.systemDefault()).toLocalDate().toString()

            }.getOrDefault(LocalDate.now().toString()),

        )

    }



    val previewScheduledAtIso = remember(timeSlot, timeText, dateText) {

        val date = runCatching { LocalDate.parse(dateText) }.getOrDefault(LocalDate.now())

        scheduledAtToIso(buildPlanningScheduledAtMillis(date, timeSlot, timeText))

    }



    Card(

        modifier = Modifier.fillMaxWidth(),

        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),

    ) {

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {

            PlanningFormFields(

                timeSlot = timeSlot,

                onTimeSlotChange = { timeSlot = it },

                timeText = timeText,

                onTimeTextChange = { timeText = it },

                dateText = dateText,

                onDateTextChange = { dateText = it },

                isUrgent = isUrgent,

                onIsUrgentChange = { isUrgent = it },

                permission = permission,

                previewScheduledAtIso = previewScheduledAtIso,

            )



            Button(

                onClick = {

                    val date = runCatching { LocalDate.parse(dateText) }.getOrDefault(LocalDate.now())

                    val millis = buildPlanningScheduledAtMillis(date, timeSlot, timeText)

                    onSave(

                        scheduledAtToIso(millis),

                        timeSlot.wire,

                        isUrgent,

                    )

                },

                enabled = !isSaving,

                modifier = Modifier.fillMaxWidth(),

            ) {

                Text(if (isSaving) "Enregistrement…" else "Enregistrer le planning")

            }

        }

    }

}

