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
            options.maxBreadcrumbs = 150
        }
        enabled = true
    }

    private fun addBreadcrumb(
        category: String,
        message: String,
        data: Map<String, String> = emptyMap(),
        level: SentryLevel = SentryLevel.INFO,
    ) {
        if (!enabled) return
        Sentry.addBreadcrumb(
            Breadcrumb().apply {
                this.category = category
                this.message = message
                this.level = level
                data.forEach { (key, value) -> setData(key, value) }
            },
        )
    }

    fun addPipelineBreadcrumb(message: String, durationMs: Long? = null) {
        addBreadcrumb(
            category = "sync",
            message = message,
            data = durationMs?.let { mapOf("durationMs" to it.toString()) } ?: emptyMap(),
        )
    }

    fun onReportGenerationStarted(interventionId: String) {
        addBreadcrumb(
            category = "report",
            message = "report.generation.start",
            data = mapOf("interventionId" to interventionId),
        )
    }

    fun onReportGenerationDone(interventionId: String, fallbackUsed: Boolean) {
        addBreadcrumb(
            category = "report",
            message = "report.generation.done",
            data = mapOf(
                "interventionId" to interventionId,
                "fallbackUsed" to fallbackUsed.toString(),
            ),
        )
    }

    fun onReportGenerationFailed(interventionId: String, reason: String) {
        addBreadcrumb(
            category = "report",
            message = "report.generation.failed",
            data = mapOf(
                "interventionId" to interventionId,
                "reason" to reason.take(200),
            ),
            level = SentryLevel.WARNING,
        )
    }

    fun onDiagnosticSearchStarted(interventionId: String?, queryLength: Int) {
        addBreadcrumb(
            category = "diagnostic",
            message = "diagnostic.search.start",
            data = buildMap {
                interventionId?.let { put("interventionId", it) }
                put("queryLength", queryLength.toString())
            },
        )
    }

    fun onDiagnosticSearchDone(interventionId: String?, resultCount: Int) {
        addBreadcrumb(
            category = "diagnostic",
            message = "diagnostic.search.done",
            data = buildMap {
                interventionId?.let { put("interventionId", it) }
                put("resultCount", resultCount.toString())
            },
        )
    }

    fun onDiagnosticSynthesizeDone(interventionId: String?, resultCount: Int) {
        addBreadcrumb(
            category = "diagnostic",
            message = "diagnostic.synthesize.done",
            data = buildMap {
                interventionId?.let { put("interventionId", it) }
                put("resultCount", resultCount.toString())
            },
        )
    }

    fun onClosureStarted(interventionId: String) {
        addBreadcrumb(
            category = "closure",
            message = "closure.start",
            data = mapOf("interventionId" to interventionId),
        )
    }

    fun onClosureCompleted(interventionId: String) {
        addBreadcrumb(
            category = "closure",
            message = "closure.complete",
            data = mapOf("interventionId" to interventionId),
        )
    }

    fun onClosureFailed(interventionId: String, reason: String) {
        addBreadcrumb(
            category = "closure",
            message = "closure.failed",
            data = mapOf(
                "interventionId" to interventionId,
                "reason" to reason.take(200),
            ),
            level = SentryLevel.WARNING,
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
        addBreadcrumb(
            category = "sync.pull.protected",
            message = "Intervention $interventionId protégée du pull",
            data = mapOf("interventionId" to interventionId),
        )
    }
}
