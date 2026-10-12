package dev.tbobm.mymymeal.app.app.ui.database.exportfulldata

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.tbobm.mymymeal.app.app.ui.common.component.SomethingWentWrongScreen
import dev.tbobm.mymymeal.app.app.ui.common.utility.LocalAppConfig
import java.time.LocalDateTime
import org.koin.compose.viewmodel.koinViewModel

@Composable
actual fun ExportFullDataScreen(onBack: () -> Unit, onFinish: () -> Unit, modifier: Modifier) {
    val viewModel: ExportFullDataViewModel = koinViewModel()
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value

    val context = LocalContext.current
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) {
            uri ->
            if (uri == null) {
                onBack()
            } else {
                viewModel.handleZip(uri, context)
            }
        }

    val appConfig = LocalAppConfig.current
    val fileName = remember {
        "Food You ${appConfig.versionName}-full-export-${LocalDateTime.now()}.zip"
    }
    LaunchedEffect(uiState) {
        when (uiState) {
            is UiState.Error,
            is UiState.Exported,
            is UiState.Exporting -> Unit

            UiState.WaitingForFile -> launcher.launch(fileName)
        }
    }

    when (uiState) {
        is UiState.Error ->
            SomethingWentWrongScreen(
                onBack = onBack,
                message = uiState.message,
                modifier = modifier,
            )

        is UiState.Exported -> FullDataSuccessScreen(onBack = onBack, modifier = modifier)

        UiState.WaitingForFile ->
            Scaffold(modifier) { paddingValues ->
                Box(
                    modifier =
                        Modifier.fillMaxSize()
                            .padding(paddingValues)
                            .consumeWindowInsets(paddingValues),
                    contentAlignment = Alignment.Center,
                ) {
                    LoadingIndicator()
                }
            }

        is UiState.Exporting -> ExportingFullDataScreen(count = uiState.count, modifier = modifier)
    }
}
