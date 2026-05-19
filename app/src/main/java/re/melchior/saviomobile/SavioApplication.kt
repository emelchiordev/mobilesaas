package re.melchior.saviomobile

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import re.melchior.saviomobile.data.local.objectbox.ObjectBoxStore
import re.melchior.saviomobile.observability.SavioSyncSentry
import javax.inject.Inject

@HiltAndroidApp
class SavioApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        ObjectBoxStore.init(this)
        SavioSyncSentry.init(this, BuildConfig.SENTRY_DSN)
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}