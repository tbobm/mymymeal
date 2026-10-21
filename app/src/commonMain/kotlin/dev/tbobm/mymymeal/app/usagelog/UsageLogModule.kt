package dev.tbobm.mymymeal.app.usagelog

import dev.tbobm.mymymeal.app.common.infrastructure.koin.applicationCoroutineScope
import dev.tbobm.mymymeal.app.common.infrastructure.koin.userPreferencesRepository
import dev.tbobm.mymymeal.app.usagelog.infrastructure.room.UsageLogDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.plus
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val usageLogModule = module {
    factory { get<UsageLogDatabase>().usageLogDao }
    factoryOf(::ClearUsageLogUseCase)
    single {
        UsageLogger(
            dao = get(),
            settings = userPreferencesRepository(),
            logger = get(),
            scope = applicationCoroutineScope() + Dispatchers.IO,
        )
    }
}
