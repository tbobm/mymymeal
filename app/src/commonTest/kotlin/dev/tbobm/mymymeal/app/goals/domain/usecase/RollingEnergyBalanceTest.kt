package dev.tbobm.mymymeal.app.goals.domain.usecase

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Pure-function tests for the rolling weekly energy budget calculation (PRD 3.1). No I/O, no
 * coroutines, no Room -- just the arithmetic.
 */
class RollingEnergyBalanceTest {

    @Test
    fun `balance is zero when every day exactly hits target`() {
        val days = List(7) { DayEnergy(consumedKcal = 2000.0, targetKcal = 2000.0) }

        val balance = rollingEnergyBalance(days)

        assertEquals(0.0, balance.balanceKcal)
    }

    @Test
    fun `balance sums surplus and deficit across the window`() {
        val days =
            listOf(
                DayEnergy(consumedKcal = 2500.0, targetKcal = 2000.0), // +500
                DayEnergy(consumedKcal = 1800.0, targetKcal = 2000.0), // -200
                DayEnergy(consumedKcal = 2000.0, targetKcal = 2000.0), // 0
            )

        val balance = rollingEnergyBalance(days)

        assertEquals(300.0, balance.balanceKcal)
    }

    @Test
    fun `a single high day is absorbed into an otherwise-under-target window`() {
        // One big surplus day (+1000) offset by six deficit days (-200 each = -1200).
        val highDay = DayEnergy(consumedKcal = 3000.0, targetKcal = 2000.0)
        val deficitDays = List(6) { DayEnergy(consumedKcal = 1800.0, targetKcal = 2000.0) }

        val balance = rollingEnergyBalance(listOf(highDay) + deficitDays)

        assertTrue(balance.balanceKcal < 0, "rolling window should still read as a net deficit")
        assertTrue(highDay.isOverTarget, "the high day is still individually over target")
    }

    @Test
    fun `isOverTarget flags only days that individually exceed target`() {
        val overTarget = DayEnergy(consumedKcal = 2100.0, targetKcal = 2000.0)
        val underTarget = DayEnergy(consumedKcal = 1900.0, targetKcal = 2000.0)
        val exactlyOnTarget = DayEnergy(consumedKcal = 2000.0, targetKcal = 2000.0)

        assertTrue(overTarget.isOverTarget)
        assertFalse(underTarget.isOverTarget)
        assertFalse(exactlyOnTarget.isOverTarget)
    }

    @Test
    fun `empty window has zero balance`() {
        assertEquals(0.0, rollingEnergyBalance(emptyList()).balanceKcal)
    }
}
