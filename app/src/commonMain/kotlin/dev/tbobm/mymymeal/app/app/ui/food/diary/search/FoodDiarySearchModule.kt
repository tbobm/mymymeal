package dev.tbobm.mymymeal.app.app.ui.food.diary.search

import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf

fun Module.foodDiarySearch() {
    viewModelOf(::DiaryFoodSearchViewModel)
}
