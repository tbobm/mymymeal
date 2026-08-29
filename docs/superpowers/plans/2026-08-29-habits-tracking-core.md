# Habits Tracking (Core: Coffee + Supplements) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a new "Habits" home card to mymymeal with a coffee quick-log/summary (built on existing diary + caffeine data) and a supplement adherence checklist (new domain).

**Architecture:** New `habits` package (mirrors the existing `goals` package: `domain/entity`, `domain/repository`, `infrastructure/room`, own Koin module) holds `Supplement`/`SupplementIntake` and a `HabitsPreferences` DataStore value. Coffee needs no new table: cup count reuses the existing `MeasurementSuggestion` table (already recording every diary log per catalog food id), and caffeine mg reuses the existing per-day nutrition aggregate `GoalsCard` already computes. One new Room migration (34→35) adds the two supplement tables. A new `HomeCard.Habits` entry wires a new `HabitsCard` composable into the existing modular home screen.

**Tech Stack:** Kotlin Multiplatform (Android-only build), Jetpack Compose Multiplatform, Room, Koin, kotlinx-datetime, AndroidX DataStore Preferences.

**Split note:** This is Plan A of two. Plan B (twice-daily reminders via WorkManager + notification permission) builds on top of this and is a separate plan/PR — the spec covers three logically independent pieces (`docs/superpowers/specs/2026-08-25-habits-tracking-design.md`), and this plan on its own already produces a fully working, shippable feature (log coffee, check off supplements) without the reminder subsystem.

## Global Constraints

- App ID `com.maksimowiczm.foodyou` unchanged; module count stays minimized — this feature lives entirely under `:app`, organized by package (per `docs/development/decision-log/0002-minimize-gradle-modules.md`).
- Room `FoodYouDatabase.VERSION` bumps 34 → 35. `exportSchema = true` — the new `35.json` must be committed (Room's KSP processor generates it on build, do not hand-write it).
- Every migration needs an `Abstract*Test` (common) + platform `actual` test (`androidInstrumentedTest`), per the existing pattern in `app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/infrastructure/room/migration/` and `docs/schema.md`.
- No new Gradle dependency in this plan (WorkManager is Plan B's concern).
- Build JDK: `JAVA_HOME=/opt/homebrew/opt/openjdk@21`. Run `./gradlew --offline assembleDebug test` once online caches are warm. Instrumented migration tests need a connected device/emulator (`./gradlew connectedAndroidTest`), not covered by `--offline test`.
- `docs/schema.md` must stay current (project's own "definition of done" gate) — the last task in this plan updates it.

---

### Task 1: Supplement domain entities + Room tables + migration + fixture tests

**Files:**
- Create: `app/src/commonMain/kotlin/com/maksimowiczm/foodyou/habits/domain/entity/Supplement.kt`
- Create: `app/src/commonMain/kotlin/com/maksimowiczm/foodyou/habits/infrastructure/room/SupplementEntity.kt`
- Create: `app/src/commonMain/kotlin/com/maksimowiczm/foodyou/habits/infrastructure/room/SupplementIntakeEntity.kt`
- Create: `app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/infrastructure/room/migration/AddHabitsTables.kt`
- Modify: `app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/infrastructure/room/FoodYouDatabase.kt`
- Create: `app/src/commonTest/kotlin/com/maksimowiczm/foodyou/app/infrastructure/room/migration/AbstractAddHabitsTablesTest.kt`
- Create: `app/src/androidInstrumentedTest/kotlin/com/maksimowiczm/foodyou/app/infrastructure/room/migration/AddHabitsTablesTest.kt`

**Interfaces:**
- Produces: `Supplement(id: Long, name: String, sortOrder: Int)` (domain), `SupplementEntity(id, name, sortOrder)` → table `Supplement`, `SupplementIntakeEntity(supplementId, date)` → table `SupplementIntake`, migration object `addHabitsTables: Migration`.

- [ ] **Step 1: Write the domain entity**

```kotlin
// app/src/commonMain/kotlin/com/maksimowiczm/foodyou/habits/domain/entity/Supplement.kt
package com.maksimowiczm.foodyou.habits.domain.entity

/** A supplement the user takes regularly. Adherence only -- no dose or timestamp. */
data class Supplement(val id: Long, val name: String, val sortOrder: Int)
```

- [ ] **Step 2: Write the Room entities**

```kotlin
// app/src/commonMain/kotlin/com/maksimowiczm/foodyou/habits/infrastructure/room/SupplementEntity.kt
package com.maksimowiczm.foodyou.habits.infrastructure.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Supplement")
data class SupplementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val sortOrder: Int,
)
```

```kotlin
// app/src/commonMain/kotlin/com/maksimowiczm/foodyou/habits/infrastructure/room/SupplementIntakeEntity.kt
package com.maksimowiczm.foodyou.habits.infrastructure.room

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/** Presence of a row means the supplement was taken on that [date]. No dose, no timestamp. */
@Entity(
    tableName = "SupplementIntake",
    primaryKeys = ["supplementId", "date"],
    foreignKeys =
        [
            ForeignKey(
                entity = SupplementEntity::class,
                parentColumns = ["id"],
                childColumns = ["supplementId"],
                onDelete = ForeignKey.CASCADE,
            )
        ],
    indices = [Index("supplementId")],
)
data class SupplementIntakeEntity(
    val supplementId: Long,
    /** Epoch day (local date), matching the existing `epochDay` convention on `Measurement`. */
    val date: Long,
)
```

- [ ] **Step 3: Write the migration**

```kotlin
// app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/infrastructure/room/migration/AddHabitsTables.kt
package com.maksimowiczm.foodyou.app.infrastructure.room.migration

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

/**
 * Habits tracking (coffee + supplements). Adds `Supplement` (the list of supplements the user
 * takes) and `SupplementIntake` (adherence -- presence of a row means taken that day). Purely
 * additive, no existing table or column is altered. Coffee tracking needs no new table -- it
 * reuses `MeasurementSuggestion` (cup count) and the existing per-day caffeine aggregate.
 */
internal val addHabitsTables =
    object : Migration(34, 35) {
        override fun migrate(connection: SQLiteConnection) {
            connection.execSQL(
                "CREATE TABLE IF NOT EXISTS `Supplement` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`name` TEXT NOT NULL, " +
                    "`sortOrder` INTEGER NOT NULL)"
            )

            connection.execSQL(
                "CREATE TABLE IF NOT EXISTS `SupplementIntake` (" +
                    "`supplementId` INTEGER NOT NULL, " +
                    "`date` INTEGER NOT NULL, " +
                    "PRIMARY KEY(`supplementId`, `date`), " +
                    "FOREIGN KEY(`supplementId`) REFERENCES `Supplement`(`id`) " +
                    "ON UPDATE NO ACTION ON DELETE CASCADE)"
            )
            connection.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_SupplementIntake_supplementId` " +
                    "ON `SupplementIntake` (`supplementId`)"
            )
        }
    }
```

- [ ] **Step 4: Wire the migration and entities into `FoodYouDatabase`**

In `app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/infrastructure/room/FoodYouDatabase.kt`:

Add imports:
```kotlin
import com.maksimowiczm.foodyou.app.infrastructure.room.migration.addHabitsTables
import com.maksimowiczm.foodyou.habits.infrastructure.room.SupplementEntity
import com.maksimowiczm.foodyou.habits.infrastructure.room.SupplementIntakeEntity
```

Add `SupplementEntity::class, SupplementIntakeEntity::class,` to the `entities = [...]` list (after `ManualDiaryEntryTagCrossRefEntity::class,`).

Change `const val VERSION = 34` to `const val VERSION = 35`.

Add `addHabitsTables` to the end of the `migrations` list (after `addTagTables,`).

- [ ] **Step 5: Write the fixture test (common)**

```kotlin
// app/src/commonTest/kotlin/com/maksimowiczm/foodyou/app/infrastructure/room/migration/AbstractAddHabitsTablesTest.kt
package com.maksimowiczm.foodyou.app.infrastructure.room.migration

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.execSQL
import kotlin.test.assertEquals
import kotlin.test.assertTrue

abstract class AbstractAddHabitsTablesTest {
    abstract fun getTestHelper(): MigrationTestHelper

    open fun migrate() {
        val helper = getTestHelper()

        helper.createDatabase(34).apply {
            execSQL(
                "INSERT INTO Meal (id, name, fromHour, fromMinute, toHour, toMinute, rank) " +
                    "VALUES (1, 'Breakfast', 6, 0, 10, 0, 0)"
            )
            close()
        }

        val connection = helper.runMigrationsAndValidate(35, listOf(addHabitsTables))

        // Pre-existing rows survive the migration untouched.
        connection.prepare("SELECT COUNT(*) FROM Meal").use { statement ->
            statement.step()
            assertEquals(1, statement.getLong(0).toInt())
        }

        // The new tables exist, are empty, and accept inserts + cascade deletes correctly.
        connection.prepare("SELECT COUNT(*) FROM Supplement").use { statement ->
            statement.step()
            assertEquals(0, statement.getLong(0).toInt())
        }

        connection.execSQL("INSERT INTO Supplement (id, name, sortOrder) VALUES (1, 'Vitamin D', 0)")
        connection.execSQL("INSERT INTO SupplementIntake (supplementId, date) VALUES (1, 20000)")

        connection.prepare("SELECT COUNT(*) FROM SupplementIntake WHERE supplementId = 1").use {
            statement ->
            statement.step()
            assertEquals(1, statement.getLong(0).toInt())
        }

        // Deleting the supplement cascades into its intake rows (onDelete = CASCADE).
        connection.execSQL("DELETE FROM Supplement WHERE id = 1")

        connection.prepare("SELECT COUNT(*) FROM SupplementIntake").use { statement ->
            statement.step()
            assertTrue(statement.getLong(0) == 0L)
        }
    }
}
```

- [ ] **Step 6: Write the Android instrumented test**

```kotlin
// app/src/androidInstrumentedTest/kotlin/com/maksimowiczm/foodyou/app/infrastructure/room/migration/AddHabitsTablesTest.kt
package com.maksimowiczm.foodyou.app.infrastructure.room.migration

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.SQLiteDriver
import androidx.sqlite.driver.AndroidSQLiteDriver
import androidx.test.platform.app.InstrumentationRegistry
import com.maksimowiczm.foodyou.app.infrastructure.room.FoodYouDatabase
import org.junit.Rule
import org.junit.Test

class AddHabitsTablesTest : AbstractAddHabitsTablesTest() {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val file = instrumentation.targetContext.getDatabasePath("AddHabitsTablesTest.db")
    private val driver: SQLiteDriver = AndroidSQLiteDriver()

    @get:Rule
    val helper: MigrationTestHelper =
        MigrationTestHelper(
            instrumentation = instrumentation,
            file = file,
            driver = driver,
            databaseClass = FoodYouDatabase::class,
        )

    override fun getTestHelper() = helper

    @Test
    override fun migrate() {
        super.migrate()
    }
}
```

- [ ] **Step 7: Run the migration test**

Run: `JAVA_HOME=/opt/homebrew/opt/openjdk@21 ./gradlew :app:connectedAndroidTest --tests "*AddHabitsTablesTest*"` (requires a connected device/emulator — `adb devices` must show one; see `android-dev` skill).
Expected: PASS, and `app/schemas/com.maksimowiczm.foodyou.app.infrastructure.room.FoodYouDatabase/35.json` is generated by the preceding `assembleDebug`/build (Room's schema export runs at compile time, not test time — if `35.json` is missing, run `./gradlew :app:compileDebugKotlin` first).

- [ ] **Step 8: Commit**

```bash
git add app/src/commonMain/kotlin/com/maksimowiczm/foodyou/habits/domain/entity/Supplement.kt \
  app/src/commonMain/kotlin/com/maksimowiczm/foodyou/habits/infrastructure/room/SupplementEntity.kt \
  app/src/commonMain/kotlin/com/maksimowiczm/foodyou/habits/infrastructure/room/SupplementIntakeEntity.kt \
  app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/infrastructure/room/migration/AddHabitsTables.kt \
  app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/infrastructure/room/FoodYouDatabase.kt \
  app/src/commonTest/kotlin/com/maksimowiczm/foodyou/app/infrastructure/room/migration/AbstractAddHabitsTablesTest.kt \
  app/src/androidInstrumentedTest/kotlin/com/maksimowiczm/foodyou/app/infrastructure/room/migration/AddHabitsTablesTest.kt \
  app/schemas/com.maksimowiczm.foodyou.app.infrastructure.room.FoodYouDatabase/35.json
git commit -m "feat: add Supplement/SupplementIntake tables (schema v35)"
```

---

### Task 2: SupplementDao + HabitsDatabase interface

**Files:**
- Create: `app/src/commonMain/kotlin/com/maksimowiczm/foodyou/habits/infrastructure/room/SupplementDao.kt`
- Create: `app/src/commonMain/kotlin/com/maksimowiczm/foodyou/habits/infrastructure/room/HabitsDatabase.kt`
- Modify: `app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/infrastructure/room/FoodYouDatabase.kt`

**Interfaces:**
- Consumes: `SupplementEntity`, `SupplementIntakeEntity` (Task 1).
- Produces: `SupplementDao` (abstract Room DAO), `HabitsDatabase` (interface exposing `val supplementDao: SupplementDao`), `FoodYouDatabase` implementing `HabitsDatabase`.

- [ ] **Step 1: Write the DAO**

```kotlin
// app/src/commonMain/kotlin/com/maksimowiczm/foodyou/habits/infrastructure/room/SupplementDao.kt
package com.maksimowiczm.foodyou.habits.infrastructure.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
abstract class SupplementDao {
    @Query("SELECT * FROM Supplement ORDER BY sortOrder ASC")
    abstract fun observeSupplements(): Flow<List<SupplementEntity>>

    @Query("SELECT COALESCE(MAX(sortOrder), -1) FROM Supplement")
    protected abstract suspend fun maxSortOrder(): Int

    @Insert protected abstract suspend fun insertSupplement(supplement: SupplementEntity): Long

    /** Appends a new supplement after the current highest [SupplementEntity.sortOrder]. */
    suspend fun addSupplement(name: String): Long =
        insertSupplement(SupplementEntity(name = name, sortOrder = maxSortOrder() + 1))

    @Query("DELETE FROM Supplement WHERE id = :id")
    abstract suspend fun deleteSupplement(id: Long)

    @Query("SELECT supplementId FROM SupplementIntake WHERE date = :date")
    abstract fun observeIntakeSupplementIdsForDate(date: Long): Flow<List<Long>>

    @Insert protected abstract suspend fun insertIntake(intake: SupplementIntakeEntity)

    @Query("DELETE FROM SupplementIntake WHERE supplementId = :supplementId AND date = :date")
    protected abstract suspend fun deleteIntake(supplementId: Long, date: Long)

    /** Toggles [supplementId]'s intake row for [date] -- inserts if absent, deletes if present. */
    suspend fun setTaken(supplementId: Long, date: Long, taken: Boolean) {
        if (taken) {
            insertIntake(SupplementIntakeEntity(supplementId, date))
        } else {
            deleteIntake(supplementId, date)
        }
    }
}
```

- [ ] **Step 2: Write the database interface**

```kotlin
// app/src/commonMain/kotlin/com/maksimowiczm/foodyou/habits/infrastructure/room/HabitsDatabase.kt
package com.maksimowiczm.foodyou.habits.infrastructure.room

interface HabitsDatabase {
    val supplementDao: SupplementDao
}
```

- [ ] **Step 3: Make `FoodYouDatabase` implement it**

In `FoodYouDatabase.kt`, add import `com.maksimowiczm.foodyou.habits.infrastructure.room.HabitsDatabase` and add `HabitsDatabase` to the `abstract class FoodYouDatabase : RoomDatabase(), TransactionProvider, FoodDatabase, FoodSearchDatabase, FoodDiaryDatabase, SponsorshipDatabase, TagDatabase,` → append `HabitsDatabase`. Room generates the `supplementDao` property implementation automatically (same mechanism as `tagDao` on `TagDatabase`) — no manual override needed.

- [ ] **Step 4: Compile to verify Room codegen**

Run: `JAVA_HOME=/opt/homebrew/opt/openjdk@21 ./gradlew :app:compileDebugKotlin --offline -q`
Expected: no output (success). If Room complains about a missing `supplementDao` implementation, double check `HabitsDatabase` was added to the `FoodYouDatabase` supertype list in Step 3.

- [ ] **Step 5: Commit**

```bash
git add app/src/commonMain/kotlin/com/maksimowiczm/foodyou/habits/infrastructure/room/SupplementDao.kt \
  app/src/commonMain/kotlin/com/maksimowiczm/foodyou/habits/infrastructure/room/HabitsDatabase.kt \
  app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/infrastructure/room/FoodYouDatabase.kt
git commit -m "feat: add SupplementDao and wire HabitsDatabase into FoodYouDatabase"
```

---

### Task 3: SupplementRepository + DI module

**Files:**
- Create: `app/src/commonMain/kotlin/com/maksimowiczm/foodyou/habits/domain/repository/SupplementRepository.kt`
- Create: `app/src/commonMain/kotlin/com/maksimowiczm/foodyou/habits/infrastructure/room/RoomSupplementRepository.kt`
- Create: `app/src/commonMain/kotlin/com/maksimowiczm/foodyou/habits/HabitsModule.kt`
- Modify: `app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/di/InitKoin.kt`
- Test: `app/src/commonTest/kotlin/com/maksimowiczm/foodyou/habits/infrastructure/room/RoomSupplementRepositoryTest.kt`

**Interfaces:**
- Consumes: `SupplementDao`, `HabitsDatabase` (Task 2), `Supplement` (Task 1).
- Produces: `SupplementRepository` interface with `observeSupplements(): Flow<List<Supplement>>`, `observeTakenIdsForDate(date: LocalDate): Flow<Set<Long>>`, `suspend fun addSupplement(name: String)`, `suspend fun deleteSupplement(id: Long)`, `suspend fun setTaken(id: Long, date: LocalDate, taken: Boolean)`.

- [ ] **Step 1: Write the repository interface**

```kotlin
// app/src/commonMain/kotlin/com/maksimowiczm/foodyou/habits/domain/repository/SupplementRepository.kt
package com.maksimowiczm.foodyou.habits.domain.repository

import com.maksimowiczm.foodyou.habits.domain.entity.Supplement
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface SupplementRepository {
    fun observeSupplements(): Flow<List<Supplement>>

    /** Ids of supplements already taken on [date]. */
    fun observeTakenIdsForDate(date: LocalDate): Flow<Set<Long>>

    suspend fun addSupplement(name: String)

    suspend fun deleteSupplement(id: Long)

    suspend fun setTaken(id: Long, date: LocalDate, taken: Boolean)
}
```

- [ ] **Step 2: Write the failing test**

```kotlin
// app/src/commonTest/kotlin/com/maksimowiczm/foodyou/habits/infrastructure/room/RoomSupplementRepositoryTest.kt
package com.maksimowiczm.foodyou.habits.infrastructure.room

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate

// A concrete, test-only @Database is required -- Room can only build a class carrying this
// annotation, not the bare HabitsDatabase interface (same reason MeasurementSuggestionTestDatabase
// exists in Task 5 rather than testing directly against FoodYouDatabase).
@Database(
    entities = [SupplementEntity::class, SupplementIntakeEntity::class],
    version = 1,
    exportSchema = false,
)
internal abstract class SupplementTestDatabase : RoomDatabase(), HabitsDatabase

class RoomSupplementRepositoryTest {
    private fun buildDatabase(): SupplementTestDatabase =
        Room.inMemoryDatabaseBuilder<SupplementTestDatabase>().setDriver(BundledSQLiteDriver()).build()

    @Test
    fun `add, list, and delete supplements`() = runTest {
        val db = buildDatabase()
        val repository = RoomSupplementRepository(db.supplementDao)

        repository.addSupplement("Vitamin D")
        repository.addSupplement("Magnesium")

        val supplements = repository.observeSupplements().first()
        assertEquals(listOf("Vitamin D", "Magnesium"), supplements.map { it.name })

        repository.deleteSupplement(supplements.first().id)
        assertEquals(listOf("Magnesium"), repository.observeSupplements().first().map { it.name })
    }

    @Test
    fun `toggling intake is reflected for the given date only`() = runTest {
        val db = buildDatabase()
        val repository = RoomSupplementRepository(db.supplementDao)
        repository.addSupplement("Vitamin D")
        val id = repository.observeSupplements().first().single().id
        val today = LocalDate(2026, 8, 29)
        val yesterday = LocalDate(2026, 8, 28)

        assertTrue(repository.observeTakenIdsForDate(today).first().isEmpty())

        repository.setTaken(id, today, taken = true)
        assertEquals(setOf(id), repository.observeTakenIdsForDate(today).first())
        assertTrue(repository.observeTakenIdsForDate(yesterday).first().isEmpty())

        repository.setTaken(id, today, taken = false)
        assertTrue(repository.observeTakenIdsForDate(today).first().isEmpty())
    }
}
```

- [ ] **Step 3: Run test to verify it fails**

Run: `JAVA_HOME=/opt/homebrew/opt/openjdk@21 ./gradlew :app:testDebugUnitTest --tests "*RoomSupplementRepositoryTest*" --offline`
Expected: FAIL (compile error — `RoomSupplementRepository` doesn't exist yet).

- [ ] **Step 4: Write the repository implementation**

```kotlin
// app/src/commonMain/kotlin/com/maksimowiczm/foodyou/habits/infrastructure/room/RoomSupplementRepository.kt
package com.maksimowiczm.foodyou.habits.infrastructure.room

import com.maksimowiczm.foodyou.habits.domain.entity.Supplement
import com.maksimowiczm.foodyou.habits.domain.repository.SupplementRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toEpochDays

internal class RoomSupplementRepository(private val supplementDao: SupplementDao) :
    SupplementRepository {
    override fun observeSupplements(): Flow<List<Supplement>> =
        supplementDao.observeSupplements().map { list -> list.map(SupplementEntity::toModel) }

    override fun observeTakenIdsForDate(date: LocalDate): Flow<Set<Long>> =
        supplementDao.observeIntakeSupplementIdsForDate(date.toEpochDays()).map { it.toSet() }

    override suspend fun addSupplement(name: String) {
        supplementDao.addSupplement(name)
    }

    override suspend fun deleteSupplement(id: Long) = supplementDao.deleteSupplement(id)

    override suspend fun setTaken(id: Long, date: LocalDate, taken: Boolean) =
        supplementDao.setTaken(id, date.toEpochDays(), taken)
}

private fun SupplementEntity.toModel() = Supplement(id = id, name = name, sortOrder = sortOrder)
```

- [ ] **Step 5: Run test to verify it passes**

Run: `JAVA_HOME=/opt/homebrew/opt/openjdk@21 ./gradlew :app:testDebugUnitTest --tests "*RoomSupplementRepositoryTest*" --offline`
Expected: PASS (2 tests).

- [ ] **Step 6: Write the DI module**

```kotlin
// app/src/commonMain/kotlin/com/maksimowiczm/foodyou/habits/HabitsModule.kt
package com.maksimowiczm.foodyou.habits

import com.maksimowiczm.foodyou.habits.domain.repository.SupplementRepository
import com.maksimowiczm.foodyou.habits.infrastructure.room.HabitsDatabase
import com.maksimowiczm.foodyou.habits.infrastructure.room.RoomSupplementRepository
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.scope.Scope
import org.koin.dsl.bind
import org.koin.dsl.module

val habitsModule = module {
    factory { database.supplementDao }
    factoryOf(::RoomSupplementRepository).bind<SupplementRepository>()
}

private val Scope.database: HabitsDatabase
    get() = get()
```

- [ ] **Step 7: Register the module in `InitKoin`**

In `app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/di/InitKoin.kt`, add import `com.maksimowiczm.foodyou.habits.habitsModule` and add `habitsModule,` to the `modules(...)` list (alongside `goalsModule,`).

- [ ] **Step 8: Commit**

```bash
git add app/src/commonMain/kotlin/com/maksimowiczm/foodyou/habits/domain/repository/SupplementRepository.kt \
  app/src/commonMain/kotlin/com/maksimowiczm/foodyou/habits/infrastructure/room/RoomSupplementRepository.kt \
  app/src/commonMain/kotlin/com/maksimowiczm/foodyou/habits/HabitsModule.kt \
  app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/di/InitKoin.kt \
  app/src/commonTest/kotlin/com/maksimowiczm/foodyou/habits/infrastructure/room/RoomSupplementRepositoryTest.kt
git commit -m "feat: add SupplementRepository and wire habits Koin module"
```

---

### Task 4: HabitsPreferences (default coffee configuration)

**Files:**
- Create: `app/src/commonMain/kotlin/com/maksimowiczm/foodyou/habits/domain/entity/HabitsPreferences.kt`
- Create: `app/src/commonMain/kotlin/com/maksimowiczm/foodyou/habits/infrastructure/DataStoreHabitsPreferencesRepository.kt`
- Modify: `app/src/commonMain/kotlin/com/maksimowiczm/foodyou/habits/HabitsModule.kt`

**Interfaces:**
- Consumes: `AbstractDataStoreUserPreferencesRepository<P>` (existing, `common.infrastructure.datastore`), `userPreferencesRepositoryOf` (existing Koin helper, `common.infrastructure.koin`).
- Produces: `HabitsPreferences(defaultCoffeeFoodId: Long?, defaultCoffeeIsRecipe: Boolean, defaultCoffeeMealId: Long?, defaultCoffeeMeasurementType: MeasurementType?, defaultCoffeeMeasurementValue: Double?)`. Plan B (reminders) extends this same data class with reminder fields — no migration needed since DataStore is schemaless key-value.

- [ ] **Step 1: Write the preferences entity**

`FoodId` (sealed `Product`/`Recipe`) isn't itself easily stored as one DataStore primitive, so it's split into a raw id + a `isRecipe` flag, reassembled at read time.

```kotlin
// app/src/commonMain/kotlin/com/maksimowiczm/foodyou/habits/domain/entity/HabitsPreferences.kt
package com.maksimowiczm.foodyou.habits.domain.entity

import com.maksimowiczm.foodyou.common.domain.measurement.MeasurementType
import com.maksimowiczm.foodyou.common.domain.userpreferences.UserPreferences

/**
 * The one-time-configured "default coffee" used by the Habits card's one-tap coffee log.
 * `defaultCoffeeFoodId`/`defaultCoffeeIsRecipe` together reconstruct a `FoodId`;
 * `defaultCoffeeMeasurementType`/`Value` together reconstruct a `Measurement` -- DataStore has no
 * native support for either sealed type, so both are split into primitives and reassembled at
 * read time in the call site (see `HabitsCardViewModel`).
 */
data class HabitsPreferences(
    val defaultCoffeeFoodId: Long? = null,
    val defaultCoffeeIsRecipe: Boolean = false,
    val defaultCoffeeMealId: Long? = null,
    val defaultCoffeeMeasurementType: MeasurementType? = null,
    val defaultCoffeeMeasurementValue: Double? = null,
) : UserPreferences
```

- [ ] **Step 2: Write the DataStore-backed repository**

```kotlin
// app/src/commonMain/kotlin/com/maksimowiczm/foodyou/habits/infrastructure/DataStoreHabitsPreferencesRepository.kt
package com.maksimowiczm.foodyou.habits.infrastructure

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.maksimowiczm.foodyou.common.domain.measurement.MeasurementType
import com.maksimowiczm.foodyou.common.infrastructure.datastore.AbstractDataStoreUserPreferencesRepository
import com.maksimowiczm.foodyou.habits.domain.entity.HabitsPreferences

internal class DataStoreHabitsPreferencesRepository(dataStore: DataStore<Preferences>) :
    AbstractDataStoreUserPreferencesRepository<HabitsPreferences>(dataStore) {
    override fun Preferences.toUserPreferences(): HabitsPreferences =
        HabitsPreferences(
            defaultCoffeeFoodId = this[HabitsPreferencesDataStoreKeys.defaultCoffeeFoodId],
            defaultCoffeeIsRecipe =
                this[HabitsPreferencesDataStoreKeys.defaultCoffeeIsRecipe] ?: false,
            defaultCoffeeMealId = this[HabitsPreferencesDataStoreKeys.defaultCoffeeMealId],
            defaultCoffeeMeasurementType =
                this[HabitsPreferencesDataStoreKeys.defaultCoffeeMeasurementType]?.let {
                    MeasurementType.valueOf(it)
                },
            defaultCoffeeMeasurementValue =
                this[HabitsPreferencesDataStoreKeys.defaultCoffeeMeasurementValue],
        )

    override fun MutablePreferences.applyUserPreferences(updated: HabitsPreferences) {
        setWithNull(HabitsPreferencesDataStoreKeys.defaultCoffeeFoodId, updated.defaultCoffeeFoodId)
        this[HabitsPreferencesDataStoreKeys.defaultCoffeeIsRecipe] = updated.defaultCoffeeIsRecipe
        setWithNull(HabitsPreferencesDataStoreKeys.defaultCoffeeMealId, updated.defaultCoffeeMealId)
        setWithNull(
            HabitsPreferencesDataStoreKeys.defaultCoffeeMeasurementType,
            updated.defaultCoffeeMeasurementType?.name,
        )
        setWithNull(
            HabitsPreferencesDataStoreKeys.defaultCoffeeMeasurementValue,
            updated.defaultCoffeeMeasurementValue,
        )
    }

    private fun <T> MutablePreferences.setWithNull(key: Preferences.Key<T>, value: T?) {
        if (value != null) this[key] = value else this.remove(key)
    }
}

private object HabitsPreferencesDataStoreKeys {
    val defaultCoffeeFoodId = longPreferencesKey("habits:default_coffee_food_id")
    val defaultCoffeeIsRecipe = booleanPreferencesKey("habits:default_coffee_is_recipe")
    val defaultCoffeeMealId = longPreferencesKey("habits:default_coffee_meal_id")
    val defaultCoffeeMeasurementType = stringPreferencesKey("habits:default_coffee_measurement_type")
    val defaultCoffeeMeasurementValue = doublePreferencesKey("habits:default_coffee_measurement_value")
}
```

`AbstractDataStoreUserPreferencesRepository`'s `setWithNull` used elsewhere (e.g. `DataStoreSettingsRepository.kt`) is a private top-level function in that file, not shared — this task defines its own private copy in the same style, matching the existing pattern of each preferences repository owning its own helper rather than sharing one.

- [ ] **Step 3: Register in the DI module**

In `app/src/commonMain/kotlin/com/maksimowiczm/foodyou/habits/HabitsModule.kt`, add import `com.maksimowiczm.foodyou.common.infrastructure.koin.userPreferencesRepositoryOf`, import `com.maksimowiczm.foodyou.habits.infrastructure.DataStoreHabitsPreferencesRepository`, and add `userPreferencesRepositoryOf(::DataStoreHabitsPreferencesRepository)` inside the `module { ... }` block.

- [ ] **Step 4: Compile to verify**

Run: `JAVA_HOME=/opt/homebrew/opt/openjdk@21 ./gradlew :app:compileDebugKotlin --offline -q`
Expected: no output (success).

- [ ] **Step 5: Commit**

```bash
git add app/src/commonMain/kotlin/com/maksimowiczm/foodyou/habits/domain/entity/HabitsPreferences.kt \
  app/src/commonMain/kotlin/com/maksimowiczm/foodyou/habits/infrastructure/DataStoreHabitsPreferencesRepository.kt \
  app/src/commonMain/kotlin/com/maksimowiczm/foodyou/habits/HabitsModule.kt
git commit -m "feat: add HabitsPreferences for the default-coffee configuration"
```

---

### Task 5: Coffee cup count — extend `FoodMeasurementSuggestionRepository`

**Context (read before starting):** `MeasurementSuggestion` already gets a row inserted every time any diary entry is created (`FoodDiaryEntryCreatedEventHandler`), keyed by the **catalog** `productId`/`recipeId` (confirmed via `FoodSearchDao`'s `LatestMeasurementSuggestion` view, which joins straight to `Product`/`Recipe`). This means "cups of the default coffee logged today" can be counted directly from this existing table by product/recipe id + a day's epoch-second range — no new table needed, matching the design spec's "no new persistent entity" decision for coffee.

**Files:**
- Modify: `app/src/commonMain/kotlin/com/maksimowiczm/foodyou/food/infrastructure/room/MeasurementSuggestionDao.kt`
- Modify: `app/src/commonMain/kotlin/com/maksimowiczm/foodyou/food/domain/repository/FoodMeasurementSuggestionRepository.kt`
- Modify: `app/src/commonMain/kotlin/com/maksimowiczm/foodyou/food/infrastructure/repository/RoomFoodMeasurementSuggestionRepository.kt`
- Test: `app/src/commonTest/kotlin/com/maksimowiczm/foodyou/food/infrastructure/repository/RoomFoodMeasurementSuggestionRepositoryCountTest.kt`

**Interfaces:**
- Produces: `FoodMeasurementSuggestionRepository.observeCountByFoodId(foodId: FoodId, sinceEpochSeconds: Long, untilEpochSeconds: Long): Flow<Int>`.

- [ ] **Step 1: Write the failing test**

`MeasurementSuggestionEntity` declares `@ForeignKey`s to `ProductEntity`/`RecipeEntity`. Room requires every FK's referenced entity to be declared in the *same* `@Database`, and SQLite enforces the FK at insert time (a row can't reference a nonexistent `Product.id`) — so this test builds the real `FoodYouDatabase` in-memory (it already declares every entity) rather than a stripped-down parallel database, and inserts the minimal parent `Product` rows via raw SQL first, mirroring the exact `execSQL` pattern the migration fixture tests already use.

```kotlin
// app/src/commonTest/kotlin/com/maksimowiczm/foodyou/food/infrastructure/repository/RoomFoodMeasurementSuggestionRepositoryCountTest.kt
package com.maksimowiczm.foodyou.food.infrastructure.repository

import androidx.room.Room
import androidx.room.useWriterConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import com.maksimowiczm.foodyou.app.infrastructure.room.FoodYouDatabase
import com.maksimowiczm.foodyou.common.domain.measurement.MeasurementType
import com.maksimowiczm.foodyou.food.domain.entity.FoodId
import com.maksimowiczm.foodyou.food.infrastructure.room.MeasurementSuggestionEntity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest

class RoomFoodMeasurementSuggestionRepositoryCountTest {
    private suspend fun buildDatabase(): FoodYouDatabase {
        val db =
            Room.inMemoryDatabaseBuilder<FoodYouDatabase>().setDriver(BundledSQLiteDriver()).build()
        db.useWriterConnection { connection ->
            connection.execSQL(
                "INSERT INTO Product (id, name, sourceType, isLiquid) VALUES (1, 'Coffee', 0, 0)"
            )
            connection.execSQL(
                "INSERT INTO Product (id, name, sourceType, isLiquid) VALUES (2, 'Tea', 0, 0)"
            )
        }
        return db
    }

    @Test
    fun `counts only suggestions for the given food within the day range`() = runTest {
        val db = buildDatabase()
        val dao = db.measurementSuggestionDao
        val repository = RoomFoodMeasurementSuggestionRepository(dao)
        val coffeeId = FoodId.Product(1)

        dao.insert(
            MeasurementSuggestionEntity(
                productId = 1,
                recipeId = null,
                epochSeconds = 1_000,
                type = MeasurementType.Milliliter,
                value = 250.0,
            )
        )
        dao.insert(
            MeasurementSuggestionEntity(
                productId = 1,
                recipeId = null,
                epochSeconds = 2_000,
                type = MeasurementType.Milliliter,
                value = 250.0,
            )
        )
        dao.insert(
            // outside the queried range
            MeasurementSuggestionEntity(
                productId = 1,
                recipeId = null,
                epochSeconds = 99_999,
                type = MeasurementType.Milliliter,
                value = 250.0,
            )
        )
        dao.insert(
            // different product (Tea, id 2) -- must not count toward Coffee's total
            MeasurementSuggestionEntity(
                productId = 2,
                recipeId = null,
                epochSeconds = 1_500,
                type = MeasurementType.Milliliter,
                value = 250.0,
            )
        )

        val count =
            repository
                .observeCountByFoodId(coffeeId, sinceEpochSeconds = 0, untilEpochSeconds = 10_000)
                .first()

        assertEquals(2, count)
    }
}
```

`measurementSuggestionDao` is the property Room generates on `FoodYouDatabase` from the existing `FoodDatabase` interface it implements (same mechanism as `tagDao`/`supplementDao`) — no new wiring needed for the test itself, only the DAO query and repository method being added in Steps 3-5 below.

- [ ] **Step 2: Run test to verify it fails**

Run: `JAVA_HOME=/opt/homebrew/opt/openjdk@21 ./gradlew :app:testDebugUnitTest --tests "*RoomFoodMeasurementSuggestionRepositoryCountTest*" --offline`
Expected: FAIL (compile error — `observeCountByFoodId` doesn't exist yet).

- [ ] **Step 3: Add the DAO query**

In `MeasurementSuggestionDao.kt`, add:

```kotlin
    @Query(
        """
        SELECT COUNT(*)
        FROM MeasurementSuggestion
        WHERE
            (COALESCE(:productId, -1) = productId OR COALESCE(:recipeId, -1) = recipeId)
            AND epochSeconds >= :sinceEpochSeconds
            AND epochSeconds < :untilEpochSeconds
        """
    )
    abstract fun observeCount(
        productId: Long?,
        recipeId: Long?,
        sinceEpochSeconds: Long,
        untilEpochSeconds: Long,
    ): Flow<Int>
```

(Note the explicit parentheses around the `OR` — without them, SQL's `AND`-binds-tighter-than-`OR` would silently match every row where the time range holds, regardless of product/recipe.)

- [ ] **Step 4: Extend the repository interface**

In `FoodMeasurementSuggestionRepository.kt`, add:

```kotlin
    /** Count of suggestions recorded for [foodId] within `[sinceEpochSeconds, untilEpochSeconds)`. */
    fun observeCountByFoodId(
        foodId: FoodId,
        sinceEpochSeconds: Long,
        untilEpochSeconds: Long,
    ): Flow<Int>
```

- [ ] **Step 5: Implement it**

In `RoomFoodMeasurementSuggestionRepository.kt`, add:

```kotlin
    override fun observeCountByFoodId(
        foodId: FoodId,
        sinceEpochSeconds: Long,
        untilEpochSeconds: Long,
    ): Flow<Int> =
        measurementSuggestionDao.observeCount(
            productId = (foodId as? FoodId.Product)?.id,
            recipeId = (foodId as? FoodId.Recipe)?.id,
            sinceEpochSeconds = sinceEpochSeconds,
            untilEpochSeconds = untilEpochSeconds,
        )
```

- [ ] **Step 6: Run test to verify it passes**

Run: `JAVA_HOME=/opt/homebrew/opt/openjdk@21 ./gradlew :app:testDebugUnitTest --tests "*RoomFoodMeasurementSuggestionRepositoryCountTest*" --offline`
Expected: PASS.

- [ ] **Step 7: Commit**

```bash
git add app/src/commonMain/kotlin/com/maksimowiczm/foodyou/food/infrastructure/room/MeasurementSuggestionDao.kt \
  app/src/commonMain/kotlin/com/maksimowiczm/foodyou/food/domain/repository/FoodMeasurementSuggestionRepository.kt \
  app/src/commonMain/kotlin/com/maksimowiczm/foodyou/food/infrastructure/repository/RoomFoodMeasurementSuggestionRepository.kt \
  app/src/commonTest/kotlin/com/maksimowiczm/foodyou/food/infrastructure/repository/RoomFoodMeasurementSuggestionRepositoryCountTest.kt
git commit -m "feat: count today's suggestions per food (coffee cup count)"
```

---

### Task 6: `HomeCard.Habits` + fix the existing-user backfill gap

**Context (read before starting):** `HomeCard`'s order is persisted in `Settings.homeCardOrder` as a **comma-joined list of enum ordinals** (`DataStoreSettingsRepository.getHomeCardOrder`/`setHomeCardOrder`). Appending a new enum value is safe for the ordinal values already stored, but `getHomeCardOrder`'s fallback is `stored ?: HomeCard.defaultOrder` — it does **not** merge newly-added enum entries into an *existing* stored list. Any user who already has a customized (or simply previously-saved) home card order will never see the new `Habits` card, silently, forever, unless this read path is fixed. This is a real bug this task must close, not a hypothetical.

**Files:**
- Modify: `app/src/commonMain/kotlin/com/maksimowiczm/foodyou/settings/domain/entity/HomeCard.kt`
- Modify: `app/src/commonMain/kotlin/com/maksimowiczm/foodyou/settings/infrastructure/DataStoreSettingsRepository.kt`
- Test: `app/src/commonTest/kotlin/com/maksimowiczm/foodyou/settings/infrastructure/HomeCardOrderBackfillTest.kt`

**Interfaces:**
- Produces: `HomeCard.Habits` enum entry; `getHomeCardOrder` now appends any `HomeCard.entries` missing from the stored list, in enum declaration order, after the stored ones.

- [ ] **Step 1: Write the failing test**

```kotlin
// app/src/commonTest/kotlin/com/maksimowiczm/foodyou/settings/infrastructure/HomeCardOrderBackfillTest.kt
package com.maksimowiczm.foodyou.settings.infrastructure

import com.maksimowiczm.foodyou.settings.domain.entity.HomeCard
import kotlin.test.Test
import kotlin.test.assertEquals

class HomeCardOrderBackfillTest {
    @Test
    fun `parsing an order missing a newer enum entry appends it at the end`() {
        // Simulates a pre-Habits stored value: only ordinals 0 (Calendar) and 2 (Meals),
        // deliberately dropping 1 (Goals) too, to prove backfill order follows enum
        // declaration order, not just "append the one new card".
        val stored = "0,2"

        val result = parseHomeCardOrderForTest(stored)

        assertEquals(listOf(HomeCard.Calendar, HomeCard.Meals, HomeCard.Goals, HomeCard.Habits), result)
    }

    @Test
    fun `a fully up to date stored order is returned unchanged`() {
        val stored = HomeCard.entries.joinToString(",") { it.ordinal.toString() }

        val result = parseHomeCardOrderForTest(stored)

        assertEquals(HomeCard.entries, result)
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `JAVA_HOME=/opt/homebrew/opt/openjdk@21 ./gradlew :app:testDebugUnitTest --tests "*HomeCardOrderBackfillTest*" --offline`
Expected: FAIL (compile error — `parseHomeCardOrderForTest` doesn't exist yet; it's a test-only wrapper added in Step 4 around the now-internal parsing logic).

- [ ] **Step 3: Add `HomeCard.Habits`**

In `HomeCard.kt`:

```kotlin
package com.maksimowiczm.foodyou.settings.domain.entity

enum class HomeCard {
    Calendar,
    Goals,
    Meals,
    Habits;

    companion object {
        val defaultOrder: List<HomeCard> = listOf(Calendar, Goals, Meals, Habits)
    }
}
```

- [ ] **Step 4: Fix the backfill gap in `DataStoreSettingsRepository`**

Replace the existing `getHomeCardOrder` (and add the small test-visible wrapper) in `DataStoreSettingsRepository.kt`:

```kotlin
internal fun parseHomeCardOrderForTest(stored: String?): List<HomeCard> = parseHomeCardOrder(stored)

private fun parseHomeCardOrder(stored: String?): List<HomeCard> {
    val parsed =
        runCatching { stored?.split(",")?.map { HomeCard.entries[it.toInt()] } }.getOrNull()
            ?: return HomeCard.defaultOrder
    val missing = HomeCard.entries.filter { it !in parsed }
    return parsed + missing
}

private fun Preferences.getHomeCardOrder(key: Preferences.Key<String>): List<HomeCard> =
    parseHomeCardOrder(this[key])
```

Remove the old one-line `getHomeCardOrder` implementation (the `runCatching { ... } ?: HomeCard.defaultOrder` version) — it's replaced by the two functions above. `parseHomeCardOrderForTest` is `internal` (not `private`) purely so the commonTest above can call it without going through DataStore at all.

- [ ] **Step 5: Run test to verify it passes**

Run: `JAVA_HOME=/opt/homebrew/opt/openjdk@21 ./gradlew :app:testDebugUnitTest --tests "*HomeCardOrderBackfillTest*" --offline`
Expected: PASS (2 tests).

- [ ] **Step 6: Commit**

```bash
git add app/src/commonMain/kotlin/com/maksimowiczm/foodyou/settings/domain/entity/HomeCard.kt \
  app/src/commonMain/kotlin/com/maksimowiczm/foodyou/settings/infrastructure/DataStoreSettingsRepository.kt \
  app/src/commonTest/kotlin/com/maksimowiczm/foodyou/settings/infrastructure/HomeCardOrderBackfillTest.kt
git commit -m "feat: add HomeCard.Habits and backfill missing cards into stored order"
```

---

### Task 7: `HabitsCardViewModel`

**Files:**
- Create: `app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/home/habits/HabitsCardViewModel.kt`
- Create: `app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/home/habits/HabitsCardModel.kt`
- Modify: `app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/home/HomeModule.kt`

**Interfaces:**
- Consumes: `SupplementRepository` (Task 3), `HabitsPreferences`/`UserPreferencesRepository<HabitsPreferences>` (Task 4), `FoodMeasurementSuggestionRepository.observeCountByFoodId` (Task 5), `ObserveDiaryMealsUseCase.observe(date): Flow<List<DiaryMeal>>` (existing), `ObserveFoodUseCase.observe(foodId): Flow<Food?>` (existing), `CreateFoodDiaryEntryUseCase.createDiaryEntry(...)` (existing), `EventBus`/`FoodDiaryEntryCreatedEvent` (existing), `DateProvider.nowInstant()` (existing), `Food.toDiaryFood()` (existing, `internal` in `app.ui.food.diary.add`, module-visible).
- Produces: `HabitsCardModel(cupsToday: Int, caffeineMgToday: Int, hasDefaultCoffee: Boolean, supplements: List<SupplementRow>)`, `SupplementRow(id: Long, name: String, taken: Boolean)`. `HabitsCardViewModel` exposes `model: StateFlow<HabitsCardModel?>`, `fun logCoffee()`, `fun addSupplement(name: String)`, `fun deleteSupplement(id: Long)`, `fun setSupplementTaken(id: Long, taken: Boolean)`, `fun setDate(date: LocalDate)`.

- [ ] **Step 1: Write the UI model**

```kotlin
// app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/home/habits/HabitsCardModel.kt
package com.maksimowiczm.foodyou.app.ui.home.habits

internal data class HabitsCardModel(
    val cupsToday: Int,
    val caffeineMgToday: Int,
    val hasDefaultCoffee: Boolean,
    val supplements: List<SupplementRow>,
)

internal data class SupplementRow(val id: Long, val name: String, val taken: Boolean)
```

- [ ] **Step 2: Write the ViewModel**

```kotlin
// app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/home/habits/HabitsCardViewModel.kt
package com.maksimowiczm.foodyou.app.ui.home.habits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maksimowiczm.foodyou.app.ui.food.diary.add.toDiaryFood
import com.maksimowiczm.foodyou.common.domain.date.DateProvider
import com.maksimowiczm.foodyou.common.domain.event.EventBus
import com.maksimowiczm.foodyou.common.domain.food.sum
import com.maksimowiczm.foodyou.common.domain.measurement.Measurement
import com.maksimowiczm.foodyou.common.domain.userpreferences.UserPreferencesRepository
import com.maksimowiczm.foodyou.food.domain.entity.FoodId
import com.maksimowiczm.foodyou.food.domain.repository.FoodMeasurementSuggestionRepository
import com.maksimowiczm.foodyou.food.domain.usecase.ObserveFoodUseCase
import com.maksimowiczm.foodyou.fooddiary.domain.event.FoodDiaryEntryCreatedEvent
import com.maksimowiczm.foodyou.fooddiary.domain.usecase.CreateFoodDiaryEntryUseCase
import com.maksimowiczm.foodyou.fooddiary.domain.usecase.ObserveDiaryMealsUseCase
import com.maksimowiczm.foodyou.habits.domain.entity.HabitsPreferences
import com.maksimowiczm.foodyou.habits.domain.repository.SupplementRepository
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlinx.datetime.DateTimeUnit

internal class HabitsCardViewModel(
    private val supplementRepository: SupplementRepository,
    private val habitsPreferencesRepository: UserPreferencesRepository<HabitsPreferences>,
    private val measurementSuggestionRepository: FoodMeasurementSuggestionRepository,
    private val observeDiaryMealsUseCase: ObserveDiaryMealsUseCase,
    private val observeFoodUseCase: ObserveFoodUseCase,
    private val createFoodDiaryEntryUseCase: CreateFoodDiaryEntryUseCase,
    private val eventBus: EventBus,
    private val dateProvider: DateProvider,
) : ViewModel() {
    private val dateState = MutableStateFlow<LocalDate?>(null)

    fun setDate(date: LocalDate) {
        dateState.value = date
    }

    val model =
        dateState
            .filterNotNull()
            .flatMapLatest { date ->
                val preferences = habitsPreferencesRepository.observe()
                val (sinceEpochSeconds, untilEpochSeconds) = date.dayEpochSecondsRange()

                combine(
                    observeDiaryMealsUseCase.observe(date),
                    supplementRepository.observeSupplements(),
                    supplementRepository.observeTakenIdsForDate(date),
                    preferences,
                ) { meals, supplements, takenIds, prefs ->
                    val caffeineMg =
                        ((meals.map { it.nutritionFacts }.sum().caffeine.value ?: 0.0) * 1000.0)
                            .roundToInt()

                    val defaultCoffeeFoodId = prefs.toDefaultCoffeeFoodId()
                    val cupsToday =
                        if (defaultCoffeeFoodId == null) {
                            flowOf(0)
                        } else {
                            measurementSuggestionRepository.observeCountByFoodId(
                                foodId = defaultCoffeeFoodId,
                                sinceEpochSeconds = sinceEpochSeconds,
                                untilEpochSeconds = untilEpochSeconds,
                            )
                        }

                    Triple(caffeineMg, defaultCoffeeFoodId, supplements to takenIds) to cupsToday
                }
            }
            // The inner `cupsToday` flow depends on the same combine's other emissions, so it's
            // flattened in a second step rather than nested inside the first `combine` block.
            .flatMapLatest { (rest, cupsTodayFlow) ->
                cupsTodayFlow.map { cups ->
                    val (caffeineMg, defaultCoffeeFoodId, supplementsAndTaken) = rest
                    val (supplements, takenIds) = supplementsAndTaken
                    HabitsCardModel(
                        cupsToday = cups,
                        caffeineMgToday = caffeineMg,
                        hasDefaultCoffee = defaultCoffeeFoodId != null,
                        supplements =
                            supplements.map { SupplementRow(it.id, it.name, it.id in takenIds) },
                    )
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(60_000),
                initialValue = null,
            )

    fun addSupplement(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch { supplementRepository.addSupplement(name.trim()) }
    }

    fun deleteSupplement(id: Long) {
        viewModelScope.launch { supplementRepository.deleteSupplement(id) }
    }

    fun setSupplementTaken(id: Long, taken: Boolean) {
        val date = dateState.value ?: return
        viewModelScope.launch { supplementRepository.setTaken(id, date, taken) }
    }

    /** One-tap re-log of the configured default coffee. No-op if none is configured yet. */
    fun logCoffee() {
        viewModelScope.launch {
            val prefs = habitsPreferencesRepository.observe().firstOrNull() ?: return@launch
            val foodId = prefs.toDefaultCoffeeFoodId() ?: return@launch
            val measurementType = prefs.defaultCoffeeMeasurementType ?: return@launch
            val measurementValue = prefs.defaultCoffeeMeasurementValue ?: return@launch
            val targetMealId = prefs.defaultCoffeeMealId ?: return@launch
            val date = dateState.value ?: return@launch

            val food = observeFoodUseCase.observe(foodId).firstOrNull() ?: return@launch
            val measurement = Measurement.from(type = measurementType, rawValue = measurementValue)

            createFoodDiaryEntryUseCase
                .createDiaryEntry(
                    measurement = measurement,
                    mealId = targetMealId,
                    date = date,
                    food = food.toDiaryFood(),
                )
                .also {
                    eventBus.publish(
                        FoodDiaryEntryCreatedEvent(
                            foodId = food.id,
                            timestamp = dateProvider.nowInstant(),
                            measurement = measurement,
                        )
                    )
                }
        }
    }

    /** Called after the one-time "pick default coffee" flow completes. */
    fun setDefaultCoffee(foodId: FoodId, measurement: Measurement, mealId: Long) {
        viewModelScope.launch {
            habitsPreferencesRepository.update {
                copy(
                    defaultCoffeeFoodId =
                        when (foodId) {
                            is FoodId.Product -> foodId.id
                            is FoodId.Recipe -> foodId.id
                        },
                    defaultCoffeeIsRecipe = foodId is FoodId.Recipe,
                    defaultCoffeeMealId = mealId,
                    defaultCoffeeMeasurementType = measurement.type,
                    defaultCoffeeMeasurementValue = measurement.rawValue,
                )
            }
        }
    }
}

private fun HabitsPreferences.toDefaultCoffeeFoodId(): FoodId? {
    val id = defaultCoffeeFoodId ?: return null
    return if (defaultCoffeeIsRecipe) FoodId.Recipe(id) else FoodId.Product(id)
}

private fun LocalDate.dayEpochSecondsRange(): Pair<Long, Long> {
    val timeZone = TimeZone.currentSystemDefault()
    val start = atStartOfDayIn(timeZone).epochSeconds
    val end = this.plus(1, DateTimeUnit.DAY).atStartOfDayIn(timeZone).epochSeconds
    return start to end
}
```

Import `measurement.type`/`measurement.rawValue` extensions from `com.maksimowiczm.foodyou.common.domain.measurement` (`type`, `rawValue`), matching `RoomFoodMeasurementSuggestionRepository`'s existing usage.

**Note on the double-`flatMapLatest` shape above:** this is more convoluted than the rest of the codebase's view models (which use a single `combine`). Simplify during implementation if a cleaner shape emerges (e.g. collapsing `cupsToday` into the same `combine` via `combine(a, b, c, d, e) { ... }` with five arguments once `defaultCoffeeFoodId` no longer needs to be derived *inside* the same combine step) — verify manually per Task 9 Step 5 regardless of the exact internal flow shape chosen, since (per the note below) this ViewModel has no dedicated unit test.

**No dedicated ViewModel test for this task.** This codebase has zero unit tests for any existing `ViewModel` (`GoalsViewModel`, `MealsCardsViewModel`, etc. — confirmed by searching `app/src/commonTest` for `*ViewModel*`, no matches). Its actual test convention is: repository/DAO tests (Tasks 3, 5), migration fixture tests (Task 1), and pure-function tests (Task 6). Matching that convention rather than introducing a new fake-based ViewModel-testing harness this codebase doesn't otherwise use, `HabitsCardViewModel` itself is verified manually in Task 9 Step 5 (build + install + exercise the flow) rather than unit-tested. The logic it depends on (`RoomSupplementRepository`, `observeCountByFoodId`) is already covered by Tasks 3 and 5.

- [ ] **Step 3: Register the ViewModel in `HomeModule`**

In `app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/home/HomeModule.kt`, add:

```kotlin
    viewModel {
        HabitsCardViewModel(
            supplementRepository = get(),
            habitsPreferencesRepository = get(),
            measurementSuggestionRepository = get(),
            observeDiaryMealsUseCase = get(),
            observeFoodUseCase = get(),
            createFoodDiaryEntryUseCase = get(),
            eventBus = get(),
            dateProvider = get(),
        )
    }
```

(alongside the existing `MealsCardsViewModel` registration), plus the matching import `com.maksimowiczm.foodyou.app.ui.home.habits.HabitsCardViewModel`.

- [ ] **Step 4: Commit**

```bash
git add app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/home/habits/HabitsCardViewModel.kt \
  app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/home/habits/HabitsCardModel.kt \
  app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/home/HomeModule.kt
git commit -m "feat: add HabitsCardViewModel aggregating coffee + supplement state"
```

---

### Task 8: `HabitsCard` composable (coffee row + supplement checklist)

**Files:**
- Create: `app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/home/habits/HabitsCard.kt`

**Interfaces:**
- Consumes: `HabitsCardViewModel`, `HabitsCardModel`, `SupplementRow` (Task 7). `FoodYouHomeCard` (existing shared card container, see `MealCard.kt`'s usage). `HomeState` (existing, for `selectedDate`/`shimmer` — mirrors `MealsCards.kt`'s `LaunchedEffect(homeState.selectedDate, viewModel) { viewModel.setDate(...) }` pattern).

- [ ] **Step 1: Write the composable**

```kotlin
// app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/home/habits/HabitsCard.kt
package com.maksimowiczm.foodyou.app.ui.home.habits

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maksimowiczm.foodyou.app.ui.home.shared.FoodYouHomeCard
import com.maksimowiczm.foodyou.app.ui.home.shared.HomeState
import foodyou.app.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun HabitsCard(
    homeState: HomeState,
    onSetDefaultCoffeeClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: HabitsCardViewModel = koinViewModel()
    val model by viewModel.model.collectAsStateWithLifecycle()

    LaunchedEffect(homeState.selectedDate, viewModel) { viewModel.setDate(homeState.selectedDate) }

    HabitsCard(
        model = model,
        onLogCoffee = viewModel::logCoffee,
        onSetDefaultCoffeeClick = onSetDefaultCoffeeClick,
        onAddSupplement = viewModel::addSupplement,
        onDeleteSupplement = viewModel::deleteSupplement,
        onSetSupplementTaken = viewModel::setSupplementTaken,
        modifier = modifier,
    )
}

@Composable
private fun HabitsCard(
    model: HabitsCardModel?,
    onLogCoffee: () -> Unit,
    onSetDefaultCoffeeClick: () -> Unit,
    onAddSupplement: (String) -> Unit,
    onDeleteSupplement: (Long) -> Unit,
    onSetSupplementTaken: (Long, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (model == null) return

    // Long-press re-opens the same picker used for first-time setup, so an already-configured
    // default coffee can be changed later (the spec's "change default coffee" action -- Plan A
    // has no settings dialog yet for it to live in, so it's reachable via long-press here
    // instead; Plan B's reminder settings dialog can additionally surface it once it exists).
    FoodYouHomeCard(modifier = modifier, onClick = {}, onLongClick = onSetDefaultCoffeeClick) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(
                text = stringResource(Res.string.headline_habits),
                style = MaterialTheme.typography.headlineMediumEmphasized,
            )

            Spacer(Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(
                        text = "${model.cupsToday} ${stringResource(Res.string.unit_cups)}",
                        style = MaterialTheme.typography.headlineSmall,
                    )
                    Text(
                        text = "${model.caffeineMgToday} ${stringResource(Res.string.unit_milligram_short)}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.weight(1f))
                FilledTonalIconButton(
                    onClick = { if (model.hasDefaultCoffee) onLogCoffee() else onSetDefaultCoffeeClick() }
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = stringResource(Res.string.action_add))
                }
            }

            Spacer(Modifier.height(16.dp))

            model.supplements.forEach { row ->
                key(row.id) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(
                            checked = row.taken,
                            onCheckedChange = { onSetSupplementTaken(row.id, it) },
                        )
                        Text(text = row.name, modifier = Modifier.weight(1f))

                        var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
                        if (showDeleteDialog) {
                            AlertDialog(
                                onDismissRequest = { showDeleteDialog = false },
                                confirmButton = {
                                    TextButton(
                                        onClick = {
                                            onDeleteSupplement(row.id)
                                            showDeleteDialog = false
                                        }
                                    ) {
                                        Text(stringResource(Res.string.action_delete))
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showDeleteDialog = false }) {
                                        Text(stringResource(Res.string.action_cancel))
                                    }
                                },
                                title = { Text(stringResource(Res.string.action_delete)) },
                                text = { Text(row.name) },
                            )
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = stringResource(Res.string.action_delete))
                        }
                    }
                }
            }

            var showAddDialog by rememberSaveable { mutableStateOf(false) }
            if (showAddDialog) {
                val textFieldState = rememberTextFieldState()
                AlertDialog(
                    onDismissRequest = { showAddDialog = false },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                onAddSupplement(textFieldState.text.toString())
                                showAddDialog = false
                            }
                        ) {
                            Text(stringResource(Res.string.action_confirm))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showAddDialog = false }) {
                            Text(stringResource(Res.string.action_cancel))
                        }
                    },
                    title = { Text(stringResource(Res.string.action_add)) },
                    text = { OutlinedTextField(state = textFieldState, lineLimits = androidx.compose.foundation.text.input.TextFieldLineLimits.SingleLine) },
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Spacer(Modifier.weight(1f))
                TextButton(onClick = { showAddDialog = true }) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                    Text(stringResource(Res.string.action_add))
                }
            }
        }
    }
}
```

New strings needed (add to `shared/resources/src/commonMain/composeResources/values/strings.xml`, following the existing `<string name="...">` format): `headline_habits` ("Habits"), `unit_cups` ("cups"). `unit_milligram_short`, `action_add`, `action_delete`, `action_cancel`, `action_confirm` already exist (used elsewhere, e.g. `MealCard.kt`) — confirm via `grep -n "unit_milligram_short\|action_delete\b" shared/resources/src/commonMain/composeResources/values/strings.xml` before adding duplicates.

- [ ] **Step 2: Compile to verify**

Run: `JAVA_HOME=/opt/homebrew/opt/openjdk@21 ./gradlew :app:compileDebugKotlin --offline -q`
Expected: no output (success). Fix any missing-string-resource or import errors surfaced here — this step is where the two new strings above actually get added, once the compiler names exactly which resource is missing.

- [ ] **Step 3: Commit**

```bash
git add app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/home/habits/HabitsCard.kt \
  shared/resources/src/commonMain/composeResources/values/strings.xml
git commit -m "feat: add HabitsCard composable (coffee row + supplement checklist)"
```

---

### Task 9: Wire `HabitsCard` into the home screen + "pick default coffee" flow

**Files:**
- Create: `app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/home/habits/PickDefaultCoffeeScreen.kt`
- Modify: `app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/home/master/HomeScreen.kt`
- Modify: `app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/home/personalization/HomePersonalizationScreen.kt`
- Modify: `app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/navigation/FoodYouAppNavHost.kt`

**Interfaces:**
- Consumes: `FoodSearchApp(onFoodClick: (FoodSearch, Measurement) -> Unit, onUpdateUsdaApiKey, onUpdateOpenFoodFactsCredentials, modifier, excludedRecipe)` (existing, `app.ui.food.search.FoodSearchApp`), `MealRepository.observeMeals(): Flow<List<Meal>>` (existing), `HabitsCardViewModel.setDefaultCoffee(...)` (Task 7).
- Produces: `PickDefaultCoffeeScreen` composable; nav route `HabitsPickDefaultCoffee`; `HomeScreen`'s new `onHabitsSetDefaultCoffeeClick: () -> Unit` param; `HomePersonalizationScreen`'s new `onHabits: () -> Unit` param (a placeholder no-op destination — no dedicated Habits settings screen exists per the spec, "managed inline on the card", so this can just be omitted from the personalization preview's "more" affordance; see Step 3).

**Design note resolving a gap the spec didn't cover:** logging a diary entry requires a `mealId` (`CreateFoodDiaryEntryUseCase` takes one; confirmed by reading its signature). The spec says tapping "+" with no default configured "opens food search once" but never says which meal the resulting entry (and future quick-logs) belongs to. This plan resolves it concretely: `PickDefaultCoffeeScreen` uses the **first meal in `MealRepository.observeMeals()`** (already sorted by `rank`, the same order meal cards render in) as the meal — no separate meal-picker UI, keeping the "opens food search once" one-tap wording accurate. Picking a food+measurement there **only configures the default** (calls `setDefaultCoffee`, does not itself create a diary entry) — the user taps "+" again afterward to log the first cup, keeping "configure" and "log" as distinct, unambiguous actions.

- [ ] **Step 1: Write `PickDefaultCoffeeScreen`**

```kotlin
// app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/home/habits/PickDefaultCoffeeScreen.kt
package com.maksimowiczm.foodyou.app.ui.home.habits

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maksimowiczm.foodyou.app.ui.food.search.FoodSearchApp
import com.maksimowiczm.foodyou.fooddiary.domain.repository.MealRepository
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun PickDefaultCoffeeScreen(
    onDone: () -> Unit,
    onUpdateUsdaApiKey: () -> Unit,
    onUpdateOpenFoodFactsCredentials: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: HabitsCardViewModel = koinViewModel()
    val mealRepository: MealRepository = koinInject()
    val meals by
        remember { mealRepository.observeMeals() }.collectAsStateWithLifecycle(initialValue = emptyList())

    FoodSearchApp(
        onFoodClick = { food, measurement ->
            val mealId = meals.firstOrNull()?.id ?: return@FoodSearchApp
            // `food.id` is already a `FoodId` (see FoodSearch.kt: `val id: FoodId`) -- no
            // reconstruction needed, unlike the DataStore-persisted form in HabitsPreferences.
            viewModel.setDefaultCoffee(foodId = food.id, measurement = measurement, mealId = mealId)
            onDone()
        },
        onUpdateUsdaApiKey = onUpdateUsdaApiKey,
        onUpdateOpenFoodFactsCredentials = onUpdateOpenFoodFactsCredentials,
        modifier = modifier,
    )
}
```

- [ ] **Step 2: Wire the new route into `FoodYouAppNavHost`**

Add a new route object near the other `@Serializable private data class` routes:

```kotlin
@Serializable private data object HabitsPickDefaultCoffee
```

Add a destination block (near the other `forwardBackwardComposable` blocks):

```kotlin
        forwardBackwardComposable<HabitsPickDefaultCoffee> {
            com.maksimowiczm.foodyou.app.ui.home.habits.PickDefaultCoffeeScreen(
                onDone = { navController.popBackStackInclusive<HabitsPickDefaultCoffee>() },
                onUpdateUsdaApiKey = { navController.navigateSingleTop(UsdaApiKey) },
                onUpdateOpenFoodFactsCredentials = {
                    navController.navigateSingleTop(OpenFoodFactsLogin)
                },
            )
        }
```

(`UsdaApiKey`/`OpenFoodFactsLogin` are the same existing route objects the `FoodDiarySearch` destination already navigates to at lines 236-239 of this file — reused here, not new routes.)

Wire the **Home** destination's new callback:
```kotlin
                onHabitsSetDefaultCoffeeClick = {
                    navController.navigateSingleTop(HabitsPickDefaultCoffee)
                },
```
inside the existing `HomeScreen(...)` call in the `Home` destination block.

- [ ] **Step 3: Add the `HabitsCard` render + callback to `HomeScreen`**

In `HomeScreen.kt`:
- Add import `com.maksimowiczm.foodyou.app.ui.home.habits.HabitsCard`.
- Add a new parameter: `onHabitsSetDefaultCoffeeClick: () -> Unit,`.
- Add a new `when` branch inside `items(order) { when (it) { ... } }`:

```kotlin
                    HomeCard.Habits ->
                        HabitsCard(
                            homeState = homeState,
                            onSetDefaultCoffeeClick = onHabitsSetDefaultCoffeeClick,
                            modifier = Modifier.padding(horizontal = 8.dp).padding(bottom = 8.dp),
                        )
```

- [ ] **Step 4: Add the personalization-screen preview branch**

In `HomePersonalizationScreen.kt`, the `when (it) { HomeCard.Calendar -> ...; HomeCard.Goals -> ...; HomeCard.Meals -> ... }` block (this `when` is exhaustive over the enum, so the compiler will already flag this as a required addition once `HomeCard.Habits` exists from Task 6). Add:

```kotlin
                                HomeCard.Habits -> HabitsCardContent()
```

and a matching small preview composable near `MealsCardContent`/`GoalsCardContent`:

```kotlin
@Composable
private fun HabitsCardContent() {
    Text(stringResource(Res.string.headline_habits))
}
```

No `onMore` callback for this one — `CalendarCardContent()` (also parameterless) is the existing precedent for a card with no dedicated settings screen; `GoalsCardContent(onMore = onGoals)`/`MealsCardContent(onMore = onMeals)` are the ones that do take one. The spec decided supplement management happens inline on the card itself, matching the `Calendar` shape, not the `Goals`/`Meals` one.

- [ ] **Step 5: Compile and manually verify**

Run: `JAVA_HOME=/opt/homebrew/opt/openjdk@21 ./gradlew :app:assembleDebug --offline -q`
Expected: no output (success). Then install and manually verify per the `verify` skill: the Habits card appears on Home (bottom of the default card order), tapping its "+" with no default coffee configured opens food search, picking a product returns to Home, tapping "+" again creates a diary entry under the first meal and increments the cup count, adding/checking/deleting a supplement all persist across app restart.

- [ ] **Step 6: Commit**

```bash
git add app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/home/habits/PickDefaultCoffeeScreen.kt \
  app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/home/master/HomeScreen.kt \
  app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/home/personalization/HomePersonalizationScreen.kt \
  app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/navigation/FoodYouAppNavHost.kt
git commit -m "feat: wire HabitsCard into home screen and add default-coffee picker flow"
```

---

### Task 10: Update `docs/schema.md`

**Files:**
- Modify: `docs/schema.md`

- [ ] **Step 1: Add a "Habits" entity-reference section**

After the "Tags" section, add:

```markdown
### Habits (`habits/infrastructure/room/`) — coffee + supplement tracking, added v35

| Entity | Table | Key columns | Notes |
|---|---|---|---|
| `SupplementEntity` | `Supplement` | `id` PK (autogenerate), `name`, `sortOrder` | User-managed list of supplements. `sortOrder` is append-only insertion order, no reorder UI. |
| `SupplementIntakeEntity` | `SupplementIntake` | `(supplementId, date)` composite PK, FK cascade-delete | Adherence only -- presence of a row means taken that day. No dose, no timestamp. |

Coffee tracking added no new table: cup count is derived from the existing `MeasurementSuggestion`
table (already populated on every diary log, keyed by the *catalog* `productId`/`recipeId`), and
caffeine mg reuses the existing per-day nutrition aggregate. The only new state is
`HabitsPreferences` (DataStore, not Room) recording which food/measurement/meal counts as the
one-tap "default coffee".
```

- [ ] **Step 2: Add a migration chain row**

In the "Migration chain" table, add a row after `33→34`:

```markdown
| **34→35** | Manual (`addHabitsTables`) | Adds `Supplement`, `SupplementIntake` for the habits-tracking supplement checklist. Purely additive, FK `ON DELETE CASCADE`, no existing table or column altered. |
```

Update the header line `# Database schema (as of this fork, schema version 34)` to `35`, and the sentence referencing `{1..34}.json` to `{1..35}.json`.

- [ ] **Step 3: Commit**

```bash
git add docs/schema.md
git commit -m "docs: document Habits tables and schema v35 migration"
```
