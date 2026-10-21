package dev.tbobm.mymymeal.app.usagelog.infrastructure.room

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** One raw usage event. [code] is a [dev.tbobm.mymymeal.app.usagelog.UsageCode] id. */
@Entity(tableName = "UsageEvent", indices = [Index("ts")])
data class UsageEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** Epoch seconds. */
    val ts: Long,
    val code: Int,
    /** Small optional argument, e.g. a meal id. */
    val arg: Int? = null,
)
