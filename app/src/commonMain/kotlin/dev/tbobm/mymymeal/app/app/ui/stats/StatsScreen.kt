package dev.tbobm.mymymeal.app.app.ui.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.tbobm.mymymeal.app.app.ui.about.icons8stringResource
import dev.tbobm.mymymeal.app.app.ui.calendar.DaySummary
import dev.tbobm.mymymeal.app.app.ui.calendar.observeDaySummary
import dev.tbobm.mymymeal.app.app.ui.changelog.ChangelogModalBottomSheet
import dev.tbobm.mymymeal.app.app.ui.common.component.ArrowBackIconButton
import dev.tbobm.mymymeal.app.app.ui.common.utility.LocalAppConfig
import dev.tbobm.mymymeal.app.common.domain.date.DateProvider
import dev.tbobm.mymymeal.app.common.extension.now
import dev.tbobm.mymymeal.app.fooddiary.domain.usecase.ObserveDiaryMealsUseCase
import dev.tbobm.mymymeal.app.goals.domain.repository.GoalsRepository
import dev.tbobm.mymymeal.app.goals.domain.usecase.ObserveRollingEnergyBalanceUseCase
import foodyou.app.generated.resources.*
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

private const val WindowDays = 30
private const val UpstreamUri = "https://github.com/maksimowiczm/Mymymeal"

@Composable
fun StatsScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val observeDiaryMeals = koinInject<ObserveDiaryMealsUseCase>()
    val goalsRepository = koinInject<GoalsRepository>()
    val rollingBalance = koinInject<ObserveRollingEnergyBalanceUseCase>()
    val dateProvider = koinInject<DateProvider>()
    val appConfig = LocalAppConfig.current
    val uriHandler = LocalUriHandler.current

    val today = dateProvider.observeDate().collectAsStateWithLifecycle(LocalDate.now()).value
    val stats by
        remember(today) {
                combine(
                    (WindowDays - 1 downTo 0).map {
                        observeDaySummary(
                            today.minus(DatePeriod(days = it)),
                            observeDiaryMeals,
                            goalsRepository,
                        )
                    }
                ) {
                    computeStats(it.toList())
                }
            }
            .collectAsStateWithLifecycle(initialValue = null)
    val balance by
        remember(today) { rollingBalance.observe(today) }
            .collectAsStateWithLifecycle(initialValue = null)

    var showChangelog by rememberSaveable { mutableStateOf(false) }
    if (showChangelog) {
        ChangelogModalBottomSheet(onDismissRequest = { showChangelog = false })
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.headline_your_stats)) },
                navigationIcon = { ArrowBackIconButton(onBack) },
            )
        },
    ) { paddingValues ->
        Column(
            modifier =
                Modifier.padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            stats?.let { s ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Tile(
                        stringResource(Res.string.headline_logging_streak),
                        stringResource(Res.string.stats_days, s.streak),
                        Modifier.weight(1f),
                    )
                    Tile(
                        stringResource(Res.string.headline_days_logged),
                        "${s.daysLogged} / ${s.windowDays}",
                        Modifier.weight(1f),
                    )
                }
                Tile(
                    stringResource(Res.string.headline_days_on_target),
                    "${s.daysOnTarget} / ${s.windowDays}",
                )
                Tile(
                    stringResource(Res.string.headline_avg_energy_7d),
                    s.energy7.kcal(),
                )
                Tile(
                    stringResource(Res.string.headline_avg_energy_30d),
                    s.energy30.kcal(),
                )
                Tile(
                    stringResource(Res.string.headline_avg_macros_7d),
                    listOf(
                            stringResource(Res.string.nutriment_proteins) to s.proteins7,
                            stringResource(Res.string.nutriment_carbohydrates) to s.carbohydrates7,
                            stringResource(Res.string.nutriment_fats) to s.fats7,
                        )
                        .joinToString("\n") { (name, avg) -> "$name  ${avg.grams()}" },
                )
            }
            balance?.let {
                Tile(
                    stringResource(Res.string.headline_rolling_balance),
                    signedKcal(it.balanceKcal),
                )
            }

            Column(Modifier.fillMaxWidth().padding(vertical = 16.dp)) {
                Text(
                    text = "${stringResource(Res.string.headline_version)} ${appConfig.versionName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row {
                    TextButton(onClick = { showChangelog = true }) {
                        Text(stringResource(Res.string.headline_changelog))
                    }
                    TextButton(onClick = { uriHandler.openUri(appConfig.sourceCodeUri) }) {
                        Text(stringResource(Res.string.headline_source_code))
                    }
                }
                TextButton(onClick = { uriHandler.openUri(UpstreamUri) }) {
                    Text(stringResource(Res.string.stats_based_on_food_you))
                }
                Text(
                    text = icons8stringResource(MaterialTheme.typography.bodyMedium),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun Tile(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(value, style = MaterialTheme.typography.headlineSmall)
        }
    }
}

private fun AverageVsGoal.kcal() = "${value.roundToInt()} / ${goal.roundToInt()} kcal"

private fun AverageVsGoal.grams() = "${value.roundToInt()} / ${goal.roundToInt()} g"

private fun signedKcal(kcal: Double) = "${if (kcal > 0) "+" else ""}${kcal.roundToInt()} kcal"
