package dev.tbobm.mymymeal.app.app.ui.tag

import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf

fun Module.tag() {
    viewModelOf(::TagSettingsViewModel)
}
