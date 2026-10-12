package dev.tbobm.mymymeal.app.app.ui.home.goals

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.tbobm.mymymeal.app.app.ui.common.component.ArrowBackIconButton
import dev.tbobm.mymymeal.app.goals.domain.entity.RollingBudgetPreferences
import foodyou.app.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun GoalsCardSettings(
    onBack: () -> Unit,
    onGoalsSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: GoalsViewModel = koinViewModel()
    val expand by viewModel.expandGoalsCard.collectAsStateWithLifecycle()
    val rollingBudgetPreferences by viewModel.rollingBudgetPreferences.collectAsStateWithLifecycle()

    GoalsCardSettings(
        onBack = onBack,
        expand = expand,
        onShowDetailsChange = viewModel::setExpandGoalsCard,
        onGoalsSettings = onGoalsSettings,
        rollingBudgetPreferences = rollingBudgetPreferences,
        onRollingBudgetWindowLengthChange = viewModel::setRollingBudgetWindowLength,
        onRollingBudgetCarryoverChange = viewModel::setRollingBudgetCarryover,
        modifier = modifier,
    )
}

@Composable
private fun GoalsCardSettings(
    onBack: () -> Unit,
    onGoalsSettings: () -> Unit,
    onShowDetailsChange: (Boolean) -> Unit,
    expand: Boolean,
    rollingBudgetPreferences: RollingBudgetPreferences,
    onRollingBudgetWindowLengthChange: (Int) -> Unit,
    onRollingBudgetCarryoverChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = modifier,
        topBar = {
            MediumFlexibleTopAppBar(
                title = { Text(stringResource(Res.string.headline_daily_goals)) },
                navigationIcon = { ArrowBackIconButton(onBack) },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
            contentPadding = paddingValues,
        ) {
            stickyHeader {
                GoalsCard(
                    expand = expand,
                    energy = 1600,
                    energyGoal = 2000,
                    proteins = 50,
                    proteinsGoal = 75,
                    carbohydrates = 200,
                    carbohydratesGoal = 300,
                    fats = 70,
                    fatsGoal = 90,
                    onClick = {},
                    onLongClick = {},
                    modifier = Modifier.padding(16.dp),
                )
            }

            item { HorizontalDivider() }

            item {
                ListItem(
                    headlineContent = { Text(stringResource(Res.string.action_show_details)) },
                    modifier = Modifier.clickable { onShowDetailsChange(!expand) },
                    supportingContent = {
                        Text(stringResource(Res.string.description_show_macronutrients_goals))
                    },
                    trailingContent = {
                        Switch(checked = expand, onCheckedChange = onShowDetailsChange)
                    },
                )
            }

            item { HorizontalDivider() }

            item {
                ListItem(
                    headlineContent = {
                        Text(stringResource(Res.string.headline_daily_goals_settings))
                    },
                    modifier = Modifier.clickable { onGoalsSettings() },
                )
            }

            item { HorizontalDivider() }

            item {
                ListItem(
                    headlineContent = { Text(stringResource(Res.string.action_enable_carryover)) },
                    modifier =
                        Modifier.clickable {
                            onRollingBudgetCarryoverChange(!rollingBudgetPreferences.carryover)
                        },
                    supportingContent = {
                        Text(stringResource(Res.string.description_rolling_budget_carryover))
                    },
                    trailingContent = {
                        Switch(
                            checked = rollingBudgetPreferences.carryover,
                            onCheckedChange = onRollingBudgetCarryoverChange,
                        )
                    },
                )
            }

            item {
                RollingBudgetWindowLengthSetting(
                    windowLength = rollingBudgetPreferences.windowLength,
                    onWindowLengthChange = onRollingBudgetWindowLengthChange,
                )
            }
        }
    }
}

@Composable
private fun RollingBudgetWindowLengthSetting(
    windowLength: Int,
    onWindowLengthChange: (Int) -> Unit,
) {
    ListItem(
        headlineContent = {
            Text(stringResource(Res.string.description_rolling_budget_window_length))
        },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { onWindowLengthChange((windowLength - 1).coerceAtLeast(1)) }
                ) {
                    Text("-")
                }
                Text(windowLength.toString(), style = MaterialTheme.typography.bodyLarge)
                IconButton(
                    onClick = { onWindowLengthChange((windowLength + 1).coerceAtMost(31)) }
                ) {
                    Text("+")
                }
            }
        },
    )
}
