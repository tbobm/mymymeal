package com.maksimowiczm.foodyou.habits.domain.repository

import com.maksimowiczm.foodyou.habits.domain.entity.CoffeeLog
import com.maksimowiczm.foodyou.habits.domain.entity.CoffeeType
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface CoffeeRepository {
    fun observeForDate(date: LocalDate): Flow<List<CoffeeLog>>

    /** Total caffeine mg logged on [date]. Not food, not a diary entry. */
    fun observeCaffeineMgForDate(date: LocalDate): Flow<Int>

    suspend fun logCoffee(type: CoffeeType, date: LocalDate)

    suspend fun deleteCoffeeLog(id: Long)
}
