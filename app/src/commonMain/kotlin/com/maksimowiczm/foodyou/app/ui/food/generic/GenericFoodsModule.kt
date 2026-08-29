package com.maksimowiczm.foodyou.app.ui.food.generic

import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf

fun Module.genericFoods() {
    viewModelOf(::GenericFoodsViewModel)
}
