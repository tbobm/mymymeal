package dev.tbobm.mymymeal.app.app.ui.database.exportfulldata

import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf

internal actual fun Module.exportFullDataModule() {
    viewModelOf(::ExportFullDataViewModel)
}
