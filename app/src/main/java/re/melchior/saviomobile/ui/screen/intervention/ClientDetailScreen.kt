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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import re.melchior.saviomobile.ui.component.ClientHistoryEmbeddedSection
import re.melchior.saviomobile.ui.refonte.SavioEdgeToEdgeScaffoldInsets
import re.melchior.saviomobile.ui.refonte.SavioClientInfoCard
import re.melchior.saviomobile.ui.refonte.SavioHeaderStyle
import re.melchior.saviomobile.ui.refonte.SavioNavyHeader
import re.melchior.saviomobile.ui.refonte.SavioRefonteCtaBar
import re.melchior.saviomobile.ui.refonte.SavioRefonteEditBottomBar
import re.melchior.saviomobile.ui.theme.SavioRefonte
import re.melchior.saviomobile.ui.theme.useSavioRefonteUi
import re.melchior.saviomobile.ui.component.SavioClientDetailSkeleton
import re.melchior.saviomobile.ui.component.SavioSnackbarHost
import re.melchior.saviomobile.ui.theme.SavioPalette
import re.melchior.saviomobile.ui.theme.SavioUi
import re.melchior.saviomobile.ui.theme.savioTopAppBarColors
import re.melchior.saviomobile.ui.theme.savioTopAppBarSubtitleColor
import re.melchior.saviomobile.ui.utils.SavioWindowSize
import re.melchior.saviomobile.ui.utils.rememberSavioWindowSize

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientDetailScreen(
    windowSizeClass: WindowSizeClass,
    onBack: () -> Unit,
    onNewIntervention: (unitId: String, customerId: String, displayName: String, addressLine: String) -> Unit = { _, _, _, _ -> },
    viewModel: ClientDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val uriHandler = LocalUriHandler.current
    val isTablet = rememberSavioWindowSize(windowSizeClass) == SavioWindowSize.EXPANDED

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

    val titleName = uiState.titleName
    val showBottomBar =
        !isTablet &&
            uiState.canShowContent &&
            !uiState.isLoading &&
            (uiState.isEditing || uiState.showNewInterventionCta)

    val refonte = useSavioRefonteUi()
    Scaffold(
        containerColor =
            if (refonte) SavioRefonte.BgPage else SavioUi.PageBackground,
        snackbarHost = { SavioSnackbarHost(snackbarHostState) },
        contentWindowInsets = if (refonte) SavioEdgeToEdgeScaffoldInsets else androidx.compose.material3.ScaffoldDefaults.contentWindowInsets,
        topBar = {
            if (refonte) {
                SavioNavyHeader(
                    title = titleName,
                    style = SavioHeaderStyle.Detail,
                    leading = {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.Filled.ArrowBack,
                                contentDescription = "Retour",
                                tint = Color.White,
                            )
                        }
                    },
                    actions = {
                        if (!uiState.isEditing) {
                            IconButton(onClick = viewModel::startEditing) {
                                Icon(
                                    Icons.Filled.Edit,
                                    contentDescription = "Modifier",
                                    tint = Color.White,
                                )
                            }
                        }
                    },
                )
            } else {
                TopAppBar(
                    colors = savioTopAppBarColors(),
                    title = {
                        Column {
                            Text(
                                text = titleName,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            if (uiState.updatesRequireValidation) {
                                Text(
                                    text = "Modifications soumises à validation",
                                    fontSize = 11.sp,
                                    color = savioTopAppBarSubtitleColor(),
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.Filled.ArrowBack,
                                contentDescription = "Retour",
                            )
                        }
                    },
                    actions = {
                        if (!uiState.isEditing) {
                            IconButton(onClick = viewModel::startEditing) {
                                Icon(
                                    Icons.Filled.Edit,
                                    contentDescription = "Modifier",
                                )
                            }
                        }
                    },
                )
            }
        },
        bottomBar = {
            if (showBottomBar) {
                ClientDetailBottomBar(
                    uiState = uiState,
                    onNewIntervention = onNewIntervention,
                    onSave = viewModel::saveChanges,
                    onCancel = viewModel::cancelEditing,
                )
            }
        },
    ) { padding ->
        when {
            uiState.isLoading -> {
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
            !uiState.canShowContent -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .background(SavioUi.PageBackground),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Client introuvable", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            else -> {
                val intervention = uiState.intervention
                val addressLine = intervention?.let {
                    "${it.unitStreet}, ${it.unitPostalCode} ${it.unitCity}"
                } ?: uiState.addressLine
                val resolvedAddress = uiState.addressLine.ifBlank { addressLine }

                if (isTablet) {
                    val pageBg = if (refonte) SavioRefonte.BgPage else SavioUi.PageBackground
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .background(pageBg),
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(0.4f)
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            ClientDetailInfoSections(
                                uiState = uiState,
                                addressLine = addressLine,
                                uriHandler = uriHandler,
                                viewModel = viewModel,
                                refonte = refonte,
                            )
                            if (uiState.showNewInterventionCta && !refonte) {
                                ClientNewInterventionButton(
                                    onClick = {
                                        onNewIntervention(
                                            uiState.resolvedUnitId,
                                            uiState.customerId,
                                            uiState.titleName,
                                            resolvedAddress,
                                        )
                                    },
                                )
                            }
                        }
                        ClientHistoryEmbeddedSection(
                            history = uiState.history,
                            photoUrls = uiState.historyPhotoUrls,
                            modifier = Modifier
                                .weight(0.6f)
                                .fillMaxSize()
                                .padding(start = 8.dp, end = 16.dp, top = 14.dp, bottom = 14.dp),
                            defaultVisibleCount = null,
                            useLazyList = true,
                        )
                    }
                } else if (refonte) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .background(SavioRefonte.BgPage)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                            .padding(bottom = if (showBottomBar) 96.dp else 0.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        ClientDetailInfoSections(
                            uiState = uiState,
                            addressLine = addressLine,
                            uriHandler = uriHandler,
                            viewModel = viewModel,
                            refonte = true,
                        )
                        ClientHistoryEmbeddedSection(
                            history = uiState.history,
                            photoUrls = uiState.historyPhotoUrls,
                            modifier = Modifier.fillMaxWidth(),
                            defaultVisibleCount = 3,
                            useLazyList = false,
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .background(SavioUi.PageBackground),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            ClientDetailInfoSections(
                                uiState = uiState,
                                addressLine = addressLine,
                                uriHandler = uriHandler,
                                viewModel = viewModel,
                                refonte = false,
                            )
                        }
                        ClientHistoryEmbeddedSection(
                            history = uiState.history,
                            photoUrls = uiState.historyPhotoUrls,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .padding(bottom = 8.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ClientDetailInfoSections(
    uiState: ClientDetailUiState,
    addressLine: String,
    uriHandler: androidx.compose.ui.platform.UriHandler,
    viewModel: ClientDetailViewModel,
    refonte: Boolean,
) {
    if (uiState.updatesRequireValidation && uiState.isEditing) {
        ClientValidationBanner()
    }
    if (refonte) {
        SavioClientInfoCard(
            addressLine = addressLine,
            addressLine2 = uiState.addressLine2,
            floor = uiState.floor,
            doorCode = uiState.doorCode,
            phone = uiState.phone,
            email = uiState.email,
            notes = uiState.notes,
            isEditing = uiState.isEditing,
            emailError = uiState.emailError,
            onAddressLine2Change = viewModel::onAddressLine2Change,
            onFloorChange = viewModel::onFloorChange,
            onDoorCodeChange = viewModel::onDoorCodeChange,
            onPhoneChange = viewModel::onPhoneChange,
            onEmailChange = viewModel::onEmailChange,
            onNotesChange = viewModel::onNotesChange,
        )
        return
    }
    ClientAccessSection(
        addressLine = addressLine,
        addressLine2 = uiState.addressLine2,
        floor = uiState.floor,
        doorCode = uiState.doorCode,
        isEditing = uiState.isEditing,
        onAddressLine2Change = viewModel::onAddressLine2Change,
        onFloorChange = viewModel::onFloorChange,
        onDoorCodeChange = viewModel::onDoorCodeChange,
    )
    ClientContactSection(
        phone = uiState.phone,
        email = uiState.email,
        notes = uiState.notes,
        emailError = uiState.emailError,
        isEditing = uiState.isEditing,
        uriHandler = uriHandler,
        onPhoneChange = viewModel::onPhoneChange,
        onEmailChange = viewModel::onEmailChange,
        onNotesChange = viewModel::onNotesChange,
    )
}

@Composable
private fun ClientDetailBottomBar(
    uiState: ClientDetailUiState,
    onNewIntervention: (unitId: String, customerId: String, displayName: String, addressLine: String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
) {
    val refonte = useSavioRefonteUi()
    val intervention = uiState.intervention
    val addressLine = intervention?.let {
        "${it.unitStreet}, ${it.unitPostalCode} ${it.unitCity}"
    } ?: uiState.addressLine
    val resolvedAddress = uiState.addressLine.ifBlank { addressLine }

    if (refonte) {
        if (uiState.isEditing) {
            SavioRefonteEditBottomBar(
                saveLabel =
                    if (uiState.updatesRequireValidation) {
                        "Soumettre les modifications"
                    } else {
                        "Enregistrer"
                    },
                onSave = onSave,
                onCancel = onCancel,
                isSaving = uiState.isSaving,
            )
        } else if (uiState.showNewInterventionCta) {
            SavioRefonteCtaBar(
                text = "Nouvelle intervention",
                onClick = {
                    onNewIntervention(
                        uiState.resolvedUnitId,
                        uiState.customerId,
                        uiState.titleName,
                        resolvedAddress,
                    )
                },
            )
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SavioUi.PageBackground)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (uiState.isEditing) {
            Button(
                onClick = onSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = !uiState.isSaving,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                shape = RoundedCornerShape(28.dp),
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
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ),
                border = BorderStroke(1.dp, SavioPalette.Accent),
            ) {
                Text("Annuler", fontWeight = FontWeight.Medium)
            }
        } else if (uiState.showNewInterventionCta) {
            ClientNewInterventionButton(
                onClick = {
                    onNewIntervention(
                        uiState.resolvedUnitId,
                        uiState.customerId,
                        uiState.titleName,
                        resolvedAddress,
                    )
                },
            )
        }
    }
}

@Composable
private fun ClientNewInterventionButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ),
        shape = RoundedCornerShape(28.dp),
    ) {
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.size(8.dp))
        Text(
            text = "Nouvelle intervention",
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun ClientValidationBanner() {
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

@Composable
private fun ClientAccessSection(
    addressLine: String,
    addressLine2: String,
    floor: String,
    doorCode: String,
    isEditing: Boolean,
    onAddressLine2Change: (String) -> Unit,
    onFloorChange: (String) -> Unit,
    onDoorCodeChange: (String) -> Unit,
) {
    ClientSectionCard {
        ClientSectionTitle(
            icon = Icons.Filled.LocationOn,
            title = "Accès logement",
        )
        Text(
            text = addressLine.ifBlank { "Adresse non renseignée" },
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 10.dp),
        )
        if (addressLine2.isNotBlank() && !isEditing) {
            Text(
                text = addressLine2,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
        if (isEditing) {
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = addressLine2,
                onValueChange = onAddressLine2Change,
                label = { Text("Complément d'adresse") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = floor,
                    onValueChange = onFloorChange,
                    label = { Text("Étage") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = doorCode,
                    onValueChange = onDoorCodeChange,
                    label = { Text("Code accès") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                )
            }
        } else {
            val hasAccessInfo = floor.isNotBlank() || doorCode.isNotBlank()
            if (hasAccessInfo) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (floor.isNotBlank()) {
                        ClientAccessChip(label = "Étage", value = floor)
                    }
                    if (doorCode.isNotBlank()) {
                        ClientAccessChip(label = "Code", value = doorCode)
                    }
                }
            }
        }
    }
}

@Composable
private fun ClientAccessChip(label: String, value: String) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = SavioPalette.PrimaryLight,
        border = BorderStroke(0.5.dp, SavioUi.CardBorder),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = SavioPalette.Accent,
            )
        }
    }
}

@Composable
private fun ClientContactSection(
    phone: String,
    email: String,
    notes: String,
    emailError: String?,
    isEditing: Boolean,
    uriHandler: androidx.compose.ui.platform.UriHandler,
    onPhoneChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
) {
    if (isEditing) {
        ClientSectionCard {
            ClientSectionTitle(
                icon = Icons.Filled.Phone,
                title = "Coordonnées",
            )
            ClientThinDivider()
            OutlinedTextField(
                value = phone,
                onValueChange = onPhoneChange,
                label = { Text("Téléphone") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = emailError != null,
                supportingText = {
                    emailError?.let { err ->
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
                value = notes,
                onValueChange = onNotesChange,
                label = { Text("Notes") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                maxLines = 4,
            )
        }
        return
    }

    val hasContact = phone.isNotBlank() || email.isNotBlank() || notes.isNotBlank()
    if (!hasContact) return

    ClientSectionCard {
        ClientSectionTitle(
            icon = Icons.Filled.Phone,
            title = "Contact",
        )
        if (phone.isNotBlank() || email.isNotBlank()) {
            Spacer(modifier = Modifier.height(10.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (phone.isNotBlank()) {
                    ClientContactLine(
                        icon = Icons.Filled.Phone,
                        text = phone,
                        onClick = { uriHandler.openUri("tel:${phone.trim()}") },
                        linkColor = MaterialTheme.colorScheme.primary,
                    )
                }
                if (email.isNotBlank()) {
                    ClientContactLine(
                        icon = Icons.Filled.Email,
                        text = email,
                        onClick = { uriHandler.openUri("mailto:${email.trim()}") },
                        linkColor = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
        if (notes.isNotBlank()) {
            if (phone.isNotBlank() || email.isNotBlank()) {
                ClientThinDivider()
            } else {
                Spacer(modifier = Modifier.height(10.dp))
            }
            Text(
                text = notes,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp,
            )
        }
    }
}

@Composable
private fun ClientContactLine(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    linkColor: Color,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = SavioUi.BusinessAccent,
            modifier = Modifier.size(18.dp),
        )
        Text(
            text = text,
            fontSize = 14.sp,
            color = linkColor,
            fontWeight = FontWeight.Medium,
        )
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
        modifier = Modifier.padding(vertical = 12.dp),
        thickness = 0.5.dp,
        color = SavioUi.CardBorder,
    )
}
