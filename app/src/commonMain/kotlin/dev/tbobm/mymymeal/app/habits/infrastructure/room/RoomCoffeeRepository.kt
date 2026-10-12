package dev.tbobm.mymymeal.app.habits.infrastructure.room

import dev.tbobm.mymymeal.app.common.domain.date.DateProvider
import dev.tbobm.mymymeal.app.habits.domain.entity.CoffeeLog
import dev.tbobm.mymymeal.app.habits.domain.entity.CoffeeType
import dev.tbobm.mymymeal.app.habits.domain.repository.CoffeeRepository
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate

internal class RoomCoffeeRepository(
    private val coffeeIntakeDao: CoffeeIntakeDao,
    private val dateProvider: DateProvider,
) : CoffeeRepository {
    override fun observeForDate(date: LocalDate): Flow<List<CoffeeLog>> =
        coffeeIntakeDao.observeForDate(date.toEpochDays()).map { list ->
            list.map(CoffeeIntakeEntity::toModel)
        }

    override fun observeCaffeineMgForDate(date: LocalDate): Flow<Int> =
        coffeeIntakeDao.observeCaffeineMgForDate(date.toEpochDays())

    override suspend fun logCoffee(type: CoffeeType, date: LocalDate) {
        coffeeIntakeDao.insert(
            CoffeeIntakeEntity(
                dateEpochDay = date.toEpochDays(),
                createdEpochSeconds = dateProvider.nowInstant().epochSeconds,
                type = type.name,
                caffeineMg = type.defaultCaffeineMg,
            )
        )
    }

    override suspend fun deleteCoffeeLog(id: Long) = coffeeIntakeDao.deleteById(id)
}

private fun CoffeeIntakeEntity.toModel() =
    CoffeeLog(
        id = id,
        instant = Instant.fromEpochSeconds(createdEpochSeconds),
        type = CoffeeType.valueOf(type),
        caffeineMg = caffeineMg,
    )
