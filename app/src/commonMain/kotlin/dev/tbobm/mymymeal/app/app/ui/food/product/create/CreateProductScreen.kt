package dev.tbobm.mymymeal.app.app.ui.food.product.create

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationEventHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import dev.tbobm.mymymeal.app.app.ui.common.component.ArrowBackIconButton
import dev.tbobm.mymymeal.app.app.ui.common.component.DiscardDialog
import dev.tbobm.mymymeal.app.app.ui.food.product.ProductForm
import dev.tbobm.mymymeal.app.app.ui.food.product.ProductFormState
import dev.tbobm.mymymeal.app.common.compose.utility.LocalClipboardManager
import dev.tbobm.mymymeal.app.food.domain.usecase.aiProductPrompt
import foodyou.app.generated.resources.*
import io.ktor.http.encodeURLQueryComponent
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun CreateProductScreen(
    state: ProductFormState,
    onBack: () -> Unit,
    onCreate: (ProductFormState) -> Unit,
    onDownload: () -> Unit,
    onAskAi: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDiscardDialog by rememberSaveable { mutableStateOf(false) }
    val handleBack = {
        if (!state.isModified) {
            onBack()
        } else {
            showDiscardDialog = true
        }
    }
    NavigationEventHandler(
        state = rememberNavigationEventState(NavigationEventInfo.None),
        isBackEnabled = state.isModified,
        onBackCompleted = { showDiscardDialog = true },
    )
    if (showDiscardDialog) {
        DiscardDialog(
            onDismissRequest = { showDiscardDialog = false },
            onDiscard = {
                showDiscardDialog = false
                onBack()
            },
        ) {
            Text(stringResource(Res.string.question_discard_product))
        }
    }

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.headline_create_product)) },
                navigationIcon = { ArrowBackIconButton(handleBack) },
                actions = {
                    FilledIconButton(onClick = { onCreate(state) }, enabled = state.isValid) {
                        Icon(imageVector = Icons.Outlined.Save, contentDescription = null)
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { paddingValues ->
        LazyColumn(
            modifier =
                Modifier.fillMaxSize()
                    .imePadding()
                    .nestedScroll(scrollBehavior.nestedScrollConnection),
            contentPadding = paddingValues,
        ) {
            item {
                val clipboardManager = LocalClipboardManager.current
                val uriHandler = LocalUriHandler.current
                val aiLabel = stringResource(Res.string.action_ask_ai)

                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    AssistChip(
                        onClick = onDownload,
                        label = { Text(stringResource(Res.string.action_download_product)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                modifier = Modifier.size(AssistChipDefaults.IconSize),
                            )
                        },
                    )
                    AssistChip(
                        onClick = {
                            val prompt = aiProductPrompt(state.name.value)
                            clipboardManager.copy(aiLabel, prompt)
                            runCatching {
                                uriHandler.openUri(
                                    "https://claude.ai/new?q=${prompt.encodeURLQueryComponent()}"
                                )
                            }
                            onAskAi()
                        },
                        label = { Text(aiLabel) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(AssistChipDefaults.IconSize),
                            )
                        },
                    )
                }
            }

            item { ProductForm(state = state, contentPadding = PaddingValues(horizontal = 16.dp)) }
        }
    }
}
