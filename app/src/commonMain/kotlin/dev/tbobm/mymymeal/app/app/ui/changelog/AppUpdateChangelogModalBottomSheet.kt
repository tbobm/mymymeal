package dev.tbobm.mymymeal.app.app.ui.changelog

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.tbobm.mymymeal.app.app.ui.common.utility.LocalAppConfig
import dev.tbobm.mymymeal.app.common.domain.userpreferences.UserPreferencesRepository
import dev.tbobm.mymymeal.app.settings.domain.entity.Settings
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.core.qualifier.named

/** Modal bottom sheet that will show the changelog of the app if the user has not seen it yet. */
@Composable
fun AppUpdateChangelogModalBottomSheet(modifier: Modifier = Modifier) {
    val coroutineScope = rememberCoroutineScope()
    val settingsRepository: UserPreferencesRepository<Settings> =
        koinInject(named(Settings::class.qualifiedName!!))
    val appConfig = LocalAppConfig.current

    val currentVersion = remember(appConfig) { appConfig.versionName }
    val settings = settingsRepository.observe().collectAsStateWithLifecycle(null).value

    if (settings != null && currentVersion != settings.lastRememberedVersion) {
        ChangelogModalBottomSheet(
            onDismissRequest = {
                coroutineScope.launch {
                    settingsRepository.update { copy(lastRememberedVersion = currentVersion) }
                }
            },
            modifier = modifier,
        )
    }
}
