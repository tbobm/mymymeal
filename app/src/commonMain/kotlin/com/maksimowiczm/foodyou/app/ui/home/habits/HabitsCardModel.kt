package com.maksimowiczm.foodyou.app.ui.home.habits

internal data class HabitsCardModel(
    val cupsToday: Int,
    val caffeineMgToday: Int,
    val hasDefaultCoffee: Boolean,
    val supplements: List<SupplementRow>,
)

internal data class SupplementRow(val id: Long, val name: String, val taken: Boolean)
