package dev.tbobm.mymymeal.app.common.infrastructure.auth

import dev.tbobm.mymymeal.app.common.auth.SessionRepository
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind

fun Module.authModule() {
    factoryOf(::SafeSessionRepository).bind<SessionRepository>()
}
