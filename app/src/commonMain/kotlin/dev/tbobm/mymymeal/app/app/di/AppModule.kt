package dev.tbobm.mymymeal.app.app.di

import dev.tbobm.mymymeal.app.app.infrastructure.MymymealConfig
import dev.tbobm.mymymeal.app.app.infrastructure.MymymealLogger
import dev.tbobm.mymymeal.app.app.infrastructure.room.roomModule
import dev.tbobm.mymymeal.app.common.config.AppConfig
import dev.tbobm.mymymeal.app.common.config.NetworkConfig
import dev.tbobm.mymymeal.app.common.infrastructure.auth.authModule
import dev.tbobm.mymymeal.app.common.infrastructure.crypto.cryptoModule
import dev.tbobm.mymymeal.app.common.infrastructure.csv.csvModule
import dev.tbobm.mymymeal.app.common.infrastructure.datastore.dataStoreModule
import dev.tbobm.mymymeal.app.common.infrastructure.inmemory.inMemoryModule
import dev.tbobm.mymymeal.app.common.infrastructure.koin.applicationCoroutineScope
import dev.tbobm.mymymeal.app.common.infrastructure.room.tag.tagModule
import dev.tbobm.mymymeal.app.common.infrastructure.system.systemModule
import dev.tbobm.mymymeal.app.common.log.Logger
import kotlinx.coroutines.CoroutineScope
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.binds
import org.koin.dsl.module

fun appModule(applicationCoroutineScope: CoroutineScope) = module {
    factoryOf(::MymymealConfig).binds(arrayOf(AppConfig::class, NetworkConfig::class))
    single { MymymealLogger }.bind<Logger>()
    applicationCoroutineScope { applicationCoroutineScope }

    authModule()
    cryptoModule()
    csvModule()
    dataStoreModule()
    inMemoryModule()
    roomModule()
    systemModule()
    tagModule()
}
