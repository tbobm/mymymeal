package dev.tbobm.mymymeal.app.usagelog.infrastructure.room

import androidx.room.Entity

/** Rollup of events older than the retention window: count per UTC epoch day and code. */
@Entity(tableName = "UsageDaily", primaryKeys = ["day", "code"])
data class UsageDailyEntity(val day: Long, val code: Int, val count: Long)
