package com.maksimowiczm.foodyou.habits.domain.repository

import com.maksimowiczm.foodyou.habits.domain.entity.Supplement
import com.maksimowiczm.foodyou.habits.domain.entity.SupplementIntake
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface SupplementRepository {
    fun observeSupplements(): Flow<List<Supplement>>

    /** Intake rows for [date], keyed by supplement id. Absence means not taken. */
    fun observeIntakeForDate(date: LocalDate): Flow<Map<Long, SupplementIntake>>

    suspend fun addSupplement(name: String, tracksDose: Boolean)

    suspend fun deleteSupplement(id: Long)

    /** [doseGrams] is only meaningful when the supplement tracks a dose. */
    suspend fun setIntake(id: Long, date: LocalDate, taken: Boolean, doseGrams: Double? = null)
}
