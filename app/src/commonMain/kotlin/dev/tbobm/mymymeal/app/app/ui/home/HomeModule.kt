package dev.tbobm.mymymeal.app.app.ui.home

import dev.tbobm.mymymeal.app.app.ui.home.goals.GoalsViewModel
import dev.tbobm.mymymeal.app.app.ui.home.habits.HabitsCardViewModel
import dev.tbobm.mymymeal.app.app.ui.home.master.HomeViewModel
import dev.tbobm.mymymeal.app.app.ui.home.meals.card.MealsCardsViewModel
import dev.tbobm.mymymeal.app.app.ui.home.meals.settings.MealsCardsSettingsViewModel
import dev.tbobm.mymymeal.app.app.ui.home.personalization.HomePersonalizationViewModel
import dev.tbobm.mymymeal.app.app.ui.home.poll.PollsViewModel
import dev.tbobm.mymymeal.app.common.infrastructure.koin.userPreferencesRepository
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel

fun Module.home() {
    viewModel { HomeViewModel(settingsRepository = userPreferencesRepository()) }
    viewModel {
        MealsCardsViewModel(
            observeDiaryMealsUseCase = get(),
            foodEntryRepository = get(),
            manualEntryRepository = get(),
            mealsPreferencesRepository = userPreferencesRepository(),
            usageLogger = get(),
        )
    }
    viewModel {
        MealsCardsSettingsViewModel(mealsPreferencesRepository = userPreferencesRepository())
    }
    viewModel {
        GoalsViewModel(
            settingsRepository = userPreferencesRepository(),
            observeDiaryMealsUseCase = get(),
            goalsRepository = get(),
            rollingBudgetPreferencesRepository = userPreferencesRepository(),
        )
    }
    viewModel { HomePersonalizationViewModel(settingsRepository = userPreferencesRepository()) }

    viewModel { HabitsCardViewModel(supplementRepository = get(), coffeeRepository = get()) }

    viewModel {
        PollsViewModel(
            observeActivePollUseCase = get(),
            pollPreferencesRepository = userPreferencesRepository(),
        )
    }
}
