package dev.tbobm.mymymeal.app.fooddiary.domain

import dev.tbobm.mymymeal.app.fooddiary.domain.entity.Meal
import kotlinx.datetime.LocalTime

private const val MINUTES_PER_DAY = 24 * 60

private fun LocalTime.minuteOfDay() = hour * 60 + minute

private fun circularDistance(a: Int, b: Int): Int {
    val d = kotlin.math.abs(a - b)
    return minOf(d, MINUTES_PER_DAY - d)
}

private fun Meal.contains(now: Int): Boolean {
    val f = from.minuteOfDay()
    val t = to.minuteOfDay()
    return if (f <= t) now in f..t else now >= f || now <= t
}

private fun Meal.distance(now: Int): Int =
    minOf(circularDistance(now, from.minuteOfDay()), circularDistance(now, to.minuteOfDay()))

/**
 * Meal whose [from, to] window contains [now] (windows may wrap midnight); otherwise the meal
 * whose window edge is nearest in time. Ties resolve to the lower rank. Null if [meals] is empty.
 */
fun plausibleMeal(now: LocalTime, meals: List<Meal>): Meal? {
    val n = now.minuteOfDay()
    return meals.sortedBy { it.rank }.let { sorted ->
        sorted.firstOrNull { it.contains(n) } ?: sorted.minByOrNull { it.distance(n) }
    }
}
