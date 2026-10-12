package dev.tbobm.mymymeal.app.food

import dev.tbobm.mymymeal.app.food.domain.foodDomainModule
import dev.tbobm.mymymeal.app.food.infrastructure.foodInfrastructureModule
import org.koin.dsl.module

val foodModule = module {
    foodDomainModule()
    foodInfrastructureModule()
}
