package dev.tbobm.mymymeal.app.settings

import dev.tbobm.mymymeal.app.settings.infrastructure.settingsInfrastructureModule
import org.koin.dsl.module

val settingsModule = module { settingsInfrastructureModule() }
