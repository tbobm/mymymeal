package com.maksimowiczm.foodyou.app.ui.database

import com.maksimowiczm.foodyou.app.ui.database.exportcsvproducts.exportCsvProductsModule
import com.maksimowiczm.foodyou.app.ui.database.exportfulldata.exportFullDataModule
import com.maksimowiczm.foodyou.app.ui.database.externaldatabases.externalDatabasesModule
import com.maksimowiczm.foodyou.app.ui.database.healthconnect.healthConnectModule
import com.maksimowiczm.foodyou.app.ui.database.importcsvproducts.importCsvProductsModule
import com.maksimowiczm.foodyou.app.ui.database.swissfoodcompositiondatabase.swissFoodCompositionDatabaseModule
import org.koin.core.module.Module

fun Module.database() {
    exportCsvProductsModule()
    exportFullDataModule()
    externalDatabasesModule()
    importCsvProductsModule()
    swissFoodCompositionDatabaseModule()
    healthConnectModule()
}
