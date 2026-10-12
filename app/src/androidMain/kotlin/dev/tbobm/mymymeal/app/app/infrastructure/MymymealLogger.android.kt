package dev.tbobm.mymymeal.app.app.infrastructure

import android.util.Log
import dev.tbobm.mymymeal.app.common.log.Logger

actual object MymymealLogger : Logger {
    actual override fun d(tag: String, throwable: Throwable?, message: () -> String) {
        Log.d(tag, message(), throwable)
    }

    actual override fun w(tag: String, throwable: Throwable?, message: () -> String) {
        Log.w(tag, message(), throwable)
    }

    actual override fun e(tag: String, throwable: Throwable?, message: () -> String) {
        Log.e(tag, message(), throwable)
    }

    actual override fun i(tag: String, throwable: Throwable?, message: () -> String) {
        Log.i(tag, message(), throwable)
    }
}
