package com.maksimowiczm.foodyou.app.ui.home.habits

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maksimowiczm.foodyou.app.ui.home.shared.FoodYouHomeCard
import com.maksimowiczm.foodyou.app.ui.home.shared.HomeState
import foodyou.app.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun HabitsCard(
    homeState: HomeState,
    onSetDefaultCoffeeClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: HabitsCardViewModel = koinViewModel()
    val model by viewModel.model.collectAsStateWithLifecycle()

    LaunchedEffect(homeState.selectedDate, viewModel) { viewModel.setDate(homeState.selectedDate) }

    HabitsCard(
        model = model,
        onLogCoffee = viewModel::logCoffee,
        onSetDefaultCoffeeClick = onSetDefaultCoffeeClick,
        onAddSupplement = viewModel::addSupplement,
        onDeleteSupplement = viewModel::deleteSupplement,
        onSetSupplementTaken = viewModel::setSupplementTaken,
        modifier = modifier,
    )
}

@Composable
private fun HabitsCard(
    model: HabitsCardModel?,
    onLogCoffee: () -> Unit,
    onSetDefaultCoffeeClick: () -> Unit,
    onAddSupplement: (String) -> Unit,
    onDeleteSupplement: (Long) -> Unit,
    onSetSupplementTaken: (Long, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (model == null) return

    // Long-press re-opens the same picker used for first-time setup, so an already-configured
    // default coffee can be changed later (the spec's "change default coffee" action -- Plan A
    // has no settings dialog yet for it to live in, so it's reachable via long-press here
    // instead; Plan B's reminder settings dialog can additionally surface it once it exists).
    FoodYouHomeCard(modifier = modifier, onClick = {}, onLongClick = onSetDefaultCoffeeClick) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(
                text = stringResource(Res.string.headline_habits),
                style = MaterialTheme.typography.headlineMediumEmphasized,
            )

            Spacer(Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(
                        text = "${model.cupsToday} ${stringResource(Res.string.unit_cups)}",
                        style = MaterialTheme.typography.headlineSmall,
                    )
                    Text(
                        text = "${model.caffeineMgToday} ${stringResource(Res.string.unit_milligram_short)}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.weight(1f))
                FilledTonalIconButton(
                    onClick = { if (model.hasDefaultCoffee) onLogCoffee() else onSetDefaultCoffeeClick() }
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = stringResource(Res.string.action_add))
                }
            }

            Spacer(Modifier.height(16.dp))

            model.supplements.forEach { row ->
                key(row.id) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(
                            checked = row.taken,
                            onCheckedChange = { onSetSupplementTaken(row.id, it) },
                        )
                        Text(text = row.name, modifier = Modifier.weight(1f))

                        var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
                        if (showDeleteDialog) {
                            AlertDialog(
                                onDismissRequest = { showDeleteDialog = false },
                                confirmButton = {
                                    TextButton(
                                        onClick = {
                                            onDeleteSupplement(row.id)
                                            showDeleteDialog = false
                                        }
                                    ) {
                                        Text(stringResource(Res.string.action_delete))
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showDeleteDialog = false }) {
                                        Text(stringResource(Res.string.action_cancel))
                                    }
                                },
                                title = { Text(stringResource(Res.string.action_delete)) },
                                text = { Text(row.name) },
                            )
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = stringResource(Res.string.action_delete))
                        }
                    }
                }
            }

            var showAddDialog by rememberSaveable { mutableStateOf(false) }
            if (showAddDialog) {
                val textFieldState = rememberTextFieldState()
                AlertDialog(
                    onDismissRequest = { showAddDialog = false },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                onAddSupplement(textFieldState.text.toString())
                                showAddDialog = false
                            }
                        ) {
                            Text(stringResource(Res.string.action_confirm))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showAddDialog = false }) {
                            Text(stringResource(Res.string.action_cancel))
                        }
                    },
                    title = { Text(stringResource(Res.string.action_add)) },
                    text = { OutlinedTextField(state = textFieldState, lineLimits = androidx.compose.foundation.text.input.TextFieldLineLimits.SingleLine) },
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Spacer(Modifier.weight(1f))
                TextButton(onClick = { showAddDialog = true }) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                    Text(stringResource(Res.string.action_add))
                }
            }
        }
    }
}
