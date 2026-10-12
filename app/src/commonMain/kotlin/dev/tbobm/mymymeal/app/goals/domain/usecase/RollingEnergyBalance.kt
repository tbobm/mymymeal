package dev.tbobm.mymymeal.app.goals.domain.usecase

/**
 * Energy consumed versus target for a single day, in kcal.
 *
 * @property isOverTarget Whether this single day, on its own, exceeded its target. Used only to
 *   flag individual days in the UI when carryover is disabled -- the rolling total below always
 *   pools all days regardless of this flag.
 */
data class DayEnergy(val consumedKcal: Double, val targetKcal: Double) {
    val differenceKcal: Double
        get() = consumedKcal - targetKcal

    val isOverTarget: Boolean
        get() = differenceKcal > 0
}

/**
 * Rolling energy balance over a window of days (PRD 3.1).
 *
 * @property days The per-day energy figures that make up the window. Order does not matter for
 *   [balanceKcal] or the over-target count -- both are order-independent aggregates.
 * @property balanceKcal Cumulative surplus (positive) or deficit (negative) across the window.
 */
data class RollingEnergyBalance(val days: List<DayEnergy>) {
    val balanceKcal: Double
        get() = days.sumOf { it.differenceKcal }
}

/** Pure calculation: no I/O, no coroutines -- just the arithmetic PRD 3.1 asks for. */
fun rollingEnergyBalance(days: List<DayEnergy>): RollingEnergyBalance = RollingEnergyBalance(days)
