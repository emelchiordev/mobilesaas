package re.melchior.saviomobile.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import re.melchior.saviomobile.data.local.entity.InterventionHistoryEntity
import re.melchior.saviomobile.ui.refonte.SavioHistoryFilterChips
import re.melchior.saviomobile.ui.refonte.SavioHistoryMonthHeader
import re.melchior.saviomobile.ui.refonte.SavioHistoryRow
import re.melchior.saviomobile.ui.refonte.SavioHistorySectionHeader
import re.melchior.saviomobile.ui.refonte.SavioHistoryShowMoreRow
import re.melchior.saviomobile.ui.refonte.SavioRefonteCard
import re.melchior.saviomobile.ui.refonte.SavioRowLine
import re.melchior.saviomobile.ui.theme.SavioDimens
import re.melchior.saviomobile.ui.theme.SavioInterventionColors
import re.melchior.saviomobile.ui.theme.SavioRefonte
import re.melchior.saviomobile.ui.theme.SavioType
import re.melchior.saviomobile.ui.theme.SavioUi
import re.melchior.saviomobile.ui.theme.useSavioRefonteUi
import java.time.YearMonth
import re.melchior.saviomobile.util.formatScheduledAtDate
import re.melchior.saviomobile.util.formatScheduledAtDateReadable
import re.melchior.saviomobile.util.formatScheduledAtDateShort
import re.melchior.saviomobile.util.SavioTimeZone

@Composable
fun ClientHistoryEmbeddedSection(
    history: List<InterventionHistoryEntity>,
    photoUrls: Map<String, List<String>>,
    modifier: Modifier = Modifier,
    compact: Boolean = true,
    defaultVisibleCount: Int? = null,
    useLazyList: Boolean = true,
) {
    var expandedIds by remember { mutableStateOf(setOf<String>()) }
    var typeFilter by remember { mutableStateOf<String?>(null) }
    var historyExpanded by remember { mutableStateOf(false) }
    val refonte = useSavioRefonteUi()
    val filteredHistory =
        remember(history, typeFilter) {
            val base =
                if (typeFilter == null) {
                    history
                } else {
                    history.filter { it.typeLabel.equals(typeFilter, ignoreCase = true) }
                }
            base.sortedByDescending { it.scheduledAt }
        }
    val filterOptions =
        listOf("Tout") +
            history.map { it.typeLabel }.distinct().sorted()
    val latestItemId = filteredHistory.firstOrNull()?.id

    LaunchedEffect(typeFilter) {
        historyExpanded = false
    }

    Column(modifier = modifier) {
        if (refonte) {
            SavioHistorySectionHeader(count = history.size)
            if (history.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                SavioHistoryFilterChips(
                    filters = filterOptions,
                    selected = typeFilter ?: "Tout",
                    onSelect = { typeFilter = it },
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }
        } else {
            Text(
                text = "Historique",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }
        if (history.isEmpty()) {
            Text(
                text = "Aucune intervention passée enregistrée pour ce logement.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else if (refonte) {
            RefonteHistoryList(
                filteredHistory = filteredHistory,
                photoUrls = photoUrls,
                expandedIds = expandedIds,
                onToggleItem = { id ->
                    expandedIds =
                        if (id in expandedIds) {
                            expandedIds - id
                        } else {
                            expandedIds + id
                        }
                },
                latestItemId = latestItemId,
                defaultVisibleCount = defaultVisibleCount,
                historyExpanded = historyExpanded,
                onShowMore = { historyExpanded = true },
                useLazyList = useLazyList,
            )
        } else if (useLazyList) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(SavioDimens.SpaceSM),
            ) {
                items(filteredHistory, key = { it.id }) { item ->
                    LegacyHistoryItem(
                        item = item,
                        photoUrls = photoUrls,
                        compact = compact,
                        expandedIds = expandedIds,
                        onToggle = { id ->
                            expandedIds =
                                if (id in expandedIds) {
                                    expandedIds - id
                                } else {
                                    expandedIds + id
                                }
                        },
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(SavioDimens.SpaceSM)) {
                filteredHistory.forEach { item ->
                    LegacyHistoryItem(
                        item = item,
                        photoUrls = photoUrls,
                        compact = compact,
                        expandedIds = expandedIds,
                        onToggle = { id ->
                            expandedIds =
                                if (id in expandedIds) {
                                    expandedIds - id
                                } else {
                                    expandedIds + id
                                }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun LegacyHistoryItem(
    item: InterventionHistoryEntity,
    photoUrls: Map<String, List<String>>,
    compact: Boolean,
    expandedIds: Set<String>,
    onToggle: (String) -> Unit,
) {
    if (compact) {
        HistoriqueCompactItemCard(
            item = item,
            signedUrls = photoUrls[item.id] ?: emptyList(),
            expanded = item.id in expandedIds,
            onToggle = { onToggle(item.id) },
        )
    } else {
        HistoriqueItemCard(
            item = item,
            signedUrls = photoUrls[item.id] ?: emptyList(),
        )
    }
}

@Composable
private fun RefonteHistoryList(
    filteredHistory: List<InterventionHistoryEntity>,
    photoUrls: Map<String, List<String>>,
    expandedIds: Set<String>,
    onToggleItem: (String) -> Unit,
    latestItemId: String?,
    defaultVisibleCount: Int?,
    historyExpanded: Boolean,
    onShowMore: () -> Unit,
    useLazyList: Boolean,
) {
    val showLimited =
        defaultVisibleCount != null && !historyExpanded && filteredHistory.size > defaultVisibleCount
    val visibleItems =
        if (showLimited) {
            filteredHistory.take(defaultVisibleCount!!)
        } else {
            filteredHistory
        }

    val content: @Composable () -> Unit = {
        if (showLimited) {
            val monthLabel = monthLabelForItem(visibleItems.first())
            if (monthLabel != null) {
                SavioHistoryMonthHeader(monthLabel = monthLabel)
            }
            SavioRefonteHistoryCard(
                items = visibleItems,
                photoUrls = photoUrls,
                expandedIds = expandedIds,
                onToggleItem = onToggleItem,
                latestItemId = latestItemId,
                showMore = true,
                onShowMore = onShowMore,
            )
        } else {
            val grouped =
                visibleItems.groupBy { item ->
                    runCatching {
                        YearMonth.from(
                            java.time.Instant.parse(item.scheduledAt)
                                .atZone(SavioTimeZone.appZone)
                                .toLocalDate(),
                        )
                    }.getOrNull()
                }
            grouped.entries.sortedByDescending { it.key }.forEach { (month, items) ->
                SavioHistoryMonthHeader(
                    monthLabel =
                        month?.let {
                            it.format(
                                java.time.format.DateTimeFormatter.ofPattern(
                                    "MMMM yyyy",
                                    java.util.Locale.FRENCH,
                                ),
                            ).replaceFirstChar { c -> c.uppercase() }
                        } ?: "Date inconnue",
                )
                SavioRefonteHistoryCard(
                    items = items,
                    photoUrls = photoUrls,
                    expandedIds = expandedIds,
                    onToggleItem = onToggleItem,
                    latestItemId = latestItemId,
                    showMore = false,
                    onShowMore = onShowMore,
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

    if (useLazyList) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            item { content() }
        }
    } else {
        content()
    }
}

private fun monthLabelForItem(item: InterventionHistoryEntity): String? {
    return runCatching {
        YearMonth.from(
            java.time.Instant.parse(item.scheduledAt)
                .atZone(SavioTimeZone.appZone)
                .toLocalDate(),
        ).format(
            java.time.format.DateTimeFormatter.ofPattern("MMMM yyyy", java.util.Locale.FRENCH),
        ).replaceFirstChar { c -> c.uppercase() }
    }.getOrNull()
}

@Composable
private fun SavioRefonteHistoryCard(
    items: List<InterventionHistoryEntity>,
    photoUrls: Map<String, List<String>>,
    expandedIds: Set<String>,
    onToggleItem: (String) -> Unit,
    latestItemId: String?,
    showMore: Boolean,
    onShowMore: () -> Unit,
) {
    SavioRefonteCard {
        items.forEachIndexed { index, item ->
            if (index > 0) {
                SavioRowLine()
            }
            val dateIso = item.completedAt ?: item.scheduledAt
            SavioHistoryRow(
                typeLabel = item.typeLabel,
                typeCode = item.typeCode,
                dateLabel = formatScheduledAtDateShort(dateIso),
                isLast = item.id == latestItemId,
                expanded = item.id in expandedIds,
                onClick = { onToggleItem(item.id) },
                expandedContent = {
                    HistoriqueItemCardBody(
                        item = item,
                        signedUrls = photoUrls[item.id] ?: emptyList(),
                    )
                },
            )
        }
        if (showMore) {
            SavioRowLine()
            SavioHistoryShowMoreRow(onClick = onShowMore)
        }
    }
}

@Composable
fun HistoriquePage(
    history: List<InterventionHistoryEntity>,
    photoUrls: Map<String, List<String>>,
) {
    if (history.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center,
        ) {
            SavioEmptyState(
                icon = Icons.Outlined.Schedule,
                title = "Aucun historique disponible",
                subtitle = "Les interventions passées sur ce logement apparaîtront ici.",
            )
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = SavioDimens.SpaceLG, vertical = SavioDimens.SpaceSM),
        verticalArrangement = Arrangement.spacedBy(SavioDimens.SpaceSM),
    ) {
        item { Spacer(modifier = Modifier.height(SavioDimens.SpaceXS)) }
        items(history) { item ->
            HistoriqueItemCard(
                item = item,
                signedUrls = photoUrls[item.id] ?: emptyList(),
            )
        }
        item { Spacer(modifier = Modifier.height(SavioDimens.SpaceSM)) }
    }
}

@Composable
fun HistoriqueCompactItemCard(
    item: InterventionHistoryEntity,
    signedUrls: List<String>,
    expanded: Boolean,
    onToggle: () -> Unit,
) {
    val typeColor = historyTypeColor(item)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(SavioDimens.RadiusLG),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(SavioDimens.BorderThin, MaterialTheme.colorScheme.outline),
        shadowElevation = 0.dp,
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
        ) {
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .background(typeColor),
            )
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onToggle)
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Surface(
                            shape = RoundedCornerShape(SavioDimens.RadiusBadge),
                            color = typeColor.copy(alpha = 0.12f),
                            shadowElevation = 0.dp,
                            tonalElevation = 0.dp,
                        ) {
                            Text(
                                text = item.typeLabel,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = SavioType.Label,
                                color = typeColor,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                            )
                        }
                        Text(
                            text = item.completedAt?.let { formatScheduledAtDateReadable(it) }
                                ?: formatScheduledAtDateReadable(item.scheduledAt),
                            style = SavioType.Label,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Icon(
                        imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = if (expanded) "Réduire" else "Développer",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp),
                    )
                }
                if (expanded) {
                    HistoriqueItemCardBody(
                        item = item,
                        signedUrls = signedUrls,
                    )
                }
            }
        }
    }
}

@Composable
fun HistoriqueItemCard(
    item: InterventionHistoryEntity,
    signedUrls: List<String> = emptyList(),
) {
    val typeColor = historyTypeColor(item)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(SavioDimens.RadiusLG),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(SavioDimens.BorderThin, MaterialTheme.colorScheme.outline),
        shadowElevation = 0.dp,
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
        ) {
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .background(typeColor),
            )
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        shape = RoundedCornerShape(SavioDimens.RadiusBadge),
                        color = typeColor.copy(alpha = 0.12f),
                        shadowElevation = 0.dp,
                        tonalElevation = 0.dp,
                    ) {
                        Text(
                            text = item.typeLabel,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = SavioType.Label,
                            color = typeColor,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                    Text(
                        text = item.completedAt?.let { formatScheduledAtDate(it) }
                            ?: formatScheduledAtDate(item.scheduledAt),
                        style = SavioType.Label,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                HistoriqueItemCardBody(
                    item = item,
                    signedUrls = signedUrls,
                )
            }
        }
    }
}

@Composable
private fun HistoriqueItemCardBody(
    item: InterventionHistoryEntity,
    signedUrls: List<String>,
) {
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
    ) {
        Icon(
            imageVector = Icons.Filled.Build,
            contentDescription = null,
            tint = SavioUi.BusinessAccent,
            modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = "Origine de l'intervention",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = item.typeLabel,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }

    if (!item.notes.isNullOrBlank()) {
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
        ) {
            Icon(
                imageVector = Icons.Filled.Info,
                contentDescription = null,
                tint = SavioUi.BusinessAccent,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Notes dispatcher",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = SavioInterventionColors.NotesDispatcherBg,
                    shadowElevation = 0.dp,
                ) {
                    Text(
                        text = item.notes!!,
                        fontSize = 14.sp,
                        color = SavioInterventionColors.NotesDispatcherText,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(10.dp),
                    )
                }
            }
        }
    }

    item.report?.takeIf { it.isNotBlank() }?.let { report ->
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
        ) {
            Icon(
                imageVector = Icons.Filled.Description,
                contentDescription = null,
                tint = SavioUi.BusinessAccent,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Compte-rendu",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = report,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 4,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                )
            }
        }
    }

    val techName = listOfNotNull(item.technicianFirstName, item.technicianLastName)
        .joinToString(" ").ifEmpty { null }
    if (techName != null || item.number != null) {
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
        ) {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = null,
                tint = SavioUi.BusinessAccent,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                techName?.let {
                    Text(
                        text = it,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                item.number?.let {
                    Text(
                        text = it,
                        style = SavioType.Label,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }

    if (signedUrls.isNotEmpty()) {
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp),
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outline,
        )
        Column(
            modifier = Modifier.padding(
                horizontal = 16.dp,
                vertical = 14.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(SavioDimens.SpaceXS),
        ) {
            signedUrls.chunked(3).forEach { row ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(SavioDimens.SpaceXS),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    row.forEach { url ->
                        AsyncImage(
                            model = url,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(SavioDimens.RadiusSM)),
                        )
                    }
                    repeat(3 - row.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    } else {
        val photoCount = try {
            item.photoKeys?.let {
                com.google.gson.Gson()
                    .fromJson(it, Array<String>::class.java)?.size ?: 0
            } ?: 0
        } catch (_: Exception) {
            0
        }

        if (photoCount > 0) {
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outline,
            )
            Row(
                modifier = Modifier.padding(
                    horizontal = 16.dp,
                    vertical = 14.dp,
                ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Filled.CameraAlt,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = SavioUi.BusinessAccent,
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "$photoCount photo${if (photoCount > 1) "s" else ""}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
private fun historyTypeColor(item: InterventionHistoryEntity): Color {
    return remember(item.typeColor) {
        try {
            item.typeColor?.let { Color(android.graphics.Color.parseColor(it)) }
        } catch (_: Exception) {
            null
        }
    } ?: SavioUi.BusinessAccent
}
