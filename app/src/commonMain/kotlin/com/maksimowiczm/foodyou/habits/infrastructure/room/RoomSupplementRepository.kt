package com.maksimowiczm.foodyou.habits.infrastructure.room

import com.maksimowiczm.foodyou.habits.domain.entity.Supplement
import com.maksimowiczm.foodyou.habits.domain.entity.SupplementIntake
import com.maksimowiczm.foodyou.habits.domain.repository.SupplementRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate

internal class RoomSupplementRepository(private val supplementDao: SupplementDao) :
    SupplementRepository {
    override fun observeSupplements(): Flow<List<Supplement>> =
        supplementDao.observeSupplements().map { list -> list.map(SupplementEntity::toModel) }

    override fun observeIntakeForDate(date: LocalDate): Flow<Map<Long, SupplementIntake>> =
        supplementDao.observeIntakeForDate(date.toEpochDays()).map { rows ->
            rows.associate { it.supplementId to SupplementIntake(it.supplementId, it.doseGrams) }
        }

    override suspend fun addSupplement(name: String, tracksDose: Boolean) {
        supplementDao.addSupplement(name, tracksDose)
    }

    override suspend fun deleteSupplement(id: Long) = supplementDao.deleteSupplement(id)

    override suspend fun setIntake(id: Long, date: LocalDate, taken: Boolean, doseGrams: Double?) =
        supplementDao.setIntake(id, date.toEpochDays(), taken, doseGrams)
}

private fun SupplementEntity.toModel() =
    Supplement(id = id, name = name, sortOrder = sortOrder, tracksDose = tracksDose)
