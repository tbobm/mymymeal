package com.maksimowiczm.foodyou.habits.domain.entity

/**
 * A supplement the user takes regularly. [tracksDose] selects whether intake carries a daily
 * amount (grams) or is presence-only (taken/not-taken).
 */
data class Supplement(
    val id: Long,
    val name: String,
    val sortOrder: Int,
    val tracksDose: Boolean = false,
)

/** A day's intake for one supplement. Presence implies taken; [doseGrams] only when tracked. */
data class SupplementIntake(val supplementId: Long, val doseGrams: Double?)
