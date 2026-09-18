package com.maksimowiczm.foodyou.app.infrastructure.android.healthconnect

import android.content.Context
import com.maksimowiczm.foodyou.common.domain.date.DateProvider
import com.maksimowiczm.foodyou.common.domain.userpreferences.UserPreferencesRepository
import com.maksimowiczm.foodyou.fooddiary.domain.entity.DiaryMeal
import com.maksimowiczm.foodyou.fooddiary.domain.healthexport.HealthConnectPreferences
import com.maksimowiczm.foodyou.fooddiary.domain.healthexport.toExportRecord
import com.maksimowiczm.foodyou.fooddiary.domain.usecase.ObserveDiaryMealsUseCase
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus

/**
 * Keeps Health Connect's nutrition records in sync with the diary, per [HealthConnectPreferences].
 *
 * Two entry points, per the design: [collectLiveUpdates] runs for the process lifetime and syncs
 * today whenever it changes; [backfill] is the on-demand "sync last N days" action. Both funnel
 * through [syncDate], which is idempotent (see [HealthConnectNutritionWriter]'s clientRecordId
 * scheme) — running either twice for the same day is harmless.
 */
class HealthConnectSyncService(
    private val context: Context,
    private val observeDiaryMeals: ObserveDiaryMealsUseCase,
    private val dateProvider: DateProvider,
    private val preferences: UserPreferencesRepository<HealthConnectPreferences>,
) {
    private val zone = TimeZone.currentSystemDefault()

    suspend fun collectLiveUpdates() {
        preferences
            .observe()
            .map { it.syncEnabled }
            .distinctUntilChanged()
            .collectLatest { enabled ->
                if (!enabled) return@collectLatest
                val ready = checkHealthConnectAvailability(context) as? HealthConnectAvailability.Ready
                    ?: return@collectLatest
                val writer = HealthConnectNutritionWriter(ready.client)

                dateProvider.observeDate(zone).collectLatest { today ->
                    observeDiaryMeals.observe(today).collectLatest { meals ->
                        syncDate(writer, today, meals)
                    }
                }
            }
    }

    suspend fun backfill(days: Int) {
        val ready = checkHealthConnectAvailability(context) as? HealthConnectAvailability.Ready
            ?: return
        val writer = HealthConnectNutritionWriter(ready.client)
        val today = dateProvider.now(zone).date

        for (offset in 0 until days) {
            val date = today.minus(offset, DateTimeUnit.DAY)
            syncDate(writer, date, observeDiaryMeals.observe(date).first())
        }
    }

    private suspend fun syncDate(
        writer: HealthConnectNutritionWriter,
        date: LocalDate,
        meals: List<DiaryMeal>,
    ) {
        val records = meals.mapNotNull { it.toExportRecord(date, zone) }
        val emptyMealIds =
            meals.filter { it.entries.isEmpty() }.map { "meal:${it.meal.id}:${date.toEpochDays()}" }

        writer.upsert(records)
        writer.delete(emptyMealIds)

        preferences.update { copy(lastSyncedEpochSeconds = dateProvider.nowInstant().epochSeconds) }
    }
}
