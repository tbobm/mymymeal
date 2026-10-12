package dev.tbobm.mymymeal.app.goals.infrastructure

import dev.tbobm.mymymeal.app.common.infrastructure.koin.userPreferencesRepositoryOf
import dev.tbobm.mymymeal.app.goals.domain.repository.GoalsRepository
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind

internal fun Module.goalsInfrastructureModule() {
    factoryOf(::DataStoreGoalsRepository).bind<GoalsRepository>()

    userPreferencesRepositoryOf(::DataStoreRollingBudgetPreferencesRepository)
}
