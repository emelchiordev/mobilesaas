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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import re.melchior.saviomobile.data.local.entity.InvoiceLineEntity
import re.melchior.saviomobile.data.remote.dto.PrestationDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceScreen(
    interventionId: String,
    onBack: () -> Unit,
    viewModel: InvoiceViewModel = hiltViewModel()
) {
    val invoice by viewModel.invoice.collectAsStateWithLifecycle()
    val lines by viewModel.lines.collectAsStateWithLifecycle()
    val payments by viewModel.payments.collectAsStateWithLifecycle()
    val remainingAmount by viewModel.remainingAmount.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val requireValidation by viewModel.requireValidation.collectAsStateWithLifecycle()
    val canValidateDirectly = !requireValidation
    val isEditable =
        invoice?.status == "draft" || invoice?.status == "pending_validation"
    var showCatalogue by remember { mutableStateOf(false) }
    var showAddFree by remember { mutableStateOf(false) }
    var showAddPayment by remember { mutableStateOf(false) }

    LaunchedEffect(interventionId) {
        viewModel.loadInvoice(interventionId)
    }

    LaunchedEffect(invoice?.id) {
        invoice?.id?.let { viewModel.loadPayments(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = invoice?.number ?: "Facture",
                        style = MaterialTheme.typography.titleMedium
                    )
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
                if (isEditable) {
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
                        Button(
                            onClick = { viewModel.submitInvoice(canValidateDirectly) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = lines.isNotEmpty()
                        ) {
                            Text(
                                if (canValidateDirectly) "Valider la facture"
                                else "Soumettre pour validation"
                            )
                        }
                    }
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(lines, key = { it.id }) { line ->
                    InvoiceLineCard(
                        line = line,
                        canEdit = isEditable,
                        onDelete = { viewModel.removeLine(line.id) }
                    )
                }

                if (isEditable) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showCatalogue = true },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    Icons.Filled.Search,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text("Catalogue", fontSize = 13.sp)
                            }
                            OutlinedButton(
                                onClick = { showAddFree = true },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    Icons.Filled.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text("Ligne libre", fontSize = 13.sp)
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
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Aucune ligne — ajoutez une prestation\nou une ligne libre",
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                if (invoice?.status == "draft") {
                    item {
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
                                TextButton(onClick = { showAddPayment = true }) {
                                    Icon(
                                        Icons.Filled.Add,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text("Encaisser", fontSize = 13.sp)
                                }
                            }

                            if (payments.isEmpty()) {
                                Text(
                                    "Aucun règlement enregistré",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            } else {
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
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                label,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                "%.2f €".format(payment.amount),
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Medium,
                                            )
                                            IconButton(
                                                onClick = { viewModel.removePayment(payment.id) },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    Icons.Filled.Close,
                                                    contentDescription = "Supprimer",
                                                    tint = MaterialTheme.colorScheme.error,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(Modifier.height(4.dp))
                                HorizontalDivider()
                                Spacer(Modifier.height(4.dp))
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
                                        color = if (remainingAmount <= 0)
                                            MaterialTheme.colorScheme.primary
                                        else
                                            MaterialTheme.colorScheme.error,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCatalogue) {
        CatalogueBottomSheet(
            viewModel = viewModel,
            onSelect = { item ->
                viewModel.addLineFromCatalogue(item)
                showCatalogue = false
            },
            onDismiss = { showCatalogue = false }
        )
    }

    if (showAddFree) {
        AddFreeLineDialog(
            onConfirm = { label, qty, price, vat ->
                viewModel.addFreeLine(label, qty, price, vat)
                showAddFree = false
            },
            onDismiss = { showAddFree = false }
        )
    }

    if (showAddPayment) {
        AddPaymentDialog(
            totalTtc = invoice?.totalTtc ?: 0.0,
            remainingAmount = remainingAmount,
            onConfirm = { amount, code ->
                viewModel.addPayment(amount, code)
                showAddPayment = false
            },
            onDismiss = { showAddPayment = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPaymentDialog(
    totalTtc: Double,
    remainingAmount: Double,
    onConfirm: (amount: Double, paymentMethodCode: String) -> Unit,
    onDismiss: () -> Unit,
) {
    var amount by remember {
        mutableStateOf("%.2f".format(remainingAmount.coerceAtLeast(0.0)))
    }
    var selectedMethod by remember { mutableStateOf("cash") }

    val paymentMethods = listOf(
        "cash" to "Espèces",
        "card" to "CB",
        "check" to "Chèque",
        "transfer" to "Virement",
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Encaisser un règlement") },
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
                    val amt = amount.toDoubleOrNull() ?: return@Button
                    if (amt > 0) onConfirm(amt, selectedMethod)
                },
                enabled = amount.toDoubleOrNull()?.let { it > 0 } == true,
            ) {
                Text("Encaisser")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        },
    )
}

@Composable
fun InvoiceLineCard(
    line: InvoiceLineEntity,
    canEdit: Boolean,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                line.reference?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Text(
                    line.label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    "%.0f × %.2f € HT — TVA %.0f%%".format(
                        line.quantity,
                        line.unitPriceHt,
                        line.vatRate
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "%.2f €".format(line.totalHt),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                if (canEdit) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = "Supprimer",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
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
                                buildString {
                                    append(item.reference)
                                    item.unitPriceHt?.let { append(" — %.2f € HT".format(it)) }
                                    append(" — TVA %.0f%%".format(item.vatRate))
                                }
                            )
                        },
                        leadingContent = {
                            Surface(
                                color = if (item.type == "prestation")
                                    MaterialTheme.colorScheme.primaryContainer
                                else
                                    MaterialTheme.colorScheme.secondaryContainer,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    if (item.type == "prestation") "P" else "A",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (item.type == "prestation")
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    else
                                        MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        },
                        modifier = Modifier.clickable { onSelect(item) }
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
fun AddFreeLineDialog(
    onConfirm: (label: String, qty: Double, price: Double, vat: Double) -> Unit,
    onDismiss: () -> Unit
) {
    val vatRates = listOf("0", "5.5", "10", "20")
    var vatExpanded by remember { mutableStateOf(false) }
    var label by remember { mutableStateOf("") }
    var qty by remember { mutableStateOf("1") }
    var price by remember { mutableStateOf("") }
    var vat by remember { mutableStateOf("20") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ligne libre") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(
                        label,
                        qty.toDoubleOrNull() ?: 1.0,
                        price.toDoubleOrNull() ?: 0.0,
                        vat.toDoubleOrNull() ?: 20.0
                    )
                },
                enabled = label.isNotBlank() && price.isNotBlank()
            ) { Text("Ajouter") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
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
