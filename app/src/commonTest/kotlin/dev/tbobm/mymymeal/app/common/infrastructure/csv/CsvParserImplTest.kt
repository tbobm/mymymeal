package dev.tbobm.mymymeal.app.common.infrastructure.csv

import dev.tbobm.mymymeal.app.common.csv.CsvParser
import dev.tbobm.mymymeal.app.common.csv.RfcCsvParserTest

class CsvParserImplTest : RfcCsvParserTest() {
    override val parser: CsvParser = CsvParserImpl()
}
