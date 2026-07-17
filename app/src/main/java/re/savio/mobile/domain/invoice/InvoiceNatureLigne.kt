package re.savio.mobile.domain.invoice

import re.savio.mobile.data.local.entity.InvoiceLineEntity

data class NatureLigneOption(
    val code: String,
    val label: String,
)

object InvoiceNatureLigne {
    const val MISSING_NATURE_MESSAGE =
        "Certaines lignes n'ont pas de nature comptable renseignée."

    val OPTIONS: List<NatureLigneOption> =
        listOf(
            NatureLigneOption("PC", "Pièces"),
            NatureLigneOption("CT", "Contrat"),
            NatureLigneOption("MO", "Main d'œuvre"),
            NatureLigneOption("DEP", "Déplacement"),
            NatureLigneOption("NT", "Non taxable"),
            NatureLigneOption("DIV", "Divers"),
        )

    private val labelByCode: Map<String, String> =
        OPTIONS.associate { it.code to it.label }

    fun labelFor(code: String?): String? = code?.trim()?.takeIf { it.isNotEmpty() }?.let { labelByCode[it] }

    /** Catalogue seulement si [catalogLineType] est fourni — jamais déduit d’une référence seule. */
    fun resolveStoredType(catalogLineType: String?): String =
        when (catalogLineType?.lowercase()) {
            "piece", "article" -> "piece"
            "prestation" -> "prestation"
            else -> "free_text"
        }

    fun isCatalogLine(line: InvoiceLineEntity): Boolean =
        line.type == "prestation" || line.type == "piece"

    fun isFreeLine(line: InvoiceLineEntity): Boolean {
        if (line.isTextBlock || line.type == "text_block" || line.type == "subtotal") return false
        if (!line.natureLigne.isNullOrBlank() && isCatalogLine(line)) return true
        if (isCatalogLine(line)) return false
        return line.type == "free_text" || line.type.isBlank()
    }

    /** Lignes libres + prestations (guérit les anciennes refs libres mal typées). */
    fun canEditNature(line: InvoiceLineEntity): Boolean {
        if (line.isTextBlock || line.type == "text_block" || line.type == "subtotal") return false
        if (line.type == "piece") return false
        return isFreeLine(line) || line.type == "prestation"
    }

    fun requiresNature(line: InvoiceLineEntity): Boolean {
        if (!isFreeLine(line)) return false
        val labelMeaningful = line.label.replace("\u200b", "").trim().isNotEmpty()
        val hasReference = !line.reference.isNullOrBlank()
        val hasPrice = line.unitPriceHt > 0.005
        return labelMeaningful || hasReference || hasPrice
    }

    fun missingNatureLineIds(lines: List<InvoiceLineEntity>): List<String> =
        lines
            .filter { requiresNature(it) && it.natureLigne.isNullOrBlank() }
            .map { it.id }

    fun typeForPush(line: InvoiceLineEntity): String {
        if (line.isTextBlock || line.type == "text_block") return "text_block"
        if (line.type == "subtotal") return "subtotal"
        if (!line.natureLigne.isNullOrBlank()) return "free_text"
        return line.type.ifBlank { "free_text" }
    }

    fun isPhotoEligible(line: InvoiceLineEntity): Boolean {
        if (line.isTextBlock || line.type == "text_block" || line.type == "subtotal") return false
        if (line.natureLigne == "PC") return true
        return line.type == "piece"
    }
}
