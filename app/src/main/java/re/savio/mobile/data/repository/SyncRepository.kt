package re.savio.mobile.data.repository

import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.Flow
import re.savio.mobile.data.local.dao.AnomalyDraftDao
import re.savio.mobile.data.local.dao.AttestationVeDao
import re.savio.mobile.data.local.dao.AttestationVePointControleDao
import re.savio.mobile.data.local.dao.CatalogEquipmentDao
import re.savio.mobile.data.local.dao.ColdMeasureDao
import re.savio.mobile.data.local.dao.EquipmentDao
import re.savio.mobile.data.local.dao.EquipmentSnapshotDao
import re.savio.mobile.data.local.dao.InstallationCheckDao
import re.savio.mobile.data.local.dao.InterventionActualTypeDao
import re.savio.mobile.data.local.dao.InterventionDao
import re.savio.mobile.data.local.dao.InterventionHistoryDao
import re.savio.mobile.data.local.dao.AnomalyTypeDao
import re.savio.mobile.data.local.dao.PendingOperationDao
import re.savio.mobile.data.local.dao.ReferentielDao
import re.savio.mobile.data.local.dao.SettingsDao
import re.savio.mobile.data.local.entity.CivilityOptionEntity
import re.savio.mobile.data.local.entity.EnergyTypeEntity
import re.savio.mobile.data.local.entity.EquipmentEntity
import re.savio.mobile.data.local.entity.EquipmentSnapshotEntity
import re.savio.mobile.data.local.entity.EquipmentTypeEntity
import re.savio.mobile.data.local.entity.UnitTypeEntity
import re.savio.mobile.data.local.entity.InterventionActualTypeEntity
import re.savio.mobile.data.local.entity.InterventionEntity
import re.savio.mobile.util.UnitEnergyEquipmentInput
import re.savio.mobile.util.UnitEnergySummary
import re.savio.mobile.util.UnitEnergySummaryItem
import re.savio.mobile.util.shouldApplyFollowUpFromPull
import re.savio.mobile.data.local.entity.InterventionHistoryEntity
import re.savio.mobile.data.local.entity.toEntity
import re.savio.mobile.data.local.entity.toEntityFromLegacyReferentiel
import re.savio.mobile.data.local.entity.SettingsEntity
import re.savio.mobile.data.remote.api.SyncApi
import re.savio.mobile.data.remote.dto.InterventionDto
import re.savio.mobile.data.remote.dto.InterventionTypeDto
import re.savio.mobile.data.remote.dto.stableKey
import re.savio.mobile.util.buildInterventionPolicySnapshot
import re.savio.mobile.util.interventionPolicySnapshotToJson
import re.savio.mobile.util.policyOverridesToJson
import re.savio.mobile.util.policySnapshotDtoToJson
import re.savio.mobile.util.settingsToResolvedPolicies
import re.savio.mobile.util.InvoicePolicy
import re.savio.mobile.util.FieldModificationPolicy
import re.savio.mobile.util.ClosingPolicy
import re.savio.mobile.util.MobilePlanningPermissionPolicy
import re.savio.mobile.util.UserProfile
import re.savio.mobile.data.remote.dto.SettingsDto
import re.savio.mobile.util.SavioTimeZone
import re.savio.mobile.util.toScheduledAtIsoRange
import java.time.Instant
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
    private val anomalyTypeDao: AnomalyTypeDao,
    private val anomalyDraftDao: AnomalyDraftDao,
    private val installationCheckDao: InstallationCheckDao,
    private val attestationVeDao: AttestationVeDao,
    private val attestationVePointControleDao: AttestationVePointControleDao,
    private val measureRepository: MeasureRepository,
    private val pacMeasureRepository: PacMeasureRepository,
    private val installationCheckRepository: InstallationCheckRepository,
    private val photoRepository: PhotoRepository,
    private val invoiceRepository: InvoiceRepository,
    private val pushRepository: PushRepository,
    private val clientFinancialRepository: ClientFinancialRepository,
) {
    private val backgroundScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _interventionRemoteUpdates = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val interventionRemoteUpdates: SharedFlow<String> = _interventionRemoteUpdates.asSharedFlow()

    /** Dernière réponse pull — utilisée pour détecter un écart métier après démarrage. */
    @Volatile
    private var lastPullInterventionsById: Map<String, InterventionDto> = emptyMap()

    fun getEquipmentsByIntervention(interventionId: String) =
        equipmentDao.getEquipmentsByIntervention(interventionId)

    /**
     * Les équipements du logement sont copiés en local au pull mobile.
     * Si la liste est vide (ex. intervention ouverte avant fin du sync), on force un pull
     * sur la date planifiée pour hydrater depuis le serveur.
     */
    suspend fun hydrateInterventionEquipmentsIfEmpty(interventionId: String) {
        if (equipmentDao.getEquipmentsByInterventionOnce(interventionId).isNotEmpty()) return
        val intervention = interventionDao.getInterventionByIdOnce(interventionId) ?: return
        if (intervention.unitId.isBlank()) return
        if (intervention.syncStatus == "PENDING") return
        val pullDate =
            runCatching {
                Instant.parse(intervention.scheduledAt)
                    .atZone(SavioTimeZone.appZone)
                    .toLocalDate()
            }.getOrNull() ?: return
        pull(pullDate, force = true)
    }

    suspend fun computeEnergyBadgesByInterventionId(
        interventionIds: List<String>,
    ): Map<String, List<UnitEnergySummaryItem>> {
        if (interventionIds.isEmpty()) return emptyMap()
        return equipmentDao
            .getEquipmentsForInterventions(interventionIds)
            .groupBy { it.interventionId }
            .mapValues { (_, equipments) ->
                UnitEnergySummary.compute(
                    equipments.map {
                        UnitEnergyEquipmentInput(
                            energyCode = it.energyCode,
                            parentEquipmentId = it.parentEquipmentId,
                        )
                    },
                )
            }
    }

    suspend fun startIntervention(interventionId: String) {
        ensurePendingPlanningPushedOrThrow(interventionId)
        applyLocalInterventionStart(interventionId)
        backgroundScope.launch {
            runCatching {
                refreshInterventionVersionAfterStart(interventionId)
            }.onFailure {
                android.util.Log.w("SYNC", "Refresh post-démarrage échoué pour $interventionId", it)
            }
        }
    }

    private suspend fun applyLocalInterventionStart(interventionId: String) {
        interventionDao.resetOtherInProgressToScheduled(
            exceptInterventionId = interventionId,
        )
        val now = java.time.Instant.now().toString()
        interventionDao.markAsInProgress(interventionId, now)
        val existing = interventionDao.getInterventionByIdOnce(interventionId)
        if (existing?.policySnapshotJson.isNullOrBlank()) {
            val settings = settingsDao.getSettingsOnce()
            if (settings != null) {
                val resolved = settingsToResolvedPolicies(settings)
                val snapshot = buildInterventionPolicySnapshot(resolved)
                interventionDao.setPolicySnapshot(
                    interventionId,
                    interventionPolicySnapshotToJson(snapshot),
                )
            }
        }
        snapshotEquipments(interventionId)
    }

    private suspend fun ensurePendingPlanningPushedOrThrow(interventionId: String) {
        val pendingPlanning =
            pendingOperationDao.getPendingByInterventionIdOnce(interventionId)
                .any { it.type == "UPDATE_INTERVENTION" }
        if (!pendingPlanning) return
        when (val pushResult = pushRepository.push()) {
            is PushResult.Error ->
                throw IllegalStateException(
                    "Synchronisation planning requise avant de démarrer. Vérifiez votre connexion.",
                )
            else -> Unit
        }
    }

    private suspend fun refreshInterventionVersionAfterStart(interventionId: String) {
        val beforeIntervention = interventionDao.getInterventionByIdOnce(interventionId) ?: return
        val beforeEquipments = equipmentDao.getEquipmentsByInterventionOnce(interventionId)
        val pullDate = resolvePullDateForIntervention(beforeIntervention)
        pull(pullDate, force = true)
        val remoteDto = lastPullInterventionsById[interventionId]
        if (detectInterventionBusinessDrift(beforeIntervention, beforeEquipments, remoteDto)) {
            _interventionRemoteUpdates.tryEmit(interventionId)
        }
    }

    private suspend fun resolvePullDateForIntervention(intervention: InterventionEntity): LocalDate =
        runCatching {
            Instant.parse(intervention.scheduledAt)
                .atZone(SavioTimeZone.appZone)
                .toLocalDate()
        }.getOrNull() ?: LocalDate.now()

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
                evacuationMode = eq.evacuationMode,
                hybridePacEquipmentId = eq.hybridePacEquipmentId,
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
        val wasDemo =
            interventionDao.getInterventionByIdOnce(interventionId)?.isDemo == true
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

        // Félicitations démo : maj locale immédiate (sans attendre le prochain pull settings).
        if (wasDemo) {
            markDemoOnboardingCompletedLocally()
        }
    }

    private suspend fun markDemoOnboardingCompletedLocally() {
        val current = settingsDao.getSettingsOnce() ?: return
        if (current.demoOnboardingState == "completed") return
        settingsDao.updateDemoOnboardingState("completed")
    }

    fun getEquipmentById(id: String) =
        equipmentDao.getEquipmentByServerId(id)

    suspend fun saveReport(interventionId: String, report: String) {
        interventionDao.saveReport(interventionId, report)
    }

    suspend fun saveFollowUp(
        interventionId: String,
        required: Boolean,
        note: String?,
    ) {
        val trimmedNote =
            if (required) note?.trim()?.takeIf { it.isNotEmpty() } else null
        interventionDao.saveFollowUp(interventionId, required, trimmedNote)
    }

    /** Débloque les CONFLICT_VERSION orphelins (version déjà resync, rien à pousser côté planning). */
    suspend fun healStaleVersionConflicts() {
        val stuck = interventionDao.getVersionConflictInterventionsOnce()
        for (row in stuck) {
            val hasPendingPlanning =
                pendingOperationDao.getPendingByInterventionIdOnce(row.id)
                    .any { it.type == "UPDATE_INTERVENTION" }
            if (hasPendingPlanning) continue
            val operational =
                PushVersionSync.operationalSyncStatus(row.status, row.syncStatus, row.completedAt)
            interventionDao.setSyncStatus(row.id, operational)
            interventionDao.resetConflictResolveAttempts(row.id)
        }
    }

    private fun preserveLocalPolicySnapshot(
        entity: InterventionEntity,
        existing: InterventionEntity?,
    ): InterventionEntity = preserveInterventionPolicySnapshot(entity, existing)

    // Méthode au niveau de la classe — pas à l'intérieur de pull()
    private suspend fun mergeFollowUpFromPullIfNeeded(
        existing: InterventionEntity,
        remote: InterventionEntity,
    ) {
        if (!shouldApplyFollowUpFromPull(existing, remote)) return
        interventionDao.updateFollowUpFromPull(
            id = remote.id,
            required = remote.followUpRequired,
            note = remote.followUpNote,
            status = remote.followUpStatus,
        )
        android.util.Log.d(
            "InsertAllSafe",
            "→ merge follow-up ${remote.id} (${existing.followUpStatus} → ${remote.followUpStatus})",
        )
    }

    private suspend fun insertAllSafe(interventions: List<InterventionEntity>) {
        interventions.forEach { entity ->
            val existing = interventionDao.getInterventionByIdOnce(entity.id)

            // Pull ne crée ni ne remplace les lignes clôturées — mais le suivi « à revoir » peut être fusionné.
            if (entity.status in IMMUTABLE_INTERVENTION_STATUSES_FOR_PULL) {
                if (existing != null) {
                    mergeFollowUpFromPullIfNeeded(existing, entity)
                }
                return@forEach
            }

            when {
                existing?.status in IMMUTABLE_INTERVENTION_STATUSES_FOR_PULL -> {
                    mergeFollowUpFromPullIfNeeded(existing!!, entity)
                    android.util.Log.d("InsertAllSafe", "→ skip closed local ${entity.id}")
                }

                existing != null && existing.syncStatus in CONFLICT_SYNC_STATUSES_FOR_PULL -> {
                    android.util.Log.d(
                        "InsertAllSafe",
                        "→ overwrite conflict ${entity.id} (${existing.syncStatus})",
                    )
                    interventionDao.insertOrReplace(
                        mergePullInterventionWithLocalLifecycle(entity, existing),
                    )
                    interventionDao.markLocalChanges(entity.id, false)
                    interventionDao.resetConflictResolveAttempts(entity.id)
                }

                existing == null -> {
                    interventionDao.insertOrReplace(entity)
                }

                existing.syncStatus in PULL_PROTECTED_SYNC_STATUSES && existing.hasLocalChanges -> {
                    if (entity.version > existing.version) {
                        interventionDao.updateVersion(entity.id, entity.version)
                    }
                    android.util.Log.d(
                        "InsertAllSafe",
                        "→ skip ${entity.id} (local=${existing.syncStatus}, hasLocalChanges)",
                    )
                    re.savio.mobile.observability.SavioSyncSentry.onPullProtected(entity.id)
                }

                else -> {
                    interventionDao.insertOrReplace(
                        mergePullInterventionWithLocalLifecycle(entity, existing),
                    )
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

            // Date calendaire (yyyy-MM-dd) — filtre Room en plage ISO Europe/Paris (aligné pull API).
            val dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
            val dayRange = date.toScheduledAtIsoRange()

            val localSettings = settingsDao.getSettingsOnce()
            val ifModifiedSince = localSettings?.lastPulledAt

            val response = syncApi.pull(
                date = dateStr,
                ifModifiedSince = ifModifiedSince
            )

            lastPullInterventionsById = response.interventions.associateBy { it.id }

            val settings = response.settings ?: SettingsDto()

            val interventionEntities = response.interventions.map { it.toEntity(response.pulledAt) }

            // Supprimer les équipements des interventions qui ne sont plus dans la liste retournée
            // par le serveur pour cette date (interventions SYNCED du jour uniquement).
            // Ne pas supprimer les équipements des interventions d'autres dates — le nettoyage
            // global repose sur interventionDao.deleteOlderThan et deleteOlderThan sur les équipements.
            val returnedInterventionIds = response.interventions.map { it.id }
            if (returnedInterventionIds.isEmpty()) {
                equipmentDao.deleteEquipmentsForAllSyncedInterventionsOnDate(
                    dayRange.startIso,
                    dayRange.endIso,
                )
                interventionActualTypeDao.deleteForSyncedOpenInterventionsOnDate(
                    dayRange.startIso,
                    dayRange.endIso,
                )
                interventionDao.deleteSyncedOpenInterventionsForDateWhenPullEmpty(
                    dayRange.startIso,
                    dayRange.endIso,
                )
            } else {
                equipmentDao.deleteEquipmentsForSyncedInterventionsNotInKeepList(
                    dayRange.startIso,
                    dayRange.endIso,
                    returnedInterventionIds,
                )
                interventionActualTypeDao.deleteForSyncedOpenOnDateNotInKeepList(
                    dayRange.startIso,
                    dayRange.endIso,
                    returnedInterventionIds,
                )
                interventionDao.deleteSyncedOpenForDateNotInKeepList(
                    dayRange.startIso,
                    dayRange.endIso,
                    returnedInterventionIds,
                )
            }

            // Équipements avant interventions : évite d'ouvrir une intervention visible sans appareils locaux.
            mergePullEquipmentsFromResponse(response.interventions)

            insertAllSafe(interventionEntities)

            val cutoff48h = java.time.Instant.now()
                .minusSeconds(48L * 60 * 60)
                .toString()
            interventionDao.resetStaleInProgressToScheduled(cutoff48h)

            response.interventions.forEach { dto ->
                val existing = interventionDao.getInterventionByIdOnce(dto.id)
                val preserveLocalClosed =
                    existing?.status in IMMUTABLE_INTERVENTION_STATUSES_FOR_PULL
                if (
                    !preserveLocalClosed &&
                    dto.actualTypes.isNotEmpty() &&
                    interventionDao.getInterventionByIdOnce(dto.id) != null
                ) {
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
                        notes = h.notes,
                        typeCode = h.typeCode,
                        typeLabel = h.typeLabel,
                        typeColor = h.typeColor,
                        technicianFirstName = h.technicianFirstName,
                        technicianLastName = h.technicianLastName,
                        completedAsVe = h.completedAsVe,
                        photoKeys = if (h.photoKeys.isEmpty()) null
                        else Gson().toJson(h.photoKeys)
                    )
                }
            }
            if (historyEntities.isNotEmpty()) {
                interventionHistoryDao.insertAll(historyEntities)
            }

            response.interventions
                .mapNotNull { it.customer }
                .distinctBy { it.id }
                .forEach { customer ->
                    clientFinancialRepository.replaceFromSync(
                        clientId = customer.id,
                        documents = customer.financialDocuments.orEmpty(),
                        syncedAtIso = customer.financialSummarySyncedAt ?: response.pulledAt,
                    )
                }

            response.interventions.forEach { intervention ->
                val existingIntervention = interventionDao.getInterventionByIdOnce(intervention.id)
                val blockLocalSync = existingIntervention?.syncStatus == "IN_PROGRESS" ||
                    existingIntervention?.syncStatus == "PENDING"
                if (!blockLocalSync && intervention.pacMeasures.isNotEmpty()) {
                    pacMeasureRepository.mergeFromPull(intervention.pacMeasures, intervention.id)
                }
                if (!blockLocalSync && intervention.installationCheck != null) {
                    installationCheckRepository.mergeFromPull(
                        intervention.installationCheck,
                        intervention.id,
                    )
                }
            }

            val rootTypes = response.interventionTypes
            if (!rootTypes.isNullOrEmpty()) {
                referentielDao.deleteAllInterventionTypes()
                referentielDao.upsertInterventionTypes(rootTypes.map { it.toEntity() })
            } else {
                response.referentiels?.interventionTypes?.let { types ->
                    if (types.isNotEmpty()) {
                        referentielDao.deleteAllInterventionTypes()
                        referentielDao.upsertInterventionTypes(
                            types.map { it.toEntityFromLegacyReferentiel() },
                        )
                    }
                }
            }

            response.referentiels?.let { refs ->
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
                refs.unitTypes?.let { types ->
                    if (types.isNotEmpty()) {
                        referentielDao.deleteAllUnitTypes()
                        referentielDao.insertUnitTypes(
                            types.map { UnitTypeEntity(it.code, it.label, it.category) },
                        )
                    }
                }
                refs.civilityOptions?.let { options ->
                    if (options.isNotEmpty()) {
                        referentielDao.deleteAllCivilityOptions()
                        referentielDao.insertCivilityOptions(
                            options.map { CivilityOptionEntity(it.code, it.label) },
                        )
                    }
                }
            }

            response.anomalyTypes?.let { types ->
                if (types.isNotEmpty()) {
                    anomalyTypeDao.deleteAll()
                    anomalyTypeDao.insertAll(types.map { it.toEntity() })
                }
            }

            val resolved = response.technician.resolvedPolicies
            val previous = settingsDao.getSettingsOnce()
            val pendingCapacity = previous?.refrigerantCapacityAttestationPendingSync == true
            settingsDao.save(
                SettingsEntity(
                    id = 1,
                    allowCreateIntervention = settings.allowCreateIntervention,
                    allowProposal = settings.allowProposal,
                    vatRegime = settings.vatRegime,
                    canEditVatRate = settings.canEditVatRate,
                    lastPulledAt = response.pulledAt,
                    technicianId = response.technician.id,
                    technicianFirstName = response.technician.firstName,
                    technicianLastName = response.technician.lastName,
                    profile = response.technician.profile ?: UserProfile.ARTISAN_SOLO,
                    policyOverridesJson = policyOverridesToJson(response.technician.policyOverrides),
                    resolvedInvoices = resolved?.invoices ?: InvoicePolicy.AUTO_ISSUE,
                    resolvedPlanning = resolved?.planning ?: "FREE",
                    resolvedPlanningEdit = resolved?.planningEdit ?: MobilePlanningPermissionPolicy.LIMITED_EDIT,
                    resolvedFieldModifications = resolved?.fieldModifications ?: FieldModificationPolicy.AUTO_APPLY,
                    resolvedClosing = resolved?.closing ?: ClosingPolicy.TECH_CAN_CLOSE,
                    requireInvoiceValidation = response.technician.requireInvoiceValidation ?: false,
                    updatesRequireValidation = response.technician.updatesRequireValidation ?: false,
                    mobilePlanningPermission = response.technician.mobilePlanningPermission ?: "LIMITED_EDIT",
                    blockMobileFollowUpResolve = response.technician.blockMobileFollowUpResolve ?: false,
                    // Ne pas écraser un "completed" local par un "pending" serveur
                    // (race pull avant push de clôture démo).
                    demoOnboardingState =
                        if (
                            previous?.demoOnboardingState == "completed" &&
                            settings.demoOnboardingState == "pending"
                        ) {
                            "completed"
                        } else {
                            settings.demoOnboardingState
                        },
                    refrigerantCapacityAttestationNumber =
                        if (pendingCapacity) {
                            previous?.refrigerantCapacityAttestationNumber
                        } else {
                            settings.refrigerantCapacityAttestationNumber
                        },
                    refrigerantCapacityAttestationIsDemo =
                        if (pendingCapacity) {
                            previous?.refrigerantCapacityAttestationIsDemo ?: false
                        } else {
                            settings.refrigerantCapacityAttestationIsDemo
                        },
                    refrigerantCapacityAttestationPendingSync = pendingCapacity,
                )
            )

            val cutoffBeforeIso = date.minusDays(7).toScheduledAtIsoRange().startIso
            equipmentDao.deleteOlderThan(cutoffBeforeIso)
            interventionActualTypeDao.deleteOlderThanForOpenInterventions(cutoffBeforeIso)
            interventionDao.deleteOlderThan(cutoffBeforeIso)

            SyncResult.Success

        } catch (e: Exception) {
            android.util.Log.e("SYNC", "Pull échoué", e)
            SyncResult.Error(pullErrorMessage(e))
        }
    }

    fun getInterventionsByDate(date: LocalDate): Flow<List<InterventionEntity>> {
        val range = date.toScheduledAtIsoRange()
        return interventionDao.getInterventionsByDate(range.startIso, range.endIso)
    }

    fun observePendingFollowUp(): Flow<List<InterventionEntity>> =
        interventionDao.observePendingFollowUp()

    suspend fun hasCachedInterventionsForDate(date: LocalDate): Boolean {
        val range = date.toScheduledAtIsoRange()
        return interventionDao.countInterventionsByDate(range.startIso, range.endIso) > 0
    }

    fun getInterventionById(id: String) =
        interventionDao.getInterventionById(id)

    suspend fun getInterventionByIdOnce(id: String) =
        interventionDao.getInterventionByIdOnce(id)

    fun getPendingSyncCount() =
        interventionDao.getPendingSyncCount()

    suspend fun getHistoryForUnit(unitId: String): List<InterventionHistoryEntity> =
        interventionHistoryDao.getHistoryForUnit(unitId)

    suspend fun getLastCompletedVeForUnit(unitId: String): InterventionHistoryEntity? =
        interventionHistoryDao.getLastCompletedVeForUnit(unitId)

    suspend fun getInProgressIntervention(): InterventionEntity? =
        interventionDao.findFirstInProgress()

    suspend fun abandonInterventionLocally(interventionId: String) {
        pendingOperationDao.deleteByInterventionId(interventionId)

        anomalyDraftDao.deleteByInterventionId(interventionId)
        installationCheckDao.deleteByInterventionId(interventionId)
        attestationVePointControleDao.deleteByInterventionId(interventionId)
        attestationVeDao.deleteByInterventionId(interventionId)
        coldMeasureDao.deleteByInterventionId(interventionId)
        measureRepository.deleteByInterventionId(interventionId)
        pacMeasureRepository.deleteByInterventionId(interventionId)
        photoRepository.deleteAllForIntervention(interventionId)
        invoiceRepository.deleteDraftByIntervention(interventionId)

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
        val currentById =
            equipmentDao.getEquipmentsByInterventionOnce(interventionId).associateBy { it.id }
        val restored = snapshots.map { snap ->
            val current = currentById[snap.equipmentId]
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
                evacuationMode = snap.evacuationMode ?: current?.evacuationMode,
                hybridePacEquipmentId =
                    snap.hybridePacEquipmentId ?: current?.hybridePacEquipmentId,
            )
        }
        if (restored.isNotEmpty()) {
            equipmentDao.insertAll(restored)
        }

        equipmentSnapshotDao.deleteByInterventionId(interventionId)

        interventionActualTypeDao.deleteForIntervention(interventionId)
        interventionDao.resetToScheduledAfterAbandon(interventionId)
    }

    private suspend fun mergePullEquipmentsFromResponse(interventions: List<InterventionDto>) {
        val equipmentMap = mutableMapOf<String, EquipmentEntity>()
        interventions
            .sortedByDescending { it.status == INTERVENTION_STATUS_COMPLETED }
            .forEach { intervention ->
                val existingIntervention = interventionDao.getInterventionByIdOnce(intervention.id)
                val blockLocalSync = existingIntervention?.syncStatus == "IN_PROGRESS" ||
                    existingIntervention?.syncStatus == "PENDING"
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

        interventions.forEach { intervention ->
            val existingIntervention = interventionDao.getInterventionByIdOnce(intervention.id)
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

        if (equipmentMap.isNotEmpty()) {
            equipmentDao.insertAll(equipmentMap.values.toList())
        }
    }

    private fun pullErrorMessage(e: Exception): String {
        val raw = e.message.orEmpty()
        return if (raw.contains("FOREIGN KEY", ignoreCase = true)) {
            "Synchronisation impossible. Réessayez dans un instant."
        } else {
            e.message ?: "Erreur de synchronisation"
        }
    }

    private companion object {
        const val INTERVENTION_STATUS_COMPLETED = "completed"

        /** Statuts d’intervention jamais créés ni mis à jour par le pull (ni supprimés par son nettoyage). */
        val IMMUTABLE_INTERVENTION_STATUSES_FOR_PULL = setOf(
            INTERVENTION_STATUS_COMPLETED,
            "pending_validation",
        )

        val CONFLICT_SYNC_STATUSES_FOR_PULL = setOf(
            "CONFLICT_IMMUTABLE",
            "CONFLICT_VERSION",
            "CONFLICT",
        )

        val PULL_PROTECTED_SYNC_STATUSES = setOf(
            "IN_PROGRESS",
            "PENDING",
            "COMPLETED",
        )
    }
}

private fun InterventionDto.toEntity(pulledAt: String) = InterventionEntity(
    id = id,
    scheduledAt = scheduledAt,
    timeSlot = timeSlot ?: "matin",
    isUrgent = isUrgent == true,
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
    contractStatus = contract?.status,
    contractRenewalDate = contract?.renewalDate,
    contractTariff = contract?.tariff,
    contractVatRate = contract?.vatRate,
    report = report,
    completedAt = completedAt,
    startedAt = startedAt,
    pulledAt = pulledAt,
    isChantier = isChantier == true,
    version = version,
    followUpRequired = followUpRequired == true,
    followUpNote = followUpNote,
    followUpStatus = followUpStatus?.trim()?.takeIf { it.isNotEmpty() } ?: if (followUpRequired == true) "pending" else "none",
    unitVeCoverageUnavailable = unit.coverageUnavailable,
    unitVeCoverageAttested = unit.coverageAttested,
    unitVeCoverageExpected = unit.coverageExpected,
    unitVeCoverageComplete = unit.coverageComplete,
    policySnapshotJson = policySnapshotDtoToJson(policySnapshot),
    isDemo = isDemo == true,
)