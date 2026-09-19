package com.maksimowiczm.foodyou.fooddiary.domain.healthexport

import com.maksimowiczm.foodyou.fooddiary.domain.entity.DiaryMeal
import kotlin.time.Instant
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant

/**
 * A meal type as understood by the Android health ecosystem (Health Connect's `MealType`). Kept
 * platform-agnostic here so the mapping below stays unit-testable without the Health Connect
 * dependency, which is `androidMain`-only.
 */
enum class HealthMealType {
    Unknown,
    Breakfast,
    Lunch,
    Dinner,
    Snack,
}

/**
 * One day's worth of a single diary meal, shaped for export to a health-data platform (Health
 * Connect). Mirrors how MyFitnessPal syncs to Health Connect: one aggregate record per meal, not
 * per food entry, because diary entries carry no time-of-day of their own.
 *
 * @property clientRecordId Stable, derivable from (date, meal) so re-syncing a day upserts rather
 *   than duplicates.
 */
data class NutritionExportRecord(
    val clientRecordId: String,
    val start: Instant,
    val end: Instant,
    val mealName: String,
    val mealType: HealthMealType,
    val energyKcal: Double,
    val proteinGrams: Double,
    val carbohydrateGrams: Double,
    val fatGrams: Double,
)

/**
 * Maps a diary meal on [date] to a [NutritionExportRecord], or `null` for a meal with no entries
 * (nothing to export, and nothing to keep upserting).
 *
 * The meal's [start, end) window comes from [DiaryMeal.meal]'s `from`/`to` time-of-day, the only
 * time information the diary has for a meal. Three shapes:
 * - normal window (`from < to`): both instants fall on [date].
 * - midnight-crossing window (`to < from`, e.g. a dinner meal spanning 22:00-02:00): the end
 *   instant deliberately falls on `date + 1`. This is correct for Health Connect (it records when
 *   the food was eaten) even though the diary still attributes the whole meal to [date].
 * - all-day window (`from == to`, meaning "no time restriction" per
 *   [com.maksimowiczm.foodyou.fooddiary.domain.usecase.ObserveDiaryMealsUseCase]): spans the full
 *   [date].
 */
fun DiaryMeal.toExportRecord(date: LocalDate, zone: TimeZone): NutritionExportRecord? {
    if (entries.isEmpty()) return null

    val midnight = LocalTime(0, 0)
    val (startDate, startTime) =
        if (meal.from == meal.to) date to midnight else date to meal.from
    val (endDate, endTime) =
        when {
            meal.from == meal.to -> date.plus(1, DateTimeUnit.DAY) to midnight
            meal.to < meal.from -> date.plus(1, DateTimeUnit.DAY) to meal.to
            else -> date to meal.to
        }

    val facts = nutritionFacts

    return NutritionExportRecord(
        clientRecordId = "meal:${meal.id}:${date.toEpochDays()}",
        start = LocalDateTime(startDate, startTime).toInstant(zone),
        end = LocalDateTime(endDate, endTime).toInstant(zone),
        mealName = meal.name,
        mealType = meal.name.toHealthMealType(),
        energyKcal = facts.energy.value ?: 0.0,
        proteinGrams = facts.proteins.value ?: 0.0,
        carbohydrateGrams = facts.carbohydrates.value ?: 0.0,
        fatGrams = facts.fats.value ?: 0.0,
    )
}

/**
 * mymymeal's meals are user-definable; Health Connect's meal type is a fixed enum. Map the
 * default names case-insensitively and fall back to [HealthMealType.Unknown] for anything else —
 * the same degradation MyFitnessPal applies to custom meal headings. [DiaryMeal.meal]'s real name
 * still goes on the record, so nothing is lost.
 */
private fun String.toHealthMealType(): HealthMealType =
    when (trim().lowercase()) {
        "breakfast" -> HealthMealType.Breakfast
        "lunch" -> HealthMealType.Lunch
        "dinner" -> HealthMealType.Dinner
        "snack" -> HealthMealType.Snack
        else -> HealthMealType.Unknown
    }
