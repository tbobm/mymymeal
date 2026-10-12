package dev.tbobm.mymymeal.app.food.search.domain

import dev.tbobm.mymymeal.app.common.infrastructure.koin.eventHandlerOf
import dev.tbobm.mymymeal.app.common.infrastructure.koin.userPreferencesRepository
import org.koin.core.module.Module
import org.koin.dsl.bind

fun Module.foodSearchDomainModule() {
    factory {
            FoodSearchUseCase(
                foodSearchRepository = get(),
                foodSearchPreferencesRepository = userPreferencesRepository(),
                foodRemoteMediatorFactoryAggregate = get(),
                dateProvider = get(),
                eventBus = get(),
            )
        }
        .bind<FoodSearchUseCase>()

    eventHandlerOf(::FoodSearchEventHandler)
}
