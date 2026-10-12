package dev.tbobm.mymymeal.app.app.infrastructure.android

import android.content.Intent
import android.os.Bundle
import dev.tbobm.mymymeal.app.app.ui.MymymealApp

class MainActivity : MymymealAbstractActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MymymealApp(
                onDatabaseBackup = {
                    val intent =
                        Intent(this, DeveloperActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }

                    startActivity(intent)
                }
            )
        }
    }
}
