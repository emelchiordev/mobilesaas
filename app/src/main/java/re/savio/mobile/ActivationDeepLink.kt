package re.savio.mobile

import android.content.Intent
import android.net.Uri

object ActivationDeepLink {
    fun extractToken(intent: Intent?): String? {
        if (intent?.action != Intent.ACTION_VIEW) return null
        val uri: Uri = intent.data ?: return null
        val host = uri.host?.lowercase() ?: return null
        if (host != "app.savio.re") return null
        val path = uri.path.orEmpty()
        if (!path.startsWith("/activate")) return null
        return uri.getQueryParameter("token")?.trim()?.takeIf { it.isNotEmpty() }
    }
}
