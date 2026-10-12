package dev.tbobm.mymymeal.app.app.infrastructure

import dev.tbobm.mymymeal.app.common.log.Logger

expect object MymymealLogger : Logger {
    override fun d(tag: String, throwable: Throwable?, message: () -> String)

    override fun w(tag: String, throwable: Throwable?, message: () -> String)

    override fun e(tag: String, throwable: Throwable?, message: () -> String)

    override fun i(tag: String, throwable: Throwable?, message: () -> String)
}
