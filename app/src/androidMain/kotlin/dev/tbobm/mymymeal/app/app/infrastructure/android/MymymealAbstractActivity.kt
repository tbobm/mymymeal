package dev.tbobm.mymymeal.app.app.infrastructure.android

import android.os.Bundle
import android.view.WindowManager.LayoutParams.FLAG_SECURE
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.*
import androidx.lifecycle.lifecycleScope
import dev.tbobm.mymymeal.app.app.ui.common.utility.AppConfigProvider
import dev.tbobm.mymymeal.app.common.compose.utility.AndroidClipboardManager
import dev.tbobm.mymymeal.app.common.compose.utility.AndroidDateFormatter
import dev.tbobm.mymymeal.app.common.compose.utility.ClipboardManagerProvider
import dev.tbobm.mymymeal.app.common.compose.utility.DateFormatterProvider
import dev.tbobm.mymymeal.app.common.domain.userpreferences.UserPreferencesRepository
import dev.tbobm.mymymeal.app.common.infrastructure.system.AndroidSystemDetails
import dev.tbobm.mymymeal.app.common.infrastructure.system.defaultLocale
import dev.tbobm.mymymeal.app.settings.domain.entity.Settings
import foodyou.app.generated.resources.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.getString
import org.koin.android.ext.android.get
import org.koin.core.qualifier.named

abstract class MymymealAbstractActivity : AppCompatActivity() {

    private val systemDetails: AndroidSystemDetails
        get() = get()

    private val settingsRepository: UserPreferencesRepository<Settings>
        get() = get(named(Settings::class.qualifiedName!!))

    fun setContent(content: @Composable () -> Unit) {
        enableEdgeToEdge()

        val clipboardManager =
            AndroidClipboardManager(this) { runBlocking { getString(Res.string.neutral_copied) } }
        val dateFormatter = AndroidDateFormatter(this) { defaultLocale }

        with<AppCompatActivity, Unit>(this) {
            setContent {
                ClipboardManagerProvider(clipboardManager) {
                    DateFormatterProvider(dateFormatter) { AppConfigProvider(get()) { content() } }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch { observeShowContentSecurity() }

        lifecycle.addObserver(systemDetails)
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycle.removeObserver(systemDetails)
    }

    private suspend fun observeShowContentSecurity() {
        settingsRepository
            .observe()
            .map { it.secureScreen }
            .collectLatest {
                if (it) {
                    window.setFlags(FLAG_SECURE, FLAG_SECURE)
                } else {
                    window.clearFlags(FLAG_SECURE)
                }
            }
    }
}
