package dev.tbobm.mymymeal.app.common.infrastructure.csv

import dev.tbobm.mymymeal.app.common.csv.CsvParser
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind

fun Module.csvModule() {
    factoryOf(::CsvParserImpl).bind<CsvParser>()
}
