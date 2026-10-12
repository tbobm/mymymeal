package dev.tbobm.mymymeal.app.app.ui.database.swissfoodcompositiondatabase

import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf

internal fun Module.swissFoodCompositionDatabaseModule() {
    viewModelOf(::SwissFoodCompositionDatabaseViewModel)
}
