package com.maksimowiczm.foodyou.settings.domain.entity

enum class HomeCard {
    Calendar,
    Goals,
    Meals,
    Habits;

    companion object {
        val defaultOrder: List<HomeCard> = listOf(Calendar, Goals, Meals, Habits)
    }
}
