package dev.tbobm.mymymeal.app.app.ui.database.usagelog

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import dev.tbobm.mymymeal.app.app.ui.common.component.SettingsListItem
import dev.tbobm.mymymeal.app.common.domain.userpreferences.UserPreferencesRepository
import dev.tbobm.mymymeal.app.settings.domain.entity.Settings
import dev.tbobm.mymymeal.app.usagelog.ClearUsageLogUseCase
import foodyou.app.generated.resources.*
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

/** Usage-log toggle and "clear" action. Data stays on device; export is part of full-data export. */
@Composable
fun UsageLogSettings(modifier: Modifier = Modifier) {
    val settings = koinInject<UserPreferencesRepository<Settings>>()
    val clearUsageLog = koinInject<ClearUsageLogUseCase>()
    val scope = rememberCoroutineScope()
    val enabled by settings.observe().map { it.usageLogEnabled }.collectAsState(initial = true)
    var confirmClear by rememberSaveable { mutableStateOf(false) }

    Column(modifier) {
        SettingsListItem(
            icon = { Icon(Icons.Outlined.Insights, null) },
            label = { Text(stringResource(Res.string.headline_usage_log)) },
            supportingContent = { Text(stringResource(Res.string.description_usage_log)) },
            trailingContent = { Switch(checked = enabled, onCheckedChange = null) },
            onClick = {
                scope.launch { settings.update { copy(usageLogEnabled = !usageLogEnabled) } }
            },
        )
        SettingsListItem(
            icon = { Icon(Icons.Outlined.DeleteSweep, null) },
            label = { Text(stringResource(Res.string.action_clear_usage_log)) },
            onClick = { confirmClear = true },
        )
    }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text(stringResource(Res.string.action_clear_usage_log)) },
            text = { Text(stringResource(Res.string.description_clear_usage_log)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmClear = false
                        scope.launch { clearUsageLog.clear() }
                    }
                ) {
                    Text(stringResource(Res.string.action_clear))
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmClear = false }) {
                    Text(stringResource(Res.string.action_cancel))
                }
            },
        )
    }
}
