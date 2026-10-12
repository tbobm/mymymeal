package dev.tbobm.mymymeal.app.app.ui.database.exportcsvproducts

import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf

internal actual fun Module.exportCsvProductsModule() {
    viewModelOf(::ExportProductsViewModel)
}
