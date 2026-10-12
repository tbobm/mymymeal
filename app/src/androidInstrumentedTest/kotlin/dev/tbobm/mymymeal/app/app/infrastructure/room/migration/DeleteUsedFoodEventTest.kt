package dev.tbobm.mymymeal.app.app.infrastructure.room.migration

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.SQLiteDriver
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.test.platform.app.InstrumentationRegistry
import dev.tbobm.mymymeal.app.app.infrastructure.room.MymymealDatabase
import org.junit.Rule
import org.junit.Test

class DeleteUsedFoodEventTest : AbstractDeleteUsedFoodEventTest() {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val file = instrumentation.targetContext.getDatabasePath("DeleteUsedFoodEventTest.db")
    private val driver: SQLiteDriver = BundledSQLiteDriver()

    @get:Rule
    val helper: MigrationTestHelper =
        MigrationTestHelper(
            instrumentation = instrumentation,
            file = file,
            driver = driver,
            databaseClass = MymymealDatabase::class,
        )

    override fun getTestHelper() = helper

    @Test
    override fun migrate() {
        super.migrate()
    }
}
