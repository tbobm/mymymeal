package dev.tbobm.mymymeal.app.fooddiary.domain.repository

import dev.tbobm.mymymeal.app.common.domain.measurement.Measurement
import dev.tbobm.mymymeal.app.fooddiary.domain.entity.DiaryFood
import dev.tbobm.mymymeal.app.fooddiary.domain.entity.FoodDiaryEntry
import dev.tbobm.mymymeal.app.fooddiary.domain.entity.FoodDiaryEntryId
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

interface FoodDiaryEntryRepository {
    fun observe(id: FoodDiaryEntryId): Flow<FoodDiaryEntry?>

    fun observeAll(mealId: Long, date: LocalDate): Flow<List<FoodDiaryEntry>>

    /** Bulk read of every entry, regardless of meal or date. For full-data export. */
    fun observeAll(): Flow<List<FoodDiaryEntry>>

    suspend fun insert(
        measurement: Measurement,
        mealId: Long,
        date: LocalDate,
        food: DiaryFood,
        createdAt: LocalDateTime,
    ): FoodDiaryEntryId

    suspend fun update(entry: FoodDiaryEntry)

    suspend fun delete(id: FoodDiaryEntryId)
}
