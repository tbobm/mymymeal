package com.maksimowiczm.foodyou.habits.infrastructure.room

import com.maksimowiczm.foodyou.habits.domain.entity.Supplement
import com.maksimowiczm.foodyou.habits.domain.repository.SupplementRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate

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
