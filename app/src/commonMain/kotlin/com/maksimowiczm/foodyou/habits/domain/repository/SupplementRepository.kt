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
