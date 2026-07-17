package re.savio.mobile.data.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import re.savio.mobile.data.local.entity.InvoiceEntity

class InvoiceClosureSubmitStatusTest {

    private fun invoice(
        status: String = "draft",
        devisSignatureUrl: String? = null,
        acceptedAt: String? = null,
        documentType: String = "invoice",
    ) = InvoiceEntity(
        id = "inv-1",
        interventionId = "int-1",
        status = status,
        number = null,
        totalHt = 100.0,
        totalVat = 20.0,
        totalTtc = 120.0,
        emittedAt = null,
        dueAt = null,
        customerEmail = null,
        notes = null,
        createdAt = "2026-06-04T10:00:00Z",
        updatedAt = "2026-06-04T10:00:00Z",
        devisSignatureUrl = devisSignatureUrl,
        acceptedAt = acceptedAt,
        documentType = documentType,
    )

    @Test
    fun hasDevisEvidence_falseForDirectDraft() {
        assertFalse(invoiceHasDevisEvidence(invoice()))
    }

    @Test
    fun hasDevisEvidence_trueWithSignature() {
        assertTrue(invoiceHasDevisEvidence(invoice(devisSignatureUrl = "file:///sig.png")))
    }

    @Test
    fun hasDevisEvidence_ignoresPrematureInvoicedStatus() {
        assertFalse(invoiceHasDevisEvidence(invoice(status = "invoiced")))
    }

    @Test
    fun resolveClosureDocumentType_readsStoredInvoice() {
        assertEquals("invoice", resolveClosureDocumentType(invoice()))
    }

    @Test
    fun resolveClosureDocumentType_readsStoredQuote() {
        assertEquals(
            "quote",
            resolveClosureDocumentType(invoice(documentType = "quote")),
        )
    }

    @Test
    fun resolveClosureDocumentType_ignoresSignatureWhenTypeIsInvoice() {
        assertEquals(
            "invoice",
            resolveClosureDocumentType(
                invoice(
                    documentType = "invoice",
                    devisSignatureUrl = "file:///sig.png",
                    acceptedAt = "2026-06-04T10:00:00Z",
                ),
            ),
        )
    }

    @Test
    fun resolveClosureDocumentType_convertedDevisIsInvoice() {
        assertEquals(
            "invoice",
            resolveClosureDocumentType(
                invoice(
                    status = "invoiced",
                    documentType = "invoice",
                    devisSignatureUrl = "file:///sig.png",
                    acceptedAt = "2026-06-04T10:00:00Z",
                ),
            ),
        )
    }

    @Test
    fun resolveClosureSubmitStatus_directWithValidationIsDraft() {
        assertEquals("draft", resolveClosureSubmitStatus(invoice(), requireInvoiceValidation = true))
    }

    @Test
    fun resolveClosureSubmitStatus_directWithoutValidationIsInvoiced() {
        assertEquals("invoiced", resolveClosureSubmitStatus(invoice(), requireInvoiceValidation = false))
    }

    @Test
    fun resolveClosureSubmitStatus_localInvoicedWithInvoiceTypeIsInvoiced() {
        assertEquals(
            "invoiced",
            resolveClosureSubmitStatus(
                invoice(status = "invoiced", documentType = "invoice"),
                requireInvoiceValidation = true,
            ),
        )
    }

    @Test
    fun resolveClosureSubmitStatus_devisSigned() {
        assertEquals(
            "accepted",
            resolveClosureSubmitStatus(
                invoice(
                    documentType = "quote",
                    devisSignatureUrl = "file:///sig.png",
                    acceptedAt = "2026-06-04T10:00:00Z",
                ),
                requireInvoiceValidation = false,
            ),
        )
    }

    @Test
    fun resolveClosureSubmitStatus_devisConvertedLocallyIsInvoiced() {
        assertEquals(
            "invoiced",
            resolveClosureSubmitStatus(
                invoice(
                    status = "invoiced",
                    documentType = "invoice",
                    devisSignatureUrl = "file:///sig.png",
                    acceptedAt = "2026-06-04T10:00:00Z",
                ),
                requireInvoiceValidation = true,
            ),
        )
    }
}
