package re.melchior.saviomobile.data.repository

import android.provider.Settings
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import re.melchior.saviomobile.data.local.dao.InterventionDao
import re.melchior.saviomobile.data.remote.api.PushApi
import re.melchior.saviomobile.data.remote.dto.PushOperationDto
import re.melchior.saviomobile.data.remote.dto.PushRequestDto
import re.melchior.saviomobile.data.local.entity.InterventionEntity
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

sealed class PushResult {
    object Success : PushResult()
    object NothingToPush : PushResult()
    data class Error(val message: String) : PushResult()
}

@Singleton
class PushRepository @Inject constructor(
    private val pushApi: PushApi,
    private val interventionDao: InterventionDao,
    @ApplicationContext private val context: Context
) {

    suspend fun push(): PushResult {
        return try {
            // Récupérer toutes les interventions à synchroniser
            val pending = interventionDao.getPendingSyncOnce()

            if (pending.isEmpty()) return PushResult.NothingToPush

            // Construire les opérations
            val operations = mutableListOf<PushOperationDto>()

            pending.forEach { intervention ->
                // Opération START_INTERVENTION si startedAt renseigné
                intervention.startedAt?.let { startedAt ->
                    operations.add(
                        PushOperationDto(
                            id = "op-start-${intervention.id}",
                            type = "START_INTERVENTION",
                            occurredAt = startedAt,
                            payload = mapOf(
                                "interventionId" to intervention.id,
                                "startedAt" to startedAt
                            )
                        )
                    )
                }

                // Opération COMPLETE_INTERVENTION si completedAt renseigné
                intervention.completedAt?.let { completedAt ->
                    operations.add(
                        PushOperationDto(
                            id = "op-complete-${intervention.id}",
                            type = "COMPLETE_INTERVENTION",
                            occurredAt = completedAt,
                            payload = mapOf(
                                "interventionId" to intervention.id,
                                "report" to (intervention.report ?: ""),
                                "completedAt" to completedAt
                            )
                        )
                    )
                }
            }

            if (operations.isEmpty()) return PushResult.NothingToPush

            // Récupérer deviceId stable
            val deviceId = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ANDROID_ID
            ) ?: "unknown-device"

            // Envoyer le batch
            val response = pushApi.push(
                PushRequestDto(
                    deviceId = deviceId,
                    pushedAt = Instant.now().toString(),
                    operations = operations
                )
            )

            // Traiter les résultats opération par opération
            response.results.forEach { result ->
                val interventionId = extractInterventionId(result.operationId)
                    ?: return@forEach

                when (result.status) {
                    "ok" -> {
                        // Si c'est une opération de clôture → SYNCED
                        if (result.operationId.startsWith("op-complete-")) {
                            interventionDao.markAsSynced(interventionId)
                        }
                    }
                    "conflict" -> {
                        interventionDao.markAsConflict(interventionId)
                    }
                    "rejected" -> {
                        interventionDao.markAsConflict(interventionId)
                    }
                }
            }

            PushResult.Success

        } catch (e: Exception) {
            PushResult.Error(e.message ?: "Erreur de synchronisation")
        }
    }

    private fun extractInterventionId(operationId: String): String? {
        return when {
            operationId.startsWith("op-start-") ->
                operationId.removePrefix("op-start-")
            operationId.startsWith("op-complete-") ->
                operationId.removePrefix("op-complete-")
            else -> null
        }
    }
}