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
