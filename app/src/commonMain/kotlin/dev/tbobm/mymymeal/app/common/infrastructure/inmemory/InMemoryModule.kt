package dev.tbobm.mymymeal.app.common.infrastructure.inmemory

import dev.tbobm.mymymeal.app.common.domain.event.EventBus
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind

fun Module.inMemoryModule() {
    singleOf(::SharedFlowEventBus).bind<EventBus>()
}
