package re.melchior.saviomobile.data.repository

import re.melchior.saviomobile.data.local.entity.InvoiceEntity

/** Preuve locale de parcours devis (signature ou acceptation), pas le statut d'émission. */
fun invoiceHasDevisEvidence(invoice: InvoiceEntity): Boolean {
    if (!invoice.devisSignatureUrl.isNullOrBlank()) return true
    if (!invoice.acceptedAt.isNullOrBlank()) return true
    return false
}

/** Type document explicite dans le snapshot CLOSE_INTERVENTION. */
fun resolveClosureDocumentType(invoice: InvoiceEntity): String {
    if (!invoiceHasDevisEvidence(invoice)) return "invoice"
    if (invoice.status in setOf("invoiced", "paid")) return "invoice"
    return "quote"
}

/**
 * Statut envoyé dans CLOSE_INTERVENTION — dérivé du paramètre technicien pour facture directe.
 *
 * - Facture directe + validation requise → `draft`
 * - Facture directe sans validation → `invoiced` (émission à la clôture)
 * - Devis signé non converti → `accepted`
 * - Devis converti localement → `invoiced`
 */
fun resolveClosureSubmitStatus(
    invoice: InvoiceEntity,
    requireInvoiceValidation: Boolean,
): String {
    if (!invoiceHasDevisEvidence(invoice)) {
        return if (requireInvoiceValidation) "draft" else "invoiced"
    }
    if (invoice.status in setOf("invoiced", "paid")) {
        return "invoiced"
    }
    return "accepted"
}
