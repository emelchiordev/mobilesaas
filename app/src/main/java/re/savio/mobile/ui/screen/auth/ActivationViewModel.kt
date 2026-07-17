package re.savio.mobile.ui.screen.auth

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import re.savio.mobile.data.remote.dto.SocieteDto
import re.savio.mobile.data.repository.ActivateAccountResult
import re.savio.mobile.data.repository.AuthRepository

sealed interface ActivationUiState {
    data object Verifying : ActivationUiState

    /** JWT enregistré — navigation vers l’accueil. */
    data object Authenticated : ActivationUiState

    data object NeedsAccountPrep : ActivationUiState

    data class ChooseSociete(val societes: List<SocieteDto>) : ActivationUiState

    data class Error(val message: String) : ActivationUiState
}

@HiltViewModel
class ActivationViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val token: String = savedStateHandle.get<String>("token").orEmpty()

    private val _uiState = MutableStateFlow<ActivationUiState>(ActivationUiState.Verifying)
    val uiState: StateFlow<ActivationUiState> = _uiState.asStateFlow()

    init {
        if (token.isBlank()) {
            _uiState.value = ActivationUiState.Error("Lien d’activation invalide.")
        } else {
            verify()
        }
    }

    fun verify() {
        if (token.isBlank()) return
        viewModelScope.launch {
            _uiState.value = ActivationUiState.Verifying
            when (val r = authRepository.activateAccount(token)) {
                ActivateAccountResult.Authenticated ->
                    _uiState.value = ActivationUiState.Authenticated
                ActivateAccountResult.NeedsAccountPrep ->
                    _uiState.value = ActivationUiState.NeedsAccountPrep
                is ActivateAccountResult.ChooseSociete ->
                    _uiState.value = ActivationUiState.ChooseSociete(r.societes)
                is ActivateAccountResult.Error ->
                    _uiState.value = ActivationUiState.Error(r.message)
            }
        }
    }
}
