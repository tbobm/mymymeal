package dev.tbobm.mymymeal.app.settings.domain

import dev.tbobm.mymymeal.app.common.infrastructure.koin.eventHandler
import dev.tbobm.mymymeal.app.common.infrastructure.koin.userPreferencesRepository
import dev.tbobm.mymymeal.app.settings.domain.event.AppLaunchEventHandler
import org.koin.core.module.Module

internal fun Module.settingsModule() {
    eventHandler {
        AppLaunchEventHandler(
            settingsRepository = userPreferencesRepository(),
            changelogRepository = get(),
        )
    }
}
