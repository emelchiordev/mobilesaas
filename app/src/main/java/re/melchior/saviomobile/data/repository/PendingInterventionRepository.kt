package re.melchior.saviomobile.data.repository

import re.melchior.saviomobile.data.local.dao.PendingInterventionDao
import re.melchior.saviomobile.data.local.entity.PendingInterventionEntity
import re.melchior.saviomobile.data.remote.api.SyncApi
import re.melchior.saviomobile.data.remote.dto.MobilePendingInterventionRequestDto
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PendingInterventionRepository @Inject constructor(
    private val dao: PendingInterventionDao,
    private val syncApi: SyncApi,
) {

    fun countPending(): Flow<Int> = dao.countPending()

    fun observeAll(): Flow<List<PendingInterventionEntity>> = dao.getPending()

    fun observePendingForDate(date: LocalDate): Flow<List<PendingInterventionEntity>> {
        val (dayStart, dayEnd) = date.dayRangeMillis()
        return dao.getPendingForDate(dayStart, dayEnd)
    }

    suspend fun hasVisiblePendingForDate(date: LocalDate): Boolean {
        val (dayStart, dayEnd) = date.dayRangeMillis()
        return dao.countPendingForDate(dayStart, dayEnd) > 0
    }

    suspend fun insert(entity: PendingInterventionEntity) {
        dao.insert(entity)
    }

    suspend fun syncPendingQueue() {
        val rows = dao.listPendingToSync()
        for (row in rows) {
            try {
                val scheduledIso = Instant.ofEpochMilli(row.scheduledAt).atOffset(ZoneOffset.UTC).toString()
                val res = syncApi.postPendingIntervention(
                    MobilePendingInterventionRequestDto(
                        localId = row.localId,
                        clientNameFree = row.clientNameFree,
                        addressFree = row.addressFree,
                        city = row.city,
                        zipCode = row.zipCode,
                        phone = row.phone,
                        interventionType = row.interventionType,
                        scheduledAt = scheduledIso,
                        notes = row.notes,
                    ),
                )
                dao.updateSyncStatus(row.localId, "SYNCED", res.remoteId)
            } catch (e: HttpException) {
                if (e.code() in 400..499) {
                    dao.updateSyncStatus(row.localId, "ERROR", null)
                } else {
                    throw e
                }
            }
        }
    }

    private fun LocalDate.dayRangeMillis(zone: ZoneId = ZoneId.systemDefault()): Pair<Long, Long> {
        val dayStart = atStartOfDay(zone).toInstant().toEpochMilli()
        val dayEnd = plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        return dayStart to dayEnd
    }
}
