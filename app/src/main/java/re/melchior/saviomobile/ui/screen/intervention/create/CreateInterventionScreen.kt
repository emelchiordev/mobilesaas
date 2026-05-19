package re.melchior.saviomobile.ui.screen.intervention.create

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material3.Surface
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import re.melchior.saviomobile.data.local.entity.InterventionTypeEntity
import re.melchior.saviomobile.data.remote.dto.CustomerSearchRowDto
import androidx.compose.material3.MaterialTheme
import re.melchior.saviomobile.ui.theme.SavioPalette
import re.melchior.saviomobile.ui.theme.savioFieldColors
import re.melchior.saviomobile.ui.theme.savioTopAppBarColors
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateInterventionScreen(
    onBack: () -> Unit,
    onCreated: (scheduledAtMillis: Long) -> Unit,
    onNavigateOffline: () -> Unit,
    viewModel: CreateInterventionViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.events.collect { ev ->
            when (ev) {
                is CreateInterventionEvent.Created -> onCreated(ev.scheduledAtMillis)
                CreateInterventionEvent.NavigateOffline -> onNavigateOffline()
            }
        }
    }

    val dateTimeLabel = remember(uiState.scheduledAtMillis) {
        val zdt = Instant.ofEpochMilli(uiState.scheduledAtMillis).atZone(ZoneId.systemDefault())
        zdt.format(
            DateTimeFormatter.ofPattern("EEEE d MMMM yyyy 'à' HH:mm", Locale.FRENCH),
        ).replaceFirstChar { it.uppercase(Locale.FRENCH) }
    }

    fun openDateTimePicker() {
        val z = ZoneId.systemDefault()
        val zdt = Instant.ofEpochMilli(uiState.scheduledAtMillis).atZone(z)
        DatePickerDialog(
            context,
            { _, y, m0, d ->
                TimePickerDialog(
                    context,
                    { _, h, min ->
                        val ld = java.time.LocalDate.of(y, m0 + 1, d)
                        val lt = java.time.LocalTime.of(h, min)
                        viewModel.onScheduledAtChange(ld.atTime(lt).atZone(z).toInstant().toEpochMilli())
                    },
                    zdt.hour,
                    zdt.minute,
                    true,
                ).show()
            },
            zdt.year,
            zdt.monthValue - 1,
            zdt.dayOfMonth,
        ).show()
    }

    val fieldColors = savioFieldColors()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Nouvelle intervention",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour",
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                },
                colors = savioTopAppBarColors(),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            SectionLabel("1. Client")
            Spacer(modifier = Modifier.height(8.dp))

            val selected = uiState.selectedCustomer
            if (selected == null) {
                OutlinedTextField(
                    value = uiState.customerSearchQuery,
                    onValueChange = viewModel::onCustomerSearchChange,
                    label = { Text("Rechercher un client") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = fieldColors,
                    shape = RoundedCornerShape(12.dp),
                )
                if (uiState.customerSearchLoading) {
                    Spacer(modifier = Modifier.height(8.dp))
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = SavioPalette.Accent,
                        strokeWidth = 2.dp,
                    )
                }
                uiState.customerSearchError?.let { err ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(err, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
                uiState.customerSearchResults.forEach { row ->
                    Spacer(modifier = Modifier.height(6.dp))
                    CustomerSearchResultCard(row = row, onClick = { viewModel.selectCustomer(row) })
                }
            } else {
                SelectedCustomerCard(
                    displayName = selected.displayName,
                    addressLine = selected.addressLine,
                    onChange = viewModel::clearSelectedCustomer,
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
            SectionLabel("2. Type d'intervention")
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                uiState.interventionTypes.forEach { type ->
                    InterventionTypeChip(
                        type = type,
                        selected = uiState.selectedTypeCode == type.code,
                        onClick = { viewModel.selectType(type.code) },
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            SectionLabel("3. Date et heure")
            Spacer(modifier = Modifier.height(8.dp))
            ScheduledDateTimeField(
                label = dateTimeLabel,
                onClick = { openDateTimePicker() },
            )

            Spacer(modifier = Modifier.height(20.dp))
            SectionLabel("4. Notes (optionnel)")
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = uiState.notes,
                onValueChange = viewModel::onNotesChange,
                placeholder = { Text("Notes pour cette intervention...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                minLines = 3,
                colors = fieldColors,
                shape = RoundedCornerShape(12.dp),
            )

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = viewModel::create,
                enabled = !uiState.isSubmitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
                shape = RoundedCornerShape(12.dp),
            ) {
                if (uiState.isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = SavioPalette.OnAccent,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text("Créer l'intervention", fontWeight = FontWeight.Bold)
                }
            }
            uiState.submitError?.let { err ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(err, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ScheduledDateTimeField(
    label: String,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, SavioPalette.Accent),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.CalendarToday,
                contentDescription = "Choisir la date et l'heure",
                tint = SavioPalette.Accent,
                modifier = Modifier.size(22.dp),
            )
            Spacer(modifier = Modifier.size(12.dp))
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
    )
}

@Composable
private fun CustomerSearchResultCard(
    row: CustomerSearchRowDto,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = row.customerDisplayName.ifBlank {
                    "${row.customerFirstName} ${row.customerLastName}".trim()
                },
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = row.formattedAddress(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun SelectedCustomerCard(
    displayName: String,
    addressLine: String,
    onChange: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = displayName,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                )
                if (addressLine.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = addressLine,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp,
                    )
                }
            }
            TextButton(onClick = onChange) {
                Text("Changer", color = SavioPalette.Accent)
            }
        }
    }
}

@Composable
private fun InterventionTypeChip(
    type: InterventionTypeEntity,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val chipColor = remember(type.color) {
        try {
            type.color?.let { Color(android.graphics.Color.parseColor(it)) }
        } catch (_: Exception) {
            null
        }
    } ?: SavioPalette.Accent

    val chipModifier = if (selected) {
        Modifier.border(BorderStroke(2.dp, SavioPalette.White), RoundedCornerShape(50))
    } else {
        Modifier
    }

    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                type.label,
                color = SavioPalette.White,
                fontSize = 13.sp,
            )
        },
        modifier = chipModifier,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = chipColor,
            labelColor = SavioPalette.White,
            selectedContainerColor = chipColor,
            selectedLabelColor = SavioPalette.White,
        ),
        border = null,
    )
}
