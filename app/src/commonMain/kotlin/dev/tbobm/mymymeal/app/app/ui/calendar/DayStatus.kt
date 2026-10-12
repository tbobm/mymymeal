package dev.tbobm.mymymeal.app.app.ui.calendar

/** A day's logged energy classified against its energy goal, for calendar-cell coloring. */
enum class DayStatus {
    /** Nothing logged for the day. */
    NoData,

    /** Logged energy is at or below the goal. */
    UnderTarget,

    /** Logged energy is above the goal. */
    OverTarget,
}

/**
 * Classifies a day for calendar coloring.
 *
 * @param loggedEnergy total logged energy (kcal) for the day, or null/non-positive if nothing was
 *   logged.
 * @param goalEnergy the day's energy goal (kcal).
 */
fun classifyDay(loggedEnergy: Double?, goalEnergy: Double): DayStatus = when {
    loggedEnergy == null || loggedEnergy <= 0.0 -> DayStatus.NoData
    loggedEnergy <= goalEnergy -> DayStatus.UnderTarget
    else -> DayStatus.OverTarget
}
