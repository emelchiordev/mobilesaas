package re.melchior.saviomobile.data.repository

import com.google.gson.Gson
import re.melchior.saviomobile.data.local.dao.CatalogEquipmentDao
import re.melchior.saviomobile.data.local.dao.ColdMeasureDao
import re.melchior.saviomobile.data.local.dao.EquipmentDao
import re.melchior.saviomobile.data.local.dao.EquipmentSnapshotDao
import re.melchior.saviomobile.data.local.dao.InterventionActualTypeDao
import re.melchior.saviomobile.data.local.dao.InterventionDao
import re.melchior.saviomobile.data.local.dao.InterventionHistoryDao
import re.melchior.saviomobile.data.local.dao.PendingOperationDao
import re.melchior.saviomobile.data.local.dao.ReferentielDao
import re.melchior.saviomobile.data.local.dao.SettingsDao
import re.melchior.saviomobile.data.local.entity.EnergyTypeEntity
import re.melchior.saviomobile.data.local.entity.EquipmentEntity
import re.melchior.saviomobile.data.local.entity.EquipmentSnapshotEntity
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
    private val catalogEquipmentDao: CatalogEquipmentDao,
    private val referentielDao: ReferentielDao,
    private val settingsDao: SettingsDao,
    private val interventionHistoryDao: InterventionHistoryDao,
    private val interventionActualTypeDao: InterventionActualTypeDao,
    private val pendingOperationDao: PendingOperationDao,
    private val coldMeasureDao: ColdMeasureDao,
    private val equipmentSnapshotDao: EquipmentSnapshotDao,
    private val measureRepository: MeasureRepository,
    private val pacMeasureRepository: PacMeasureRepository,
) {

    fun getEquipmentsByIntervention(interventionId: String) =
        equipmentDao.getEquipmentsByIntervention(interventionId)

    suspend fun startIntervention(interventionId: String) {
        interventionDao.resetOtherInProgressToScheduled(
            exceptInterventionId = interventionId,
        )
        val now = java.time.Instant.now().toString()
        interventionDao.markAsInProgress(interventionId, now)
        snapshotEquipments(interventionId)
    }

    private suspend fun snapshotEquipments(interventionId: String) {
        val existing = equipmentSnapshotDao.countByInterventionId(interventionId)
        if (existing > 0) return

        val intervention = interventionDao.getInterventionByIdOnce(interventionId) ?: return
        val equipments = equipmentDao.getEquipmentsByInterventionOnce(interventionId)
        if (equipments.isEmpty()) return

        val snapshots = equipments.map { eq ->
            EquipmentSnapshotEntity(
                equipmentId = eq.id,
                interventionId = interventionId,
                brand = eq.brand,
                model = eq.model,
                typeCode = eq.typeCode,
                energyCode = eq.energyCode,
                serialNumber = eq.serialNumber,
                installDate = eq.installDate,
                isPrimary = eq.isPrimary,
                equipmentCatalogId = eq.equipmentCatalogId,
                catalogBrandId = eq.catalogBrandId,
                parentEquipmentId = eq.parentEquipmentId,
                order = eq.order,
                unitId = eq.unitId,
                createdAt = java.time.Instant.now().toString(),
            )
        }
        equipmentSnapshotDao.insertAll(snapshots)
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
        equipmentSnapshotDao.deleteByInterventionId(interventionId)
    }

    fun getEquipmentById(id: String) =
        equipmentDao.getEquipmentByServerId(id)

    suspend fun saveReport(interventionId: String, report: String) {
        interventionDao.saveReport(interventionId, report)
    }

    // Méthode au niveau de la classe — pas à l'intérieur de pull()
    private suspend fun insertAllSafe(interventions: List<InterventionEntity>) {
        interventions.forEach { entity ->
            // Pull ne crée ni ne met à jour les lignes clôturées (même si le serveur les renvoyait).
            if (entity.status in IMMUTABLE_INTERVENTION_STATUSES_FOR_PULL) {
                return@forEach
            }
            val existing = interventionDao.getInterventionByIdOnce(entity.id)

            when {
                existing?.status in IMMUTABLE_INTERVENTION_STATUSES_FOR_PULL -> {
                    android.util.Log.d("InsertAllSafe", "→ skip closed local ${entity.id}")
                }

                existing == null -> {
                    interventionDao.insertOrReplace(entity)
                }

                existing.syncStatus in listOf("IN_PROGRESS", "PENDING") -> {
                    android.util.Log.d("InsertAllSafe", "→ skip ${entity.id} (local=${existing.syncStatus})")
                }

                else -> {
                    interventionDao.insertOrReplace(entity)
                }
            }
        }
    }

    fun getInterventionByCustomerId(customerId: String) =
        interventionDao.getInterventionByCustomerId(customerId)

    suspend fun pull(date: LocalDate, force: Boolean = false): SyncResult {
        return try {
            // Throttle — skip si pull < 5 min et pas forcé
            if (!force) {
                val localSettings = settingsDao.getSettingsOnce()
                val lastPull = localSettings?.lastPulledAt
                if (lastPull != null) {
                    val elapsed = java.time.Instant.now().toEpochMilli() -
                        java.time.Instant.parse(lastPull).toEpochMilli()
                    if (elapsed < 5 * 60 * 1000) {
                        android.util.Log.d(
                            "SYNC",
                            "Pull throttled — dernier pull il y a ${elapsed / 1000}s"
                        )
                        return SyncResult.Success
                    }
                }
            }

            val dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE)

            val localSettings = settingsDao.getSettingsOnce()
            val ifModifiedSince = localSettings?.lastPulledAt

            val response = syncApi.pull(
                date = dateStr,
                ifModifiedSince = ifModifiedSince
            )
            

            val settings = response.settings ?: SettingsDto()

            val interventionEntities = response.interventions.map { it.toEntity(response.pulledAt) }

            // Supprimer les équipements des interventions qui ne sont plus dans la liste retournée
            // par le serveur pour cette date (interventions SYNCED du jour uniquement).
            // Ne pas supprimer les équipements des interventions d'autres dates — le nettoyage
            // global repose sur interventionDao.deleteOlderThan et deleteOlderThan sur les équipements.
            val returnedInterventionIds = response.interventions.map { it.id }
            if (returnedInterventionIds.isEmpty()) {
                equipmentDao.deleteEquipmentsForAllSyncedInterventionsOnDate(dateStr)
                interventionDao.deleteSyncedOpenInterventionsForDateWhenPullEmpty(dateStr)
            } else {
                equipmentDao.deleteEquipmentsForSyncedInterventionsNotInKeepList(
                    dateStr,
                    returnedInterventionIds,
                )
                interventionDao.deleteSyncedOpenForDateNotInKeepList(
                    dateStr,
                    returnedInterventionIds,
                )
            }

            // Appel correct ici
            insertAllSafe(interventionEntities)

            val cutoff48h = java.time.Instant.now()
                .minusSeconds(48L * 60 * 60)
                .toString()
            interventionDao.resetStaleInProgressToScheduled(cutoff48h)

            response.interventions.forEach { dto ->
                val existing = interventionDao.getInterventionByIdOnce(dto.id)
                val preserveLocalClosed =
                    existing?.status in IMMUTABLE_INTERVENTION_STATUSES_FOR_PULL
                if (!preserveLocalClosed && dto.actualTypes.isNotEmpty()) {
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




            // Dédupliquer les équipements — prioriser l'intervention active
            val equipmentMap = mutableMapOf<String, EquipmentEntity>()
            response.interventions
                .sortedByDescending { it.status == INTERVENTION_STATUS_COMPLETED }
                .forEach { intervention ->
                    val existingIntervention =
                        interventionDao.getInterventionByIdOnce(intervention.id)
                    val blockLocalSync = existingIntervention?.syncStatus == "IN_PROGRESS" ||
                        existingIntervention?.syncStatus == "PENDING"

                    // Aligné sur deleteEquipmentsNotInList : seul IN_PROGRESS / PENDING bloque le serveur
                    if (!blockLocalSync) {
                        intervention.equipment.forEachIndexed { index, eq ->
                            val catalogBrandId = eq.catalogBrandId?.takeIf { it.isNotBlank() }
                                ?: eq.equipmentCatalogId?.let { cid ->
                                    catalogEquipmentDao.getBrandIdForCatalogEquipment(cid)
                                }
                            val ord = eq.order ?: (index + 1)
                            val mapKey = "${intervention.id}_$ord"
                            equipmentMap[mapKey] = EquipmentEntity(
                                interventionId = intervention.id,
                                order = ord,
                                id = eq.id,
                                unitId = intervention.unit.id,
                                brand = eq.brand,
                                model = eq.model,
                                typeCode = eq.typeCode,
                                energyCode = eq.energyCode,
                                serialNumber = eq.serialNumber,
                                installDate = eq.installDate,
                                isPrimary = eq.isPrimary,
                                equipmentCatalogId = eq.equipmentCatalogId,
                                catalogBrandId = catalogBrandId,
                                parentEquipmentId = eq.parentEquipmentId,
                                powerKw = eq.powerKw?.let { p ->
                                    if (p % 1.0 == 0.0) p.toInt().toString() else p.toString()
                                },
                                evacuationMode = eq.evacuationMode,
                                hybridePacEquipmentId = eq.hybridePacEquipmentId,
                                attrsJson = eq.attrs?.toString(),
                            )
                        }
                    }
                }

            // Pour chaque intervention de la réponse : retirer les équipements de l'intervention
            // absents côté serveur (sauf sync local IN_PROGRESS / PENDING).
            response.interventions.forEach { intervention ->
                val existingIntervention =
                    interventionDao.getInterventionByIdOnce(intervention.id)
                val isInProgress = existingIntervention?.syncStatus == "IN_PROGRESS"
                val isPendingPush = existingIntervention?.syncStatus == "PENDING"

                if (!isInProgress && !isPendingPush) {
                    val serverOrders = intervention.equipment.mapIndexed { index, eq ->
                        eq.order ?: (index + 1)
                    }
                    equipmentDao.deleteEquipmentsNotInList(
                        interventionId = intervention.id,
                        keepOrders = if (serverOrders.isEmpty()) {
                            listOf(-1)
                        } else {
                            serverOrders
                        },
                    )
                }
            }

            equipmentDao.insertAll(equipmentMap.values.toList())

            response.interventions.forEach { intervention ->
                val existingIntervention = interventionDao.getInterventionByIdOnce(intervention.id)
                val blockLocalSync = existingIntervention?.syncStatus == "IN_PROGRESS" ||
                    existingIntervention?.syncStatus == "PENDING"
                if (!blockLocalSync && intervention.pacMeasures.isNotEmpty()) {
                    pacMeasureRepository.mergeFromPull(intervention.pacMeasures, intervention.id)
                }
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

            response.interventions.forEach { intervention ->
                val existingIntervention = interventionDao.getInterventionByIdOnce(intervention.id)
                val isInProgress = existingIntervention?.syncStatus == "IN_PROGRESS"
                val isPendingPush = existingIntervention?.syncStatus == "PENDING"

                android.util.Log.d("EQ_PURGE",
                    "intervention=${intervention.id} " +
                            "willPurge=${!isInProgress && !isPendingPush} " +
                            "serverOrders=${
                                intervention.equipment.mapIndexed { index, eq ->
                                    eq.order ?: (index + 1)
                                }
                            }",
                )

                if (!isInProgress && !isPendingPush) {
                    val serverOrders = intervention.equipment.mapIndexed { index, eq ->
                        eq.order ?: (index + 1)
                    }
                    equipmentDao.deleteEquipmentsNotInList(
                        interventionId = intervention.id,
                        keepOrders = if (serverOrders.isEmpty()) {
                            listOf(-1)
                        } else {
                            serverOrders
                        },
                    )
                }
            }

            val cutoffDate = date.minusDays(7).format(DateTimeFormatter.ISO_LOCAL_DATE)
            equipmentDao.deleteOlderThan(cutoffDate)
            interventionDao.deleteOlderThan(cutoffDate)

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

    suspend fun getInProgressIntervention(): InterventionEntity? =
        interventionDao.findFirstInProgress()

    suspend fun abandonInterventionLocally(interventionId: String) {
        pendingOperationDao.deleteByInterventionId(interventionId)

        val intervention = interventionDao.getInterventionByIdOnce(interventionId)
        if (intervention != null) {
            val toDelete = equipmentDao
                .getEquipmentsByInterventionOnce(interventionId)
                .filter { it.order >= 101 }
            toDelete.forEach { eq ->
                equipmentDao.deleteByInterventionAndOrder(eq.interventionId, eq.order)
            }
        }

        val snapshots = equipmentSnapshotDao.getByInterventionId(interventionId)
        val restored = snapshots.map { snap ->
            EquipmentEntity(
                interventionId = snap.interventionId.ifBlank { interventionId },
                order = snap.order ?: 0,
                id = snap.equipmentId,
                unitId = snap.unitId ?: "",
                brand = snap.brand,
                model = snap.model,
                typeCode = snap.typeCode,
                energyCode = snap.energyCode,
                serialNumber = snap.serialNumber,
                installDate = snap.installDate,
                isPrimary = snap.isPrimary,
                equipmentCatalogId = snap.equipmentCatalogId,
                catalogBrandId = snap.catalogBrandId,
                parentEquipmentId = snap.parentEquipmentId,
                powerKw = null,
                evacuationMode = null,
                hybridePacEquipmentId = null,
            )
        }
        if (restored.isNotEmpty()) {
            equipmentDao.insertAll(restored)
        }

        coldMeasureDao.deleteByInterventionId(interventionId)

        measureRepository.deleteByInterventionId(interventionId)

        pacMeasureRepository.deleteByInterventionId(interventionId)

        equipmentSnapshotDao.deleteByInterventionId(interventionId)

        interventionActualTypeDao.deleteForIntervention(interventionId)
        interventionDao.resetToScheduledAfterAbandon(interventionId)
    }

    private companion object {
        const val INTERVENTION_STATUS_COMPLETED = "completed"

        /** Statuts d’intervention jamais créés ni mis à jour par le pull (ni supprimés par son nettoyage). */
        val IMMUTABLE_INTERVENTION_STATUSES_FOR_PULL = setOf(
            INTERVENTION_STATUS_COMPLETED,
            "pending_validation",
        )
    }
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
    pulledAt = pulledAt,
    isChantier = isChantier == true,
)