package dev.tbobm.mymymeal.app.app.ui.meal

import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf

fun Module.meal() {
    viewModelOf(::MealSettingsViewModel)
}
