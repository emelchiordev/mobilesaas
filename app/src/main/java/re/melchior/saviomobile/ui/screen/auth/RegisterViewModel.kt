package re.melchior.saviomobile.ui.screen.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import re.melchior.saviomobile.data.remote.dto.SocieteDto
import re.melchior.saviomobile.data.repository.AuthRepository
import re.melchior.saviomobile.data.repository.RegisterResult
import re.melchior.saviomobile.data.repository.ResendRegistrationEmailResult
import javax.inject.Inject

data class RegisterUiState(
    val contactFullName: String = "",
    val companyName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val contactFullNameError: String? = null,
    val companyNameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    /** Compte créé : écran « vérifiez votre mail » (pas de JWT). */
    val pendingVerificationEmail: String? = null,
    val isResendingVerificationEmail: Boolean = false,
    val resendVerificationFeedback: String? = null,
    val resendVerificationError: String? = null,
)

sealed interface RegisterNavEvent {
    data object Onboarding : RegisterNavEvent

    data class ChooseSociete(val societes: List<SocieteDto>) : RegisterNavEvent
}

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    private val _navEvents = Channel<RegisterNavEvent>(capacity = Channel.BUFFERED)
    val navEvents = _navEvents.receiveAsFlow()

    fun onContactFullNameChange(value: String) {
        _uiState.update {
            it.copy(contactFullName = value, contactFullNameError = null, errorMessage = null)
        }
    }

    fun onCompanyNameChange(value: String) {
        _uiState.update {
            it.copy(companyName = value, companyNameError = null, errorMessage = null)
        }
    }

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, emailError = null, errorMessage = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, passwordError = null, errorMessage = null) }
    }

    fun onConfirmPasswordChange(value: String) {
        _uiState.update {
            it.copy(confirmPassword = value, confirmPasswordError = null, errorMessage = null)
        }
    }

    fun resendVerificationEmail() {
        val email = _uiState.value.pendingVerificationEmail ?: return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isResendingVerificationEmail = true,
                    resendVerificationError = null,
                    resendVerificationFeedback = null,
                )
            }
            when (
                val r = authRepository.resendRegistrationEmail(email)
            ) {
                is ResendRegistrationEmailResult.Success ->
                    _uiState.update {
                        it.copy(
                            isResendingVerificationEmail = false,
                            resendVerificationFeedback = r.message,
                            resendVerificationError = null,
                        )
                    }

                is ResendRegistrationEmailResult.Error ->
                    _uiState.update {
                        it.copy(
                            isResendingVerificationEmail = false,
                            resendVerificationError = r.message,
                        )
                    }
            }
        }
    }

    fun submit() {
        val s = _uiState.value
        var ok = true
        var contactErr: String? = null
        var companyErr: String? = null
        var emailErr: String? = null
        var passErr: String? = null
        var confirmErr: String? = null

        val contactTrim = s.contactFullName.trim()
        if (contactTrim.isNotEmpty() && contactTrim.length < 2) {
            contactErr = "Au moins 2 caractères pour le nom"
            ok = false
        }

        if (s.companyName.trim().length < 2) {
            companyErr = "Nom de société trop court"
            ok = false
        }
        val emailTrim = s.email.trim()
        if (emailTrim.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(emailTrim).matches()) {
            emailErr = "Email invalide"
            ok = false
        }
        if (s.password.length < 8) {
            passErr = "Au moins 8 caractères"
            ok = false
        }
        if (s.password != s.confirmPassword) {
            confirmErr = "Les mots de passe ne correspondent pas"
            ok = false
        }

        if (!ok) {
            _uiState.update {
                it.copy(
                    contactFullNameError = contactErr,
                    companyNameError = companyErr,
                    emailError = emailErr,
                    passwordError = passErr,
                    confirmPasswordError = confirmErr,
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (
                val result =
                    authRepository.register(
                        companyName = s.companyName.trim(),
                        email = emailTrim,
                        password = s.password,
                        contactFullName = contactTrim.takeIf { it.isNotEmpty() },
                    )
            ) {
                RegisterResult.SuccessContinueOnboarding -> {
                    _uiState.update { it.copy(isLoading = false) }
                    _navEvents.send(RegisterNavEvent.Onboarding)
                }

                is RegisterResult.ChooseSociete -> {
                    _uiState.update { it.copy(isLoading = false) }
                    _navEvents.send(RegisterNavEvent.ChooseSociete(result.societes))
                }

                is RegisterResult.PendingEmailConfirmation -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            pendingVerificationEmail = emailTrim,
                            resendVerificationFeedback = null,
                            resendVerificationError = null,
                        )
                    }
                }

                is RegisterResult.Error -> {
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = result.message)
                    }
                }
            }
        }
    }
}
