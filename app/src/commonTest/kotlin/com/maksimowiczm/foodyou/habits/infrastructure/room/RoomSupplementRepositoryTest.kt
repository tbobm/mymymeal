package com.maksimowiczm.foodyou.habits.infrastructure.room

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.test.core.app.ApplicationProvider
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

// A concrete, test-only @Database is required -- Room can only build a class carrying this
// annotation, not the bare HabitsDatabase interface (same reason MeasurementSuggestionTestDatabase
// exists in Task 5 rather than testing directly against FoodYouDatabase). CoffeeIntakeEntity is
// unused here but must be present: HabitsDatabase also exposes coffeeIntakeDao, and Room validates
// every DAO's tables exist against whichever concrete @Database implements the interface.
@Database(
    entities =
        [SupplementEntity::class, SupplementIntakeEntity::class, CoffeeIntakeEntity::class],
    version = 1,
    exportSchema = false,
)
internal abstract class SupplementTestDatabase : RoomDatabase(), HabitsDatabase

@RunWith(RobolectricTestRunner::class)
class RoomSupplementRepositoryTest {
    private fun buildDatabase(): SupplementTestDatabase =
        Room.inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                SupplementTestDatabase::class.java,
            )
            .build()

    @Test
    fun `add, list, and delete supplements`() = runTest {
        val db = buildDatabase()
        val repository = RoomSupplementRepository(db.supplementDao)

        repository.addSupplement("Vitamin D", tracksDose = false)
        repository.addSupplement("Creatine", tracksDose = true)

        val supplements = repository.observeSupplements().first()
        assertEquals(listOf("Vitamin D", "Creatine"), supplements.map { it.name })
        assertEquals(listOf(false, true), supplements.map { it.tracksDose })

        repository.deleteSupplement(supplements.first().id)
        assertEquals(listOf("Creatine"), repository.observeSupplements().first().map { it.name })
    }

    @Test
    fun `toggling intake is reflected for the given date only`() = runTest {
        val db = buildDatabase()
        val repository = RoomSupplementRepository(db.supplementDao)
        repository.addSupplement("Vitamin D", tracksDose = false)
        val id = repository.observeSupplements().first().single().id
        val today = LocalDate(2026, 8, 29)
        val yesterday = LocalDate(2026, 8, 28)

        assertTrue(repository.observeIntakeForDate(today).first().isEmpty())

        repository.setIntake(id, today, taken = true)
        assertEquals(setOf(id), repository.observeIntakeForDate(today).first().keys)
        assertTrue(repository.observeIntakeForDate(yesterday).first().isEmpty())

        repository.setIntake(id, today, taken = false)
        assertTrue(repository.observeIntakeForDate(today).first().isEmpty())
    }

    @Test
    fun `concurrent double-tap setIntake does not throw and does not duplicate the row`() = runTest {
        val db = buildDatabase()
        val repository = RoomSupplementRepository(db.supplementDao)
        repository.addSupplement("Vitamin D", tracksDose = false)
        val id = repository.observeSupplements().first().single().id
        val today = LocalDate(2026, 8, 29)

        repository.setIntake(id, today, taken = true)
        repository.setIntake(id, today, taken = true)

        assertEquals(setOf(id), repository.observeIntakeForDate(today).first().keys)
    }

    @Test
    fun `dose-tracking supplement records and updates its daily amount`() = runTest {
        val db = buildDatabase()
        val repository = RoomSupplementRepository(db.supplementDao)
        repository.addSupplement("Creatine", tracksDose = true)
        val id = repository.observeSupplements().first().single().id
        val today = LocalDate(2026, 8, 29)

        repository.setIntake(id, today, taken = true, doseGrams = 5.0)
        assertEquals(5.0, repository.observeIntakeForDate(today).first()[id]?.doseGrams)

        // Re-logging the same day replaces rather than duplicates the row.
        repository.setIntake(id, today, taken = true, doseGrams = 3.0)
        assertEquals(3.0, repository.observeIntakeForDate(today).first()[id]?.doseGrams)

        repository.setIntake(id, today, taken = false)
        assertNull(repository.observeIntakeForDate(today).first()[id])
    }
}
