package com.maksimowiczm.foodyou.app.infrastructure.android.healthconnect

import android.content.Context
import android.util.Log
import com.maksimowiczm.foodyou.common.domain.date.DateProvider
import com.maksimowiczm.foodyou.common.domain.userpreferences.UserPreferencesRepository
import com.maksimowiczm.foodyou.fooddiary.domain.entity.DiaryMeal
import com.maksimowiczm.foodyou.fooddiary.domain.healthexport.HealthConnectPreferences
import com.maksimowiczm.foodyou.fooddiary.domain.healthexport.NutritionExportRecord
import com.maksimowiczm.foodyou.fooddiary.domain.healthexport.toExportRecord
import com.maksimowiczm.foodyou.fooddiary.domain.usecase.ObserveDiaryMealsUseCase
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
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

    private data class DayExport(
        val records: List<NutritionExportRecord>,
        val emptyMealIds: List<String>,
    )

    private fun List<DiaryMeal>.toDayExport(date: LocalDate) =
        DayExport(
            records = mapNotNull { it.toExportRecord(date, zone) },
            emptyMealIds =
                filter { it.entries.isEmpty() }.map { "meal:${it.meal.id}:${date.toEpochDays()}" },
        )

    @OptIn(FlowPreview::class)
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
                    // The diary flow re-emits every second; only write when the export changes.
                    observeDiaryMeals
                        .observe(today)
                        .map { it.toDayExport(today) }
                        .distinctUntilChanged()
                        .debounce(WRITE_DEBOUNCE_MS)
                        .collectLatest { syncDate(writer, it) }
                }
            }
    }

    /** Returns false if Health Connect rejected a write (e.g. rate limited) and the run stopped. */
    suspend fun backfill(days: Int): Boolean {
        val ready = checkHealthConnectAvailability(context) as? HealthConnectAvailability.Ready
            ?: return true
        val writer = HealthConnectNutritionWriter(ready.client)
        val today = dateProvider.now(zone).date

        for (offset in 0 until days) {
            val date = today.minus(offset, DateTimeUnit.DAY)
            val export = observeDiaryMeals.observe(date).first().toDayExport(date)
            if (!syncDate(writer, export)) return false
        }
        return true
    }

    /** Never throws (bar cancellation): the next real change retries, writes are idempotent. */
    private suspend fun syncDate(writer: HealthConnectNutritionWriter, export: DayExport): Boolean =
        try {
            writer.upsert(export.records)
            writer.delete(export.emptyMealIds)
            preferences.update {
                copy(lastSyncedEpochSeconds = dateProvider.nowInstant().epochSeconds)
            }
            true
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "Health Connect write failed, skipping", e)
            false
        }

    private companion object {
        const val TAG = "HealthConnectSync"
        const val WRITE_DEBOUNCE_MS = 2_000L
    }
}
