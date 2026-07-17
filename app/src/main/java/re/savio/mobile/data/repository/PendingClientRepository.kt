package re.savio.mobile.data.repository

import android.util.Log
import androidx.room.withTransaction
import re.savio.mobile.data.local.dao.InterventionDao
import re.savio.mobile.data.local.dao.PendingClientDao
import re.savio.mobile.data.local.dao.PendingUpdateDao
import re.savio.mobile.data.local.dao.PhotoDao
import re.savio.mobile.data.local.database.SavioDatabase
import re.savio.mobile.data.local.entity.PendingClientEntity
import re.savio.mobile.data.remote.api.ClientApi
import re.savio.mobile.data.remote.dto.CreateClientLogementDto
import re.savio.mobile.data.remote.dto.CreateClientOccupancyDto
import re.savio.mobile.data.remote.dto.CreateTenantClientRequestDto
import retrofit2.HttpException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PendingClientRepository @Inject constructor(
    private val database: SavioDatabase,
    private val dao: PendingClientDao,
    private val clientApi: ClientApi,
    private val interventionDao: InterventionDao,
    private val photoDao: PhotoDao,
    private val pendingUpdateDao: PendingUpdateDao,
) {

    suspend fun insert(client: PendingClientEntity) {
        dao.insert(client)
    }

    suspend fun syncPendingClients(): Boolean {
        val pending = dao.getPending()
        if (pending.isEmpty()) return true

        var allOk = true
        for (client in pending) {
            try {
                val response = clientApi.createClient(client.toRequestDto())
                val remoteCustomerId = response.resolvedCustomerId()
                val remoteUnitId = response.unitId
                database.withTransaction {
                    remapLocalId(
                        localId = client.localId,
                        remoteId = remoteCustomerId,
                        remoteUnitId = remoteUnitId,
                    )
                    dao.updateSyncStatus(
                        client.localId,
                        "SYNCED",
                        remoteCustomerId,
                        remoteUnitId,
                    )
                }
                Log.i(
                    TAG,
                    "Client sync OK localId=${client.localId} → customer=$remoteCustomerId unit=$remoteUnitId",
                )
            } catch (e: HttpException) {
                if (e.code() in 400..499) {
                    dao.updateSyncStatus(client.localId, "ERROR", null, null)
                    Log.e(TAG, "Client rejeté (${e.code()}) localId=${client.localId}")
                } else {
                    allOk = false
                    Log.w(TAG, "Client sync réseau localId=${client.localId}: ${e.message}")
                }
            } catch (e: Exception) {
                allOk = false
                Log.w(TAG, "Client sync erreur localId=${client.localId}: ${e.message}")
            }
        }
        return allOk
    }

    /** Alias utilisé par l’orchestrateur de sync. */
    suspend fun syncPendingQueue(): Boolean = syncPendingClients()

    private suspend fun remapLocalId(
        localId: String,
        remoteId: String,
        remoteUnitId: String?,
    ) {
        interventionDao.remapCustomerId(localId, remoteId)
        photoDao.remapCustomerId(localId, remoteId)
        pendingUpdateDao.remapTargetId(localId, remoteId)
        if (!remoteUnitId.isNullOrBlank()) {
            interventionDao.remapUnitId(localId, remoteUnitId)
            photoDao.remapUnitId(localId, remoteUnitId)
            pendingUpdateDao.remapTargetId(localId, remoteUnitId)
        }
    }

    private fun PendingClientEntity.toRequestDto(): CreateTenantClientRequestDto {
        val logement =
            CreateClientLogementDto(
                street = address,
                postalCode = zipCode,
                city = city,
                latitude = lat,
                longitude = lng,
                addressLine2 = addressComplement?.takeIf { it.isNotBlank() },
                floor = floor?.takeIf { it.isNotBlank() },
                unitType = unitType,
                unitCategory = unitCategory,
            )
        return CreateTenantClientRequestDto(
            firstName = firstName,
            lastName = lastName,
            civility = civility,
            phone = phone?.takeIf { it.isNotBlank() },
            email = email?.takeIf { it.isNotBlank() },
            logement = logement,
            occupancy =
                CreateClientOccupancyDto(
                    role = "tenant",
                    startDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
                ),
        )
    }

    private companion object {
        const val TAG = "PendingClientSync"
    }
}
