package re.savio.mobile.ui.screen.intervention.offline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import re.savio.mobile.data.local.entity.PendingInterventionEntity
import re.savio.mobile.data.repository.PendingInterventionRepository
import javax.inject.Inject

@HiltViewModel
class PendingOfflineListViewModel @Inject constructor(
    repository: PendingInterventionRepository,
) : ViewModel() {

    val items: StateFlow<List<PendingInterventionEntity>> = repository
        .observeAll()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList(),
        )
}
