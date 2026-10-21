package dev.tbobm.mymymeal.app.app.infrastructure.android

import android.content.Intent
import android.os.Bundle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import dev.tbobm.mymymeal.app.app.ui.MymymealApp

class MainActivity : MymymealAbstractActivity() {

    private var quickAddRequest by mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) handleQuickAdd(intent)

        setContent {
            MymymealApp(
                onDatabaseBackup = {
                    val intent =
                        Intent(this, DeveloperActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }

                    startActivity(intent)
                },
                quickAddRequest = quickAddRequest,
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleQuickAdd(intent)
    }

    private fun handleQuickAdd(intent: Intent?) {
        if (intent?.getBooleanExtra(EXTRA_QUICK_ADD, false) == true) {
            intent.removeExtra(EXTRA_QUICK_ADD)
            quickAddRequest++
        }
    }

    companion object {
        const val EXTRA_QUICK_ADD = "dev.tbobm.mymymeal.app.QUICK_ADD"
    }
}
