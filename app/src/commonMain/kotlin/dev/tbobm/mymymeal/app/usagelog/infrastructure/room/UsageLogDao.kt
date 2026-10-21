package dev.tbobm.mymymeal.app.usagelog.infrastructure.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction

@Dao
abstract class UsageLogDao {
    @Insert abstract suspend fun insert(event: UsageEventEntity)

    @Query("SELECT * FROM UsageEvent ORDER BY ts, id")
    abstract suspend fun events(): List<UsageEventEntity>

    @Query("SELECT * FROM UsageDaily ORDER BY day, code")
    abstract suspend fun daily(): List<UsageDailyEntity>

    @Query(
        "INSERT OR REPLACE INTO UsageDaily (day, code, count) " +
            "SELECT e.ts / 86400, e.code, COUNT(*) + COALESCE(" +
            "(SELECT d.count FROM UsageDaily d WHERE d.day = e.ts / 86400 AND d.code = e.code), 0) " +
            "FROM UsageEvent e WHERE e.ts < :cutoff GROUP BY e.ts / 86400, e.code"
    )
    protected abstract suspend fun foldInto(cutoff: Long)

    @Query("DELETE FROM UsageEvent WHERE ts < :cutoff") protected abstract suspend fun prune(cutoff: Long)

    /** Folds raw events older than [cutoff] (epoch seconds) into [UsageDailyEntity] rows. */
    @Transaction
    open suspend fun compact(cutoff: Long) {
        foldInto(cutoff)
        prune(cutoff)
    }

    @Query("DELETE FROM UsageEvent") protected abstract suspend fun deleteEvents()

    @Query("DELETE FROM UsageDaily") protected abstract suspend fun deleteDaily()

    @Transaction
    open suspend fun clear() {
        deleteEvents()
        deleteDaily()
    }
}
