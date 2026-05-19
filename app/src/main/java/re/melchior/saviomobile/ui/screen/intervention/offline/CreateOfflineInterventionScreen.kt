package re.melchior.saviomobile.ui.screen.intervention.offline

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import re.melchior.saviomobile.ui.component.SavioSnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarDuration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import androidx.compose.material3.MaterialTheme
import re.melchior.saviomobile.ui.theme.SavioPalette
import re.melchior.saviomobile.ui.theme.savioFieldColors
import re.melchior.saviomobile.ui.theme.savioTopAppBarColors

private data class InterventionTypeOption(val label: String, val code: String)

private val typeOptions = listOf(
    InterventionTypeOption("Entretien", "01"),
    InterventionTypeOption("Dépannage", "02"),
    InterventionTypeOption("Installation", "03"),
    InterventionTypeOption("Mise en service", "04"),
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateOfflineInterventionScreen(
    onBack: () -> Unit,
    viewModel: CreateOfflineInterventionViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var clientName by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var zip by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var scheduledAtMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var selectedType by remember { mutableStateOf(typeOptions[0]) }

    val dateTimeLabel = remember(scheduledAtMillis) {
        val z = ZoneId.systemDefault()
        val zdt = Instant.ofEpochMilli(scheduledAtMillis).atZone(z)
        zdt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { ev ->
            when (ev) {
                CreateOfflineInterventionEvent.Saved -> {
                    snackbarHostState.showSnackbar(
                        message = "Intervention sauvegardée ☁️\nSync automatique dès que réseau disponible",
                        duration = SnackbarDuration.Long,
                    )
                    onBack()
                }
            }
        }
    }

    fun openDateTimePicker() {
        val z = ZoneId.systemDefault()
        val zdt = Instant.ofEpochMilli(scheduledAtMillis).atZone(z)
        DatePickerDialog(
            context,
            { _, y, m0, d ->
                TimePickerDialog(
                    context,
                    { _, h, min ->
                        val ld = java.time.LocalDate.of(y, m0 + 1, d)
                        val lt = java.time.LocalTime.of(h, min)
                        scheduledAtMillis = ld.atTime(lt).atZone(z).toInstant().toEpochMilli()
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
        snackbarHost = { SavioSnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Intervention terrain",
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
            Surface(
                color = SavioPalette.Accent,
                shape = RoundedCornerShape(10.dp),
            ) {
                Text(
                    text = "Mode hors ligne — sera synchronisé dès que le réseau sera disponible ☁️",
                    color = SavioPalette.Black,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = clientName,
                onValueChange = { clientName = it },
                label = { Text("Nom du client *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = fieldColors,
                shape = RoundedCornerShape(12.dp),
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Adresse") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = fieldColors,
                shape = RoundedCornerShape(12.dp),
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = city,
                onValueChange = { city = it },
                label = { Text("Ville") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = fieldColors,
                shape = RoundedCornerShape(12.dp),
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = zip,
                onValueChange = { zip = it },
                label = { Text("Code postal") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = fieldColors,
                shape = RoundedCornerShape(12.dp),
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Téléphone") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = fieldColors,
                shape = RoundedCornerShape(12.dp),
            )
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                "Type d'intervention",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
            )
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                typeOptions.forEach { opt ->
                    FilterChip(
                        selected = selectedType == opt,
                        onClick = { selectedType = opt },
                        label = { Text(opt.label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SavioPalette.Accent.copy(alpha = 0.35f),
                            selectedLabelColor = SavioPalette.Accent,
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = MaterialTheme.colorScheme.onSurface,
                        ),
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = dateTimeLabel,
                onValueChange = {},
                readOnly = true,
                label = { Text("Date et heure") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { openDateTimePicker() },
                colors = fieldColors,
                shape = RoundedCornerShape(12.dp),
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Appuyez pour modifier la date et l'heure",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes") },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                minLines = 3,
                colors = fieldColors,
                shape = RoundedCornerShape(12.dp),
            )

            Spacer(modifier = Modifier.height(20.dp))
            val canSave = clientName.isNotBlank()
            Button(
                onClick = {
                    viewModel.save(
                        clientNameFree = clientName,
                        addressFree = address,
                        city = city,
                        zipCode = zip,
                        phone = phone,
                        interventionTypeCode = selectedType.code,
                        scheduledAtMillis = scheduledAtMillis,
                        notes = notes,
                    )
                },
                enabled = canSave,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text("Sauvegarder", fontWeight = FontWeight.SemiBold)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
