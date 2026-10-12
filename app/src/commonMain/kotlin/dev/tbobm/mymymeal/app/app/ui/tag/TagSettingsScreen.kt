package dev.tbobm.mymymeal.app.app.ui.tag

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.tbobm.mymymeal.app.app.ui.common.component.ArrowBackIconButton
import dev.tbobm.mymymeal.app.app.ui.common.component.SettingsListItem
import dev.tbobm.mymymeal.app.common.compose.extension.add
import dev.tbobm.mymymeal.app.common.domain.tag.Tag
import foodyou.app.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/** PRD 3.5 (categorisation). Create/rename/delete tags. */
@Composable
fun TagSettingsScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val viewModel: TagSettingsViewModel = koinViewModel()
    val tags = viewModel.tags.collectAsStateWithLifecycle().value

    if (tags == null) {
        // TODO loading state
        return
    }

    TagSettingsScreen(
        onBack = onBack,
        tags = tags,
        onCreate = viewModel::createTag,
        onRename = viewModel::renameTag,
        onDelete = { viewModel.deleteTag(it.id) },
        modifier = modifier,
    )
}

@Composable
private fun TagSettingsScreen(
    onBack: () -> Unit,
    tags: List<Tag>,
    onCreate: (String) -> Unit,
    onRename: (id: Long, name: String) -> Unit,
    onDelete: (Tag) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showCreateDialog by rememberSaveable { mutableStateOf(false) }
    var tagBeingRenamed by remember { mutableStateOf<Tag?>(null) }

    if (showCreateDialog) {
        TagNameDialog(
            title = stringResource(Res.string.action_new_tag),
            initialName = "",
            onDismissRequest = { showCreateDialog = false },
            onConfirm = {
                onCreate(it)
                showCreateDialog = false
            },
        )
    }

    tagBeingRenamed?.let { tag ->
        TagNameDialog(
            title = stringResource(Res.string.action_rename_tag),
            initialName = tag.name,
            onDismissRequest = { tagBeingRenamed = null },
            onConfirm = {
                onRename(tag.id, it)
                tagBeingRenamed = null
            },
        )
    }

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.headline_tags)) },
                navigationIcon = { ArrowBackIconButton(onBack) },
                scrollBehavior = scrollBehavior,
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(Res.string.action_new_tag))
            }
        },
    ) { paddingValues ->
        if (tags.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(Res.string.neutral_no_tags),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
                contentPadding = paddingValues.add(vertical = 8.dp),
            ) {
                items(items = tags, key = { it.id }) { tag ->
                    SettingsListItem(
                        label = { Text(tag.name) },
                        onClick = { tagBeingRenamed = tag },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        trailingContent = {
                            Row {
                                IconButton(onClick = { tagBeingRenamed = tag }) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = stringResource(Res.string.action_rename_tag),
                                    )
                                }
                                IconButton(onClick = { onDelete(tag) }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = stringResource(Res.string.action_delete_tag),
                                    )
                                }
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun TagNameDialog(
    title: String,
    initialName: String,
    onDismissRequest: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    val textFieldState = rememberTextFieldState(initialName)

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                state = textFieldState,
                placeholder = { Text(stringResource(Res.string.hint_tag_name)) },
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(textFieldState.text.toString()) },
                enabled = textFieldState.text.isNotBlank(),
            ) {
                Text(stringResource(Res.string.action_save))
            }
        },
        dismissButton = {
            TextButton(onDismissRequest) { Text(stringResource(Res.string.action_cancel)) }
        },
    )
}
