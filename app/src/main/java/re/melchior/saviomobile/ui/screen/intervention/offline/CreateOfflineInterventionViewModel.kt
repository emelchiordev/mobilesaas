package re.melchior.saviomobile.ui.screen.intervention.offline

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import re.melchior.saviomobile.ui.screen.intervention.create.buildPlanningScheduledAtMillis
import re.melchior.saviomobile.ui.screen.intervention.create.defaultScheduledMillis
import re.melchior.saviomobile.util.InterventionTimeSlot
import java.time.LocalDate
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import re.melchior.saviomobile.data.local.dao.ReferentielDao
import re.melchior.saviomobile.data.local.entity.InterventionTypeEntity
import re.melchior.saviomobile.data.local.entity.PendingInterventionEntity
import re.melchior.saviomobile.data.repository.PendingInterventionRepository
import re.melchior.saviomobile.worker.PendingInterventionSyncWorker
import androidx.work.WorkManager
import java.util.UUID
import javax.inject.Inject

sealed interface CreateOfflineInterventionEvent {
    data object Saved : CreateOfflineInterventionEvent
}

@HiltViewModel
class CreateOfflineInterventionViewModel @Inject constructor(
    private val pendingInterventionRepository: PendingInterventionRepository,
    private val referentielDao: ReferentielDao,
    private val workManager: WorkManager,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val initialScheduledAtMillis: Long = initialScheduledAtFromNavArgs(savedStateHandle)

    val interventionTypes: StateFlow<List<InterventionTypeEntity>> =
        referentielDao.getCreateInterventionTypes()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _events = Channel<CreateOfflineInterventionEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun save(
        clientNameFree: String,
        addressFree: String,
        city: String?,
        zipCode: String?,
        phone: String?,
        interventionTypeCode: String,
        scheduledAtMillis: Long,
        notes: String?,
    ) {
        viewModelScope.launch {
            val localId = UUID.randomUUID().toString()
            pendingInterventionRepository.insert(
                PendingInterventionEntity(
                    localId = localId,
                    clientNameFree = clientNameFree.trim(),
                    addressFree = addressFree.trim(),
                    city = city?.trim()?.takeIf { it.isNotEmpty() },
                    zipCode = zipCode?.trim()?.takeIf { it.isNotEmpty() },
                    phone = phone?.trim()?.takeIf { it.isNotEmpty() },
                    interventionType = interventionTypeCode,
                    scheduledAt = scheduledAtMillis,
                    notes = notes?.trim()?.takeIf { it.isNotEmpty() },
                    syncStatus = "PENDING",
                    remoteId = null,
                ),
            )
            PendingInterventionSyncWorker.enqueue(workManager)
            _events.send(CreateOfflineInterventionEvent.Saved)
        }
    }

    private companion object {
        fun initialScheduledAtFromNavArgs(savedStateHandle: SavedStateHandle): Long {
            val raw = savedStateHandle.get<String>("defaultDate")?.takeIf { it.isNotBlank() }
            if (raw != null) {
                runCatching { LocalDate.parse(raw) }
                    .map { buildPlanningScheduledAtMillis(it, InterventionTimeSlot.MATIN, "") }
                    .getOrNull()
                    ?.let { return it }
            }
            return defaultScheduledMillis()
        }
    }
}
