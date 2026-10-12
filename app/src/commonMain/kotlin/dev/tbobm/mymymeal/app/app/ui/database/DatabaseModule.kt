package dev.tbobm.mymymeal.app.app.ui.database

import dev.tbobm.mymymeal.app.app.ui.database.exportcsvproducts.exportCsvProductsModule
import dev.tbobm.mymymeal.app.app.ui.database.exportfulldata.exportFullDataModule
import dev.tbobm.mymymeal.app.app.ui.database.externaldatabases.externalDatabasesModule
import dev.tbobm.mymymeal.app.app.ui.database.healthconnect.healthConnectModule
import dev.tbobm.mymymeal.app.app.ui.database.importcsvproducts.importCsvProductsModule
import dev.tbobm.mymymeal.app.app.ui.database.swissfoodcompositiondatabase.swissFoodCompositionDatabaseModule
import org.koin.core.module.Module

fun Module.database() {
    exportCsvProductsModule()
    exportFullDataModule()
    externalDatabasesModule()
    importCsvProductsModule()
    swissFoodCompositionDatabaseModule()
    healthConnectModule()
}
