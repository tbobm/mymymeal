package dev.tbobm.mymymeal.app.app.ui.database.exportfulldata

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

@Composable
expect fun ExportFullDataScreen(
    onBack: () -> Unit,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
)
