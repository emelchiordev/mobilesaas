package re.savio.mobile.ui.screen.client

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import re.savio.mobile.data.remote.dto.CustomerSearchRowDto
import re.savio.mobile.data.repository.CustomerSearchOutcome
import re.savio.mobile.data.repository.InterventionCreateRepository
import javax.inject.Inject

data class ClientsUiState(
    val searchQuery: String = "",
    val results: List<CustomerSearchRowDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

@OptIn(FlowPreview::class)
@HiltViewModel
class ClientsViewModel @Inject constructor(
    private val interventionCreateRepository: InterventionCreateRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClientsUiState())
    val uiState: StateFlow<ClientsUiState> = _uiState.asStateFlow()

    private val _searchInput = MutableStateFlow("")

    init {
        viewModelScope.launch {
            _searchInput
                .debounce(350)
                .distinctUntilChanged()
                .collectLatest { raw ->
                    val trimmed = raw.trim()
                    if (trimmed.isEmpty()) {
                        _uiState.update {
                            it.copy(
                                results = emptyList(),
                                isLoading = false,
                                error = null,
                            )
                        }
                        return@collectLatest
                    }
                    _uiState.update { it.copy(isLoading = true, error = null) }
                    when (val outcome = interventionCreateRepository.searchCustomers(trimmed, limit = 20)) {
                        is CustomerSearchOutcome.Success ->
                            _uiState.update {
                                it.copy(
                                    results = outcome.items,
                                    isLoading = false,
                                    error = null,
                                )
                            }

                        is CustomerSearchOutcome.NetworkError ->
                            _uiState.update {
                                it.copy(
                                    results = emptyList(),
                                    isLoading = false,
                                    error = "Hors ligne",
                                )
                            }

                        is CustomerSearchOutcome.Error ->
                            _uiState.update {
                                it.copy(
                                    results = emptyList(),
                                    isLoading = false,
                                    error = outcome.message,
                                )
                            }
                    }
                }
        }
    }

    fun onSearchChange(value: String) {
        _uiState.update { it.copy(searchQuery = value) }
        _searchInput.value = value
    }
}
