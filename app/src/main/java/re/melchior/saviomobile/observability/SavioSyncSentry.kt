package re.melchior.saviomobile.observability

import android.app.Application
import android.util.Log
import io.sentry.Breadcrumb
import io.sentry.Sentry
import io.sentry.SentryLevel
import io.sentry.android.core.SentryAndroid

object SavioSyncSentry {
    private const val TAG = "SavioSyncSentry"
    private var enabled = false

    fun init(app: Application, dsn: String?) {
        val trimmed = dsn?.trim().orEmpty()
        if (trimmed.isEmpty()) {
            Log.i(TAG, "Sentry désactivé (SENTRY_DSN vide)")
            return
        }
        SentryAndroid.init(app) { options ->
            options.dsn = trimmed
            options.isSendDefaultPii = false
        }
        enabled = true
    }

    fun addPipelineBreadcrumb(message: String, durationMs: Long? = null) {
        if (!enabled) return
        Sentry.addBreadcrumb(
            Breadcrumb().apply {
                category = "sync"
                this.message = message
                level = SentryLevel.INFO
                durationMs?.let { setData("durationMs", it) }
            },
        )
    }

    fun onConflictImmutable(interventionId: String, attempts: Int) {
        if (!enabled) return
        Sentry.captureMessage("sync.conflict.immutable.android", SentryLevel.WARNING) { scope ->
            scope.setExtra("interventionId", interventionId)
            scope.setExtra("attempts", attempts.toString())
        }
    }

    fun onConflictVersion(interventionId: String, clientVersion: Int?) {
        if (!enabled) return
        Sentry.captureMessage("sync.conflict.version.android", SentryLevel.WARNING) { scope ->
            scope.setExtra("interventionId", interventionId)
            clientVersion?.let { scope.setExtra("clientVersion", it.toString()) }
        }
    }

    fun onConflictUnresolved(interventionId: String, syncStatus: String, attempts: Int) {
        if (!enabled) return
        Sentry.captureMessage("sync.conflict.unresolved", SentryLevel.ERROR) { scope ->
            scope.setExtra("interventionId", interventionId)
            scope.setExtra("syncStatus", syncStatus)
            scope.setExtra("attempts", attempts.toString())
        }
    }

    fun onPullProtected(interventionId: String) {
        if (!enabled) return
        Sentry.addBreadcrumb(
            Breadcrumb().apply {
                category = "sync.pull.protected"
                message = "Intervention $interventionId protégée du pull"
                level = SentryLevel.INFO
            },
        )
    }
}
