package re.melchior.saviomobile.data.repository

import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import re.melchior.saviomobile.data.local.dao.InvoiceDao
import re.melchior.saviomobile.data.local.dao.InvoiceLineDao
import re.melchior.saviomobile.data.local.dao.InvoicePaymentDao
import re.melchior.saviomobile.data.local.dao.PendingUpdateDao
import re.melchior.saviomobile.data.local.entity.InvoiceEntity
import re.melchior.saviomobile.data.local.entity.InvoiceLineEntity
import re.melchior.saviomobile.data.local.entity.InvoicePaymentEntity
import re.melchior.saviomobile.data.local.entity.PendingUpdateEntity
import re.melchior.saviomobile.data.remote.api.InvoiceApi
import re.melchior.saviomobile.data.remote.dto.InvoiceDto
import re.melchior.saviomobile.data.remote.dto.InvoiceLineDto
import re.melchior.saviomobile.data.remote.dto.SearchRefResponseDto
import java.io.File
import java.time.Instant
import java.util.Base64
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

data class InvoiceBillingSummary(
    val invoice: InvoiceEntity?,
    val lineCount: Int,
    val billableLineCount: Int,
)

@Singleton
class InvoiceRepository @Inject constructor(
    private val invoiceApi: InvoiceApi,
    private val invoiceDao: InvoiceDao,
    private val invoiceLineDao: InvoiceLineDao,
    private val invoicePaymentDao: InvoicePaymentDao,
    private val pendingUpdateDao: PendingUpdateDao
) {

    private val gson = Gson()

    companion object {
        const val DEVIS_RESIGN_REQUIRED_MESSAGE =
            "Le devis a été modifié. Le client doit signer à nouveau avant l'émission de la facture."

        private val LOCKED_INVOICE_STATUSES = setOf("invoiced", "paid")
        private val SIGNED_DEVIS_STATUSES = setOf("accepted", "pending_validation")
    }

    suspend fun getInvoiceById(id: String): InvoiceEntity? =
        invoiceDao.getById(id)

    suspend fun refreshInvoiceFromServer(interventionId: String): InvoiceEntity? {
        val local = invoiceDao.getByInterventionId(interventionId)
        return try {
            val invoices = invoiceApi.getInvoiceByIntervention(interventionId)
            val dto =
                invoices.firstOrNull {
                    it.interventionId == interventionId && it.status != "cancelled"
                } ?: return local
            val server = dto.toEntity()
            if (local == null) {
                invoiceDao.insert(server)
                dto.lines?.let { lines ->
                    invoiceLineDao.deleteByInvoiceId(server.id)
                    invoiceLineDao.insertAll(lines.map { it.toEntity() })
                }
                return server
            }
            val merged =
                local.copy(
                    status = server.status,
                    number = server.number ?: local.number,
                    totalHt = server.totalHt,
                    totalVat = server.totalVat,
                    totalTtc = server.totalTtc,
                    emittedAt = server.emittedAt ?: local.emittedAt,
                    dueAt = server.dueAt ?: local.dueAt,
                    acceptedAt = server.acceptedAt ?: local.acceptedAt,
                    invoicedAt = server.invoicedAt ?: local.invoicedAt,
                    paidAt = server.paidAt ?: local.paidAt,
                    devisSignatureUrl = server.devisSignatureUrl ?: local.devisSignatureUrl,
                    hamonSignatureUrl = server.hamonSignatureUrl ?: local.hamonSignatureUrl,
                    hamonRequested = server.hamonRequested,
                    updatedAt = server.updatedAt,
                    syncStatus = "synced",
                )
            invoiceDao.update(merged)
            merged
        } catch (_: Exception) {
            local
        }
    }

    /** Facture courante pour une intervention (toujours via interventionId, pas un id potentiellement périmé). */
    suspend fun resolveInvoiceForIntervention(interventionId: String): InvoiceEntity? =
        invoiceDao.getByInterventionId(interventionId)
            ?: getInvoiceForIntervention(interventionId)

    suspend fun getInvoiceForIntervention(interventionId: String): InvoiceEntity? {
        val local = invoiceDao.getByInterventionId(interventionId)
        if (local != null) return local

        return try {
            val invoices = invoiceApi.getInvoiceByIntervention(interventionId)
            val invoice = invoices.firstOrNull {
                it.interventionId == interventionId && it.status != "cancelled"
            }
            invoice?.let {
                val entity = it.toEntity()
                invoiceDao.insert(entity)
                it.lines?.let { lines ->
                    invoiceLineDao.insertAll(lines.map { l -> l.toEntity() })
                }
                entity
            }
        } catch (e: Exception) {
            null
        }
    }

    fun getLinesForInvoice(invoiceId: String): Flow<List<InvoiceLineEntity>> =
        invoiceLineDao.getByInvoiceId(invoiceId)

    fun observeBillingForIntervention(interventionId: String): Flow<InvoiceBillingSummary> =
        invoiceDao.observeByInterventionId(interventionId).flatMapLatest { invoice ->
            if (invoice == null) {
                flowOf(InvoiceBillingSummary(invoice = null, lineCount = 0, billableLineCount = 0))
            } else {
                invoiceLineDao.getByInvoiceId(invoice.id).map { lines ->
                    InvoiceBillingSummary(
                        invoice = invoice,
                        lineCount = lines.count {
                            !it.isTextBlock && it.type != "subtotal" && it.type != "text_block"
                        },
                        billableLineCount = lines.count {
                            it.billingType == "billable" &&
                                !it.isTextBlock &&
                                it.type != "subtotal" &&
                                it.type != "text_block"
                        },
                    )
                }
            }
        }

    fun observeInvoiceForIntervention(interventionId: String): Flow<InvoiceEntity?> =
        invoiceDao.observeByInterventionId(interventionId)

    fun getPaymentsForInvoice(invoiceId: String): Flow<List<InvoicePaymentEntity>> =
        invoicePaymentDao.getByInvoiceId(invoiceId)

    suspend fun addPayment(
        invoiceId: String,
        amount: Double,
        paymentMethodCode: String,
        paidAt: String,
    ): InvoicePaymentEntity {
        val payment = InvoicePaymentEntity(
            id = UUID.randomUUID().toString(),
            invoiceId = invoiceId,
            amount = amount,
            paymentMethodCode = paymentMethodCode,
            paidAt = paidAt,
        )
        invoicePaymentDao.insert(payment)
        return payment
    }

    suspend fun removePayment(paymentId: String) {
        invoicePaymentDao.deleteById(paymentId)
    }

    suspend fun createInvoice(
        interventionId: String,
        unitId: String,
        technicianId: String
    ): InvoiceEntity {
        val invoiceId = UUID.randomUUID().toString()
        val now = Instant.now().toString()
        val entity = InvoiceEntity(
            id = invoiceId,
            interventionId = interventionId,
            status = "draft",
            number = null,
            totalHt = 0.0,
            totalVat = 0.0,
            totalTtc = 0.0,
            emittedAt = null,
            dueAt = null,
            customerEmail = null,
            notes = null,
            syncStatus = "pending",
            createdAt = now,
            updatedAt = now
        )
        invoiceDao.insert(entity)
        return entity
    }

    /** @return true si le devis signé a été invalidé (re-signature requise). */
    suspend fun addLine(
        invoiceId: String,
        reference: String?,
        label: String,
        quantity: Double,
        unitPriceHt: Double,
        vatRate: Double,
        billingType: String = "billable"
    ): Boolean {
        ensureInvoiceLinesEditable(invoiceId)
        val lineId = UUID.randomUUID().toString()
        val existingLines = invoiceLineDao.getByInvoiceIdOnce(invoiceId)
        val nextOrder = (existingLines.maxOfOrNull { it.order } ?: 0) + 1
        val totalHt = quantity * unitPriceHt
        val entity = InvoiceLineEntity(
            id = lineId,
            invoiceId = invoiceId,
            type = if (reference != null) "prestation" else "free_text",
            label = label,
            reference = reference,
            quantity = quantity,
            unitPriceHt = unitPriceHt,
            vatRate = vatRate,
            discountPercent = 0.0,
            totalHt = totalHt,
            billingType = billingType,
            isTextBlock = false,
            order = nextOrder
        )
        invoiceLineDao.insert(entity)
        recalcTotals(invoiceId)
        return afterLinesMutated(invoiceId)
    }

    /** @return true si le devis signé a été invalidé (re-signature requise). */
    suspend fun removeLine(invoiceId: String, lineId: String): Boolean {
        ensureInvoiceLinesEditable(invoiceId)
        invoiceLineDao.deleteById(lineId)
        recalcTotals(invoiceId)
        return afterLinesMutated(invoiceId)
    }

    suspend fun addTextBlockLine(invoiceId: String, text: String): InvoiceLineEntity {
        ensureInvoiceLinesEditable(invoiceId)
        val lineId = UUID.randomUUID().toString()
        val existingLines = invoiceLineDao.getByInvoiceIdOnce(invoiceId)
        val nextOrder = (existingLines.maxOfOrNull { it.order } ?: 0) + 1
        val entity = InvoiceLineEntity(
            id = lineId,
            invoiceId = invoiceId,
            type = "text_block",
            label = text.trim(),
            reference = null,
            quantity = 0.0,
            unitPriceHt = 0.0,
            vatRate = 0.0,
            discountPercent = 0.0,
            totalHt = 0.0,
            billingType = "billable",
            isTextBlock = true,
            order = nextOrder,
        )
        invoiceLineDao.insert(entity)
        afterLinesMutated(invoiceId)
        return entity
    }

    /** @return true si le devis signé a été invalidé (re-signature requise). */
    suspend fun moveLine(invoiceId: String, fromIndex: Int, toIndex: Int): Boolean {
        ensureInvoiceLinesEditable(invoiceId)
        val list =
            invoiceLineDao.getByInvoiceIdOnce(invoiceId).sortedBy { it.order }.toMutableList()
        if (fromIndex !in list.indices || toIndex !in list.indices || fromIndex == toIndex) {
            return false
        }
        val item = list.removeAt(fromIndex)
        list.add(toIndex, item)
        list.forEachIndexed { index, line ->
            if (line.order != index + 1) {
                invoiceLineDao.update(line.copy(order = index + 1))
            }
        }
        return afterLinesMutated(invoiceId)
    }

    suspend fun updateLine(
        lineId: String,
        label: String,
        quantity: Double,
        unitPriceHt: Double,
        vatRate: Double,
        billingType: String,
    ) {
        val existing = invoiceLineDao.getById(lineId) ?: return
        val totalHt = quantity * unitPriceHt
        invoiceLineDao.update(
            existing.copy(
                label = label,
                quantity = quantity,
                unitPriceHt = unitPriceHt,
                vatRate = vatRate,
                billingType = billingType,
                totalHt = totalHt,
            ),
        )
        recalcTotals(existing.invoiceId)
    }

    suspend fun updateLineEntity(line: InvoiceLineEntity) {
        val totalHt = line.quantity * line.unitPriceHt
        invoiceLineDao.update(line.copy(totalHt = totalHt))
        recalcTotals(line.invoiceId)
    }

    suspend fun acceptDevis(
        interventionId: String,
        unitId: String,
        technicianId: String,
        devisSignatureBase64: String,
        hamonRequested: Boolean,
        hamonSignatureBase64: String? = null,
        devisSignaturePreviewUrl: String? = null,
        hamonSignaturePreviewUrl: String? = null,
        preferredInvoiceId: String? = null,
    ): Result<Unit> =
        runCatching {
            val now = Instant.now().toString()
            val current =
                when {
                    preferredInvoiceId != null ->
                        invoiceDao.getById(preferredInvoiceId)
                            ?: invoiceDao.getByInterventionId(interventionId)
                    else -> null
                } ?: invoiceDao.getByInterventionId(interventionId)
                    ?: error("Facture introuvable pour cette intervention")
            val invoiceId = current.id
            if (current.status in LOCKED_INVOICE_STATUSES) {
                error("Impossible de signer un devis sur une facture déjà émise.")
            }
            invoiceDao.updateLifecycle(
                id = invoiceId,
                status = "accepted",
                acceptedAt = now,
                invoicedAt = current.invoicedAt,
                paidAt = current.paidAt,
                devisSignatureUrl = devisSignaturePreviewUrl ?: current.devisSignatureUrl,
                hamonSignatureUrl = hamonSignaturePreviewUrl ?: current.hamonSignatureUrl,
                hamonRequested = hamonRequested,
                updatedAt = now,
            )
            invoiceDao.updateAcceptedLinesFingerprint(
                invoiceId,
                computeLinesFingerprint(invoiceId),
            )
        }

    suspend fun submitForValidation(
        invoiceId: String,
        interventionId: String,
        unitId: String,
        technicianId: String,
    ): Result<Unit> =
        runCatching {
            val now = Instant.now().toString()
            val current = invoiceDao.getById(invoiceId) ?: error("Facture introuvable")
            invoiceDao.updateLifecycle(
                id = invoiceId,
                status = "pending_validation",
                acceptedAt = current.acceptedAt,
                invoicedAt = current.invoicedAt,
                paidAt = current.paidAt,
                devisSignatureUrl = current.devisSignatureUrl,
                hamonSignatureUrl = current.hamonSignatureUrl,
                hamonRequested = current.hamonRequested,
                updatedAt = now,
            )
        }

    suspend fun emitInvoice(invoiceId: String): Result<Unit> =
        runCatching {
            val now = Instant.now().toString()
            val current = invoiceDao.getById(invoiceId) ?: error("Facture introuvable")
            if (current.status !in SIGNED_DEVIS_STATUSES) {
                error("Le devis doit être signé avant d'émettre la facture.")
            }
            invoiceDao.updateLifecycle(
                id = invoiceId,
                status = "invoiced",
                acceptedAt = current.acceptedAt,
                invoicedAt = now,
                paidAt = current.paidAt,
                devisSignatureUrl = current.devisSignatureUrl,
                hamonSignatureUrl = current.hamonSignatureUrl,
                hamonRequested = current.hamonRequested,
                updatedAt = now,
            )
        }

    suspend fun markInvoicePaid(invoiceId: String): Result<Unit> =
        runCatching {
            val now = Instant.now().toString()
            val current = invoiceDao.getById(invoiceId) ?: error("Facture introuvable")
            invoiceDao.updateLifecycle(
                id = invoiceId,
                status = "paid",
                acceptedAt = current.acceptedAt,
                invoicedAt = current.invoicedAt,
                paidAt = now,
                devisSignatureUrl = current.devisSignatureUrl,
                hamonSignatureUrl = current.hamonSignatureUrl,
                hamonRequested = current.hamonRequested,
                updatedAt = now,
            )
        }

    /**
     * Enfile un unique SUBMIT_INVOICE_FULL pour la clôture (état final Room).
     * Aucun push facture ne doit être enqueue avant la clôture.
     */
    suspend fun prepareInvoicePushForClosure(
        interventionId: String,
        unitId: String,
        technicianId: String,
    ) {
        val invoice =
            invoiceDao.getByInterventionId(interventionId) ?: return
        val invoiceId = invoice.id
        pendingUpdateDao.deleteByTargetId(invoiceId)

        val devisSignatureBase64 = readSignatureFileBase64(invoice.devisSignatureUrl)
        val hamonSignatureBase64 = readSignatureFileBase64(invoice.hamonSignatureUrl)
        val hamonRequested =
            when (invoice.status) {
                "accepted", "pending_validation", "invoiced", "paid" -> invoice.hamonRequested
                else -> null
            }

        enqueueSubmitInvoiceFull(
            invoiceId = invoiceId,
            interventionId = interventionId,
            unitId = unitId,
            technicianId = technicianId,
            status = invoice.status,
            acceptedAt = invoice.acceptedAt,
            invoicedAt = invoice.invoicedAt,
            paidAt = invoice.paidAt,
            devisSignatureBase64 = devisSignatureBase64,
            hamonSignatureBase64 = hamonSignatureBase64,
            hamonRequested = hamonRequested,
        )
    }

    private fun readSignatureFileBase64(path: String?): String? {
        if (path.isNullOrBlank()) return null
        val file = File(path)
        if (!file.isFile) return null
        return Base64.getEncoder().encodeToString(file.readBytes())
    }

    private suspend fun enqueueSubmitInvoiceFull(
        invoiceId: String,
        interventionId: String,
        unitId: String,
        technicianId: String,
        status: String,
        acceptedAt: String? = null,
        invoicedAt: String? = null,
        paidAt: String? = null,
        devisSignatureBase64: String? = null,
        hamonSignatureBase64: String? = null,
        hamonRequested: Boolean? = null,
    ) {
        val now = Instant.now().toString()
        val payload = buildSyncPayload(
            interventionId = interventionId,
            unitId = unitId,
            technicianId = technicianId,
            status = status,
            invoiceId = invoiceId,
            acceptedAt = acceptedAt,
            invoicedAt = invoicedAt,
            paidAt = paidAt,
            devisSignatureBase64 = devisSignatureBase64,
            hamonSignatureBase64 = hamonSignatureBase64,
            hamonRequested = hamonRequested,
        )
        pendingUpdateDao.insert(
            PendingUpdateEntity(
                id = "op-submit-invoice-$invoiceId",
                type = "SUBMIT_INVOICE_FULL",
                targetId = invoiceId,
                payload = gson.toJson(payload),
                occurredAt = now,
                syncStatus = "PENDING",
            ),
        )
    }

    private suspend fun buildSyncPayload(
        interventionId: String,
        unitId: String,
        technicianId: String,
        status: String,
        invoiceId: String,
        acceptedAt: String? = null,
        invoicedAt: String? = null,
        paidAt: String? = null,
        devisSignatureBase64: String? = null,
        hamonSignatureBase64: String? = null,
        hamonRequested: Boolean? = null,
    ): Map<String, Any?> {
        val lines = invoiceLineDao.getByInvoiceIdOnce(invoiceId)
        val payments = invoicePaymentDao.getByInvoiceIdOnce(invoiceId)
        val linesPayload = lines.sortedBy { it.order }.map { line ->
            mapOf(
                "type" to line.type,
                "reference" to line.reference,
                "label" to line.label,
                "quantity" to line.quantity,
                "unitPriceHt" to line.unitPriceHt,
                "vatRate" to line.vatRate,
                "billingType" to line.billingType,
                "isTextBlock" to line.isTextBlock,
                "order" to line.order,
            )
        }
        val paymentsPayload = payments.map { payment ->
            mapOf(
                "amount" to payment.amount,
                "paymentMethodCode" to payment.paymentMethodCode,
                "paidAt" to payment.paidAt,
            )
        }
        return mapOf(
            "invoiceId" to invoiceId,
            "interventionId" to interventionId,
            "unitId" to unitId,
            "technicianId" to technicianId,
            "status" to status,
            "acceptedAt" to acceptedAt,
            "invoicedAt" to invoicedAt,
            "paidAt" to paidAt,
            "devisSignatureBase64" to devisSignatureBase64,
            "hamonSignatureBase64" to hamonSignatureBase64,
            "hamonRequested" to hamonRequested,
            "lines" to linesPayload,
            "payments" to paymentsPayload,
        )
    }

    suspend fun searchRef(query: String): SearchRefResponseDto =
        invoiceApi.searchRef(query)

    suspend fun deleteDraftByIntervention(interventionId: String) {
        val inv = invoiceDao.getByInterventionId(interventionId) ?: return
        if (inv.status != "draft") return
        invoiceLineDao.deleteByInvoiceId(inv.id)
        invoicePaymentDao.deleteByInvoiceId(inv.id)
        pendingUpdateDao.deleteByTargetId(inv.id)
        invoiceDao.deleteById(inv.id)
    }

    private suspend fun ensureInvoiceLinesEditable(invoiceId: String) {
        val inv = invoiceDao.getById(invoiceId) ?: return
        if (inv.status in LOCKED_INVOICE_STATUSES) {
            error("Impossible de modifier une facture déjà émise.")
        }
    }

    private suspend fun computeLinesFingerprint(invoiceId: String): String {
        val lines = invoiceLineDao.getByInvoiceIdOnce(invoiceId).sortedBy { it.order }
        return lines.joinToString("|") { line ->
            "${line.id}:${line.order}:${line.quantity}:${line.unitPriceHt}:${line.totalHt}:${line.label}:${line.type}"
        }
    }

    private suspend fun afterLinesMutated(invoiceId: String): Boolean =
        maybeInvalidateDevisAcceptance(invoiceId)

    private suspend fun maybeInvalidateDevisAcceptance(invoiceId: String): Boolean {
        val inv = invoiceDao.getById(invoiceId) ?: return false
        if (inv.status !in SIGNED_DEVIS_STATUSES) return false
        val stored = inv.acceptedLinesFingerprint?.trim().orEmpty()
        if (stored.isEmpty()) return false
        val current = computeLinesFingerprint(invoiceId)
        if (stored == current) return false
        invalidateDevisAcceptance(invoiceId)
        return true
    }

    private suspend fun invalidateDevisAcceptance(invoiceId: String) {
        val inv = invoiceDao.getById(invoiceId) ?: return
        val now = Instant.now().toString()
        deleteLocalSignatureFile(inv.devisSignatureUrl)
        deleteLocalSignatureFile(inv.hamonSignatureUrl)
        invoiceDao.updateLifecycle(
            id = invoiceId,
            status = "draft",
            acceptedAt = null,
            invoicedAt = inv.invoicedAt,
            paidAt = inv.paidAt,
            devisSignatureUrl = null,
            hamonSignatureUrl = null,
            hamonRequested = inv.hamonRequested,
            updatedAt = now,
        )
        invoiceDao.updateAcceptedLinesFingerprint(invoiceId, null)
    }

    private fun deleteLocalSignatureFile(path: String?) {
        if (path.isNullOrBlank()) return
        runCatching { File(path).delete() }
    }

    private suspend fun recalcTotals(invoiceId: String) {
        val lines = invoiceLineDao.getByInvoiceIdOnce(invoiceId)
        val billable = lines.filter {
            it.billingType == "billable" &&
                !it.isTextBlock &&
                it.type != "subtotal" &&
                it.type != "text_block"
        }
        val totalHt = billable.sumOf { it.totalHt }

        val vatByRate = billable.groupBy { it.vatRate }
        val totalVat = vatByRate.entries.sumOf { (rate, lineList) ->
            lineList.sumOf { it.totalHt } * rate / 100.0
        }

        val totalTtc = totalHt + totalVat
        val invoice = invoiceDao.getById(invoiceId) ?: return
        val now = Instant.now().toString()
        invoiceDao.update(
            invoice.copy(
                totalHt = totalHt,
                totalVat = totalVat,
                totalTtc = totalTtc,
                updatedAt = now
            )
        )
    }
}

fun InvoiceDto.toEntity() = InvoiceEntity(
    id = id,
    interventionId = interventionId,
    status = status,
    number = number,
    totalHt = totalHt,
    totalVat = totalVat,
    totalTtc = totalTtc,
    emittedAt = emittedAt,
    dueAt = dueAt,
    customerEmail = customerEmail,
    notes = notes,
    syncStatus = "synced",
    createdAt = createdAt,
    updatedAt = updatedAt,
    acceptedAt = acceptedAt,
    invoicedAt = invoicedAt,
    paidAt = paidAt,
    devisSignatureUrl = devisSignatureUrl,
    hamonSignatureUrl = hamonSignatureUrl,
    hamonRequested = hamonRequested,
    acceptedLinesFingerprint = null,
)

fun InvoiceLineDto.toEntity() = InvoiceLineEntity(
    id = id,
    invoiceId = invoiceId,
    type = type,
    label = label,
    reference = reference,
    quantity = quantity,
    unitPriceHt = unitPriceHt,
    vatRate = vatRate,
    discountPercent = discountPercent,
    totalHt = totalHt,
    billingType = billingType,
    isTextBlock = isTextBlock,
    order = order
)
