package re.melchior.saviomobile.data.repository

import com.google.gson.Gson
import re.melchior.saviomobile.data.local.dao.EquipmentDao
import re.melchior.saviomobile.data.local.dao.InterventionActualTypeDao
import re.melchior.saviomobile.data.local.dao.InterventionDao
import re.melchior.saviomobile.data.local.dao.InterventionHistoryDao
import re.melchior.saviomobile.data.local.dao.ReferentielDao
import re.melchior.saviomobile.data.local.dao.SettingsDao
import re.melchior.saviomobile.data.local.entity.EnergyTypeEntity
import re.melchior.saviomobile.data.local.entity.EquipmentEntity
import re.melchior.saviomobile.data.local.entity.EquipmentTypeEntity
import re.melchior.saviomobile.data.local.entity.InterventionActualTypeEntity
import re.melchior.saviomobile.data.local.entity.InterventionEntity
import re.melchior.saviomobile.data.local.entity.InterventionHistoryEntity
import re.melchior.saviomobile.data.local.entity.InterventionTypeEntity
import re.melchior.saviomobile.data.local.entity.SettingsEntity
import re.melchior.saviomobile.data.remote.api.SyncApi
import re.melchior.saviomobile.data.remote.dto.InterventionDto
import re.melchior.saviomobile.data.remote.dto.InterventionTypeDto
import re.melchior.saviomobile.data.remote.dto.stableKey
import re.melchior.saviomobile.data.remote.dto.SettingsDto
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

sealed class SyncResult {
    object Success : SyncResult()
    data class Error(val message: String) : SyncResult()
}

@Singleton
class SyncRepository @Inject constructor(
    private val syncApi: SyncApi,
    private val interventionDao: InterventionDao,
    private val equipmentDao: EquipmentDao,
    private val referentielDao: ReferentielDao,
    private val settingsDao: SettingsDao,
    private val interventionHistoryDao: InterventionHistoryDao,
    private val interventionActualTypeDao: InterventionActualTypeDao
) {

    fun getEquipmentsByIntervention(interventionId: String) =
        equipmentDao.getEquipmentsByIntervention(interventionId)

    suspend fun startIntervention(interventionId: String) {
        val now = java.time.Instant.now().toString()
        interventionDao.markAsInProgress(interventionId, now)
    }

    suspend fun completeIntervention(
        interventionId: String,
        completedAt: String,
        signaturePath: String?,
        techSignaturePath: String,
        selectedTypes: List<InterventionTypeDto>
    ) {
        val first = selectedTypes.firstOrNull()
        interventionActualTypeDao.deleteForIntervention(interventionId)
        if (selectedTypes.isNotEmpty()) {
            interventionActualTypeDao.insertAll(
                selectedTypes.mapIndexed { index, t ->
                    InterventionActualTypeEntity(
                        id = "${interventionId}_${t.stableKey()}",
                        interventionId = interventionId,
                        interventionTypeId = t.id ?: t.code,
                        code = t.code,
                        label = t.label,
                        color = t.color,
                        isVeType = t.isVeType,
                        order = index + 1
                    )
                }
            )
        }
        interventionDao.completeIntervention(
            id = interventionId,
            completedAt = completedAt,
            signaturePath = signaturePath,
            techSignaturePath = techSignaturePath,
            actualTypeId = first?.id ?: first?.code ?: "",
            actualTypeCode = first?.code,
            actualTypeLabel = when {
                selectedTypes.isEmpty() -> null
                selectedTypes.size == 1 -> first?.label
                else -> selectedTypes.joinToString(", ") { it.label }
            }
        )
    }

    fun getEquipmentById(id: String) =
        equipmentDao.getEquipmentById(id)

    suspend fun saveReport(interventionId: String, report: String) {
        interventionDao.saveReport(interventionId, report)
    }

    // Méthode au niveau de la classe — pas à l'intérieur de pull()
    private suspend fun insertAllSafe(interventions: List<InterventionEntity>) {
        interventions.forEach { entity ->
            val existing = interventionDao.getInterventionByIdOnce(entity.id)

            when {
                // Pas encore en local → insert direct
                existing == null -> {
                    interventionDao.insertOrReplace(entity)
                }

                // Intervention terminée localement → données terrain font foi TOUJOURS
                existing.status == "completed" || existing.status == "pending_validation" -> {
                    interventionDao.insertOrReplace(
                        existing.copy(
                            // Seul le syncStatus peut être mis à jour par le serveur
                            syncStatus = if (entity.status == "completed") "SYNCED" else existing.syncStatus,
                            // Tout le reste : données locales prioritaires
                            report = existing.report ?: entity.report,
                            completedAt = existing.completedAt ?: entity.completedAt,
                            startedAt = existing.startedAt ?: entity.startedAt,
                            signaturePath = existing.signaturePath ?: entity.signaturePath,
                            techSignaturePath = existing.techSignaturePath ?: entity.techSignaturePath,
                            interventionTypeId = existing.interventionTypeId ?: entity.interventionTypeId,
                            actualTypeId = existing.actualTypeId ?: entity.actualTypeId,
                            actualTypeCode = existing.actualTypeCode ?: entity.actualTypeCode,
                            actualTypeLabel = existing.actualTypeLabel ?: entity.actualTypeLabel
                        )
                    )
                    android.util.Log.d("InsertAllSafe", "→ completed local, données terrain préservées ${entity.id}")
                }

                // En cours ou en attente de push → ne pas écraser
                existing.syncStatus in listOf("IN_PROGRESS", "PENDING") -> {
                    android.util.Log.d("InsertAllSafe", "→ skip ${entity.id} (local=${existing.syncStatus})")
                }

                // SYNCED et pas completed → serveur fait foi
                else -> {
                    interventionDao.insertOrReplace(entity)
                }
            }
        }
    }

    fun getInterventionByCustomerId(customerId: String) =
        interventionDao.getInterventionByCustomerId(customerId)

    suspend fun pull(date: LocalDate): SyncResult {
        return try {
            val dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE)

            val localSettings = settingsDao.getSettingsOnce()
            val ifModifiedSince = localSettings?.lastPulledAt

            val response = syncApi.pull(
                date = dateStr,
                ifModifiedSince = ifModifiedSince
            )

            val settings = response.settings ?: SettingsDto()

            val interventionEntities = response.interventions.map { it.toEntity(response.pulledAt) }

            interventionDao.deleteSyncedForDate(
                date = dateStr,
                keepIds = response.interventions.map { it.id }
            )

            // Appel correct ici
            insertAllSafe(interventionEntities)

            response.interventions.forEach { dto ->
                val existing = interventionDao.getInterventionByIdOnce(dto.id)
                val preserveLocalCompleted =
                    existing?.status == "completed" || existing?.status == "pending_validation"
                if (!preserveLocalCompleted && dto.actualTypes.isNotEmpty()) {
                    interventionActualTypeDao.deleteForIntervention(dto.id)
                    interventionActualTypeDao.insertAll(
                        dto.actualTypes.map { at ->
                            InterventionActualTypeEntity(
                                id = "${dto.id}_${at.code}",
                                interventionId = dto.id,
                                interventionTypeId = at.id ?: at.code,
                                code = at.code,
                                label = at.label,
                                color = at.color,
                                isVeType = at.isVeType,
                                order = at.order
                            )
                        }
                    )
                }
            }

            // ← HISTORIQUE ICI — après insertAllSafe
            val historyEntities = response.interventions.flatMap { dto ->
                dto.history.map { h ->
                    InterventionHistoryEntity(
                        id = h.id,
                        unitId = dto.unit.id,
                        number = h.number,
                        scheduledAt = h.scheduledAt,
                        completedAt = h.completedAt,
                        report = h.report,
                        typeCode = h.typeCode,
                        typeLabel = h.typeLabel,
                        typeColor = h.typeColor,
                        technicianFirstName = h.technicianFirstName,
                        technicianLastName = h.technicianLastName,
                        photoKeys = if (h.photoKeys.isEmpty()) null
                        else Gson().toJson(h.photoKeys)
                    )
                }
            }
            if (historyEntities.isNotEmpty()) {
                interventionHistoryDao.insertAll(historyEntities)
            }




            response.interventions.forEach { intervention ->
                val equipmentEntities = intervention.equipment.map { eq ->
                    EquipmentEntity(
                        id = eq.id,
                        interventionId = intervention.id,
                        brand = eq.brand,
                        model = eq.model,
                        typeCode = eq.typeCode,
                        energyCode = eq.energyCode,
                        serialNumber = eq.serialNumber,
                        installDate = eq.installDate,
                        isPrimary = eq.isPrimary
                    )
                }
                equipmentDao.insertAll(equipmentEntities)
            }

            response.referentiels?.let { refs ->
                refs.interventionTypes?.let { types ->
                    referentielDao.insertInterventionTypes(
                        types.map { InterventionTypeEntity(it.code, it.label, it.color) }
                    )
                }
                refs.equipmentTypes?.let { types ->
                    referentielDao.insertEquipmentTypes(
                        types.map { EquipmentTypeEntity(it.code, it.label) }
                    )
                }
                refs.energyTypes?.let { types ->
                    referentielDao.insertEnergyTypes(
                        types.map { EnergyTypeEntity(it.code, it.label) }
                    )
                }
            }

            settingsDao.save(
                SettingsEntity(
                    id = 1,
                    allowCreateIntervention = settings.allowCreateIntervention,
                    allowProposal = settings.allowProposal,
                    lastPulledAt = response.pulledAt,
                    technicianId = response.technician.id,
                    technicianFirstName = response.technician.firstName,
                    technicianLastName = response.technician.lastName,
                    requireInvoiceValidation = response.technician.requireInvoiceValidation ?: false
                )
            )

            val cutoffDate = date.minusDays(7).format(DateTimeFormatter.ISO_LOCAL_DATE)
            interventionDao.deleteOlderThan(cutoffDate)
            equipmentDao.deleteOlderThan(cutoffDate)

            SyncResult.Success

        } catch (e: Exception) {
            SyncResult.Error(e.message ?: "Erreur de synchronisation")
        }
    }

    fun getInterventionsByDate(date: LocalDate) =
        interventionDao.getInterventionsByDate(
            date.format(DateTimeFormatter.ISO_LOCAL_DATE)
        )

    fun getInterventionById(id: String) =
        interventionDao.getInterventionById(id)

    suspend fun getInterventionByIdOnce(id: String) =
        interventionDao.getInterventionByIdOnce(id)

    fun getPendingSyncCount() =
        interventionDao.getPendingSyncCount()

    suspend fun getHistoryForUnit(unitId: String): List<InterventionHistoryEntity> =
        interventionHistoryDao.getHistoryForUnit(unitId)
}

private fun InterventionDto.toEntity(pulledAt: String) = InterventionEntity(
    id = id,
    scheduledAt = scheduledAt,
    status = status,
    syncStatus = "SYNCED",
    typeCode = type.code,
    typeLabel = type.label,
    typeColor = type.color,
    interventionTypeId = type.id,
    actualTypeId = actualTypes.firstOrNull()?.let { it.id ?: it.code } ?: actualTypeId,
    actualTypeCode = actualTypes.firstOrNull()?.code ?: actualTypeCode,
    actualTypeLabel = when {
        actualTypes.size > 1 -> actualTypes.joinToString(", ") { it.label }
        actualTypes.size == 1 -> actualTypes.first().label
        else -> actualTypeLabel
    },
    number = number,
    unitId = unit.id,
    unitStreet = unit.street,
    unitAddressLine2 = unit.addressLine2,
    unitPostalCode = unit.postalCode,
    unitCity = unit.city,
    unitFloor = unit.floor,
    unitDoorCode = unit.doorCode,
    unitLatitude = unit.latitude,
    unitLongitude = unit.longitude,
    customerId = customer?.id,
    customerFirstName = customer?.firstName,
    customerLastName = customer?.lastName,
    customerPhone = customer?.phone,
    customerEmail = customer?.email,
    notes = notes,
    contractType = contract?.type,
    contractRenewalDate = contract?.renewalDate,
    contractTariff = contract?.tariff,
    contractVatRate = contract?.vatRate,
    report = report,
    completedAt = completedAt,
    startedAt = startedAt,
    pulledAt = pulledAt
)