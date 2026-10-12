package dev.tbobm.mymymeal.app.app.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.tbobm.mymymeal.app.app.ui.common.component.ArrowBackIconButton
import dev.tbobm.mymymeal.app.common.compose.utility.LocalDateFormatter
import dev.tbobm.mymymeal.app.common.domain.date.DateProvider
import dev.tbobm.mymymeal.app.common.extension.now
import dev.tbobm.mymymeal.app.fooddiary.domain.usecase.ObserveDiaryMealsUseCase
import dev.tbobm.mymymeal.app.goals.domain.repository.GoalsRepository
import foodyou.app.generated.resources.*
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minusMonth
import kotlinx.datetime.plus
import kotlinx.datetime.plusMonth
import kotlinx.datetime.yearMonth
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

/**
 * PRD 3.4: month grid, one cell per day, coloured by that day's position against target. Tapping
 * a day selects it and returns to the diary (via [onDayClick]); long-pressing shows a quick
 * summary in place.
 */
@Composable
fun CalendarMonthScreen(
    onBack: () -> Unit,
    onDayClick: (epochDay: Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val observeDiaryMeals = koinInject<ObserveDiaryMealsUseCase>()
    val goalsRepository = koinInject<GoalsRepository>()
    val dateProvider = koinInject<DateProvider>()
    val dateFormatter = LocalDateFormatter.current

    val today = dateProvider.observeDate().collectAsStateWithLifecycle(LocalDate.now()).value
    var yearMonth by remember(today) { mutableStateOf(today.yearMonth) }

    val days = remember(yearMonth) { yearMonth.daysInMonth() }
    val summaries by
        remember(days) {
                combine(days.map { observeDaySummary(it, observeDiaryMeals, goalsRepository) }) {
                    it.toList()
                }
            }
            .collectAsStateWithLifecycle(initialValue = emptyList())

    var detailDay by remember { mutableStateOf<DaySummary?>(null) }
    detailDay?.let { summary ->
        DaySummaryBottomSheet(summary = summary, onDismissRequest = { detailDay = null })
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(dateFormatter.formatMonthYear(yearMonth)) },
                navigationIcon = { ArrowBackIconButton(onBack) },
                actions = {
                    IconButton(onClick = { yearMonth = yearMonth.minusMonth() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowLeft,
                            contentDescription = stringResource(Res.string.action_previous_month),
                        )
                    }
                    IconButton(
                        onClick = { yearMonth = yearMonth.plusMonth() },
                        enabled = yearMonth < today.yearMonth,
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                            contentDescription = stringResource(Res.string.action_next_month),
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).padding(horizontal = 16.dp)) {
            WeekDayHeader()
            LazyVerticalGrid(columns = GridCells.Fixed(7)) {
                val leadingBlanks = (days.first().dayOfWeek.isoDayNumber - 1) % 7
                items(leadingBlanks) { Box(modifier = Modifier.aspectRatio(1f)) }

                items(summaries, key = { it.date.toEpochDays() }) { summary ->
                    DayCell(
                        summary = summary,
                        isToday = summary.date == today,
                        onClick = { onDayClick(summary.date.toEpochDays()) },
                        onLongClick = { detailDay = summary },
                    )
                }
            }
        }
    }
}

private fun YearMonth.daysInMonth(): List<LocalDate> {
    val end = lastDay
    return buildList {
        var date = firstDay
        while (date <= end) {
            add(date)
            date = date.plus(1, DateTimeUnit.DAY)
        }
    }
}

@Composable
private fun WeekDayHeader(modifier: Modifier = Modifier) {
    val namesOfDayOfWeek = LocalDateFormatter.current.weekDayNamesShort

    Row(modifier = modifier.fillMaxWidth()) {
        namesOfDayOfWeek.forEach { name ->
            Text(
                text = name,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}

@Composable
private fun DayCell(
    summary: DaySummary,
    isToday: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val hapticFeedback = LocalHapticFeedback.current

    val containerColor =
        when (summary.status) {
            DayStatus.NoData -> MaterialTheme.colorScheme.surfaceContainerHighest
            DayStatus.UnderTarget -> MaterialTheme.colorScheme.primaryContainer
            DayStatus.OverTarget -> MaterialTheme.colorScheme.errorContainer
        }
    val contentColor =
        when (summary.status) {
            DayStatus.NoData -> MaterialTheme.colorScheme.onSurfaceVariant
            DayStatus.UnderTarget -> MaterialTheme.colorScheme.onPrimaryContainer
            DayStatus.OverTarget -> MaterialTheme.colorScheme.onErrorContainer
        }

    Box(
        modifier =
            modifier
                .padding(2.dp)
                .aspectRatio(1f)
                .clip(CircleShape)
                .background(containerColor)
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        onLongClick()
                    },
                ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = summary.date.day.toString(),
            color = contentColor,
            textAlign = TextAlign.Center,
            style =
                if (isToday) {
                    MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                } else {
                    MaterialTheme.typography.bodyMedium
                },
        )
    }
}

@Composable
private fun DaySummaryBottomSheet(summary: DaySummary, onDismissRequest: () -> Unit) {
    val dateFormatter = LocalDateFormatter.current
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(onDismissRequest = onDismissRequest, sheetState = sheetState) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = dateFormatter.formatDate(summary.date),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text =
                    stringResource(
                        Res.string.description_calendar_day_summary_energy,
                        summary.energy.roundToInt(),
                        summary.energyGoal.roundToInt(),
                    )
            )
            Text(
                text =
                    stringResource(
                        Res.string.description_calendar_day_summary_macros,
                        summary.proteins.roundToInt(),
                        summary.proteinsGoal.roundToInt(),
                        summary.carbohydrates.roundToInt(),
                        summary.carbohydratesGoal.roundToInt(),
                        summary.fats.roundToInt(),
                        summary.fatsGoal.roundToInt(),
                    )
            )
        }
    }
}
