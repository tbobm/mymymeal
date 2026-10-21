package dev.tbobm.mymymeal.app.usagelog

import kotlin.test.Test
import kotlin.test.assertEquals

class UsageCodeTest {
    @Test
    fun `ids are unique`() {
        assertEquals(UsageCode.entries.size, UsageCode.entries.map { it.id }.toSet().size)
    }

    @Test
    fun `fromRoute maps object, arg and query routes`() {
        assertEquals(UsageCode.NAV_HOME, UsageCode.fromRoute("dev.tbobm.mymymeal.app.app.navigation.Home"))
        assertEquals(
            UsageCode.NAV_FOOD_SEARCH,
            UsageCode.fromRoute("dev.tbobm.mymymeal.app.app.navigation.FoodDiarySearch/{date}/{mealId}"),
        )
        assertEquals(
            UsageCode.NAV_GOALS,
            UsageCode.fromRoute("dev.tbobm.mymymeal.app.app.navigation.Goals?epochDay={epochDay}"),
        )
    }

    @Test
    fun `fromRoute falls back to NAV_OTHER`() {
        assertEquals(UsageCode.NAV_OTHER, UsageCode.fromRoute(null))
        assertEquals(UsageCode.NAV_OTHER, UsageCode.fromRoute("some.Unknown/{x}"))
    }
}
