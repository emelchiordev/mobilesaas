package re.melchior.saviomobile.ui.screen.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import re.melchior.saviomobile.data.remote.dto.SocieteDto
import re.melchior.saviomobile.data.repository.AuthRepository
import re.melchior.saviomobile.data.repository.AuthResult
import javax.inject.Inject

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isLoggedIn: Boolean = false,
    val societesToChoose: List<SocieteDto> = emptyList()
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email, errorMessage = null) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password, errorMessage = null) }
    }

    fun login() {
        val state = _uiState.value
        loginWithCredentials(state.email, state.password)
    }

    /**
     * Connexion explicite (écran login ou après activation) : persistance JWT via [AuthRepository.login].
     */
    fun loginWithCredentials(email: String, password: String) {
        val em = email.trim()
        if (em.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Email et mot de passe requis") }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    email = em,
                    password = password,
                    societesToChoose = emptyList(),
                )
            }

            when (val result = authRepository.login(em, password)) {
                is AuthResult.Success -> {
                    _uiState.update { it.copy(isLoading = false, isLoggedIn = true) }
                }
                is AuthResult.ChooseSociete -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            societesToChoose = result.societes,
                        )
                    }
                }
                is AuthResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message,
                        )
                    }
                }
            }
        }
    }

    fun selectSociete(societe: SocieteDto, isRegistrationFlow: Boolean = false, onCompleted: () -> Unit = {}) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            authRepository.selectSociete(societe, isRegistrationFlow)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    isLoggedIn = true,
                    societesToChoose = emptyList(),
                )
            }
            onCompleted()
        }
    }

    fun importSocietesForSelection(societes: List<SocieteDto>) {
        _uiState.update { it.copy(societesToChoose = societes) }
    }

    fun logout(onLoggedOut: () -> Unit = {}) {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.update { AuthUiState() }
            onLoggedOut()
        }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}