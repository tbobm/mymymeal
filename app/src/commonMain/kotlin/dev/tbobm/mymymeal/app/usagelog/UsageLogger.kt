package dev.tbobm.mymymeal.app.usagelog

import dev.tbobm.mymymeal.app.common.domain.userpreferences.UserPreferencesRepository
import dev.tbobm.mymymeal.app.settings.domain.entity.Settings
import dev.tbobm.mymymeal.app.usagelog.infrastructure.room.UsageEventEntity
import dev.tbobm.mymymeal.app.usagelog.infrastructure.room.UsageLogDao
import kotlin.time.Clock
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Local-only usage log. [log] is fire-and-forget on [scope] (an IO scope in production) and never
 * throws. Raw events are kept [RETENTION_DAYS] days, then folded into daily counts on creation.
 */
class UsageLogger(
    private val dao: UsageLogDao,
    private val settings: UserPreferencesRepository<Settings>,
    private val scope: CoroutineScope,
    private val nowEpochSeconds: () -> Long = { Clock.System.now().epochSeconds },
) {
    internal val startupCompaction: Job =
        scope.launch { safely { dao.compact(nowEpochSeconds() - RETENTION_DAYS * SECONDS_PER_DAY) } }

    fun log(code: UsageCode, arg: Int? = null): Job =
        scope.launch {
            safely {
                if (settings.observe().first().usageLogEnabled) {
                    dao.insert(UsageEventEntity(ts = nowEpochSeconds(), code = code.id, arg = arg))
                }
            }
        }

    private suspend fun safely(block: suspend () -> Unit) {
        try {
            block()
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            // Logging must never affect the app.
        }
    }

    companion object {
        const val RETENTION_DAYS = 90L
        const val SECONDS_PER_DAY = 86_400L
    }
}
