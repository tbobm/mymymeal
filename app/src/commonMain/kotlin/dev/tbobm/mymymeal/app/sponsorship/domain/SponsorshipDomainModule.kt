package dev.tbobm.mymymeal.app.sponsorship.domain

import dev.tbobm.mymymeal.app.common.infrastructure.koin.eventHandler
import dev.tbobm.mymymeal.app.common.infrastructure.koin.userPreferencesRepository
import dev.tbobm.mymymeal.app.sponsorship.domain.event.AppLaunchEventHandler
import org.koin.core.module.Module

fun Module.sponsorshipDomainModule() {
    eventHandler {
        AppLaunchEventHandler(
            sponsorshipPreferencesRepository = userPreferencesRepository(),
            sponsorshipRepository = get(),
        )
    }
}
