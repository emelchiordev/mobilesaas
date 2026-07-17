package re.savio.mobile.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import re.savio.mobile.data.local.entity.ClientFinancialSummaryEntity
import re.savio.mobile.data.remote.dto.ClientFinancialDocumentDto
import re.savio.mobile.ui.designsystem.AppBadge
import re.savio.mobile.ui.designsystem.SectionCard
import re.savio.mobile.ui.refonte.SavioRefonteCard
import re.savio.mobile.ui.refonte.SavioSectionHeader
import re.savio.mobile.ui.theme.SavioRefonte
import re.savio.mobile.ui.theme.SavioType
import re.savio.mobile.ui.theme.useSavioRefonteUi
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val moneyFormat = NumberFormat.getCurrencyInstance(Locale.FRANCE)
private val shortDateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.FRANCE)
private val freshnessFormat = DateTimeFormatter.ofPattern("dd/MM HH'h'mm", Locale.FRANCE)

data class ClientFinancialRowUi(
    val documentId: String,
    val documentType: String,
    val number: String,
    val title: String?,
    val emittedAt: String,
    val statusLabel: String,
    val statusTone: String,
    val totalTtc: Double,
)

fun ClientFinancialSummaryEntity.toRowUi() = ClientFinancialRowUi(
    documentId = documentId,
    documentType = documentType,
    number = number,
    title = title,
    emittedAt = emittedAt,
    statusLabel = statusLabel,
    statusTone = statusTone,
    totalTtc = totalTtc,
)

fun ClientFinancialDocumentDto.toRowUi() = ClientFinancialRowUi(
    documentId = documentId,
    documentType = documentType,
    number = number,
    title = title,
    emittedAt = emittedAt,
    statusLabel = statusLabel,
    statusTone = statusTone,
    totalTtc = totalTtc,
)

@Composable
fun ClientFinancialOfflineSection(
    quotes: List<ClientFinancialRowUi>,
    invoices: List<ClientFinancialRowUi>,
    syncedAt: Long?,
    isPdfLoading: Boolean,
    onDocumentClick: (ClientFinancialRowUi) -> Unit,
    modifier: Modifier = Modifier,
) {
    val refonte = useSavioRefonteUi()
    val documentCount = quotes.size + invoices.size
    val content: @Composable () -> Unit = {
        FinancialSubsection(
            title = "Devis",
            items = quotes.take(4),
            emptyLabel = "Aucun devis",
            onDocumentClick = onDocumentClick,
            refonte = refonte,
        )
        HorizontalDivider(
            modifier = Modifier.padding(vertical = if (refonte) 10.dp else 8.dp),
            color = if (refonte) SavioRefonte.Line else MaterialTheme.colorScheme.outline,
        )
        FinancialSubsection(
            title = "Factures",
            items = invoices.take(4),
            emptyLabel = "Aucune facture",
            onDocumentClick = onDocumentClick,
            refonte = refonte,
        )
        syncedAt?.let { ts ->
            Text(
                text = "À jour au ${formatFreshness(ts)}",
                style = if (refonte) SavioType.BodySmall else SavioType.BodySmall,
                color = if (refonte) SavioRefonte.Muted else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 10.dp),
            )
        }
        if (isPdfLoading) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = if (refonte) 8.dp else 0.dp, bottom = if (refonte) 4.dp else 12.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                CircularProgressIndicator(modifier = Modifier.padding(4.dp))
            }
        } else if (!refonte) {
            Spacer(modifier = Modifier.padding(bottom = 12.dp))
        }
    }

    if (refonte) {
        Column(modifier = modifier.fillMaxWidth()) {
            SavioSectionHeader(title = "Financier", count = documentCount.takeIf { it > 0 })
            Spacer(modifier = Modifier.height(8.dp))
            SavioRefonteCard(contentPadding = false) {
                Column(modifier = Modifier.padding(horizontal = 15.dp, vertical = 12.dp)) {
                    content()
                }
            }
        }
        return
    }

    SectionCard(modifier = modifier) {
        Text(
            text = "Financier",
            style = SavioType.H3,
            modifier = Modifier.padding(top = 12.dp, bottom = 8.dp),
        )
        content()
    }
}

@Composable
fun ClientFinancialOnlineSection(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    items: List<ClientFinancialRowUi>,
    isLoading: Boolean,
    isLoadingMore: Boolean,
    hasMore: Boolean,
    isOffline: Boolean,
    isPdfLoading: Boolean,
    onDocumentClick: (ClientFinancialRowUi) -> Unit,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val refonte = useSavioRefonteUi()
    val body: @Composable () -> Unit = {
        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { onTabSelected(0) },
                text = { Text("Devis") },
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { onTabSelected(1) },
                text = { Text("Factures") },
            )
        }
        when {
            isOffline -> {
                Text(
                    text = "Consultation disponible en ligne uniquement",
                    style = SavioType.BodyMedium,
                    color = if (refonte) SavioRefonte.Muted else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp),
                )
            }
            isLoading && items.isEmpty() -> {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
            items.isEmpty() -> {
                Text(
                    text = if (selectedTab == 0) "Aucun devis" else "Aucune facture",
                    style = SavioType.BodySmall,
                    color = if (refonte) SavioRefonte.Muted else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 12.dp),
                )
            }
            else -> {
                Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                    items.forEachIndexed { index, item ->
                        if (index > 0) {
                            HorizontalDivider(color = if (refonte) SavioRefonte.Line else MaterialTheme.colorScheme.outline)
                        }
                        ClientFinancialDocumentRow(item = item, onClick = { onDocumentClick(item) })
                    }
                }
                if (hasMore) {
                    OutlinedButton(
                        onClick = onLoadMore,
                        enabled = !isLoadingMore,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                    ) {
                        Text(if (isLoadingMore) "Chargement…" else "Charger plus")
                    }
                }
            }
        }
        if (isPdfLoading) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = if (refonte) 4.dp else 12.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                CircularProgressIndicator(modifier = Modifier.padding(4.dp))
            }
        }
    }

    if (refonte) {
        Column(modifier = modifier.fillMaxWidth()) {
            SavioSectionHeader(title = "Financier", count = items.size.takeIf { it > 0 })
            Spacer(modifier = Modifier.height(8.dp))
            SavioRefonteCard(contentPadding = false) {
                Column(modifier = Modifier.padding(horizontal = 15.dp, vertical = 12.dp)) {
                    body()
                }
            }
        }
        return
    }

    SectionCard(modifier = modifier) {
        Text(
            text = "Financier",
            style = SavioType.H3,
            modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
        )
        body()
    }
}

@Composable
private fun FinancialSubsection(
    title: String,
    items: List<ClientFinancialRowUi>,
    emptyLabel: String,
    onDocumentClick: (ClientFinancialRowUi) -> Unit,
    refonte: Boolean,
) {
    Text(
        text = title,
        style = SavioType.Label,
        fontWeight = FontWeight.SemiBold,
        color = if (refonte) SavioRefonte.Ink else MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(bottom = 6.dp),
    )
    if (items.isEmpty()) {
        Text(
            text = emptyLabel,
            style = SavioType.BodySmall,
            color = if (refonte) SavioRefonte.Muted else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(vertical = 4.dp),
        )
        return
    }
    items.forEachIndexed { index, item ->
        if (index > 0) {
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                color = if (refonte) SavioRefonte.Line else MaterialTheme.colorScheme.outline,
            )
        }
        ClientFinancialDocumentRow(item = item, onClick = { onDocumentClick(item) })
    }
}

@Composable
fun ClientFinancialDocumentRow(
    item: ClientFinancialRowUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val refonte = useSavioRefonteUi()
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            val headline = buildString {
                append(item.number)
                item.title?.takeIf { it.isNotBlank() }?.let { append(" · $it") }
            }
            Text(
                text = headline,
                style = SavioType.BodyMedium,
                fontWeight = if (refonte) FontWeight.Medium else FontWeight.Normal,
                color = if (refonte) SavioRefonte.Ink else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = formatDocumentDate(item.emittedAt),
                style = SavioType.BodySmall,
                color = if (refonte) SavioRefonte.Muted else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        AppBadge(
            text = item.statusLabel,
            style = statusToneToBadgeStyle(item.statusTone),
        )
        Text(
            text = moneyFormat.format(item.totalTtc),
            style = SavioType.BodyMedium,
            fontWeight = FontWeight.Medium,
            color = if (refonte) SavioRefonte.Ink else MaterialTheme.colorScheme.onSurface,
        )
    }
}

private fun formatDocumentDate(isoDate: String): String =
    runCatching {
        val date =
            if (isoDate.length <= 10) {
                LocalDate.parse(isoDate.take(10))
            } else {
                Instant.parse(isoDate).atZone(ZoneId.systemDefault()).toLocalDate()
            }
        shortDateFormat.format(date)
    }.getOrElse {
        isoDate.take(10).replace('-', '/')
    }

private fun formatFreshness(epochMs: Long): String =
    runCatching {
        freshnessFormat.format(
            Instant.ofEpochMilli(epochMs).atZone(ZoneId.systemDefault()),
        )
    }.getOrElse { "" }
