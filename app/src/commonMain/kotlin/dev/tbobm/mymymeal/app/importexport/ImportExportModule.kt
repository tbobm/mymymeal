package dev.tbobm.mymymeal.app.importexport

import dev.tbobm.mymymeal.app.importexport.domain.importExportDomainModule
import org.koin.dsl.module

val importExportModule = module { importExportDomainModule() }
