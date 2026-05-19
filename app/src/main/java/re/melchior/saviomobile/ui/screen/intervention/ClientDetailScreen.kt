package re.melchior.saviomobile.ui.screen.intervention

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import re.melchior.saviomobile.ui.component.SavioClientDetailSkeleton
import re.melchior.saviomobile.ui.component.SavioSnackbarHost
import re.melchior.saviomobile.ui.theme.SavioPalette
import re.melchior.saviomobile.ui.theme.SavioUi
import re.melchior.saviomobile.ui.theme.savioTopAppBarColors
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientDetailScreen(
    onBack: () -> Unit,
    viewModel: ClientDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val uriHandler = LocalUriHandler.current

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { msg ->
            val result =
                snackbarHostState.showSnackbar(
                    message = msg,
                    actionLabel = "Réessayer",
                    duration = SnackbarDuration.Short,
                )
            viewModel.dismissError()
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.saveChanges()
            }
        }
    }

    LaunchedEffect(uiState.savedSuccess) {
        if (uiState.savedSuccess) {
            val message = if (uiState.updatesRequireValidation) {
                "Modifications enregistrées — en attente de validation"
            } else {
                "Modifications enregistrées"
            }
            snackbarHostState.showSnackbar(message)
            viewModel.dismissSuccess()
        }
    }

    val titleName = uiState.intervention?.let { inv ->
        "${inv.customerFirstName.orEmpty()} ${inv.customerLastName.orEmpty()}".trim()
    }.orEmpty().ifBlank { "Fiche client" }

    Scaffold(
        containerColor = SavioUi.PageBackground,
        snackbarHost = { SavioSnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                colors = savioTopAppBarColors(),
                title = {
                    Column {
                        Text(
                            text = titleName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        if (uiState.updatesRequireValidation) {
                            Text(
                                text = "Modifications soumises à validation",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = "Retour",
                            tint = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                },
                actions = {
                    if (!uiState.isEditing) {
                        IconButton(onClick = viewModel::startEditing) {
                            Icon(
                                Icons.Filled.Edit,
                                contentDescription = "Modifier",
                                tint = MaterialTheme.colorScheme.onBackground,
                            )
                        }
                    }
                },
            )
        },
    ) { padding ->
        when {
            uiState.intervention == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .background(SavioUi.PageBackground),
                    contentAlignment = Alignment.Center,
                ) {
                    SavioClientDetailSkeleton()
                }
            }
            else -> {
                val intervention = uiState.intervention!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .background(SavioUi.PageBackground)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (uiState.updatesRequireValidation && uiState.isEditing) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = SavioPalette.WarningTintBg,
                            border = BorderStroke(0.5.dp, SavioUi.CardBorder),
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Icon(
                                    Icons.Filled.Info,
                                    contentDescription = null,
                                    tint = SavioUi.BusinessAccent,
                                    modifier = Modifier.size(20.dp),
                                )
                                Text(
                                    text = "Vos modifications seront soumises à validation avant d'être appliquées.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }

                    ClientSectionCard {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center,
                        ) {
                            ClientInitialsAvatar(
                                firstName = intervention.customerFirstName,
                                lastName = intervention.customerLastName,
                                sizeDp = 44.dp,
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        ClientSectionTitle(
                            icon = Icons.Filled.Person,
                            title = "Identité",
                        )
                        ClientThinDivider()
                        ClientDataRow(
                            label = "Prénom",
                            rawValue = intervention.customerFirstName,
                        )
                        ClientThinDivider()
                        ClientDataRow(
                            label = "Nom",
                            rawValue = intervention.customerLastName,
                        )
                    }

                    ClientSectionCard {
                        ClientSectionTitle(
                            icon = Icons.Filled.Phone,
                            title = "Coordonnées",
                        )
                        ClientThinDivider()
                        if (uiState.isEditing) {
                            OutlinedTextField(
                                value = uiState.phone,
                                onValueChange = viewModel::onPhoneChange,
                                label = { Text("Téléphone") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = uiState.email,
                                onValueChange = viewModel::onEmailChange,
                                label = { Text("Email") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                isError = uiState.emailError != null,
                                supportingText = {
                                    uiState.emailError?.let { err ->
                                        Text(
                                            text = err,
                                            color = SavioUi.DestructiveRed,
                                            fontSize = 12.sp,
                                        )
                                    }
                                },
                                colors =
                                    OutlinedTextFieldDefaults.colors(
                                        errorBorderColor = SavioUi.DestructiveRed,
                                        errorLabelColor = SavioUi.DestructiveRed,
                                        errorSupportingTextColor = SavioUi.DestructiveRed,
                                    ),
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = uiState.notes,
                                onValueChange = viewModel::onNotesChange,
                                label = { Text("Notes") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp),
                                maxLines = 4,
                            )
                        } else {
                            ClientDataRow(
                                label = "Téléphone",
                                rawValue = uiState.phone,
                                linkColor = MaterialTheme.colorScheme.primary,
                                onValueClick = uiState.phone.takeIf { it.isNotBlank() }?.let { phone ->
                                    { uriHandler.openUri("tel:${phone.trim()}") }
                                },
                            )
                            ClientThinDivider()
                            ClientDataRow(
                                label = "Email",
                                rawValue = uiState.email,
                                linkColor = MaterialTheme.colorScheme.primary,
                                onValueClick = uiState.email.takeIf { it.isNotBlank() }?.let { email ->
                                    { uriHandler.openUri("mailto:${email.trim()}") }
                                },
                            )
                            ClientThinDivider()
                            ClientDataRow(
                                label = "Notes",
                                rawValue = uiState.notes,
                                multiline = true,
                            )
                        }
                    }

                    ClientSectionCard {
                        ClientSectionTitle(
                            icon = Icons.Filled.LocationOn,
                            title = "Accès logement",
                        )
                        ClientThinDivider()
                        val addressLine =
                            "${intervention.unitStreet}, ${intervention.unitPostalCode} ${intervention.unitCity}"
                        ClientDataRow(
                            label = "Adresse",
                            rawValue = addressLine,
                            forceValue = true,
                        )
                        if (uiState.isEditing) {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = uiState.addressLine2,
                                onValueChange = viewModel::onAddressLine2Change,
                                label = { Text("Complément d'adresse (bât, résidence...)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = uiState.floor,
                                    onValueChange = viewModel::onFloorChange,
                                    label = { Text("Étage") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                )
                                OutlinedTextField(
                                    value = uiState.doorCode,
                                    onValueChange = viewModel::onDoorCodeChange,
                                    label = { Text("Code accès") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                )
                            }
                        } else {
                            if (uiState.addressLine2.isNotBlank()) {
                                ClientThinDivider()
                                ClientDataRow(
                                    label = "Complément",
                                    rawValue = uiState.addressLine2,
                                    forceValue = true,
                                )
                            }
                            ClientThinDivider()
                            ClientDataRow(
                                label = "Étage",
                                rawValue = uiState.floor,
                            )
                            ClientThinDivider()
                            ClientDataRow(
                                label = "Code accès",
                                rawValue = uiState.doorCode,
                            )
                        }
                    }

                    if (uiState.isEditing) {
                        Button(
                            onClick = viewModel::saveChanges,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            enabled = !uiState.isSaving,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                            ),
                            shape = RoundedCornerShape(20.dp),
                        ) {
                            if (uiState.isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = SavioPalette.OnAccent,
                                    strokeWidth = 2.dp,
                                )
                            } else {
                                Text(
                                    text = if (uiState.updatesRequireValidation) {
                                        "Soumettre les modifications"
                                    } else {
                                        "Enregistrer"
                                    },
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = viewModel::cancelEditing,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.onSurface,
                            ),
                            border = BorderStroke(1.dp, SavioPalette.Accent),
                        ) {
                            Text("Annuler", fontWeight = FontWeight.Medium)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun ClientSectionCard(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(0.5.dp, SavioUi.CardBorder),
        shadowElevation = 0.dp,
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            content()
        }
    }
}

@Composable
private fun ClientSectionTitle(
    icon: ImageVector,
    title: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = SavioUi.BusinessAccent,
            modifier = Modifier.size(20.dp),
        )
        Text(
            text = title,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun ClientThinDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(vertical = 14.dp),
        thickness = 0.5.dp,
        color = SavioUi.CardBorder,
    )
}

@Composable
private fun ClientDataRow(
    label: String,
    rawValue: String?,
    forceValue: Boolean = false,
    multiline: Boolean = false,
    linkColor: Color? = null,
    onValueClick: (() -> Unit)? = null,
) {
    val trimmed = rawValue?.trim().orEmpty()
    val isEmpty = trimmed.isBlank() && !forceValue
    val display = if (isEmpty) "Non renseigné" else trimmed
    val isPlaceholder = isEmpty

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = if (multiline) Alignment.Top else Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Start,
        )
        val valueModifier = Modifier
            .weight(1f)
            .then(
                if (onValueClick != null && !isPlaceholder) {
                    Modifier.clickable(onClick = onValueClick)
                } else {
                    Modifier
                },
            )
        Text(
            text = display,
            modifier = valueModifier,
            fontSize = 14.sp,
            color = when {
                isPlaceholder -> MaterialTheme.colorScheme.onSurfaceVariant
                linkColor != null && onValueClick != null -> linkColor
                else -> MaterialTheme.colorScheme.onSurface
            },
            fontStyle = if (isPlaceholder) FontStyle.Italic else FontStyle.Normal,
            fontWeight = FontWeight.Normal,
            textAlign = if (multiline) TextAlign.Start else TextAlign.End,
            maxLines = if (multiline) 6 else 3,
        )
    }
}

@Composable
private fun ClientInitialsAvatar(
    firstName: String?,
    lastName: String?,
    sizeDp: Dp,
) {
    val initials = customerInitials(firstName, lastName)
    Box(
        modifier = Modifier
            .size(sizeDp)
            .clip(CircleShape)
            .background(SavioPalette.PrimaryLight),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initials,
            color = SavioPalette.Accent,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

private fun customerInitials(firstName: String?, lastName: String?): String {
    val f = firstName?.trim().orEmpty()
    val l = lastName?.trim().orEmpty()
    return when {
        f.isNotEmpty() && l.isNotEmpty() ->
            "${f.first().uppercaseChar()}${l.first().uppercaseChar()}"
        f.length >= 2 -> f.take(2).uppercase()
        f.isNotEmpty() -> f.first().uppercaseChar().toString()
        l.length >= 2 -> l.take(2).uppercase()
        l.isNotEmpty() -> l.first().uppercaseChar().toString()
        else -> "?"
    }
}
