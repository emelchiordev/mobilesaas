package re.melchior.saviomobile.data.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import re.melchior.saviomobile.data.local.entity.InvoiceEntity

class InvoiceClosureSubmitStatusTest {

    private fun invoice(
        status: String = "draft",
        devisSignatureUrl: String? = null,
        acceptedAt: String? = null,
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
    fun resolveClosureDocumentType_directInvoice() {
        assertEquals("invoice", resolveClosureDocumentType(invoice()))
    }

    @Test
    fun resolveClosureDocumentType_signedQuote() {
        assertEquals(
            "quote",
            resolveClosureDocumentType(
                invoice(devisSignatureUrl = "file:///sig.png", acceptedAt = "2026-06-04T10:00:00Z"),
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
    fun resolveClosureSubmitStatus_directIgnoresLocalInvoicedWhenValidationRequired() {
        assertEquals(
            "draft",
            resolveClosureSubmitStatus(invoice(status = "invoiced"), requireInvoiceValidation = true),
        )
    }

    @Test
    fun resolveClosureSubmitStatus_devisSigned() {
        assertEquals(
            "accepted",
            resolveClosureSubmitStatus(
                invoice(devisSignatureUrl = "file:///sig.png", acceptedAt = "2026-06-04T10:00:00Z"),
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
                    devisSignatureUrl = "file:///sig.png",
                    acceptedAt = "2026-06-04T10:00:00Z",
                ),
                requireInvoiceValidation = true,
            ),
        )
    }
}
