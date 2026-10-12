package dev.tbobm.mymymeal.app.common.log

import dev.tbobm.mymymeal.app.common.result.Err
import dev.tbobm.mymymeal.app.common.result.Result

interface Logger {
    fun d(tag: String, throwable: Throwable? = null, message: () -> String)

    fun w(tag: String, throwable: Throwable? = null, message: () -> String)

    fun e(tag: String, throwable: Throwable? = null, message: () -> String)

    fun i(tag: String, throwable: Throwable? = null, message: () -> String)
}

inline fun <reified E> Logger.logAndReturnFailure(
    tag: String,
    error: E,
    throwable: Throwable? = null,
    noinline message: () -> String,
): Result<Nothing, E> {
    w(tag, throwable) {
        buildString {
            appendLine("Error: $error")
            appendLine(message())
        }
    }
    return Err(error)
}
