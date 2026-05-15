package re.melchior.saviomobile.ui.screen.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import re.melchior.saviomobile.data.local.database.TokenDataStore
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val tokenDataStore: TokenDataStore,
) : ViewModel() {

    fun completeOnboarding(onDone: () -> Unit) {
        viewModelScope.launch {
            tokenDataStore.setOnboardingCompleted(true)
            onDone()
        }
    }
}
