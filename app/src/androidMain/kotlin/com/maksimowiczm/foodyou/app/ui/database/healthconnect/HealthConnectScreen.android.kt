package com.maksimowiczm.foodyou.app.ui.database.healthconnect

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maksimowiczm.foodyou.app.infrastructure.android.healthconnect.HEALTH_CONNECT_PERMISSIONS
import com.maksimowiczm.foodyou.app.infrastructure.android.healthconnect.HealthConnectAvailability
import com.maksimowiczm.foodyou.app.infrastructure.android.healthconnect.healthConnectInstallIntent
import com.maksimowiczm.foodyou.app.infrastructure.android.healthconnect.healthConnectPermissionContract
import com.maksimowiczm.foodyou.app.ui.common.component.ArrowBackIconButton
import foodyou.app.generated.resources.*
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
actual fun HealthConnectScreen(onBack: () -> Unit, modifier: Modifier) {
    val viewModel: HealthConnectViewModel = koinViewModel()
    val availability = viewModel.availability.collectAsStateWithLifecycle().value
    val preferences = viewModel.preferences.collectAsStateWithLifecycle().value
    val backfillState = viewModel.backfillState.collectAsStateWithLifecycle().value

    val permissionLauncher =
        rememberLauncherForActivityResult(healthConnectPermissionContract()) {
            viewModel.refreshAvailability()
        }
    val context = LocalContext.current

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        modifier = modifier,
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text(stringResource(Res.string.headline_health_connect)) },
                subtitle = { Text(stringResource(Res.string.description_health_connect)) },
                navigationIcon = { ArrowBackIconButton(onBack) },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { paddingValues ->
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
                    .padding(paddingValues)
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            when (availability) {
                null ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        CircularProgressIndicator()
                    }

                HealthConnectAvailability.Unavailable ->
                    Text(stringResource(Res.string.description_health_connect_unavailable))

                HealthConnectAvailability.NeedsInstall -> {
                    Text(stringResource(Res.string.description_health_connect_needs_install))
                    Button(onClick = { context.startActivity(healthConnectInstallIntent()) }) {
                        Text(stringResource(Res.string.action_install_health_connect))
                    }
                }

                is HealthConnectAvailability.NeedsPermission -> {
                    Text(stringResource(Res.string.description_health_connect_needs_permission))
                    Button(onClick = { permissionLauncher.launch(HEALTH_CONNECT_PERMISSIONS) }) {
                        Text(stringResource(Res.string.action_grant_health_connect_permission))
                    }
                }

                is HealthConnectAvailability.Ready -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(stringResource(Res.string.action_sync_nutrition_to_health_connect))
                        Switch(
                            checked = preferences.syncEnabled,
                            onCheckedChange = viewModel::setSyncEnabled,
                        )
                    }
                    Text(
                        text = stringResource(Res.string.description_health_connect_toggle_off_keeps_data),
                        style = MaterialTheme.typography.bodySmall,
                    )

                    Button(
                        onClick = { viewModel.backfill(30) },
                        enabled = backfillState != BackfillState.Running,
                    ) {
                        Text(stringResource(Res.string.action_sync_last_30_days))
                    }

                    Text(
                        text =
                            when {
                                backfillState == BackfillState.Running ->
                                    stringResource(Res.string.description_health_connect_syncing)

                                preferences.lastSyncedEpochSeconds != null ->
                                    stringResource(
                                        Res.string.description_health_connect_last_synced,
                                        preferences.lastSyncedEpochSeconds.toFormattedDateTime(),
                                    )

                                else -> stringResource(Res.string.description_health_connect_never_synced)
                            },
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}

private fun Long.toFormattedDateTime(): String =
    Instant.ofEpochSecond(this)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT))
