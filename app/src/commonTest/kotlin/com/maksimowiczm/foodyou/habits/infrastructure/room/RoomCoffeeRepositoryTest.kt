package com.maksimowiczm.foodyou.habits.infrastructure.room

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.test.core.app.ApplicationProvider
import com.maksimowiczm.foodyou.common.domain.date.DateProvider
import com.maksimowiczm.foodyou.habits.domain.entity.CoffeeType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

// SupplementEntity/SupplementIntakeEntity are unused here but must be present: HabitsDatabase
// also exposes supplementDao, and Room validates every DAO's tables exist against whichever
// concrete @Database implements the interface.
@Database(
    entities =
        [CoffeeIntakeEntity::class, SupplementEntity::class, SupplementIntakeEntity::class],
    version = 1,
    exportSchema = false,
)
internal abstract class CoffeeTestDatabase : RoomDatabase(), HabitsDatabase

private class FakeDateProvider(private val instant: Instant) : DateProvider {
    override fun nowInstant(): Instant = instant

    override fun observeInstant(interval: Duration): Flow<Instant> = flowOf(instant)

    override fun observeDate(timeZone: kotlinx.datetime.TimeZone): Flow<LocalDate> =
        flowOf(LocalDate(2026, 8, 29))
}

@RunWith(RobolectricTestRunner::class)
class RoomCoffeeRepositoryTest {
    private fun buildDatabase(): CoffeeTestDatabase =
        Room.inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                CoffeeTestDatabase::class.java,
            )
            .build()

    @Test
    fun `logging a coffee snapshots the type's default caffeine mg`() = runTest {
        val db = buildDatabase()
        val repository =
            RoomCoffeeRepository(db.coffeeIntakeDao, FakeDateProvider(Instant.fromEpochSeconds(0)))
        val today = LocalDate(2026, 8, 29)

        repository.logCoffee(CoffeeType.Espresso, today)

        val logs = repository.observeForDate(today).first()
        assertEquals(1, logs.size)
        assertEquals(CoffeeType.Espresso, logs.single().type)
        assertEquals(CoffeeType.Espresso.defaultCaffeineMg, logs.single().caffeineMg)
        assertEquals(CoffeeType.Espresso.defaultCaffeineMg, repository.observeCaffeineMgForDate(today).first())
    }

    @Test
    fun `caffeine is summed across coffees logged the same day and scoped to that day`() = runTest {
        val db = buildDatabase()
        val repository =
            RoomCoffeeRepository(db.coffeeIntakeDao, FakeDateProvider(Instant.fromEpochSeconds(0)))
        val today = LocalDate(2026, 8, 29)
        val yesterday = LocalDate(2026, 8, 28)

        repository.logCoffee(CoffeeType.Cup, today)
        repository.logCoffee(CoffeeType.Latte, today)
        repository.logCoffee(CoffeeType.Espresso, yesterday)

        assertEquals(2, repository.observeForDate(today).first().size)
        assertEquals(
            CoffeeType.Cup.defaultCaffeineMg + CoffeeType.Latte.defaultCaffeineMg,
            repository.observeCaffeineMgForDate(today).first(),
        )
        assertEquals(CoffeeType.Espresso.defaultCaffeineMg, repository.observeCaffeineMgForDate(yesterday).first())
    }

    @Test
    fun `deleting a coffee log removes it from the day's total`() = runTest {
        val db = buildDatabase()
        val repository =
            RoomCoffeeRepository(db.coffeeIntakeDao, FakeDateProvider(Instant.fromEpochSeconds(0)))
        val today = LocalDate(2026, 8, 29)

        repository.logCoffee(CoffeeType.Cup, today)
        val id = repository.observeForDate(today).first().single().id

        repository.deleteCoffeeLog(id)

        assertEquals(0, repository.observeCaffeineMgForDate(today).first())
    }
}
