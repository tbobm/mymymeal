package dev.tbobm.mymymeal.app.common.infrastructure.system

import dev.tbobm.mymymeal.app.common.domain.date.DateProvider
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind

expect fun Module.systemDetailsDefinition()

fun Module.systemModule() {
    systemDetailsDefinition()
    factoryOf(::DateProviderImpl).bind<DateProvider>()
}
