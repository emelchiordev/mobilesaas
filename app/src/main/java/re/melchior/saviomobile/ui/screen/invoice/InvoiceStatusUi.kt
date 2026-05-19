package re.melchior.saviomobile.ui.screen.invoice

import androidx.compose.ui.graphics.Color
import re.melchior.saviomobile.data.local.entity.InvoiceEntity
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class InvoiceMobileStatus {
    DRAFT,
    PENDING_VALIDATION,
    ACCEPTED,
    INVOICED,
    PAID,
}

fun normalizeInvoiceStatus(raw: String): InvoiceMobileStatus =
    when (raw.lowercase(Locale.ROOT)) {
        "pending_validation" -> InvoiceMobileStatus.PENDING_VALIDATION
        "accepted" -> InvoiceMobileStatus.ACCEPTED
        "invoiced", "validated", "partial" -> InvoiceMobileStatus.INVOICED
        "paid" -> InvoiceMobileStatus.PAID
        else -> InvoiceMobileStatus.DRAFT
    }

fun InvoiceEntity.mobileStatus(): InvoiceMobileStatus = normalizeInvoiceStatus(status)

fun InvoiceEntity.isEditableMobile(): Boolean = mobileStatus() == InvoiceMobileStatus.DRAFT

fun formatInvoiceDate(iso: String?): String? {
    if (iso.isNullOrBlank()) return null
    return runCatching {
        val instant = Instant.parse(iso)
        val local = instant.atZone(ZoneId.systemDefault()).toLocalDate()
        DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.FRANCE).format(local)
    }.getOrNull()
}

data class StatusBadgeStyle(
    val label: String,
    val background: Color,
    val foreground: Color,
)

fun badgeForStatus(status: InvoiceMobileStatus): StatusBadgeStyle =
    when (status) {
        InvoiceMobileStatus.DRAFT -> StatusBadgeStyle(
            label = "Brouillon",
            background = Color(0xFFFFF3E0),
            foreground = Color(0xFFE65100),
        )
        InvoiceMobileStatus.PENDING_VALIDATION -> StatusBadgeStyle(
            label = "En attente validation",
            background = Color(0xFFFAEEDA),
            foreground = Color(0xFF633806),
        )
        InvoiceMobileStatus.ACCEPTED -> StatusBadgeStyle(
            label = "Devis signé",
            background = Color(0xFFEAF3DE),
            foreground = Color(0xFF27500A),
        )
        InvoiceMobileStatus.INVOICED -> StatusBadgeStyle(
            label = "Facture émise",
            background = Color(0xFFE6F1FB),
            foreground = Color(0xFF0C447C),
        )
        InvoiceMobileStatus.PAID -> StatusBadgeStyle(
            label = "Payée ✓",
            background = Color(0xFF27500A),
            foreground = Color(0xFFFFFFFF),
        )
    }

/** Badge affiché au technicien : uniquement si le statut lui apporte une info utile. */
fun technicianFieldBadge(status: InvoiceMobileStatus): StatusBadgeStyle? =
    when (status) {
        InvoiceMobileStatus.DRAFT,
        InvoiceMobileStatus.PENDING_VALIDATION -> null
        InvoiceMobileStatus.ACCEPTED -> StatusBadgeStyle(
            label = "Devis signé ✓",
            background = Color(0xFFEAF3DE),
            foreground = Color(0xFF27500A),
        )
        InvoiceMobileStatus.INVOICED -> StatusBadgeStyle(
            label = "Facture émise",
            background = Color(0xFFE6F1FB),
            foreground = Color(0xFF0C447C),
        )
        InvoiceMobileStatus.PAID -> StatusBadgeStyle(
            label = "Payée ✓",
            background = Color(0xFF27500A),
            foreground = Color(0xFFFFFFFF),
        )
    }

fun billingTypeBadge(label: String, billingType: String): StatusBadgeStyle? =
    when (billingType) {
        "internal" -> StatusBadgeStyle("À charge", Color(0xFFF1EFE8), Color(0xFF444441))
        "warranty" -> StatusBadgeStyle("Garantie", Color(0xFFEAF3DE), Color(0xFF27500A))
        "billable" -> StatusBadgeStyle("Facturé", Color(0xFFE8EFF7), Color(0xFF1B4F8A))
        else -> null
    }
