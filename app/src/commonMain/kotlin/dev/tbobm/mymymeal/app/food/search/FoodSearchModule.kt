package dev.tbobm.mymymeal.app.food.search

import dev.tbobm.mymymeal.app.food.search.domain.foodSearchDomainModule
import dev.tbobm.mymymeal.app.food.search.infrastructure.foodSearchInfrastructureModule
import org.koin.dsl.module

val foodSearchModule = module {
    foodSearchDomainModule()
    foodSearchInfrastructureModule()
}
