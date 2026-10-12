package dev.tbobm.mymymeal.app.goals.domain

import dev.tbobm.mymymeal.app.common.infrastructure.koin.userPreferencesRepository
import dev.tbobm.mymymeal.app.goals.domain.usecase.ObserveRollingEnergyBalanceUseCase
import org.koin.core.module.Module

internal fun Module.goalsDomainModule() {
    factory {
        ObserveRollingEnergyBalanceUseCase(
            observeDiaryMeals = get(),
            goalsRepository = get(),
            preferencesRepository = userPreferencesRepository(),
        )
    }
}
