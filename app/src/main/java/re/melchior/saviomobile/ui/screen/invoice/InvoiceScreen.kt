package re.melchior.saviomobile.ui.screen.invoice

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable
import re.melchior.saviomobile.data.local.entity.InvoiceEntity
import re.melchior.saviomobile.data.local.entity.InvoiceLineEntity
import re.melchior.saviomobile.data.remote.dto.PrestationDto
import re.melchior.saviomobile.ui.theme.savioTopAppBarColors
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceScreen(
    interventionId: String,
    onBack: () -> Unit,
    onNavigateToDevisSignature: () -> Unit,
    viewModel: InvoiceViewModel = hiltViewModel(),
) {
    val invoice by viewModel.invoice.collectAsStateWithLifecycle()
    val lines by viewModel.lines.collectAsStateWithLifecycle()
    val payments by viewModel.payments.collectAsStateWithLifecycle()
    val remainingAmount by viewModel.remainingAmount.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val showEmitConfirmDialog by viewModel.showEmitConfirmDialog.collectAsStateWithLifecycle()
    val showEmitAdjustmentsSheet by viewModel.showEmitAdjustmentsSheet.collectAsStateWithLifecycle()
    val emitSuccessMessage by viewModel.emitSuccessMessage.collectAsStateWithLifecycle()
    val paymentSuccessMessage by viewModel.paymentSuccessMessage.collectAsStateWithLifecycle()
    val devisResignRequiredMessage by viewModel.devisResignRequiredMessage.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val mobileStatus = invoice?.mobileStatus()
    val isEditable = invoice?.isEditableMobile() == true
    val showPaymentsSection =
        mobileStatus == InvoiceMobileStatus.DRAFT ||
            mobileStatus == InvoiceMobileStatus.INVOICED ||
            mobileStatus == InvoiceMobileStatus.PAID
    val showPaymentsEditable =
        mobileStatus == InvoiceMobileStatus.DRAFT ||
            mobileStatus == InvoiceMobileStatus.INVOICED
    var showCatalogue by remember { mutableStateOf(false) }
    var catalogueItemToEdit by remember { mutableStateOf<PrestationDto?>(null) }
    var showAddFree by remember { mutableStateOf(false) }
    var showAddComment by remember { mutableStateOf(false) }
    var showAddPayment by remember { mutableStateOf(false) }
    var paymentDialogSettleMode by remember { mutableStateOf(false) }
    val sortedLines = remember(lines) { lines.sortedBy { it.order } }

    LaunchedEffect(interventionId) {
        viewModel.loadInvoice(interventionId)
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.resetEmitUiState() }
    }

    LaunchedEffect(emitSuccessMessage) {
        emitSuccessMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.dismissEmitSuccessMessage()
        }
    }

    LaunchedEffect(paymentSuccessMessage) {
        paymentSuccessMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.dismissPaymentSuccessMessage()
        }
    }

    LaunchedEffect(devisResignRequiredMessage) {
        devisResignRequiredMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg, duration = SnackbarDuration.Long)
            viewModel.dismissDevisResignMessage()
        }
    }

    LaunchedEffect(invoice?.id) {
        invoice?.id?.let { viewModel.loadPayments(it) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                colors = savioTopAppBarColors(),
                title = {
                    Column {
                        Text(
                            text = invoice?.number ?: "Facturation",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        invoice?.let { inv ->
                            technicianFieldBadge(inv.mobileStatus())?.let { badge ->
                                Text(
                                    text = badge.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = badge.foreground,
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        },
        bottomBar = {
            invoice?.let { inv ->
                when (mobileStatus) {
                InvoiceMobileStatus.DRAFT -> {
                    val vatDetails = lines
                        .filter {
                            it.billingType == "billable" &&
                                !it.isTextBlock &&
                                it.type != "subtotal" &&
                                it.type != "text_block"
                        }
                        .groupBy { it.vatRate }
                        .map { (rate, lineList) ->
                            val base = lineList.sumOf { it.totalHt }
                            val amount = base * rate / 100.0
                            Triple(rate, base, amount)
                        }
                        .filter { it.second > 0 }
                        .sortedBy { it.first }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(16.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "Total HT",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    "%.2f €".format(inv.totalHt),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                            vatDetails.forEach { (rate, base, amount) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        "TVA ${if (rate % 1 == 0.0) rate.toInt() else rate}%" +
                                            " (base ${"%.2f".format(base)} €)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Text(
                                        "%.2f €".format(amount),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                Spacer(Modifier.height(2.dp))
                            }
                            Spacer(Modifier.height(4.dp))
                            HorizontalDivider()
                            Spacer(Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "Total TTC",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "%.2f €".format(inv.totalTtc),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = onNavigateToDevisSignature,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = lines.isNotEmpty(),
                        ) {
                            Text("Faire signer le devis")
                        }
                    }
                }
                InvoiceMobileStatus.INVOICED -> {
                    Column(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .navigationBarsPadding()
                                .padding(16.dp),
                    ) {
                        formatInvoiceDate(inv.invoicedAt)?.let { date ->
                            Text(
                                "Facture émise le $date",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                        if (remainingAmount > 0) {
                            Button(
                                onClick = {
                                    paymentDialogSettleMode = true
                                    showAddPayment = true
                                },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(
                                    "Solder la facture (%.2f €)".format(remainingAmount),
                                )
                            }
                        }
                    }
                }
                InvoiceMobileStatus.PENDING_VALIDATION -> {
                    Column(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .navigationBarsPadding()
                                .padding(16.dp),
                    ) {
                        Text(
                            "Facture soumise au responsable pour validation.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                InvoiceMobileStatus.ACCEPTED -> {
                    Column(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .navigationBarsPadding()
                                .padding(16.dp),
                    ) {
                        formatInvoiceDate(inv.acceptedAt)?.let { date ->
                            Text(
                                "Devis signé le $date",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                        InvoiceSignaturesBlock(invoice = inv)
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = { viewModel.onEmitInvoiceClicked() },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isLoading,
                        ) {
                            Text("Convertir en facture")
                        }
                    }
                }
                InvoiceMobileStatus.PAID -> {
                    Column(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .navigationBarsPadding()
                                .padding(16.dp),
                    ) {
                        formatInvoiceDate(inv.paidAt)?.let { date ->
                            Text(
                                "Payée le $date",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF27500A),
                            )
                        }
                    }
                }
                else -> Unit
                }
            }
        }
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (invoice == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val lazyListState = rememberLazyListState()
            val reorderState = rememberReorderableLazyListState(
                onMove = { from, to ->
                    viewModel.moveLine(from.index, to.index)
                },
            )
            val columnState = if (isEditable) reorderState.listState else lazyListState
            val columnModifier =
                if (isEditable) {
                    Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .reorderable(reorderState)
                } else {
                    Modifier.fillMaxSize().padding(padding)
                }

            LazyColumn(
                state = columnState,
                modifier = columnModifier,
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(sortedLines, key = { it.id }) { line ->
                    if (isEditable) {
                        ReorderableItem(reorderState, key = line.id) { isDragging ->
                            InvoiceLineRow(
                                line = line,
                                isDragging = isDragging,
                                showDragHandle = true,
                                reorderModifier = Modifier.detectReorderAfterLongPress(reorderState),
                                canEdit = true,
                                onDelete = { viewModel.removeLine(line.id) },
                            )
                        }
                    } else {
                        InvoiceLineRow(
                            line = line,
                            isDragging = false,
                            showDragHandle = false,
                            reorderModifier = Modifier,
                            canEdit = false,
                            onDelete = { viewModel.removeLine(line.id) },
                        )
                    }
                }

                if (isEditable) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            OutlinedButton(
                                onClick = { showCatalogue = true },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                            ) {
                                Icon(
                                    Icons.Filled.Search,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                )
                                Spacer(Modifier.width(4.dp))
                                Text("Catalogue", fontSize = 12.sp, maxLines = 1)
                            }
                            OutlinedButton(
                                onClick = { showAddComment = true },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                            ) {
                                Icon(
                                    Icons.Filled.ChatBubbleOutline,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                )
                                Spacer(Modifier.width(4.dp))
                                Text("Texte", fontSize = 12.sp, maxLines = 1)
                            }
                            OutlinedButton(
                                onClick = { showAddFree = true },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                            ) {
                                Icon(
                                    Icons.Filled.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                )
                                Spacer(Modifier.width(4.dp))
                                Text("Ligne libre", fontSize = 12.sp, maxLines = 1)
                            }
                        }
                    }
                }

                if (lines.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                "Aucune ligne — catalogue, texte\nou ligne libre",
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                if (showPaymentsSection) {
                    item {
                        val paidGreen = Color(0xFF27500A)
                        val totalCollected = payments.sumOf { it.amount }
                        Column(Modifier.fillMaxWidth()) {
                            Spacer(Modifier.height(8.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 0.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    "Règlements",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                if (showPaymentsEditable) {
                                    TextButton(
                                        onClick = {
                                            paymentDialogSettleMode = false
                                            showAddPayment = true
                                        },
                                    ) {
                                        Icon(
                                            Icons.Filled.Add,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text("Encaisser", fontSize = 13.sp)
                                    }
                                }
                            }

                            if (payments.isEmpty() && showPaymentsEditable) {
                                Text(
                                    "Aucun règlement enregistré",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(vertical = 8.dp),
                                )
                            } else if (payments.isNotEmpty()) {
                                payments.forEach { payment ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        ) {
                                            val (icon, label) = when (payment.paymentMethodCode) {
                                                "cash" -> Pair(Icons.Filled.AttachMoney, "Espèces")
                                                "card" -> Pair(Icons.Filled.CreditCard, "CB")
                                                "check" -> Pair(Icons.Filled.Description, "Chèque")
                                                "transfer" -> Pair(Icons.Filled.SwapHoriz, "Virement")
                                                else -> Pair(Icons.Filled.Payment, payment.paymentMethodCode)
                                            }
                                            Icon(
                                                icon,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp),
                                                tint = MaterialTheme.colorScheme.primary,
                                            )
                                            Text(
                                                label,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        }
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        ) {
                                            Text(
                                                "%.2f €".format(payment.amount),
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Medium,
                                            )
                                            if (showPaymentsEditable) {
                                                IconButton(
                                                    onClick = { viewModel.removePayment(payment.id) },
                                                    modifier = Modifier.size(28.dp),
                                                ) {
                                                    Icon(
                                                        Icons.Filled.Close,
                                                        contentDescription = "Supprimer",
                                                        tint = MaterialTheme.colorScheme.error,
                                                        modifier = Modifier.size(16.dp),
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            if (payments.isNotEmpty() || showPaymentsEditable) {
                                Spacer(Modifier.height(4.dp))
                                HorizontalDivider()
                                Spacer(Modifier.height(4.dp))
                            }

                            when {
                                mobileStatus == InvoiceMobileStatus.PAID -> {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                    ) {
                                        Text(
                                            "Total encaissé",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium,
                                        )
                                        Text(
                                            "%.2f €".format(totalCollected),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                        )
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        "Facture soldée",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = paidGreen,
                                    )
                                }
                                remainingAmount > 0 -> {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                    ) {
                                        Text(
                                            "Reste à payer",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium,
                                        )
                                        Text(
                                            "%.2f €".format(remainingAmount),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.error,
                                        )
                                    }
                                }
                                else -> {
                                    Text(
                                        "Soldée",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = paidGreen,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showEmitConfirmDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissEmitConfirmDialog() },
            title = { Text("Convertir en facture ?") },
            text = {
                Text(
                    "Le devis sera converti en facture. Cette action est irréversible.",
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmEmitDialog() }) {
                    Text("Convertir")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissEmitConfirmDialog() }) {
                    Text("Annuler")
                }
            },
        )
    }

    val invoiceForEmitSheet = invoice
    if (showEmitAdjustmentsSheet && invoiceForEmitSheet != null) {
        EmitInvoiceAdjustmentsSheet(
            invoice = invoiceForEmitSheet,
            lines = sortedLines,
            isLoading = isLoading,
            canEmit = invoiceForEmitSheet.mobileStatus() == InvoiceMobileStatus.ACCEPTED,
            onAddLine = { showCatalogue = true },
            onAddFreeLine = { showAddFree = true },
            onEmit = { viewModel.emitInvoice() },
            onDismiss = { viewModel.dismissEmitAdjustmentsSheet() },
        )
    }

    if (showCatalogue) {
        CatalogueBottomSheet(
            viewModel = viewModel,
            onSelect = { item ->
                catalogueItemToEdit = item
                showCatalogue = false
            },
            onDismiss = { showCatalogue = false },
        )
    }

    catalogueItemToEdit?.let { item ->
        AddCatalogueLineBottomSheet(
            item = item,
            onConfirm = { reference, label, qty, price, vat, billingType ->
                viewModel.addLineFromCatalogue(
                    reference = reference,
                    label = label,
                    quantity = qty,
                    unitPriceHt = price,
                    vatRate = vat,
                    billingType = billingType,
                    catalogLineType = item.type,
                )
                catalogueItemToEdit = null
            },
            onDismiss = { catalogueItemToEdit = null },
        )
    }

    if (showAddComment) {
        AddTextBlockDialog(
            onConfirm = { text ->
                viewModel.addTextBlockLine(text)
                showAddComment = false
            },
            onDismiss = { showAddComment = false },
        )
    }

    if (showAddFree) {
        AddFreeLineDialog(
            onConfirm = { reference, label, qty, price, vat, billingType ->
                viewModel.addFreeLine(reference, label, qty, price, vat, billingType)
                showAddFree = false
            },
            onDismiss = { showAddFree = false },
        )
    }

    if (showAddPayment) {
        AddPaymentDialog(
            totalTtc = invoice?.totalTtc ?: 0.0,
            remainingAmount = remainingAmount,
            settleFullBalance = paymentDialogSettleMode,
            onConfirm = { amount, code ->
                viewModel.addPayment(amount, code)
                showAddPayment = false
                paymentDialogSettleMode = false
            },
            onDismiss = {
                showAddPayment = false
                paymentDialogSettleMode = false
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPaymentDialog(
    totalTtc: Double,
    remainingAmount: Double,
    settleFullBalance: Boolean = false,
    onConfirm: (amount: Double, paymentMethodCode: String) -> Unit,
    onDismiss: () -> Unit,
) {
    var amount by remember(remainingAmount) {
        mutableStateOf(formatDecimalInput(remainingAmount.coerceAtLeast(0.0)))
    }
    var selectedMethod by remember { mutableStateOf("cash") }

    val parsedAmount = parseDecimalInput(amount)
    val canConfirm = parsedAmount != null && parsedAmount > 0 && selectedMethod.isNotBlank()

    val paymentMethods = listOf(
        "cash" to "Espèces",
        "card" to "CB",
        "check" to "Chèque",
        "transfer" to "Virement",
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (settleFullBalance) {
                    "Solder la facture"
                } else {
                    "Encaisser un règlement"
                },
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Total TTC : %.2f €".format(totalTtc),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (remainingAmount > 0) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text(
                            "Reste à payer : %.2f €".format(remainingAmount),
                            modifier = Modifier.padding(
                                horizontal = 12.dp, vertical = 8.dp
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Montant (€) *") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal
                    ),
                    singleLine = true,
                    suffix = { Text("€") },
                )

                Text(
                    "Mode de paiement",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    paymentMethods.forEach { (code, label) ->
                        FilterChip(
                            selected = selectedMethod == code,
                            onClick = { selectedMethod = code },
                            label = { Text(label, fontSize = 12.sp) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = parsedAmount ?: return@Button
                    onConfirm(amt, selectedMethod)
                },
                enabled = canConfirm,
            ) {
                Text(if (settleFullBalance) "Solder" else "Encaisser")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        },
    )
}

private fun isTextBlockLine(line: InvoiceLineEntity): Boolean =
    line.isTextBlock || line.type == "text_block"

@Composable
private fun InvoiceLineRow(
    line: InvoiceLineEntity,
    isDragging: Boolean,
    showDragHandle: Boolean,
    reorderModifier: Modifier,
    canEdit: Boolean,
    onDelete: () -> Unit,
) {
    if (isTextBlockLine(line)) {
        TextBlockLineCard(
            line = line,
            isDragging = isDragging,
            showDragHandle = showDragHandle,
            reorderModifier = reorderModifier,
            canEdit = canEdit,
            onDelete = onDelete,
        )
    } else {
        InvoiceLineCard(
            line = line,
            isDragging = isDragging,
            showDragHandle = showDragHandle,
            reorderModifier = reorderModifier,
            canEdit = canEdit,
            onDelete = onDelete,
        )
    }
}

@Composable
private fun TextBlockLineCard(
    line: InvoiceLineEntity,
    isDragging: Boolean,
    showDragHandle: Boolean,
    reorderModifier: Modifier,
    canEdit: Boolean,
    onDelete: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isDragging) {
                Color(0xFFE8E8E8)
            } else {
                Color(0xFFF3F3F3)
            },
        ),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (showDragHandle) {
                Icon(
                    imageVector = Icons.Filled.DragHandle,
                    contentDescription = "Déplacer",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = reorderModifier
                        .size(24.dp)
                        .padding(end = 4.dp),
                )
            }
            Icon(
                imageVector = Icons.Filled.ChatBubbleOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = line.label,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (canEdit) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Supprimer",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

@Composable
fun InvoiceLineCard(
    line: InvoiceLineEntity,
    isDragging: Boolean = false,
    showDragHandle: Boolean = false,
    reorderModifier: Modifier = Modifier,
    canEdit: Boolean,
    onDelete: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isDragging) {
                MaterialTheme.colorScheme.surfaceContainerHigh
            } else {
                MaterialTheme.colorScheme.surface
            },
        ),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (showDragHandle) {
                Icon(
                    imageVector = Icons.Filled.DragHandle,
                    contentDescription = "Déplacer",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = reorderModifier
                        .size(24.dp)
                        .padding(end = 4.dp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                line.reference?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = FontFamily.Monospace,
                    )
                }
                Text(
                    line.label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    "%.0f × %.2f € HT — TVA %.0f%%".format(
                        line.quantity,
                        line.unitPriceHt,
                        line.vatRate,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "%.2f €".format(line.totalHt),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                )
                if (canEdit) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = "Supprimer",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AddTextBlockDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var text by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Commentaire") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                placeholder = { Text("Saisissez votre commentaire...") },
                minLines = 4,
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(text.trim()) },
                enabled = text.isNotBlank(),
            ) {
                Text("Ajouter")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogueBottomSheet(
    viewModel: InvoiceViewModel,
    onSelect: (PrestationDto) -> Unit,
    onDismiss: () -> Unit
) {
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    var query by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Catalogue",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            OutlinedTextField(
                value = query,
                onValueChange = {
                    query = it
                    viewModel.searchCatalogue(it)
                },
                placeholder = { Text("Rechercher ENT-GAZ, Entretien...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Filled.Search, contentDescription = null)
                }
            )
            Spacer(Modifier.height(8.dp))

            LazyColumn {
                items(searchResults) { item ->
                    ListItem(
                        headlineContent = { Text(item.label) },
                        supportingContent = {
                            Text(
                                item.reference,
                                fontFamily = FontFamily.Monospace,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        },
                        leadingContent = {
                            CatalogueRefBadge(item = item)
                        },
                        modifier = Modifier.clickable { onSelect(item) },
                    )
                    HorizontalDivider()
                }

                if (query.length >= 2 && searchResults.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Aucun résultat pour \"$query\"",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCatalogueLineBottomSheet(
    item: PrestationDto,
    onConfirm: (
        reference: String,
        label: String,
        qty: Double,
        price: Double,
        vat: Double,
        billingType: String,
    ) -> Unit,
    onDismiss: () -> Unit,
) {
    val vatRates = listOf("0", "5.5", "10", "20")
    var vatExpanded by remember { mutableStateOf(false) }
    var label by remember(item) { mutableStateOf(item.label) }
    var qty by remember { mutableStateOf("1") }
    var price by remember(item) {
        mutableStateOf(
            item.unitPriceHt?.let { formatDecimalInput(it) }.orEmpty(),
        )
    }
    var vat by remember(item) {
        mutableStateOf(vatRateLabelFor(item.vatRate, vatRates))
    }
    var billingType by remember { mutableStateOf("billable") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text("Ajouter une ligne", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = item.reference,
                onValueChange = {},
                readOnly = true,
                enabled = false,
                label = { Text("Référence") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            OutlinedTextField(
                value = label,
                onValueChange = { label = it },
                label = { Text("Désignation *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = qty,
                    onValueChange = { qty = it },
                    label = { Text("Quantité") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("PU HT (€) *") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                )
            }
            ExposedDropdownMenuBox(
                expanded = vatExpanded,
                onExpandedChange = { vatExpanded = !vatExpanded },
            ) {
                OutlinedTextField(
                    value = "$vat %",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("TVA") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = vatExpanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(
                            type = MenuAnchorType.PrimaryNotEditable,
                            enabled = true,
                        ),
                )
                ExposedDropdownMenu(
                    expanded = vatExpanded,
                    onDismissRequest = { vatExpanded = false },
                ) {
                    vatRates.forEach { rate ->
                        DropdownMenuItem(
                            text = { Text("$rate %") },
                            onClick = {
                                vat = rate
                                vatExpanded = false
                            },
                        )
                    }
                }
            }
            Text(
                "Type de ligne",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            BillingTypeChips(
                selected = billingType,
                onSelected = { billingType = it },
            )
            Button(
                onClick = {
                    onConfirm(
                        item.reference,
                        label.trim(),
                        parseDecimalInput(qty) ?: 1.0,
                        parseDecimalInput(price) ?: 0.0,
                        parseDecimalInput(vat) ?: item.vatRate,
                        billingType,
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = label.isNotBlank() && parseDecimalInput(price) != null,
            ) {
                Text("Ajouter à la facture")
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFreeLineDialog(
    onConfirm: (
        reference: String?,
        label: String,
        qty: Double,
        price: Double,
        vat: Double,
        billingType: String,
    ) -> Unit,
    onDismiss: () -> Unit,
) {
    val vatRates = listOf("0", "5.5", "10", "20")
    var vatExpanded by remember { mutableStateOf(false) }
    var reference by remember { mutableStateOf("") }
    var label by remember { mutableStateOf("") }
    var qty by remember { mutableStateOf("1") }
    var price by remember { mutableStateOf("") }
    var vat by remember { mutableStateOf("20") }
    var billingType by remember { mutableStateOf("billable") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ligne libre") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = reference,
                    onValueChange = { reference = it },
                    label = { Text("Référence") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Désignation *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = qty,
                        onValueChange = { qty = it },
                        label = { Text("Qté") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = price,
                        onValueChange = { price = it },
                        label = { Text("PU HT (€) *") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                }
                ExposedDropdownMenuBox(
                    expanded = vatExpanded,
                    onExpandedChange = { vatExpanded = !vatExpanded }
                ) {
                    OutlinedTextField(
                        value = "$vat %",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("TVA") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = vatExpanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(
                                type = MenuAnchorType.PrimaryNotEditable,
                                enabled = true
                            ),
                    )
                    ExposedDropdownMenu(
                        expanded = vatExpanded,
                        onDismissRequest = { vatExpanded = false }
                    ) {
                        vatRates.forEach { rate ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "$rate %",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                },
                                onClick = {
                                    vat = rate
                                    vatExpanded = false
                                }
                            )
                        }
                    }
                }
                Text(
                    "Type de ligne",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                BillingTypeChips(
                    selected = billingType,
                    onSelected = { billingType = it },
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(
                        reference.trim().ifBlank { null },
                        label.trim(),
                        parseDecimalInput(qty) ?: 1.0,
                        parseDecimalInput(price) ?: 0.0,
                        parseDecimalInput(vat) ?: 20.0,
                        billingType,
                    )
                },
                enabled = label.isNotBlank() && parseDecimalInput(price) != null,
            ) { Text("Ajouter") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}

@Composable
private fun CatalogueRefBadge(item: PrestationDto) {
    val (bg, fg, text) = when (item.type) {
        "prestation" -> Triple(Color(0xFFFFF3E0), Color(0xFFE65100), "P")
        "piece" -> Triple(Color(0xFFEAF3DE), Color(0xFF27500A), "P")
        else -> {
            val marque = item.marque?.trim().orEmpty()
            if (marque.isNotEmpty()) {
                Triple(Color(0xFFE8EFF7), Color(0xFF1B4F8A), marque)
            } else {
                Triple(Color(0xFFE8EFF7), Color(0xFF1B4F8A), "—")
            }
        }
    }
    Surface(
        color = bg,
        shape = RoundedCornerShape(4.dp),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = fg,
            maxLines = 1,
        )
    }
}

@Composable
private fun BillingTypeChips(
    selected: String,
    onSelected: (String) -> Unit,
) {
    val options = listOf(
        "billable" to "Facturé",
        "internal" to "À charge",
        "warranty" to "Garantie",
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEach { (code, label) ->
            FilterChip(
                selected = selected == code,
                onClick = { onSelected(code) },
                label = { Text(label, fontSize = 12.sp) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

private fun formatDecimalInput(value: Double): String =
    String.format(Locale.US, "%.2f", value)

private fun parseDecimalInput(raw: String): Double? {
    val normalized = raw.trim().replace(',', '.')
    if (normalized.isEmpty()) return null
    return normalized.toDoubleOrNull()
}

private fun vatRateLabelFor(rate: Double, options: List<String>): String {
    val match = options.firstOrNull { opt ->
        val optValue = opt.toDoubleOrNull() ?: return@firstOrNull false
        kotlin.math.abs(optValue - rate) < 0.01
    }
    return match ?: if (rate <= 0.0) "0" else "20"
}

@Composable
fun InvoiceStatusBadge(status: String) {
    val (bg, fg, labelText) = when (status) {
        "draft" -> Triple(Color(0xFFF1EFE8), Color(0xFF444441), "Brouillon")
        "pending_validation" -> Triple(Color(0xFFFAEEDA), Color(0xFF633806), "En validation")
        "validated" -> Triple(Color(0xFFE6F1FB), Color(0xFF0C447C), "Validée")
        "partial" -> Triple(Color(0xFFFAEEDA), Color(0xFF633806), "Partielle")
        "paid" -> Triple(Color(0xFFEAF3DE), Color(0xFF27500A), "Payée")
        "cancelled" -> Triple(Color(0xFFFCEBEB), Color(0xFF791F1F), "Annulée")
        else -> Triple(Color(0xFFF1EFE8), Color(0xFF444441), status)
    }
    Surface(
        color = bg,
        shape = RoundedCornerShape(20.dp)
    ) {
        Text(
            labelText,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = fg,
            fontWeight = FontWeight.Medium
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmitInvoiceAdjustmentsSheet(
    invoice: InvoiceEntity,
    lines: List<InvoiceLineEntity>,
    isLoading: Boolean,
    canEmit: Boolean,
    onAddLine: () -> Unit,
    onAddFreeLine: () -> Unit,
    onEmit: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .navigationBarsPadding()
                    .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                "Ajustements avant conversion",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                "Vous pouvez ajouter des lignes supplémentaires avant de convertir en facture. " +
                    "Toute modification après signature du devis impose une nouvelle signature client " +
                    "(la précédente sera remplacée).",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            lines.forEach { line ->
                if (line.isTextBlock || line.type == "text_block") {
                    Text(line.label, style = MaterialTheme.typography.bodyMedium)
                } else if (line.type != "subtotal") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            line.label,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Text(
                            "%.2f €".format(line.totalHt * (1 + line.vatRate / 100.0)),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
            OutlinedButton(onClick = onAddLine, modifier = Modifier.fillMaxWidth()) {
                Text("+ Ajouter une ligne")
            }
            OutlinedButton(onClick = onAddFreeLine, modifier = Modifier.fillMaxWidth()) {
                Text("+ Ligne libre")
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    "Total TTC",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    "%.2f €".format(invoice.totalTtc),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading,
                ) {
                    Text("Annuler")
                }
                Button(
                    onClick = onEmit,
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading && canEmit,
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.height(20.dp),
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text("Convertir en facture")
                    }
                }
            }
        }
    }
}

@Composable
fun InvoiceSignaturesBlock(invoice: re.melchior.saviomobile.data.local.entity.InvoiceEntity) {
    Text(
        "Signatures",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
    )
    Spacer(Modifier.height(8.dp))
    Text(
        "Signature client — Bon pour accord",
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    invoice.devisSignatureUrl?.let { url ->
        Spacer(Modifier.height(4.dp))
        AsyncImage(
            model = url,
            contentDescription = "Signature devis",
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(120.dp),
        )
    }
    Spacer(Modifier.height(12.dp))
    if (invoice.hamonRequested) {
        Text(
            "Renonciation droit de rétractation",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        invoice.hamonSignatureUrl?.let { url ->
            Spacer(Modifier.height(4.dp))
            AsyncImage(
                model = url,
                contentDescription = "Signature Hamon",
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(120.dp),
            )
        }
    } else {
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(8.dp),
        ) {
            Text(
                "Droit de rétractation conservé (14 jours)",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
