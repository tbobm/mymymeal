package dev.tbobm.mymymeal.app.habits

import dev.tbobm.mymymeal.app.habits.domain.repository.CoffeeRepository
import dev.tbobm.mymymeal.app.habits.domain.repository.SupplementRepository
import dev.tbobm.mymymeal.app.habits.infrastructure.room.HabitsDatabase
import dev.tbobm.mymymeal.app.habits.infrastructure.room.RoomCoffeeRepository
import dev.tbobm.mymymeal.app.habits.infrastructure.room.RoomSupplementRepository
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.scope.Scope
import org.koin.dsl.bind
import org.koin.dsl.module

val habitsModule = module {
    factory { database.supplementDao }
    factory { database.coffeeIntakeDao }
    factoryOf(::RoomSupplementRepository).bind<SupplementRepository>()
    factoryOf(::RoomCoffeeRepository).bind<CoffeeRepository>()
}

private val Scope.database: HabitsDatabase
    get() = get()
