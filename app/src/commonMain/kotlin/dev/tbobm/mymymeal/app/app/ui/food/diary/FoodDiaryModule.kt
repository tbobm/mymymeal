package dev.tbobm.mymymeal.app.app.ui.food.diary

import dev.tbobm.mymymeal.app.app.ui.food.diary.add.foodDiaryAdd
import dev.tbobm.mymymeal.app.app.ui.food.diary.quickadd.foodDiaryQuickAdd
import dev.tbobm.mymymeal.app.app.ui.food.diary.search.foodDiarySearch
import dev.tbobm.mymymeal.app.app.ui.food.diary.update.foodDiaryUpdate
import org.koin.core.module.Module

fun Module.foodDiary() {
    foodDiaryAdd()
    foodDiaryQuickAdd()
    foodDiarySearch()
    foodDiaryUpdate()
}
