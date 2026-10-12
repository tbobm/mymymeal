package dev.tbobm.mymymeal.app.app.ui.calendar

import kotlin.test.Test
import kotlin.test.assertEquals

class DayStatusTest {
    @Test
    fun `null logged energy is no data`() {
        assertEquals(DayStatus.NoData, classifyDay(loggedEnergy = null, goalEnergy = 2000.0))
    }

    @Test
    fun `zero logged energy is no data`() {
        assertEquals(DayStatus.NoData, classifyDay(loggedEnergy = 0.0, goalEnergy = 2000.0))
    }

    @Test
    fun `logged energy below goal is under target`() {
        assertEquals(DayStatus.UnderTarget, classifyDay(loggedEnergy = 1500.0, goalEnergy = 2000.0))
    }

    @Test
    fun `logged energy equal to goal is under target`() {
        assertEquals(DayStatus.UnderTarget, classifyDay(loggedEnergy = 2000.0, goalEnergy = 2000.0))
    }

    @Test
    fun `logged energy above goal is over target`() {
        assertEquals(DayStatus.OverTarget, classifyDay(loggedEnergy = 2500.0, goalEnergy = 2000.0))
    }
}
