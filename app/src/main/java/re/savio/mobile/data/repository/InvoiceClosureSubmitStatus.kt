package re.savio.mobile.data.repository

import re.savio.mobile.data.local.entity.InvoiceEntity

/** Preuve locale de parcours devis (signature ou acceptation), pas le statut d'émission. */
fun invoiceHasDevisEvidence(invoice: InvoiceEntity): Boolean {
    if (!invoice.devisSignatureUrl.isNullOrBlank()) return true
    if (!invoice.acceptedAt.isNullOrBlank()) return true
    return false
}

/** Type document explicite dans le snapshot CLOSE_INTERVENTION. */
fun resolveClosureDocumentType(invoice: InvoiceEntity): String {
    val stored = invoice.documentType.trim().lowercase()
    return if (stored == "quote") "quote" else "invoice"
}

/**
 * Statut envoyé dans CLOSE_INTERVENTION — dérivé du documentType stocké + statut local converti.
 *
 * - Devis signé (`quote`) → `accepted`
 * - Devis converti localement (`invoice` + invoiced/paid) → `invoiced`
 * - Facture directe → `draft` ou `invoiced` selon validation technicien
 */
fun resolveClosureSubmitStatus(
    invoice: InvoiceEntity,
    requireInvoiceValidation: Boolean,
): String {
    if (resolveClosureDocumentType(invoice) == "quote") {
        return "accepted"
    }
    if (invoice.status in setOf("invoiced", "paid")) {
        return "invoiced"
    }
    return if (requireInvoiceValidation) "draft" else "invoiced"
}
