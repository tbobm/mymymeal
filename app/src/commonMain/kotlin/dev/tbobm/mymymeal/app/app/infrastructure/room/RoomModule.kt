package dev.tbobm.mymymeal.app.app.infrastructure.room

import androidx.room.RoomDatabase
import dev.tbobm.mymymeal.app.common.domain.database.TransactionProvider
import dev.tbobm.mymymeal.app.common.infrastructure.room.tag.TagDatabase
import dev.tbobm.mymymeal.app.food.infrastructure.room.FoodDatabase
import dev.tbobm.mymymeal.app.food.search.infrastructure.room.FoodSearchDatabase
import dev.tbobm.mymymeal.app.fooddiary.infrastructure.room.FoodDiaryDatabase
import dev.tbobm.mymymeal.app.habits.infrastructure.room.HabitsDatabase
import dev.tbobm.mymymeal.app.sponsorship.infrastructure.room.SponsorshipDatabase
import org.koin.core.module.Module
import org.koin.core.scope.Scope
import org.koin.dsl.binds

internal const val DATABASE_NAME = "open_source_database.db"

internal expect fun Scope.database(): MymymealDatabase

private val Scope.database: MymymealDatabase
    get() = get<MymymealDatabase>()

fun Module.roomModule() {
    single<MymymealDatabase> { database() }
        .binds(
            arrayOf(
                RoomDatabase::class,
                TransactionProvider::class,
                FoodDatabase::class,
                FoodSearchDatabase::class,
                FoodDiaryDatabase::class,
                SponsorshipDatabase::class,
                TagDatabase::class,
                HabitsDatabase::class,
            )
        )
}
