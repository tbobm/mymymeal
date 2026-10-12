package dev.tbobm.mymymeal.app.app.ui.personalization

import dev.tbobm.mymymeal.app.common.infrastructure.koin.userPreferencesRepository
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel

fun Module.personalization() {
    viewModel { PersonalizationScreenViewModel(settingsRepository = userPreferencesRepository()) }
    viewModel {
        PersonalizeNutritionFactsViewModel(settingsRepository = userPreferencesRepository())
    }
}
