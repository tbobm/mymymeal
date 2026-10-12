package dev.tbobm.mymymeal.app.common.compose.component

import androidx.compose.runtime.*

@Composable
expect fun FullScreenDialog(onDismissRequest: () -> Unit, content: @Composable () -> Unit)
