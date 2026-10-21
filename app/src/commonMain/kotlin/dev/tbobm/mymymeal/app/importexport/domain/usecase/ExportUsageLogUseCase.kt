package dev.tbobm.mymymeal.app.importexport.domain.usecase

import dev.tbobm.mymymeal.app.usagelog.UsageCode
import dev.tbobm.mymymeal.app.usagelog.infrastructure.room.UsageLogDao
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.LocalDate

interface ExportUsageLogUseCase {
    /** Raw events: `ts_iso,code_name,arg`. */
    fun exportEvents(): Flow<String>

    /** Rolled-up events older than the raw retention window: `day,code_name,count`. */
    fun exportDaily(): Flow<String>
}

internal class ExportUsageLogUseCaseImpl(private val dao: UsageLogDao) : ExportUsageLogUseCase {
    override fun exportEvents(): Flow<String> = flow {
        emit("ts_iso,code_name,arg")
        dao.events().forEach {
            emit("${Instant.fromEpochSeconds(it.ts)},${codeName(it.code)},${it.arg ?: ""}")
        }
    }

    override fun exportDaily(): Flow<String> = flow {
        emit("day,code_name,count")
        dao.daily().forEach {
            emit("${LocalDate.fromEpochDays(it.day)},${codeName(it.code)},${it.count}")
        }
    }

    private fun codeName(id: Int) = UsageCode.fromId(id)?.name ?: "UNKNOWN_$id"
}
