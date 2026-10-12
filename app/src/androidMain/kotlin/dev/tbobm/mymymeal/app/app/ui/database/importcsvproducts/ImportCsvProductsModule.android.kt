package dev.tbobm.mymymeal.app.app.ui.database.importcsvproducts

import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf

internal actual fun Module.importCsvProductsModule() {
    viewModelOf(::ImportCsvProductsViewModel)
}
