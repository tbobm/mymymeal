package dev.tbobm.mymymeal.app.poll.infrastructure

import dev.tbobm.mymymeal.app.common.infrastructure.koin.userPreferencesRepositoryOf
import dev.tbobm.mymymeal.app.poll.domain.repository.PollRepository
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind

internal fun Module.pollInfrastructureModule() {
    factoryOf(::StaticPollRepository).bind<PollRepository>()
    userPreferencesRepositoryOf(::DataStorePollPreferencesRepository)
}
