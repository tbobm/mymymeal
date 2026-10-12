package dev.tbobm.mymymeal.app.fooddiary.domain.entity

import dev.tbobm.mymymeal.app.common.domain.userpreferences.UserPreferences

data class MealsPreferences(
    val layout: MealsCardsLayout,
    val useTimeBasedSorting: Boolean,
    val ignoreAllDayMeals: Boolean,
) : UserPreferences
