package re.savio.mobile

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.AndroidEntryPoint
import re.savio.mobile.data.local.database.TokenDataStore
import re.savio.mobile.data.remote.interceptor.AuthEventBus
import re.savio.mobile.ui.SavioApp
import re.savio.mobile.worker.CatalogSyncWorker
import re.savio.mobile.worker.PendingInterventionSyncWorker
import re.savio.mobile.worker.SyncWorker
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var tokenDataStore: TokenDataStore

    @Inject
    lateinit var accountSwitchCoordinator: re.savio.mobile.data.account.AccountSwitchCoordinator

    @Inject lateinit var authEventBus: AuthEventBus

    private val workManager by lazy { WorkManager.getInstance(this) }

    private lateinit var deepLinkIntentState: MutableState<Intent?>

    /** Aligné sur SavioRefonte.Navy (#1B4E80). */
    private companion object {
        const val SAVIO_NAVY_STATUS_BAR = 0xFF1B4E80.toInt()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        deepLinkIntentState = mutableStateOf(intent)
        // Edge-to-edge : barre statut navy (icônes claires), barre nav claire
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(SAVIO_NAVY_STATUS_BAR),
            navigationBarStyle = SystemBarStyle.light(Color.WHITE, Color.WHITE),
        )

        // Enregistrer les workers périodiques
        scheduleSyncWorker()
        enqueueOneTimeSyncWhenOnline()
        CatalogSyncWorker.enqueue(workManager)
        PendingInterventionSyncWorker.enqueue(workManager)

        setContent {
            val deepLinkIntent by deepLinkIntentState
            SavioApp(
                windowSizeClass = calculateWindowSizeClass(this),
                tokenDataStore = tokenDataStore,
                accountSwitchCoordinator = accountSwitchCoordinator,
                authEventBus = authEventBus,
                deepLinkIntent = deepLinkIntent,
                onConsumeDeepLinkIntent = {
                    deepLinkIntentState.value = null
                    setIntent(Intent(this@MainActivity, MainActivity::class.java))
                },
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        deepLinkIntentState.value = intent
    }

    private fun scheduleSyncWorker() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            repeatInterval = 30,
            repeatIntervalTimeUnit = TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .build()

        workManager.enqueueUniquePeriodicWork(
            SyncWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }

    /** Un passage sync (push + photos) dès que le réseau est dispo, sans attendre la période 30 min. */
    private fun enqueueOneTimeSyncWhenOnline() {
        SyncWorker.enqueueNow(workManager)
    }
}