package com.maksimowiczm.foodyou.app.ui.stats

import com.maksimowiczm.foodyou.app.ui.calendar.DayStatus
import com.maksimowiczm.foodyou.app.ui.calendar.DaySummary

/** Mean of [value] and [goal] over the logged days of a window; zeros when nothing was logged. */
data class AverageVsGoal(val value: Double, val goal: Double)

data class StatsSummary(
    val daysLogged: Int,
    val windowDays: Int,
    val streak: Int,
    val daysOnTarget: Int,
    val energy7: AverageVsGoal,
    val energy30: AverageVsGoal,
    val proteins7: AverageVsGoal,
    val carbohydrates7: AverageVsGoal,
    val fats7: AverageVsGoal,
)

private fun List<DaySummary>.average(
    value: (DaySummary) -> Double,
    goal: (DaySummary) -> Double,
): AverageVsGoal {
    val logged = filter { it.energy > 0.0 }
    if (logged.isEmpty()) return AverageVsGoal(0.0, 0.0)
    return AverageVsGoal(logged.sumOf(value) / logged.size, logged.sumOf(goal) / logged.size)
}

/**
 * Pure aggregation over [days], ordered oldest to newest with the last element being today. A day
 * counts as logged when its energy is positive. Today being empty doesn't break the streak -- it
 * may simply not be logged yet.
 */
fun computeStats(days: List<DaySummary>): StatsSummary {
    val logged = days.map { it.energy > 0.0 }
    val streak =
        logged
            .let { if (it.lastOrNull() == false) it.dropLast(1) else it }
            .reversed()
            .takeWhile { it }
            .size
    val last7 = days.takeLast(7)
    return StatsSummary(
        daysLogged = logged.count { it },
        windowDays = days.size,
        streak = streak,
        daysOnTarget = days.count { it.status == DayStatus.UnderTarget },
        energy7 = last7.average({ it.energy }, { it.energyGoal }),
        energy30 = days.average({ it.energy }, { it.energyGoal }),
        proteins7 = last7.average({ it.proteins }, { it.proteinsGoal }),
        carbohydrates7 = last7.average({ it.carbohydrates }, { it.carbohydratesGoal }),
        fats7 = last7.average({ it.fats }, { it.fatsGoal }),
    )
}
