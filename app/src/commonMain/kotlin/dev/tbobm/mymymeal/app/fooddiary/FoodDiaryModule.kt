package dev.tbobm.mymymeal.app.fooddiary

import dev.tbobm.mymymeal.app.fooddiary.domain.foodDiaryDomainModule
import dev.tbobm.mymymeal.app.fooddiary.infrastructure.foodDiaryInfrastructureModule
import org.koin.dsl.module

val foodDiaryModule = module {
    foodDiaryDomainModule()
    foodDiaryInfrastructureModule()
}
