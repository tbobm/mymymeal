package dev.tbobm.mymymeal.app.fooddiary.domain

import dev.tbobm.mymymeal.app.fooddiary.domain.entity.Meal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.datetime.LocalTime

class PlausibleMealTest {
    private fun meal(id: Long, from: Int, to: Int) =
        Meal(id, "m$id", LocalTime(from, 0), LocalTime(to, 0), rank = id.toInt())

    private val breakfast = meal(1, 6, 10)
    private val lunch = meal(2, 12, 14)
    private val dinner = meal(3, 19, 22)
    private val meals = listOf(dinner, breakfast, lunch)

    @Test fun inRange() = assertEquals(2, plausibleMeal(LocalTime(13, 0), meals)?.id)

    @Test fun inclusiveEdge() = assertEquals(1, plausibleMeal(LocalTime(10, 0), meals)?.id)

    @Test fun gapPicksNearest() {
        assertEquals(2, plausibleMeal(LocalTime(11, 30), meals)?.id)
        assertEquals(2, plausibleMeal(LocalTime(16, 0), meals)?.id)
        assertEquals(3, plausibleMeal(LocalTime(17, 30), meals)?.id)
    }

    @Test fun gapTieLowerRank() =
        assertEquals(1, plausibleMeal(LocalTime(11, 0), listOf(meal(2, 12, 14), meal(1, 6, 10)))?.id)

    @Test fun lateNightWrapsToBreakfast() =
        assertEquals(1, plausibleMeal(LocalTime(2, 0), meals)?.id)

    @Test fun windowWrappingMidnight() {
        val night = meal(4, 23, 1)
        assertEquals(4, plausibleMeal(LocalTime(0, 30), meals + night)?.id)
        assertEquals(4, plausibleMeal(LocalTime(23, 30), meals + night)?.id)
    }

    @Test fun singleMeal() = assertEquals(2, plausibleMeal(LocalTime(3, 0), listOf(lunch))?.id)

    @Test fun empty() = assertNull(plausibleMeal(LocalTime(3, 0), emptyList()))
}
