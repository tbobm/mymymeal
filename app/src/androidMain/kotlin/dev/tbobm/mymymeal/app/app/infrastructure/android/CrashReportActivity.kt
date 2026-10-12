package dev.tbobm.mymymeal.app.app.infrastructure.android

import android.os.Bundle
import dev.tbobm.mymymeal.app.app.ui.crash.CrashReportScreen
import dev.tbobm.mymymeal.app.app.ui.theme.MymymealTheme
import dev.tbobm.mymymeal.app.common.config.AppConfig
import org.koin.android.ext.android.get

class CrashReportActivity : MymymealAbstractActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val errorMessage = intent.getStringExtra("report").toString()
        val appConfig: AppConfig = get()

        setContent {
            MymymealTheme {
                CrashReportScreen(
                    message = errorMessage,
                    issueTrackerUrl = appConfig.issueTrackerUri,
                )
            }
        }
    }
}
