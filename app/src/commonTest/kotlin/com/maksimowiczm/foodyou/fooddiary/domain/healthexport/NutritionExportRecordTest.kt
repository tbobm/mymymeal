package com.maksimowiczm.foodyou.fooddiary.domain.healthexport

import com.maksimowiczm.foodyou.common.domain.food.NutritionFacts
import com.maksimowiczm.foodyou.fooddiary.domain.entity.DiaryMeal
import com.maksimowiczm.foodyou.fooddiary.domain.entity.ManualDiaryEntry
import com.maksimowiczm.foodyou.fooddiary.domain.entity.ManualDiaryEntryId
import com.maksimowiczm.foodyou.fooddiary.domain.entity.Meal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

private val UTC = TimeZone.UTC
private val DATE = LocalDate(2026, 9, 18)

private fun meal(id: Long = 1, name: String, from: LocalTime, to: LocalTime) =
    Meal(id = id, name = name, from = from, to = to, rank = 0)

private fun entry(nutritionFacts: NutritionFacts = NutritionFacts()) =
    ManualDiaryEntry(
        id = ManualDiaryEntryId(1),
        mealId = 1,
        date = DATE,
        name = "entry",
        nutritionFacts = nutritionFacts,
        createdAt = LocalDateTime(DATE, LocalTime(12, 0)),
        updatedAt = LocalDateTime(DATE, LocalTime(12, 0)),
    )

class NutritionExportRecordTest {
    @Test
    fun `normal meal window maps to same-day start and end instants`() {
        val diaryMeal =
            DiaryMeal(
                meal = meal(name = "Breakfast", from = LocalTime(8, 0), to = LocalTime(10, 0)),
                entries = listOf(entry()),
            )

        val record = diaryMeal.toExportRecord(DATE, UTC)!!

        assertEquals(LocalDateTime(DATE, LocalTime(8, 0)).toInstant(UTC), record.start)
        assertEquals(LocalDateTime(DATE, LocalTime(10, 0)).toInstant(UTC), record.end)
        assertEquals(HealthMealType.Breakfast, record.mealType)
    }

    @Test
    fun `midnight crossing meal window deliberately lands its end instant on the next day`() {
        // A late dinner meal window (22:00-02:00). The diary still attributes every entry logged
        // under it to DATE, but Health Connect records when food was eaten, so the end instant
        // must fall on DATE + 1. Asserted deliberately so a future change cannot "fix" this into
        // a day shift.
        val diaryMeal =
            DiaryMeal(
                meal = meal(name = "Dinner", from = LocalTime(22, 0), to = LocalTime(2, 0)),
                entries = listOf(entry()),
            )

        val record = diaryMeal.toExportRecord(DATE, UTC)!!

        val nextDay = LocalDate(2026, 9, 19)
        assertEquals(LocalDateTime(DATE, LocalTime(22, 0)).toInstant(UTC), record.start)
        assertEquals(LocalDateTime(nextDay, LocalTime(2, 0)).toInstant(UTC), record.end)
    }

    @Test
    fun `all-day meal window spans the full date`() {
        val diaryMeal =
            DiaryMeal(
                meal = meal(name = "Snacks", from = LocalTime(0, 0), to = LocalTime(0, 0)),
                entries = listOf(entry()),
            )

        val record = diaryMeal.toExportRecord(DATE, UTC)!!

        val nextDay = LocalDate(2026, 9, 19)
        assertEquals(LocalDateTime(DATE, LocalTime(0, 0)).toInstant(UTC), record.start)
        assertEquals(LocalDateTime(nextDay, LocalTime(0, 0)).toInstant(UTC), record.end)
    }

    @Test
    fun `empty meal maps to null`() {
        val diaryMeal =
            DiaryMeal(
                meal = meal(name = "Lunch", from = LocalTime(12, 0), to = LocalTime(14, 0)),
                entries = emptyList(),
            )

        assertNull(diaryMeal.toExportRecord(DATE, UTC))
    }

    @Test
    fun `clientRecordId is stable across repeated calls for the same meal and date`() {
        val diaryMeal =
            DiaryMeal(
                meal = meal(id = 42, name = "Lunch", from = LocalTime(12, 0), to = LocalTime(14, 0)),
                entries = listOf(entry()),
            )

        val first = diaryMeal.toExportRecord(DATE, UTC)!!
        val second = diaryMeal.toExportRecord(DATE, UTC)!!

        assertEquals(first.clientRecordId, second.clientRecordId)
        assertEquals("meal:42:${DATE.toEpochDays()}", first.clientRecordId)
    }

    @Test
    fun `meal type mapping is case-insensitive and falls back to unknown for custom names`() {
        val breakfast =
            DiaryMeal(
                meal = meal(name = "BREAKFAST", from = LocalTime(8, 0), to = LocalTime(9, 0)),
                entries = listOf(entry()),
            )
        val custom =
            DiaryMeal(
                meal = meal(name = "Post-workout", from = LocalTime(8, 0), to = LocalTime(9, 0)),
                entries = listOf(entry()),
            )

        assertEquals(HealthMealType.Breakfast, breakfast.toExportRecord(DATE, UTC)!!.mealType)
        assertEquals(HealthMealType.Unknown, custom.toExportRecord(DATE, UTC)!!.mealType)
    }

    @Test
    fun `incomplete nutrition facts default to zero rather than dropping the record`() {
        val diaryMeal =
            DiaryMeal(
                meal = meal(name = "Lunch", from = LocalTime(12, 0), to = LocalTime(14, 0)),
                entries = listOf(entry(nutritionFacts = NutritionFacts())),
            )

        val record = diaryMeal.toExportRecord(DATE, UTC)!!

        assertEquals(0.0, record.energyKcal)
        assertEquals(0.0, record.proteinGrams)
        assertEquals(0.0, record.carbohydrateGrams)
        assertEquals(0.0, record.fatGrams)
    }
}
