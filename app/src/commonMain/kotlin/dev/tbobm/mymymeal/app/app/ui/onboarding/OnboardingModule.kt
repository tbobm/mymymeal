package dev.tbobm.mymymeal.app.app.ui.onboarding

import dev.tbobm.mymymeal.app.common.infrastructure.koin.userPreferencesRepository
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel

fun Module.onboarding() {
    viewModel {
        OnboardingViewModel(
            importSwissUseCase = get(),
            foodSearchPreferencesRepository = userPreferencesRepository(),
        )
    }
}
