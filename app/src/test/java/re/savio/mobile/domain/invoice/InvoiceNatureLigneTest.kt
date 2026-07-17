package re.savio.mobile.domain.invoice

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import re.savio.mobile.data.local.entity.InvoiceLineEntity

class InvoiceNatureLigneTest {

    @Test
    fun resolveStoredType_freeLineKeepsFreeTextEvenWithCatalogTypeAbsent() {
        assertEquals("free_text", InvoiceNatureLigne.resolveStoredType(null))
        assertEquals("free_text", InvoiceNatureLigne.resolveStoredType(""))
        assertEquals("free_text", InvoiceNatureLigne.resolveStoredType("unknown"))
    }

    @Test
    fun resolveStoredType_catalogueTypes() {
        assertEquals("piece", InvoiceNatureLigne.resolveStoredType("piece"))
        assertEquals("piece", InvoiceNatureLigne.resolveStoredType("article"))
        assertEquals("prestation", InvoiceNatureLigne.resolveStoredType("prestation"))
        assertEquals("piece", InvoiceNatureLigne.resolveStoredType("PIECE"))
    }

    @Test
    fun typeForPush_forcesFreeTextWhenNaturePresent() {
        val line =
            line(
                type = "prestation",
                reference = "REF-CUSTOM",
                natureLigne = "MO",
            )
        assertEquals("free_text", InvoiceNatureLigne.typeForPush(line))
    }

    @Test
    fun typeForPush_keepsCatalogueTypeWhenNoNature() {
        val line = line(type = "prestation", reference = "MO-STD", natureLigne = null)
        assertEquals("prestation", InvoiceNatureLigne.typeForPush(line))
    }

    @Test
    fun canEditNature_allowsMisclassifiedPrestationHeal() {
        val line = line(type = "prestation", reference = "X", natureLigne = null)
        assertTrue(InvoiceNatureLigne.canEditNature(line))
    }

    @Test
    fun canEditNature_rejectsPieces() {
        val line = line(type = "piece", reference = "FILTRE", natureLigne = null)
        assertFalse(InvoiceNatureLigne.canEditNature(line))
    }

    @Test
    fun requiresNature_trueForFreeTextWithLabel() {
        val line = line(type = "free_text", label = "Travaux", natureLigne = null)
        assertTrue(InvoiceNatureLigne.requiresNature(line))
    }

    @Test
    fun requiresNature_falseForCataloguePrestationWithoutNature() {
        val line = line(type = "prestation", reference = "MO-STD", natureLigne = null)
        assertFalse(InvoiceNatureLigne.requiresNature(line))
    }

    @Test
    fun isFreeLine_trueWhenPrestationAlreadyCarriesNature() {
        val line = line(type = "prestation", natureLigne = "DIV")
        assertTrue(InvoiceNatureLigne.isFreeLine(line))
        assertTrue(InvoiceNatureLigne.requiresNature(line))
    }

    private fun line(
        type: String = "free_text",
        label: String = "Ligne",
        reference: String? = null,
        natureLigne: String? = null,
        unitPriceHt: Double = 10.0,
    ) = InvoiceLineEntity(
        id = "line-1",
        invoiceId = "inv-1",
        type = type,
        label = label,
        reference = reference,
        quantity = 1.0,
        unitPriceHt = unitPriceHt,
        vatRate = 20.0,
        discountPercent = 0.0,
        totalHt = unitPriceHt,
        billingType = "billable",
        isTextBlock = false,
        order = 1,
        natureLigne = natureLigne,
    )
}
