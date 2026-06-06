package re.melchior.saviomobile.ui.screen.intervention.installation

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import re.melchior.saviomobile.ui.refonte.SavioRefonteCard
import re.melchior.saviomobile.ui.screen.intervention.attestation.AttestationChip
import re.melchior.saviomobile.ui.theme.SavioPalette
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.size
import re.melchior.saviomobile.ui.theme.SavioRefonte
import re.melchior.saviomobile.ui.theme.SavioUi
import re.melchior.saviomobile.util.GasPipeValidityStatus
import re.melchior.saviomobile.ui.theme.useSavioRefonteUi

private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
private val displayDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.FRANCE)

@Composable
fun InstallationCheckTab(
    interventionId: String,
    viewModel: InstallationCheckViewModel = hiltViewModel(),
) {
    LaunchedEffect(interventionId) {
        viewModel.bindIntervention(interventionId)
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val entity = uiState.entity ?: return
    val refonte = useSavioRefonteUi()

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = if (refonte) 15.dp else 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        InstallationSection(title = "Contrôle embouement") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("Test de turbidité effectué")
                Switch(
                    checked = entity.turbidityTested,
                    onCheckedChange = viewModel::setTurbidityTested,
                )
            }
            if (entity.turbidityTested) {
                OutlinedTextField(
                    value = entity.turbidityNtu,
                    onValueChange = viewModel::setTurbidityNtu,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Turbidité (NTU)") },
                    singleLine = true,
                )
                Text(
                    "État de l'eau",
                    fontWeight = FontWeight.Medium,
                    color = if (refonte) SavioRefonte.Navy else MaterialTheme.colorScheme.onSurface,
                )
                ExclusiveChips(
                    options =
                        listOf(
                            "propre" to "Propre",
                            "embouee" to "Embouée",
                            "tres_embouee" to "Très embouée",
                        ),
                    selected = entity.turbidityState,
                    onSelect = viewModel::setTurbidityState,
                )
            }
        }

        InstallationSection(title = "Tuyauterie gaz (cuisson)") {
            GasPipeTypeDropdown(
                selected = entity.gasPipeType,
                onSelect = viewModel::setGasPipeType,
            )
            GasValidityDateField(
                value = entity.gasPipeValidityDate,
                onDateSelected = viewModel::setGasPipeValidityDate,
            )
            GasPipeValidityBanner(
                status = uiState.validityStatus,
                validityDateIso = entity.gasPipeValidityDate,
                hasAnomalyDraft = uiState.hasGasPipeAnomalyDraft,
                onCreateAnomaly = viewModel::createGasPipeAnomalyManually,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("Tuyau remplacé")
                Switch(
                    checked = entity.gasPipeReplaced,
                    onCheckedChange = viewModel::setGasPipeReplaced,
                )
            }
            Text(
                "Robinet de coupure",
                fontWeight = FontWeight.Medium,
                color = if (refonte) SavioRefonte.Navy else MaterialTheme.colorScheme.onSurface,
            )
            ExclusiveChips(
                options =
                    listOf(
                        "conforme" to "Conforme",
                        "non_conforme" to "Non conforme",
                        "bouchonne" to "Bouchonné",
                    ),
                selected = entity.gasTapCompliant,
                onSelect = viewModel::setGasTapCompliant,
            )
        }

        InstallationSection(title = "Notes") {
            OutlinedTextField(
                value = entity.notes,
                onValueChange = viewModel::setNotes,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Observations (optionnel)") },
                minLines = 3,
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun InstallationSection(
    title: String,
    content: @Composable () -> Unit,
) {
    val refonte = useSavioRefonteUi()
    if (refonte) {
        SavioRefonteCard {
            Column(
                modifier = Modifier.padding(horizontal = 15.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    title,
                    fontWeight = FontWeight.Bold,
                    color = SavioRefonte.Navy,
                )
                content()
            }
        }
    } else {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp,
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                content()
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ExclusiveChips(
    options: List<Pair<String, String>>,
    selected: String?,
    onSelect: (String?) -> Unit,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEach { (value, label) ->
            AttestationChip(
                label = label,
                selected = selected == value,
                selectedColor = SavioRefonte.Navy,
                onClick = { onSelect(if (selected == value) null else value) },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GasPipeTypeDropdown(
    selected: String?,
    onSelect: (String?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val options =
        listOf(
            null to "Non concerné",
            "souple" to "Souple",
            "gazinox" to "Gazinox",
            "cuivre" to "Cuivre",
        )
    val label = options.firstOrNull { it.first == selected }?.second ?: "Non concerné"

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = Modifier.fillMaxWidth(),
    ) {
        OutlinedTextField(
            value = label,
            onValueChange = {},
            readOnly = true,
            label = { Text("Type de tuyau") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier =
                Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            options.forEach { (value, text) ->
                DropdownMenuItem(
                    text = { Text(text) },
                    onClick = {
                        onSelect(value)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun GasPipeValidityBanner(
    status: GasPipeValidityStatus,
    validityDateIso: String,
    hasAnomalyDraft: Boolean,
    onCreateAnomaly: () -> Unit,
) {
    val displayDate =
        validityDateIso.takeIf { it.isNotBlank() }?.let {
            runCatching { LocalDate.parse(it, dateFormatter) }
                .getOrNull()
                ?.format(displayDateFormatter)
        }.orEmpty()

    when {
        hasAnomalyDraft -> {
            InstallationAlertBanner(
                message = "Anomalie tuyau gaz enregistrée.",
                isWarning = false,
            )
        }
        status == GasPipeValidityStatus.Expired -> {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                InstallationAlertBanner(
                    message =
                        if (displayDate.isNotBlank()) {
                            "Tuyau gaz hors validité depuis le $displayDate. " +
                                "Une anomalie de non-conformité est requise."
                        } else {
                            "Tuyau gaz hors validité. Une anomalie de non-conformité est requise."
                        },
                    isWarning = true,
                )
                OutlinedButton(onClick = onCreateAnomaly) {
                    Text("Créer l'anomalie")
                }
            }
        }
        status == GasPipeValidityStatus.ExpiringSoon -> {
            InstallationAlertBanner(
                message =
                    if (displayDate.isNotBlank()) {
                        "Le tuyau gaz expire bientôt (validité : $displayDate)."
                    } else {
                        "Le tuyau gaz expire bientôt."
                    },
                isWarning = true,
            )
        }
    }
}

@Composable
private fun InstallationAlertBanner(
    message: String,
    isWarning: Boolean,
) {
    val refonte = useSavioRefonteUi()
    val backgroundColor =
        when {
            isWarning && refonte -> SavioRefonte.StatusLiveBg
            isWarning -> SavioPalette.WarningTintBg
            refonte -> SavioRefonte.StatusDoneBg
            else -> MaterialTheme.colorScheme.secondaryContainer
        }
    val borderColor =
        when {
            isWarning && refonte -> SavioRefonte.OrangeAction.copy(alpha = 0.55f)
            isWarning -> SavioPalette.Warning.copy(alpha = 0.6f)
            refonte -> SavioRefonte.StatusDoneDot.copy(alpha = 0.45f)
            else -> SavioUi.CardBorder
        }
    val iconTint =
        when {
            isWarning && refonte -> SavioRefonte.StatusLiveFg
            isWarning -> SavioPalette.Warning
            refonte -> SavioRefonte.StatusDoneFg
            else -> MaterialTheme.colorScheme.primary
        }
    val textColor =
        when {
            isWarning && refonte -> SavioRefonte.Ink
            isWarning -> SavioPalette.TextPrimary
            refonte -> SavioRefonte.StatusDoneFg
            else -> MaterialTheme.colorScheme.onSecondaryContainer
        }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        border = BorderStroke(1.dp, borderColor),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                imageVector = if (isWarning) Icons.Filled.Warning else Icons.Filled.Info,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp),
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = textColor,
                lineHeight = 20.sp,
            )
        }
    }
}

@Composable
private fun GasValidityDateField(
    value: String,
    onDateSelected: (String) -> Unit,
) {
    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }
    val display =
        value.takeIf { it.isNotBlank() }?.let {
            runCatching { LocalDate.parse(it, dateFormatter) }
                .getOrNull()
                ?.format(displayDateFormatter)
        }.orEmpty()

    fun openDatePicker() {
        val initial =
            runCatching { LocalDate.parse(value, dateFormatter) }.getOrDefault(LocalDate.now())
        DatePickerDialog(
            context,
            { _, year, month, day ->
                onDateSelected(LocalDate.of(year, month + 1, day).format(dateFormatter))
            },
            initial.year,
            initial.monthValue - 1,
            initial.dayOfMonth,
        ).show()
    }

    OutlinedTextField(
        value = display.ifBlank { "" },
        onValueChange = {},
        readOnly = true,
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = { openDatePicker() },
                ),
        label = { Text("Date de validité") },
        placeholder = { Text("Sélectionner une date") },
        singleLine = true,
        trailingIcon = {
            Row {
                if (value.isNotBlank()) {
                    IconButton(onClick = { onDateSelected("") }) {
                        Icon(
                            imageVector = Icons.Filled.Clear,
                            contentDescription = "Effacer la date",
                        )
                    }
                }
                IconButton(onClick = { openDatePicker() }) {
                    Icon(
                        imageVector = Icons.Filled.CalendarToday,
                        contentDescription = "Choisir une date",
                    )
                }
            }
        },
    )
}
