package dev.tbobm.mymymeal.app.app.infrastructure.android

import android.app.Application
import android.content.Intent
import android.os.Build
import dev.tbobm.mymymeal.app.app.BuildConfig
import dev.tbobm.mymymeal.app.app.di.initKoin
import dev.tbobm.mymymeal.app.common.domain.date.DateProvider
import dev.tbobm.mymymeal.app.common.domain.event.EventBus
import dev.tbobm.mymymeal.app.settings.domain.event.AppLaunchEvent
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext

class MymymealApplication : Application() {

    private val coroutineScope by lazy {
        CoroutineScope(Dispatchers.Default + SupervisorJob() + CoroutineName("MymymealApplication"))
    }

    override fun onCreate() {
        super.onCreate()

        initKoin(coroutineScope) { androidContext(this@MymymealApplication) }
        publishLaunchEvent()

        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { t, e ->
            handleUncaughtException(e)
            defaultHandler?.uncaughtException(t, e)
        }
    }

    private fun publishLaunchEvent() {
        val dateProvider: DateProvider by inject()
        val eventBus: EventBus by inject()

        val event = AppLaunchEvent(timestamp = dateProvider.nowInstant())
        eventBus.publish(event)
    }

    private fun handleUncaughtException(e: Throwable) {
        val intent = Intent(this, CrashReportActivity::class.java)

        val report = buildString {
            appendLine("Version: ${BuildConfig.VERSION_NAME}")
            appendLine("Android ${Build.VERSION.RELEASE} (${Build.VERSION.SDK_INT})")
            appendLine()
            appendLine(e.stackTraceToString())
        }

        intent.putExtra("report", report)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

        startActivity(intent)
    }
}
