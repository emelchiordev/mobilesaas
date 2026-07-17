package re.savio.mobile.ui.screen.intervention.create

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import re.savio.mobile.data.local.entity.InterventionTypeEntity
import re.savio.mobile.ui.screen.client.CustomerSearchField
import re.savio.mobile.ui.screen.client.CustomerSearchResultsSection
import androidx.compose.material3.MaterialTheme
import re.savio.mobile.ui.screen.tournee.PlanningFormFields
import re.savio.mobile.ui.theme.SavioPalette
import re.savio.mobile.ui.theme.savioFieldColors
import re.savio.mobile.ui.theme.savioTopAppBarColors
import re.savio.mobile.util.MobilePlanningPermission

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateInterventionScreen(
    onBack: () -> Unit,
    onCreated: (scheduledAtMillis: Long) -> Unit,
    onNavigateOffline: (defaultDate: String) -> Unit,
    onCreateClient: () -> Unit = {},
    viewModel: CreateInterventionViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collect { ev ->
            when (ev) {
                is CreateInterventionEvent.Created -> onCreated(ev.scheduledAtMillis)
                is CreateInterventionEvent.NavigateOffline -> onNavigateOffline(ev.defaultDate)
            }
        }
    }

    val previewScheduledAtIso = remember(
        uiState.timeSlot,
        uiState.planningTimeText,
        uiState.planningDateText,
    ) {
        scheduledAtToIso(uiState.previewScheduledAtMillis)
    }

    val fieldColors = savioFieldColors()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Nouvelle intervention",
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour",
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
                CustomerSearchField(
                    query = uiState.customerSearchQuery,
                    onQueryChange = viewModel::onCustomerSearchChange,
                )
                CustomerSearchResultsSection(
                    query = uiState.customerSearchQuery,
                    results = uiState.customerSearchResults,
                    isLoading = uiState.customerSearchLoading,
                    error = uiState.customerSearchError,
                    onSelect = viewModel::selectCustomer,
                )
                Spacer(modifier = Modifier.height(4.dp))
                TextButton(onClick = onCreateClient) {
                    Text("Client introuvable ? Créer un client", color = SavioPalette.Accent)
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
            SectionLabel("3. Planning")
            Spacer(modifier = Modifier.height(8.dp))
            if (uiState.planningPermission != MobilePlanningPermission.READ_ONLY) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    ),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    PlanningFormFields(
                        timeSlot = uiState.timeSlot,
                        onTimeSlotChange = viewModel::onTimeSlotChange,
                        timeText = uiState.planningTimeText,
                        onTimeTextChange = viewModel::onPlanningTimeChange,
                        dateText = uiState.planningDateText,
                        onDateTextChange = viewModel::onPlanningDateChange,
                        isUrgent = uiState.isUrgent,
                        onIsUrgentChange = viewModel::onIsUrgentChange,
                        permission = uiState.planningPermission,
                        previewScheduledAtIso = previewScheduledAtIso,
                        allowDateEdit = true,
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }

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
private fun SectionLabel(text: String) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
    )
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
