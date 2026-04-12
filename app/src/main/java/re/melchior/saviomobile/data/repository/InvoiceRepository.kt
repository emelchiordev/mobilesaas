package re.melchior.saviomobile.data.repository

import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
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
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InvoiceRepository @Inject constructor(
    private val invoiceApi: InvoiceApi,
    private val invoiceDao: InvoiceDao,
    private val invoiceLineDao: InvoiceLineDao,
    private val invoicePaymentDao: InvoicePaymentDao,
    private val pendingUpdateDao: PendingUpdateDao
) {

    private val gson = Gson()

    suspend fun getInvoiceById(id: String): InvoiceEntity? =
        invoiceDao.getById(id)

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

    suspend fun addLine(
        invoiceId: String,
        reference: String?,
        label: String,
        quantity: Double,
        unitPriceHt: Double,
        vatRate: Double,
        billingType: String = "billable"
    ): InvoiceLineEntity {
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
        return entity
    }

    suspend fun removeLine(invoiceId: String, lineId: String) {
        invoiceLineDao.deleteById(lineId)
        recalcTotals(invoiceId)
    }

    suspend fun submitInvoice(
        invoiceId: String,
        interventionId: String,
        unitId: String,
        technicianId: String,
        validateDirectly: Boolean
    ): Result<Unit> {
        return try {
            val lines = invoiceLineDao.getByInvoiceIdOnce(invoiceId)
            val payments = invoicePaymentDao.getByInvoiceIdOnce(invoiceId)
            val now = Instant.now().toString()
            val status = if (validateDirectly) "validated" else "pending_validation"

            invoiceDao.updateStatus(invoiceId, status)

            val linesPayload = lines.map { line ->
                mapOf(
                    "reference" to line.reference,
                    "label" to line.label,
                    "quantity" to line.quantity,
                    "unitPriceHt" to line.unitPriceHt,
                    "vatRate" to line.vatRate,
                    "billingType" to line.billingType
                )
            }

            val paymentsPayload = payments.map { payment ->
                mapOf(
                    "amount" to payment.amount,
                    "paymentMethodCode" to payment.paymentMethodCode,
                    "paidAt" to payment.paidAt,
                )
            }

            pendingUpdateDao.insert(
                PendingUpdateEntity(
                    id = "op-submit-invoice-$invoiceId",
                    type = "SUBMIT_INVOICE_FULL",
                    targetId = invoiceId,
                    payload = gson.toJson(
                        mapOf(
                            "interventionId" to interventionId,
                            "unitId" to unitId,
                            "technicianId" to technicianId,
                            "status" to status,
                            "lines" to linesPayload,
                            "payments" to paymentsPayload,
                        )
                    ),
                    occurredAt = now,
                    syncStatus = "PENDING"
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchRef(query: String): SearchRefResponseDto =
        invoiceApi.searchRef(query)

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
    updatedAt = updatedAt
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
