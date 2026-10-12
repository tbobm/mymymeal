package dev.tbobm.mymymeal.app.poll

import dev.tbobm.mymymeal.app.poll.domain.pollDomainModule
import dev.tbobm.mymymeal.app.poll.infrastructure.pollInfrastructureModule
import org.koin.dsl.module

val pollModule = module {
    pollDomainModule()
    pollInfrastructureModule()
}
