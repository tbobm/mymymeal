package dev.tbobm.mymymeal.app.poll.domain

import dev.tbobm.mymymeal.app.common.infrastructure.koin.userPreferencesRepository
import dev.tbobm.mymymeal.app.poll.domain.usecase.ObserveActivePollUseCase
import org.koin.core.module.Module

internal fun Module.pollDomainModule() {
    factory {
        ObserveActivePollUseCase(
            settingsRepository = userPreferencesRepository(),
            pollPreferencesRepository = userPreferencesRepository(),
            pollRepository = get(),
            dateProvider = get(),
        )
    }
}
