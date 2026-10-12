package dev.tbobm.mymymeal.app.fooddiary.infrastructure.repository

import dev.tbobm.mymymeal.app.common.domain.food.NutritionFacts
import dev.tbobm.mymymeal.app.common.infrastructure.room.toEntityNutrients
import dev.tbobm.mymymeal.app.common.infrastructure.room.toNutritionFacts
import dev.tbobm.mymymeal.app.fooddiary.domain.entity.ManualDiaryEntry
import dev.tbobm.mymymeal.app.fooddiary.domain.entity.ManualDiaryEntryId
import dev.tbobm.mymymeal.app.fooddiary.domain.repository.ManualDiaryEntryRepository
import dev.tbobm.mymymeal.app.fooddiary.infrastructure.room.ManualDiaryEntryDao
import dev.tbobm.mymymeal.app.fooddiary.infrastructure.room.ManualDiaryEntryEntity
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

internal class RoomManualDiaryEntryRepository(private val dao: ManualDiaryEntryDao) :
    ManualDiaryEntryRepository {
    override fun observe(id: ManualDiaryEntryId): Flow<ManualDiaryEntry?> =
        dao.observe(id.value).map { it?.toModel() }

    override fun observeAll(mealId: Long, date: LocalDate): Flow<List<ManualDiaryEntry>> =
        dao.observeAll(mealId, date.toEpochDays()).map { list ->
            list.map(ManualDiaryEntryEntity::toModel)
        }

    override fun observeAll(): Flow<List<ManualDiaryEntry>> =
        dao.observeAllEntries().map { list -> list.map(ManualDiaryEntryEntity::toModel) }

    override suspend fun insert(
        name: String,
        mealId: Long,
        date: LocalDate,
        nutritionFacts: NutritionFacts,
        createdAt: LocalDateTime,
    ): ManualDiaryEntryId {
        val entry =
            ManualDiaryEntry(
                id = ManualDiaryEntryId(0),
                mealId = mealId,
                date = date,
                name = name,
                nutritionFacts = nutritionFacts,
                createdAt = createdAt,
                updatedAt = createdAt,
            )
        val id = dao.insert(entry.toEntity())
        return ManualDiaryEntryId(id)
    }

    override suspend fun update(entry: ManualDiaryEntry) = dao.update(entry.toEntity())

    override suspend fun delete(id: ManualDiaryEntryId) = dao.delete(id.value)
}

private fun ManualDiaryEntryEntity.toModel(): ManualDiaryEntry =
    ManualDiaryEntry(
        id = ManualDiaryEntryId(id),
        mealId = mealId,
        date = LocalDate.fromEpochDays(dateEpochDay.toInt()),
        name = name,
        nutritionFacts =
            toNutritionFacts(nutrients = nutrients, vitamins = vitamins, minerals = minerals),
        createdAt =
            Instant.fromEpochSeconds(createdEpochSeconds)
                .toLocalDateTime(TimeZone.currentSystemDefault()),
        updatedAt =
            Instant.fromEpochSeconds(updatedEpochSeconds)
                .toLocalDateTime(TimeZone.currentSystemDefault()),
    )

private fun ManualDiaryEntry.toEntity(): ManualDiaryEntryEntity {
    val (nutrients, vitamins, minerals) = toEntityNutrients(nutritionFacts)

    return ManualDiaryEntryEntity(
        id = id.value,
        mealId = mealId,
        dateEpochDay = date.toEpochDays(),
        name = name,
        nutrients = nutrients,
        vitamins = vitamins,
        minerals = minerals,
        createdEpochSeconds = createdAt.toInstant(TimeZone.currentSystemDefault()).epochSeconds,
        updatedEpochSeconds = updatedAt.toInstant(TimeZone.currentSystemDefault()).epochSeconds,
    )
}
