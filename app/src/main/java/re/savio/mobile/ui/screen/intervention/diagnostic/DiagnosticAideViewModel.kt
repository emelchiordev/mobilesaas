package re.savio.mobile.ui.screen.intervention.diagnostic

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import re.savio.mobile.data.local.entity.EquipmentEntity
import re.savio.mobile.data.remote.api.RagApi
import re.savio.mobile.data.remote.dto.RagSearchRequestDto
import re.savio.mobile.data.remote.dto.RagSearchResultDto
import re.savio.mobile.data.remote.dto.RagSynthesizeRequestDto
import re.savio.mobile.data.remote.dto.RagSynthesizeResponseDto
import re.savio.mobile.data.remote.dto.RagSynthesizeResultItemDto
import re.savio.mobile.observability.SavioSyncSentry
import re.savio.mobile.ui.utils.NetworkUtils
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

private const val MIN_SYNTHESIS_RESULTS = 3

data class DiagnosticAideUiState(
    val query: String = "",
    val results: List<RagSearchResultDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val hasSearched: Boolean = false,
    val equipmentFilterEnabled: Boolean = false,
    val selectedEquipment: EquipmentEntity? = null,
    val interventionId: String? = null,
    val synthesis: RagSynthesizeResponseDto? = null,
    val isSynthesizing: Boolean = false,
    val showSynthesizeButton: Boolean = true,
)

@HiltViewModel
class DiagnosticAideViewModel @Inject constructor(
    private val ragApi: RagApi,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DiagnosticAideUiState())
    val uiState: StateFlow<DiagnosticAideUiState> = _uiState.asStateFlow()

    fun init(interventionId: String, equipment: EquipmentEntity?) {
        _uiState.update {
            it.copy(
                interventionId = interventionId,
                selectedEquipment = equipment,
                equipmentFilterEnabled = equipment != null,
                query = it.query,
                results = emptyList(),
                error = null,
                hasSearched = false,
                isLoading = false,
                synthesis = null,
                isSynthesizing = false,
                showSynthesizeButton = true,
            )
        }
    }

    fun onQueryChange(value: String) {
        _uiState.update { it.copy(query = value, error = null) }
    }

    fun clearEquipmentFilter() {
        _uiState.update { it.copy(equipmentFilterEnabled = false) }
    }

    fun search() {
        val state = _uiState.value
        val trimmed = state.query.trim()
        if (trimmed.length < 2) {
            _uiState.update {
                it.copy(error = "Décrivez le problème en au moins 2 caractères.")
            }
            return
        }
        if (!NetworkUtils.isOnline(context)) {
            _uiState.update {
                it.copy(error = "Réseau requis pour l'aide au diagnostic.")
            }
            return
        }

        viewModelScope.launch {
            SavioSyncSentry.onDiagnosticSearchStarted(state.interventionId, trimmed.length)
            _uiState.update {
                it.copy(
                    isLoading = true,
                    error = null,
                    synthesis = null,
                    isSynthesizing = false,
                    showSynthesizeButton = true,
                )
            }
            try {
                val equipment = state.selectedEquipment
                val useFilter = state.equipmentFilterEnabled && equipment != null
                val response =
                    ragApi.search(
                        RagSearchRequestDto(
                            query = trimmed,
                            marque = if (useFilter) equipment.brand?.trim()?.takeIf { it.isNotEmpty() } else null,
                            modele = if (useFilter) equipment.model?.trim()?.takeIf { it.isNotEmpty() } else null,
                            equipmentType =
                                if (useFilter) {
                                    equipment.typeCode?.trim()?.takeIf { it.isNotEmpty() }
                                } else {
                                    null
                                },
                            limit = 5,
                            excludeInterventionId = state.interventionId,
                        ),
                    )
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        results = response.results,
                        hasSearched = true,
                    )
                }
                SavioSyncSentry.onDiagnosticSearchDone(
                    interventionId = state.interventionId,
                    resultCount = response.results.size,
                )
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        hasSearched = true,
                        error = humanReadableApiError(e),
                    )
                }
                SavioSyncSentry.onDiagnosticSearchDone(
                    interventionId = state.interventionId,
                    resultCount = 0,
                )
            }
        }
    }

    fun synthesize() {
        val state = _uiState.value
        if (state.results.size < MIN_SYNTHESIS_RESULTS || state.isSynthesizing) return
        if (!NetworkUtils.isOnline(context)) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSynthesizing = true) }
            try {
                val equipment = state.selectedEquipment
                val useFilter = state.equipmentFilterEnabled && equipment != null
                val response =
                    ragApi.synthesize(
                        RagSynthesizeRequestDto(
                            query = state.query.trim(),
                            marque = if (useFilter) equipment.brand?.trim()?.takeIf { it.isNotEmpty() } else null,
                            modele = if (useFilter) equipment.model?.trim()?.takeIf { it.isNotEmpty() } else null,
                            results =
                                state.results.map { result ->
                                    RagSynthesizeResultItemDto(
                                        observations = result.observations,
                                        interventionType = result.interventionType,
                                    )
                                },
                        ),
                    )
                _uiState.update {
                    it.copy(
                        isSynthesizing = false,
                        synthesis = response,
                        showSynthesizeButton = false,
                    )
                }
                SavioSyncSentry.onDiagnosticSynthesizeDone(
                    interventionId = state.interventionId,
                    resultCount = state.results.size,
                )
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        isSynthesizing = false,
                        showSynthesizeButton = false,
                    )
                }
            }
        }
    }

    private fun humanReadableApiError(e: Throwable): String =
        when (e) {
            is HttpException ->
                when (e.code()) {
                    401, 403 -> "Session expirée ou accès refusé."
                    else -> "Recherche indisponible (${e.code()})."
                }
            is IOException -> "Réseau requis pour l'aide au diagnostic."
            else -> e.message?.takeIf { it.isNotBlank() } ?: "Recherche indisponible."
        }
}
