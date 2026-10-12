package dev.tbobm.mymymeal.app.goals

import dev.tbobm.mymymeal.app.goals.domain.goalsDomainModule
import dev.tbobm.mymymeal.app.goals.infrastructure.goalsInfrastructureModule
import org.koin.dsl.module

val goalsModule = module {
    goalsInfrastructureModule()
    goalsDomainModule()
}
