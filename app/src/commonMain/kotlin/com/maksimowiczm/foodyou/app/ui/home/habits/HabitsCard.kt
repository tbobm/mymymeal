package com.maksimowiczm.foodyou.app.ui.home.habits

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maksimowiczm.foodyou.app.ui.home.shared.FoodYouHomeCard
import com.maksimowiczm.foodyou.app.ui.home.shared.HomeState
import com.maksimowiczm.foodyou.habits.domain.entity.CoffeeType
import foodyou.app.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun HabitsCard(homeState: HomeState, modifier: Modifier = Modifier) {
    val viewModel: HabitsCardViewModel = koinViewModel()
    val model by viewModel.model.collectAsStateWithLifecycle()

    LaunchedEffect(homeState.selectedDate, viewModel) { viewModel.setDate(homeState.selectedDate) }

    HabitsCard(
        model = model,
        onLogCoffee = viewModel::logCoffee,
        onAddSupplement = viewModel::addSupplement,
        onDeleteSupplement = viewModel::deleteSupplement,
        onSetSupplementTaken = viewModel::setSupplementTaken,
        onSetSupplementDose = viewModel::setSupplementDose,
        modifier = modifier,
    )
}

@Composable
private fun coffeeTypeLabel(type: CoffeeType) =
    when (type) {
        CoffeeType.Cup -> stringResource(Res.string.habits_coffee_cup)
        CoffeeType.Espresso -> stringResource(Res.string.habits_coffee_espresso)
        CoffeeType.Latte -> stringResource(Res.string.habits_coffee_latte)
    }

@Composable
private fun HabitsCard(
    model: HabitsCardModel?,
    onLogCoffee: (CoffeeType) -> Unit,
    onAddSupplement: (name: String, tracksDose: Boolean) -> Unit,
    onDeleteSupplement: (Long) -> Unit,
    onSetSupplementTaken: (Long, Boolean) -> Unit,
    onSetSupplementDose: (Long, Double?) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (model == null) return

    FoodYouHomeCard(modifier = modifier, onClick = {}) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(
                text = stringResource(Res.string.headline_habits),
                style = MaterialTheme.typography.headlineMediumEmphasized,
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text =
                    "${model.coffeesToday} ${stringResource(Res.string.unit_today)} · " +
                        "${model.caffeineMgToday} ${stringResource(Res.string.unit_milligram_short)}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CoffeeType.entries.forEach { type ->
                    FilledTonalButton(onClick = { onLogCoffee(type) }, modifier = Modifier.weight(1f)) {
                        Text(coffeeTypeLabel(type))
                    }
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
                            onCheckedChange = { checked -> onSetSupplementTaken(row.id, checked) },
                        )
                        Text(text = row.name, modifier = Modifier.weight(1f))

                        if (row.tracksDose) {
                            val doseFieldState =
                                rememberTextFieldState(row.doseGrams?.toString().orEmpty())
                            OutlinedTextField(
                                state = doseFieldState,
                                label = { Text(stringResource(Res.string.habits_label_dose)) },
                                keyboardOptions =
                                    KeyboardOptions(
                                        keyboardType = KeyboardType.Decimal,
                                        imeAction = ImeAction.Done,
                                    ),
                                onKeyboardAction = {
                                    onSetSupplementDose(
                                        row.id,
                                        doseFieldState.text.toString().toDoubleOrNull(),
                                    )
                                },
                                lineLimits =
                                    androidx.compose.foundation.text.input.TextFieldLineLimits.SingleLine,
                                modifier = Modifier.width(96.dp),
                            )
                            Spacer(Modifier.width(8.dp))
                        }

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
                var tracksDose by rememberSaveable { mutableStateOf(false) }
                AlertDialog(
                    onDismissRequest = { showAddDialog = false },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                onAddSupplement(textFieldState.text.toString(), tracksDose)
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
                    text = {
                        Column {
                            OutlinedTextField(
                                state = textFieldState,
                                lineLimits = androidx.compose.foundation.text.input.TextFieldLineLimits.SingleLine,
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            ) {
                                Checkbox(checked = tracksDose, onCheckedChange = { tracksDose = it })
                                Text(stringResource(Res.string.habits_label_tracks_dose))
                            }
                        }
                    },
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
