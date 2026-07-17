package re.savio.mobile.ui.screen.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import re.savio.mobile.data.account.AccountPrepOutcome
import re.savio.mobile.data.account.AccountSwitchCoordinator
import re.savio.mobile.data.repository.UnsyncedLocalWorkSummary
import javax.inject.Inject

sealed class AccountPrepNav {
    data object GoMain : AccountPrepNav()
    data class StayOnOutgoingMain(val message: String) : AccountPrepNav()
    data class ReauthWelcome(val message: String) : AccountPrepNav()
}

data class AccountPrepUiState(
    val running: Boolean = false,
    val statusLabel: String = "Préparation de votre espace…",
    val errorMessage: String? = null,
    val pullOfflineMessage: String? = null,
    /** Flush failed — show warning with retry / override. */
    val flushWarningMessage: String? = null,
    val flushUnsynced: UnsyncedLocalWorkSummary? = null,
    val flushIsLogout: Boolean = false,
    /** Second-step confirmation before data-loss override. */
    val showOverrideConfirmDialog: Boolean = false,
    val navigation: AccountPrepNav? = null,
)

@HiltViewModel
class AccountPrepViewModel @Inject constructor(
    private val coordinator: AccountSwitchCoordinator,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AccountPrepUiState())
    val uiState: StateFlow<AccountPrepUiState> = _uiState.asStateFlow()

    fun start() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    running = true,
                    errorMessage = null,
                    pullOfflineMessage = null,
                    flushWarningMessage = null,
                    flushUnsynced = null,
                    showOverrideConfirmDialog = false,
                    statusLabel = coordinator.currentStepLabel(),
                    navigation = null,
                )
            }
            val outcome = coordinator.runOrResumePrep()
            handleOutcome(outcome)
        }
    }

    fun retryFlush() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    running = true,
                    flushWarningMessage = null,
                    flushUnsynced = null,
                    showOverrideConfirmDialog = false,
                    statusLabel = "Synchronisation des données du compte précédent…",
                    navigation = null,
                )
            }
            handleOutcome(coordinator.retryFlush())
        }
    }

    /** First tap on "Continuer quand même" — only opens confirm dialog. */
    fun requestOverrideConfirm() {
        if (_uiState.value.flushWarningMessage == null) return
        _uiState.update { it.copy(showOverrideConfirmDialog = true) }
    }

    fun dismissOverrideConfirm() {
        _uiState.update { it.copy(showOverrideConfirmDialog = false) }
    }

    /** Second explicit confirmation — proceeds to wipe despite failed flush. */
    fun confirmOverrideAndContinue() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    running = true,
                    showOverrideConfirmDialog = false,
                    flushWarningMessage = null,
                    flushUnsynced = null,
                    statusLabel = "Préparation de votre espace…",
                )
            }
            handleOutcome(coordinator.overrideFlushAndContinue())
        }
    }

    fun cancelFlushFailure() {
        viewModelScope.launch {
            val isLogout = _uiState.value.flushIsLogout
            coordinator.cancelFlushFailureStayOutgoing()
            _uiState.update {
                it.copy(
                    flushWarningMessage = null,
                    flushUnsynced = null,
                    showOverrideConfirmDialog = false,
                    running = false,
                    navigation =
                        if (isLogout) {
                            AccountPrepNav.StayOnOutgoingMain("")
                        } else {
                            AccountPrepNav.StayOnOutgoingMain(
                                "Changement de compte annulé — vos données locales sont conservées.",
                            )
                        },
                )
            }
        }
    }

    fun retryPull() {
        start()
    }

    fun continueOfflineToMain() {
        viewModelScope.launch {
            coordinator.writeFingerprintForCurrentSession()
            coordinator.resetToIdleClearPending()
            _uiState.update {
                it.copy(pullOfflineMessage = null, navigation = AccountPrepNav.GoMain)
            }
        }
    }

    fun consumeNavigation() {
        _uiState.update { it.copy(navigation = null) }
    }

    private fun handleOutcome(outcome: AccountPrepOutcome) {
        when (outcome) {
            AccountPrepOutcome.SuccessGoMain ->
                _uiState.update {
                    it.copy(running = false, navigation = AccountPrepNav.GoMain)
                }
            is AccountPrepOutcome.FlushFailed ->
                _uiState.update {
                    it.copy(
                        running = false,
                        flushWarningMessage = outcome.warningMessage,
                        flushUnsynced = outcome.unsynced,
                        flushIsLogout = outcome.isLogout,
                        showOverrideConfirmDialog = false,
                    )
                }
            is AccountPrepOutcome.ReauthRequired ->
                _uiState.update {
                    it.copy(
                        running = false,
                        navigation = AccountPrepNav.ReauthWelcome(outcome.message),
                    )
                }
            is AccountPrepOutcome.PullOffline ->
                _uiState.update {
                    it.copy(
                        running = false,
                        pullOfflineMessage = outcome.message,
                    )
                }
            is AccountPrepOutcome.LogoutSuccess ->
                _uiState.update {
                    it.copy(
                        running = false,
                        navigation = AccountPrepNav.ReauthWelcome(""),
                    )
                }
        }
    }
}
