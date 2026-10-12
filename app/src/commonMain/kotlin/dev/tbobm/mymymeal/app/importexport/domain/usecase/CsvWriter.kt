package dev.tbobm.mymymeal.app.importexport.domain.usecase

internal class CsvWriter {
    fun writeString(value: String): String = "\"$value\""

    fun writeDouble(value: Double): String = value.toString()

    fun writeLong(value: Long): String = value.toString()

    fun writeBoolean(value: Boolean): String = if (value) "1" else "0"

    fun write(value: Any?): String =
        when (value) {
            is String -> writeString(value)
            is Double -> writeDouble(value)
            is Long -> writeLong(value)
            is Boolean -> writeBoolean(value)
            null -> ""
            else -> error("Unsupported type for CSV export: ${value::class.simpleName}")
        }
}
