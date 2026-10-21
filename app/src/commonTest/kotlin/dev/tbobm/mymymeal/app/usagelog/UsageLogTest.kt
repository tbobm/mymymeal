package dev.tbobm.mymymeal.app.usagelog

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.test.core.app.ApplicationProvider
import dev.tbobm.mymymeal.app.common.log.Logger
import dev.tbobm.mymymeal.app.common.domain.userpreferences.UserPreferencesRepository
import dev.tbobm.mymymeal.app.importexport.domain.usecase.ExportUsageLogUseCaseImpl
import dev.tbobm.mymymeal.app.settings.domain.entity.AppLaunchInfo
import dev.tbobm.mymymeal.app.settings.domain.entity.EnergyFormat
import dev.tbobm.mymymeal.app.settings.domain.entity.HomeCard
import dev.tbobm.mymymeal.app.settings.domain.entity.NutrientsOrder
import dev.tbobm.mymymeal.app.settings.domain.entity.Settings
import dev.tbobm.mymymeal.app.usagelog.infrastructure.room.UsageDailyEntity
import dev.tbobm.mymymeal.app.usagelog.infrastructure.room.UsageEventEntity
import dev.tbobm.mymymeal.app.usagelog.infrastructure.room.UsageLogDao
import dev.tbobm.mymymeal.app.usagelog.infrastructure.room.UsageLogDatabase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@Database(
    entities = [UsageEventEntity::class, UsageDailyEntity::class],
    version = 1,
    exportSchema = false,
)
internal abstract class UsageTestDatabase : RoomDatabase(), UsageLogDatabase

private class FakeSettings(enabled: Boolean) : UserPreferencesRepository<Settings> {
    private val state =
        MutableStateFlow(
            Settings(
                lastRememberedVersion = null,
                hidePreviewDialog = false,
                showTranslationWarning = false,
                nutrientsOrder = NutrientsOrder.defaultOrder,
                secureScreen = false,
                homeCardOrder = HomeCard.defaultOrder,
                expandGoalCard = true,
                onboardingFinished = true,
                energyFormat = EnergyFormat.DEFAULT,
                appLaunchInfo = AppLaunchInfo(null, null, 0),
                usageLogEnabled = enabled,
            )
        )

    override fun observe() = state

    override suspend fun update(transform: Settings.() -> Settings) {
        state.value = state.value.transform()
    }
}

private object NoopLogger : Logger {
    override fun d(tag: String, throwable: Throwable?, message: () -> String) = Unit

    override fun w(tag: String, throwable: Throwable?, message: () -> String) = Unit

    override fun e(tag: String, throwable: Throwable?, message: () -> String) = Unit

    override fun i(tag: String, throwable: Throwable?, message: () -> String) = Unit
}

@RunWith(RobolectricTestRunner::class)
class UsageLogTest {
    private val day = UsageLogger.SECONDS_PER_DAY
    private val now = 1_000 * day + 123

    private fun dao(): UsageLogDao =
        Room.inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                UsageTestDatabase::class.java,
            )
            .build()
            .usageLogDao

    private fun logger(dao: UsageLogDao, enabled: Boolean = true) =
        UsageLogger(dao, FakeSettings(enabled), CoroutineScope(Dispatchers.IO), NoopLogger) { now }

    @Test
    fun `log inserts an event with timestamp, code and arg`() = runTest {
        val dao = dao()
        val logger = logger(dao)
        logger.startupCompaction.join()

        logger.log(UsageCode.ACT_ADD_CLICK, 3).join()

        assertEquals(
            listOf(Triple(now, UsageCode.ACT_ADD_CLICK.id, 3)),
            dao.events().map { Triple(it.ts, it.code, it.arg) },
        )
    }

    @Test
    fun `log does nothing when disabled`() = runTest {
        val dao = dao()
        val logger = logger(dao, enabled = false)

        logger.log(UsageCode.NAV_HOME).join()

        assertEquals(emptyList(), dao.events())
    }

    @Test
    fun `startup compaction folds old events into daily counts and keeps recent ones`() = runTest {
        val dao = dao()
        val old = now - 100 * day
        val code = UsageCode.NAV_HOME.id
        dao.insert(UsageEventEntity(ts = old, code = code))
        dao.insert(UsageEventEntity(ts = old + 5, code = code))
        dao.insert(UsageEventEntity(ts = old + 9, code = UsageCode.NAV_SETTINGS.id))
        dao.insert(UsageEventEntity(ts = now - day, code = code))
        // A pre-existing rollup for the same day/code is added to, not replaced.
        dao.compact(now - 50 * day)
        dao.insert(UsageEventEntity(ts = old + 1, code = code))

        logger(dao).startupCompaction.join()

        assertEquals(listOf(now - day), dao.events().map { it.ts })
        assertEquals(
            listOf(
                UsageDailyEntity(old / day, code, 3),
                UsageDailyEntity(old / day, UsageCode.NAV_SETTINGS.id, 1),
            ),
            dao.daily().sortedBy { it.code },
        )
    }

    @Test
    fun `clear removes events and rollups`() = runTest {
        val dao = dao()
        dao.insert(UsageEventEntity(ts = 1, code = 1))
        dao.insert(UsageEventEntity(ts = 2, code = 1))
        dao.compact(2)

        dao.clear()

        assertEquals(emptyList(), dao.events())
        assertEquals(emptyList(), dao.daily())
    }

    @Test
    fun `export writes header, iso timestamps, code names and blank args`() = runTest {
        val dao = dao()
        dao.insert(UsageEventEntity(ts = 0, code = UsageCode.NAV_HOME.id))
        dao.insert(UsageEventEntity(ts = 86_400, code = UsageCode.ACT_DELETE_ENTRY.id, arg = 2))
        dao.insert(UsageEventEntity(ts = 90_000, code = 12345))
        val export = ExportUsageLogUseCaseImpl(dao)

        assertEquals(
            listOf(
                "ts_iso,code_name,arg",
                "1970-01-01T00:00:00Z,NAV_HOME,",
                "1970-01-02T00:00:00Z,ACT_DELETE_ENTRY,2",
                "1970-01-02T01:00:00Z,UNKNOWN_12345,",
            ),
            export.exportEvents().toList(),
        )

        dao.compact(Long.MAX_VALUE)
        assertEquals(
            listOf("day,code_name,count", "1970-01-01,NAV_HOME,1", "1970-01-02,ACT_DELETE_ENTRY,1", "1970-01-02,UNKNOWN_12345,1"),
            export.exportDaily().toList(),
        )
    }
}
