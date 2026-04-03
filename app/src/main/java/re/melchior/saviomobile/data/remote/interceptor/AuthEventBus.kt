package re.melchior.saviomobile.data.remote.interceptor

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject
import javax.inject.Singleton

sealed class AuthEvent {
    object Unauthorized : AuthEvent()
}

@Singleton
class AuthEventBus @Inject constructor() {
    private val _events = MutableSharedFlow<AuthEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<AuthEvent> = _events

    suspend fun emit(event: AuthEvent) {
        _events.emit(event)
    }
}