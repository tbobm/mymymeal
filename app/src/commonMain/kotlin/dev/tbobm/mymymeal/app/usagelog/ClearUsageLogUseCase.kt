package dev.tbobm.mymymeal.app.usagelog

import dev.tbobm.mymymeal.app.usagelog.infrastructure.room.UsageLogDao

class ClearUsageLogUseCase(private val dao: UsageLogDao) {
    suspend fun clear() = dao.clear()
}
