package re.melchior.saviomobile.ui.screen.intervention

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.VpnKey
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import re.melchior.saviomobile.data.local.entity.EquipmentEntity
import re.melchior.saviomobile.ui.component.BrandLogo
import re.melchior.saviomobile.ui.component.SavioEmptyState
import re.melchior.saviomobile.ui.screen.invoice.InvoiceMobileStatus
import re.melchior.saviomobile.ui.screen.invoice.technicianFieldBadge
import re.melchior.saviomobile.ui.screen.invoice.mobileStatus
import re.melchior.saviomobile.ui.component.SavioPhotosTabSkeleton
import re.melchior.saviomobile.ui.theme.SavioInterventionColors
import re.melchior.saviomobile.ui.theme.SavioPalette
import re.melchior.saviomobile.ui.theme.SavioUi
import re.melchior.saviomobile.ui.theme.formatEquipmentTypeLabel
import re.melchior.saviomobile.ui.utils.equipmentIcon

internal enum class EquipmentBadge {
    NONE,
    NEW,
    REPLACED,
}

internal fun badgeFor(
    equipment: EquipmentEntity,
    newEquipmentIds: Set<String>,
): EquipmentBadge = when {
    equipment.typeCode == "replaced" -> EquipmentBadge.REPLACED
    equipment.id in newEquipmentIds -> EquipmentBadge.NEW
    else -> EquipmentBadge.NONE
}

@Composable
fun InterventionDetailTab(
    uiState: InterventionActiveUiState,
    onClientClick: (customerId: String) -> Unit,
) {
    val intervention = uiState.intervention ?: return
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline),
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Icon(
                            Icons.Filled.CalendarToday,
                            contentDescription = null,
                            tint = SavioUi.BusinessAccent,
                            modifier = Modifier.size(20.dp),
                        )
                        Column {
                            Text(
                                text = formatInterventionDate(intervention.scheduledAt),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = formatInterventionTime(intervention.scheduledAt),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outline,
                    )

                    intervention.customerId?.let { customerId ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onClientClick(customerId) }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = "CM",
                                    color = SavioUi.BusinessAccent,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${intervention.customerFirstName.orEmpty()} ${intervention.customerLastName.orEmpty()}".trim(),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                intervention.customerPhone?.takeIf { it.isNotBlank() }?.let { phone ->
                                    Text(
                                        text = phone,
                                        fontSize = 12.sp,
                                        color = SavioUi.BusinessAccent,
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Filled.ChevronRight,
                                contentDescription = "Fiche client",
                                tint = SavioUi.BusinessAccent,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outline,
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Icon(
                            Icons.Filled.LocationOn,
                            contentDescription = null,
                            tint = SavioUi.BusinessAccent,
                            modifier = Modifier.size(20.dp),
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = intervention.unitStreet,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = "${intervention.unitPostalCode} ${intervention.unitCity}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            if (!intervention.unitFloor.isNullOrBlank() ||
                                !intervention.unitDoorCode.isNullOrBlank()
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    intervention.unitFloor?.let { floor ->
                                        InfoChip(text = "Étage $floor")
                                    }
                                    intervention.unitDoorCode?.let { code ->
                                        InfoChip(text = "Code $code")
                                    }
                                }
                            }
                        }
                    }

                    intervention.contractType?.let { type ->
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outline,
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Icon(
                                Icons.Filled.Description,
                                contentDescription = null,
                                tint = SavioUi.BusinessAccent,
                                modifier = Modifier.size(20.dp),
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Contrat $type",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                intervention.contractRenewalDate?.let { date ->
                                    Text(
                                        text = "Échéance : ${formatContractDate(date)}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(20.dp),
                            ) {
                                Text(
                                    text = "Actif",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = SavioUi.BusinessAccent,
                                )
                            }
                        }
                    }

                    intervention.notes?.takeIf { it.isNotBlank() }?.let { notes ->
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outline,
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Icon(
                                Icons.Filled.Notes,
                                contentDescription = null,
                                tint = SavioUi.BusinessAccent,
                                modifier = Modifier.size(20.dp),
                            )
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                color = SavioInterventionColors.NotesDispatcherBg,
                            ) {
                                Text(
                                    text = notes,
                                    fontSize = 14.sp,
                                    color = SavioInterventionColors.NotesDispatcherText,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(10.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InterventionEquipementsTab(
    uiState: InterventionActiveUiState,
    newEquipmentIds: Set<String>,
    onEquipementClick: (interventionId: String, equipmentId: String) -> Unit,
    onAddEquipment: (interventionId: String, unitId: String, parentEquipmentId: String?) -> Unit,
) {
    val intervention = uiState.intervention ?: return
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline),
            shadowElevation = 0.dp,
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Build,
                            contentDescription = null,
                            tint = SavioUi.BusinessAccent,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Équipements",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        if (uiState.equipments.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .defaultMinSize(minWidth = 20.dp, minHeight = 20.dp)
                                    .clip(CircleShape)
                                    .background(SavioUi.BusinessAccent),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = uiState.equipments.size.toString(),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = SavioPalette.White,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                                )
                            }
                        }
                    }
                    IconButton(
                        onClick = {
                            onAddEquipment(intervention.id, intervention.unitId, null)
                        },
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Ajouter un appareil",
                            tint = SavioUi.BusinessAccent,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }

                if (uiState.equipments.isEmpty()) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outline,
                    )
                    SavioEmptyState(
                        icon = Icons.Outlined.VpnKey,
                        title = "Aucun équipement enregistré",
                        subtitle = "Ajoutez l'appareil installé chez le client.",
                        modifier = Modifier.padding(vertical = 8.dp),
                        primaryActionLabel = "+ Ajouter un équipement",
                        onPrimaryAction = {
                            onAddEquipment(
                                intervention.id,
                                intervention.unitId,
                                null,
                            )
                        },
                    )
                } else {
                    val allActive = uiState.equipments
                        .filter { it.typeCode != "replaced" }

                    val allReplaced = uiState.equipments
                        .filter { it.typeCode == "replaced" }

                    val rootEquipments = allActive
                        .filter { it.parentEquipmentId == null }
                        .sortedWith(
                            compareByDescending<EquipmentEntity> { it.isPrimary }
                                .thenBy { it.order },
                        )

                    val childrenByParent = allActive
                        .filter { it.parentEquipmentId != null }
                        .groupBy { it.parentEquipmentId }
                        .mapValues { (_, list) ->
                            list.sortedWith(
                                compareBy<EquipmentEntity> { it.order }.thenBy { it.id },
                            )
                        }

                    val orphans = allActive.filter { eq ->
                        eq.parentEquipmentId != null &&
                            rootEquipments.none { it.id == eq.parentEquipmentId }
                    }

                    val rootReplaced = allReplaced
                        .filter { it.parentEquipmentId == null }
                        .sortedWith(
                            compareBy<EquipmentEntity> { it.order }.thenBy { it.id },
                        )

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outline,
                    )

                    val hybrideGroups = computeHybrideGroups(
                        equipmentRoots = rootEquipments,
                        allEquipments = allActive,
                        childrenByParent = childrenByParent,
                    )
                    val hybrideIds = hybrideEquipmentIds(hybrideGroups)
                    val normalRootAndOrphans =
                        (rootEquipments + orphans).filter { it.id !in hybrideIds }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        hybrideGroups.forEach { group ->
                            HybrideGroupCard(
                                group = group,
                                newEquipmentIds = newEquipmentIds,
                                interventionId = intervention.id,
                                interactive = true,
                                onEquipementClick = onEquipementClick,
                            )
                        }
                        normalRootAndOrphans.forEach { parent ->
                            InterventionEquipmentGroupCard(
                                parent = parent,
                                children = childrenByParent[parent.id].orEmpty(),
                                newEquipmentIds = newEquipmentIds,
                                interventionId = intervention.id,
                                interactive = true,
                                onEquipementClick = onEquipementClick,
                            )
                        }
                        rootReplaced.forEach { parent ->
                            InterventionEquipmentGroupCard(
                                parent = parent,
                                children = childrenByParent[parent.id].orEmpty(),
                                newEquipmentIds = newEquipmentIds,
                                interventionId = intervention.id,
                                interactive = true,
                                onEquipementClick = onEquipementClick,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InterventionPhotosTab(
    uiState: InterventionActiveUiState,
    onOpenCamera: (unitId: String, customerId: String) -> Unit,
) {
    val intervention = uiState.intervention ?: run {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            SavioPhotosTabSkeleton()
        }
        return
    }

    val customerId = intervention.customerId ?: run {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "Client introuvable",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }

    PhotosTabContent(
        interventionId = intervention.id,
        unitId = intervention.unitId,
        customerId = customerId,
        onOpenCamera = onOpenCamera,
        isOnline = true,
    )
}

@Composable
fun InterventionFactureTab(
    uiState: InterventionActiveUiState,
    onFactureClick: (interventionId: String) -> Unit,
    viewModel: InterventionActiveViewModel,
) {
    val intervention = uiState.intervention ?: return
    val invoice = uiState.invoice
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline),
            shadowElevation = 0.dp,
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Receipt,
                        contentDescription = null,
                                tint = SavioUi.BusinessAccent,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Facturation",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outline,
                )

                if (invoice != null) {
                    val mobileStatus = invoice.mobileStatus()
                    val fieldBadge = technicianFieldBadge(mobileStatus)
                    val summaryText =
                        when {
                            uiState.invoiceLineCount == 0 -> "Aucune ligne"
                            else ->
                                "${uiState.invoiceLineCount} ligne(s) — %.2f € TTC".format(
                                    invoice.totalTtc,
                                )
                        }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onFactureClick(intervention.id)
                            }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                invoice.number?.takeIf { it.isNotBlank() }?.let { num ->
                                    Text(
                                        text = num,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                }
                                fieldBadge?.let { badge ->
                                    Surface(
                                        color = badge.background,
                                        shape = RoundedCornerShape(20.dp),
                                    ) {
                                        Text(
                                            text = badge.label,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = badge.foreground,
                                        )
                                    }
                                }
                            }
                            Text(
                                text = summaryText,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Icon(
                            imageVector = Icons.Filled.ChevronRight,
                            contentDescription = null,
                            tint = SavioUi.BusinessAccent,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                } else {
                    Column(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        SavioEmptyState(
                            icon = Icons.Outlined.Description,
                            title = "Aucune facture créée",
                            subtitle = "Créez une facture pour cette intervention.",
                            primaryActionLabel = "+ Créer une facture",
                            primaryEnabled = true,
                            onPrimaryAction = {
                                viewModel.createAndNavigateToInvoice(
                                    interventionId = intervention.id,
                                    unitId = intervention.unitId,
                                    technicianId = "",
                                )
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoChip(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(horizontal = 6.dp, vertical = 2.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
internal fun InterventionEquipmentGroupCard(
    parent: EquipmentEntity,
    children: List<EquipmentEntity>,
    newEquipmentIds: Set<String>,
    interventionId: String,
    interactive: Boolean = true,
    onEquipementClick: (interventionId: String, equipmentId: String) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shadowElevation = 1.dp,
    ) {
        Column {
            EquipmentRowItem(
                equipment = parent,
                isChild = false,
                badge = badgeFor(parent, newEquipmentIds),
                roleLabel = parent.pacClimRoleLabel(
                    isChild = false,
                    hasChildren = children.isNotEmpty(),
                ),
                interactive = interactive,
                onClick = {
                    onEquipementClick(interventionId, parent.id)
                },
            )
            children.forEach { child ->
                HorizontalDivider(
                    modifier = Modifier.padding(start = 56.dp),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                )
                EquipmentRowItem(
                    equipment = child,
                    isChild = true,
                    badge = badgeFor(child, newEquipmentIds),
                    roleLabel = child.pacClimRoleLabel(
                        isChild = true,
                        hasChildren = false,
                    ),
                    interactive = interactive,
                    onClick = {
                        onEquipementClick(interventionId, child.id)
                    },
                )
            }
        }
    }
}

@Composable
internal fun EquipmentRowItem(
    equipment: EquipmentEntity,
    isChild: Boolean,
    badge: EquipmentBadge,
    roleLabel: String?,
    interactive: Boolean = true,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .then(
                if (interactive) Modifier.clickable(onClick = onClick) else Modifier,
            )
            .padding(
                start = if (isChild) 32.dp else 12.dp,
                end = 12.dp,
                top = 12.dp,
                bottom = 12.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (isChild) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(36.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        Box(
            modifier = Modifier
                .width(if (isChild) 40.dp else 52.dp)
                .height(if (isChild) 28.dp else 36.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(
                    width = 0.5.dp,
                    color = MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(6.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            BrandLogo(
                brandId = equipment.catalogBrandId,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp),
                fallback = {
                    Icon(
                        equipmentIcon(equipment.typeCode),
                        contentDescription = null,
                        modifier = Modifier.size(if (isChild) 14.dp else 18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = listOfNotNull(equipment.brand, equipment.model)
                    .joinToString(" ")
                    .ifEmpty { "Équipement sans nom" },
                style = if (isChild) {
                    MaterialTheme.typography.bodySmall
                } else {
                    MaterialTheme.typography.bodyMedium
                },
                fontWeight = if (isChild) FontWeight.Medium else FontWeight.SemiBold,
                color = if (isChild) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                textDecoration = if (equipment.typeCode == "replaced") {
                    TextDecoration.LineThrough
                } else {
                    TextDecoration.None
                },
            )
            if (badge != EquipmentBadge.NONE) {
                Spacer(modifier = Modifier.height(2.dp))
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (badge == EquipmentBadge.NEW) {
                        Color(0xFFEAF3DE)
                    } else {
                        Color(0xFFFFECEC)
                    },
                    shadowElevation = 0.dp,
                ) {
                    Text(
                        text = if (badge == EquipmentBadge.NEW) "+ Nouveau" else "Remplacé",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (badge == EquipmentBadge.NEW) {
                            Color(0xFF27500A)
                        } else {
                            Color(0xFFA32D2D)
                        },
                    )
                }
            }
            roleLabel?.let { label ->
                Spacer(modifier = Modifier.height(2.dp))
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFFE6F1FB),
                    shadowElevation = 0.dp,
                ) {
                    Text(
                        text = label,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF0C447C),
                    )
                }
            }
            equipment.typeCode?.let { code ->
                Text(
                    text = formatEquipmentTypeLabel(code),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (interactive) {
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

internal fun EquipmentEntity.pacClimRoleLabel(
    isChild: Boolean,
    hasChildren: Boolean,
): String? {
    val code = typeCode?.uppercase() ?: return null
    val isPacOrClim = code.contains("PAC") || code.contains("CLIM")
    if (!isPacOrClim) return null
    return if (isChild) "Unité int." else if (hasChildren) "Unité ext." else null
}

private fun formatInterventionDate(scheduledAt: String?): String {
    if (scheduledAt.isNullOrBlank()) return "—"
    return try {
        val instant = java.time.Instant.parse(scheduledAt)
        val zdt = instant.atZone(java.time.ZoneId.systemDefault())
        val formatter = java.time.format.DateTimeFormatter
            .ofPattern("EEEE d MMMM yyyy", java.util.Locale.FRENCH)
        zdt.format(formatter).replaceFirstChar { it.uppercase() }
    } catch (_: Exception) {
        scheduledAt.take(10)
    }
}

private fun formatInterventionTime(scheduledAt: String?): String {
    if (scheduledAt.isNullOrBlank()) return "—"
    return try {
        val instant = java.time.Instant.parse(scheduledAt)
        val zdt = instant.atZone(java.time.ZoneId.systemDefault())
        val formatter = java.time.format.DateTimeFormatter
            .ofPattern("HH:mm", java.util.Locale.FRENCH)
        zdt.format(formatter)
    } catch (_: Exception) {
        "—"
    }
}

private fun formatContractDate(date: String?): String {
    if (date.isNullOrBlank()) return "—"
    return try {
        val ld = java.time.LocalDate.parse(date.take(10))
        ld.format(
            java.time.format.DateTimeFormatter
                .ofPattern("dd/MM/yyyy", java.util.Locale.FRENCH),
        )
    } catch (_: Exception) {
        date.take(10)
    }
}
