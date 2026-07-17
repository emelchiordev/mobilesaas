package re.savio.mobile.data.account

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import re.savio.mobile.data.local.database.AccountFingerprintStore
import re.savio.mobile.data.local.database.AccountSwitchStateStore
import re.savio.mobile.data.local.database.TokenDataStore
import re.savio.mobile.data.repository.LocalDataWipeRepository
import re.savio.mobile.data.repository.MobileSyncOrchestrator
import re.savio.mobile.data.repository.OutgoingAccountFlushGate
import re.savio.mobile.data.repository.OutgoingFlushResult
import re.savio.mobile.data.repository.SyncResult
import re.savio.mobile.data.repository.UnsyncedLocalWorkSummary
import re.savio.mobile.ui.utils.NetworkUtils
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

sealed class AccountPrepOutcome {
    data object SuccessGoMain : AccountPrepOutcome()

    /**
     * Flush failed — stay in FLUSHING with pending session intact.
     * UI must offer retry or confirmed override (data loss).
     */
    data class FlushFailed(
        val warningMessage: String,
        val unsynced: UnsyncedLocalWorkSummary,
        val isLogout: Boolean,
    ) : AccountPrepOutcome()

    /** Wipe done (or DB empty) but no credentials — go Welcome. */
    data class ReauthRequired(val message: String) : AccountPrepOutcome()

    data class PullOffline(
        val message: String,
        /** True when session is already the new account and fingerprint written. */
        val canContinueOfflineToMain: Boolean,
    ) : AccountPrepOutcome()

    data class LogoutSuccess(val goWelcome: Boolean = true) : AccountPrepOutcome()
}

@Singleton
class AccountSwitchCoordinator @Inject constructor(
    @ApplicationContext private val context: Context,
    private val fingerprintStore: AccountFingerprintStore,
    private val switchStateStore: AccountSwitchStateStore,
    private val tokenDataStore: TokenDataStore,
    private val flushGate: OutgoingAccountFlushGate,
    private val wipeRepository: LocalDataWipeRepository,
    private val mobileSyncOrchestrator: MobileSyncOrchestrator,
) {

    suspend fun hasLocalTenantData(): Boolean = flushGate.hasLocalTenantData()

    suspend fun hasUnsyncedLocalWork(): Boolean = flushGate.hasUnsyncedLocalWork()

    suspend fun countUnsyncedLocalWork(): UnsyncedLocalWorkSummary =
        flushGate.countUnsyncedLocalWork()

    fun readSwitchState() = switchStateStore.read()

    fun requiresAccountPrep(
        newUserId: String,
        newSocieteSlug: String,
        hasLocalData: Boolean,
        currentUserId: String? = null,
        currentSocieteSlug: String? = null,
    ): Boolean =
        AccountSwitchDecision.requiresAccountPrep(
            fingerprint = fingerprintStore.read(),
            newUserId = newUserId,
            newSocieteSlug = newSocieteSlug,
            hasLocalTenantData = hasLocalData,
            currentUserId = currentUserId,
            currentSocieteSlug = currentSocieteSlug,
        )

    suspend fun writeFingerprintForCurrentSession() {
        val userId = tokenDataStore.getUserId()?.takeIf { it.isNotBlank() } ?: return
        val slug = tokenDataStore.getSocieteSlugStored()?.takeIf { it.isNotBlank() } ?: return
        fingerprintStore.write(userId, slug)
    }

    /**
     * Capture pending new session without replacing the active (outgoing) session.
     * Call after validating login / me(), while outgoing session is restored in TokenDataStore.
     */
    suspend fun beginAccountSwitch(
        pending: TokenDataStore.PendingSwitchSession,
        outgoingUserId: String?,
        outgoingSocieteSlug: String?,
    ) {
        tokenDataStore.savePendingSwitchSession(pending)
        switchStateStore.write(
            state = AccountSwitchStateStore.State.FLUSHING,
            pendingUserId = pending.userId,
            pendingSocieteSlug = pending.societeSlug,
            outgoingUserId = outgoingUserId,
            outgoingSocieteSlug = outgoingSocieteSlug,
            isLogout = false,
        )
    }

    suspend fun beginLogoutWipe() {
        switchStateStore.write(
            state = AccountSwitchStateStore.State.FLUSHING,
            pendingUserId = null,
            pendingSocieteSlug = null,
            outgoingUserId = tokenDataStore.getUserId(),
            outgoingSocieteSlug = tokenDataStore.getSocieteSlugStored(),
            isLogout = true,
        )
    }

    /**
     * Runs / resumes the flush → wipe → pull pipeline based on persisted switch state.
     */
    suspend fun runOrResumePrep(): AccountPrepOutcome {
        val snap = switchStateStore.read()
        return when (snap.state) {
            AccountSwitchStateStore.State.IDLE -> AccountPrepOutcome.SuccessGoMain
            AccountSwitchStateStore.State.FLUSHING -> runFromFlushing(snap)
            AccountSwitchStateStore.State.FLUSHED,
            AccountSwitchStateStore.State.WIPING,
            -> runFromWipe(snap)
            AccountSwitchStateStore.State.PULLING -> runFromPulling(snap)
        }
    }

    /** Retry flush from FLUSHING (after user tapped Réessayer). */
    suspend fun retryFlush(): AccountPrepOutcome {
        val snap = switchStateStore.read()
        if (snap.state != AccountSwitchStateStore.State.FLUSHING) {
            return runOrResumePrep()
        }
        return runFromFlushing(snap)
    }

    /**
     * User confirmed data-loss override: skip flush, mark flushed, continue wipe.
     * Must only be called after explicit second confirmation in UI.
     */
    suspend fun overrideFlushAndContinue(): AccountPrepOutcome {
        val snap = switchStateStore.read()
        require(
            snap.state == AccountSwitchStateStore.State.FLUSHING ||
                snap.state == AccountSwitchStateStore.State.FLUSHED,
        ) {
            "overrideFlushAndContinue requires FLUSHING/FLUSHED, was ${snap.state}"
        }
        switchStateStore.write(state = AccountSwitchStateStore.State.FLUSHED)
        return runFromWipe(switchStateStore.read())
    }

    /**
     * Cancel switch after flush failure: clear pending, idle, stay on outgoing session.
     */
    suspend fun cancelFlushFailureStayOutgoing() {
        tokenDataStore.clearPendingSwitchSession()
        switchStateStore.resetToIdle()
    }

    private suspend fun runFromFlushing(
        snap: AccountSwitchStateStore.Snapshot,
    ): AccountPrepOutcome {
        val display =
            tokenDataStore.getUserFullName()
                ?: snap.outgoingUserId
        when (val flush = flushGate.flushOutgoingAccount(display)) {
            is OutgoingFlushResult.Blocked -> {
                // Keep FLUSHING + pending session so retry / override can proceed.
                return AccountPrepOutcome.FlushFailed(
                    warningMessage =
                        FlushFailureWarningCopy.build(
                            unsynced = flush.unsynced,
                            isLogout = snap.isLogout,
                        ),
                    unsynced = flush.unsynced,
                    isLogout = snap.isLogout,
                )
            }
            OutgoingFlushResult.Success,
            OutgoingFlushResult.NothingToFlush,
            -> {
                switchStateStore.write(state = AccountSwitchStateStore.State.FLUSHED)
            }
        }
        return runFromWipe(switchStateStore.read())
    }

    private suspend fun runFromWipe(
        snap: AccountSwitchStateStore.Snapshot,
    ): AccountPrepOutcome {
        switchStateStore.write(state = AccountSwitchStateStore.State.WIPING)
        wipeRepository.wipeAllLocalData()

        if (snap.isLogout) {
            tokenDataStore.clearSession()
            tokenDataStore.clearPendingSwitchSession()
            switchStateStore.resetToIdle()
            return AccountPrepOutcome.LogoutSuccess()
        }

        val pending = tokenDataStore.getPendingSwitchSession()
        if (pending == null) {
            tokenDataStore.clearSession()
            switchStateStore.resetToIdle()
            return AccountPrepOutcome.ReauthRequired(
                "Reconnectez-vous pour charger le nouveau compte (session interrompue).",
            )
        }

        tokenDataStore.consumePendingSwitchSession()
        switchStateStore.write(state = AccountSwitchStateStore.State.PULLING)
        return runFromPulling(switchStateStore.read())
    }

    private suspend fun runFromPulling(
        snap: AccountSwitchStateStore.Snapshot,
    ): AccountPrepOutcome {
        val userId = tokenDataStore.getUserId()
        val slug = tokenDataStore.getSocieteSlugStored()
        if (userId.isNullOrBlank() || slug.isNullOrBlank()) {
            val pending = tokenDataStore.getPendingSwitchSession()
            if (pending != null) {
                tokenDataStore.consumePendingSwitchSession()
            } else {
                resetToIdleClearPending()
                return AccountPrepOutcome.ReauthRequired(
                    "Reconnectez-vous pour charger le nouveau compte.",
                )
            }
        }

        if (!NetworkUtils.isOnline(context)) {
            finalizeNewAccountFingerprint()
            return AccountPrepOutcome.PullOffline(
                message = "Connexion requise pour charger votre planning.",
                canContinueOfflineToMain = true,
            )
        }

        return try {
            val result =
                mobileSyncOrchestrator.runFullSync(
                    pullDate = LocalDate.now(),
                    pullForce = true,
                )
            when (result.pullResult) {
                is SyncResult.Error ->
                    AccountPrepOutcome.PullOffline(
                        message = result.pullResult.message.ifBlank {
                            "Connexion requise pour charger votre planning."
                        },
                        canContinueOfflineToMain = true,
                    ).also { finalizeNewAccountFingerprint() }
                else -> {
                    finalizeNewAccountFingerprint()
                    AccountPrepOutcome.SuccessGoMain
                }
            }
        } catch (e: Exception) {
            finalizeNewAccountFingerprint()
            AccountPrepOutcome.PullOffline(
                message = e.message?.takeIf { it.isNotBlank() }
                    ?: "Connexion requise pour charger votre planning.",
                canContinueOfflineToMain = true,
            )
        }
    }

    private suspend fun finalizeNewAccountFingerprint() {
        writeFingerprintForCurrentSession()
        tokenDataStore.clearPendingSwitchSession()
        switchStateStore.resetToIdle()
    }

    suspend fun resetToIdleClearPending() {
        tokenDataStore.clearPendingSwitchSession()
        switchStateStore.resetToIdle()
    }

    fun currentStepLabel(): String {
        return when (switchStateStore.read().state) {
            AccountSwitchStateStore.State.IDLE -> "Préparation de votre espace…"
            AccountSwitchStateStore.State.FLUSHING ->
                "Synchronisation des données du compte précédent…"
            AccountSwitchStateStore.State.FLUSHED,
            AccountSwitchStateStore.State.WIPING,
            -> "Préparation de votre espace…"
            AccountSwitchStateStore.State.PULLING -> "Chargement de votre planning…"
        }
    }
}
