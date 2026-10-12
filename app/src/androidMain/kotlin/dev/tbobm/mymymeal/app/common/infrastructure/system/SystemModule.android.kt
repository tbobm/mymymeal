package dev.tbobm.mymymeal.app.common.infrastructure.system

import dev.tbobm.mymymeal.app.common.system.SystemDetails
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind

actual fun Module.systemDetailsDefinition() {
    singleOf(::AndroidSystemDetails).bind<SystemDetails>()
}
