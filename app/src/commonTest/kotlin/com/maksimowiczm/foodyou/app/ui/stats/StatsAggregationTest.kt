package com.maksimowiczm.foodyou.app.ui.stats

import com.maksimowiczm.foodyou.app.ui.calendar.DaySummary
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.datetime.LocalDate

class StatsAggregationTest {
    private fun day(energy: Double, goal: Double = 2000.0) =
        DaySummary(
            date = LocalDate(2026, 1, 1),
            energy = energy,
            energyGoal = goal,
            proteins = energy / 10,
            proteinsGoal = 150.0,
            carbohydrates = 0.0,
            carbohydratesGoal = 0.0,
            fats = 0.0,
            fatsGoal = 0.0,
        )

    @Test
    fun `streak skips an empty today but breaks on an earlier gap`() {
        val s = computeStats(listOf(day(0.0), day(1800.0), day(0.0), day(1900.0), day(0.0)))
        assertEquals(1, s.streak)
        assertEquals(2, s.daysLogged)
    }

    @Test
    fun `streak counts today when logged`() {
        assertEquals(3, computeStats(listOf(day(0.0), day(1.0), day(1.0), day(1.0))).streak)
    }

    @Test
    fun `averages ignore unlogged days and count on-target days`() {
        val s = computeStats(listOf(day(1000.0), day(0.0), day(3000.0)))
        assertEquals(2000.0, s.energy7.value)
        assertEquals(2000.0, s.energy7.goal)
        assertEquals(1, s.daysOnTarget)
    }

    @Test
    fun `empty window is all zeros`() {
        val s = computeStats(emptyList())
        assertEquals(0, s.streak)
        assertEquals(0.0, s.energy30.value)
    }
}
