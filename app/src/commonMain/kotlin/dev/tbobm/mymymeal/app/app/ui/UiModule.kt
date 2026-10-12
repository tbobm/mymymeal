package dev.tbobm.mymymeal.app.app.ui

import dev.tbobm.mymymeal.app.app.ui.changelog.changelog
import dev.tbobm.mymymeal.app.app.ui.database.database
import dev.tbobm.mymymeal.app.app.ui.food.diary.foodDiary
import dev.tbobm.mymymeal.app.app.ui.food.food
import dev.tbobm.mymymeal.app.app.ui.food.generic.genericFoods
import dev.tbobm.mymymeal.app.app.ui.goals.goals
import dev.tbobm.mymymeal.app.app.ui.home.home
import dev.tbobm.mymymeal.app.app.ui.language.language
import dev.tbobm.mymymeal.app.app.ui.meal.meal
import dev.tbobm.mymymeal.app.app.ui.onboarding.onboarding
import dev.tbobm.mymymeal.app.app.ui.personalization.personalization
import dev.tbobm.mymymeal.app.app.ui.tag.tag
import dev.tbobm.mymymeal.app.app.ui.theme.theme
import dev.tbobm.mymymeal.app.common.infrastructure.koin.userPreferencesRepository
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val uiModule = module {
    viewModel { AppViewModel(settingsRepository = userPreferencesRepository()) }

    changelog()
    database()
    food()
    foodDiary()
    genericFoods()
    goals()
    home()
    language()
    meal()
    onboarding()
    personalization()
    tag()
    theme()
}
