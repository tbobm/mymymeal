package com.maksimowiczm.foodyou.settings.infrastructure

import com.maksimowiczm.foodyou.settings.domain.entity.HomeCard
import kotlin.test.Test
import kotlin.test.assertEquals

class HomeCardOrderBackfillTest {
    @Test
    fun `parsing an order missing a newer enum entry appends it at the end`() {
        // Simulates a pre-Habits stored value: only ordinals 0 (Calendar) and 2 (Meals),
        // deliberately dropping 1 (Goals) too, to prove backfill order follows enum
        // declaration order, not just "append the one new card".
        val stored = "0,2"

        val result = parseHomeCardOrderForTest(stored)

        assertEquals(listOf(HomeCard.Calendar, HomeCard.Meals, HomeCard.Goals, HomeCard.Habits), result)
    }

    @Test
    fun `a fully up to date stored order is returned unchanged`() {
        val stored = HomeCard.entries.joinToString(",") { it.ordinal.toString() }

        val result = parseHomeCardOrderForTest(stored)

        assertEquals(HomeCard.entries, result)
    }
}
