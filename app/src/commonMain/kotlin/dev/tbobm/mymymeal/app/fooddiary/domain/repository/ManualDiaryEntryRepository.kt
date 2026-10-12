package dev.tbobm.mymymeal.app.fooddiary.domain.repository

import dev.tbobm.mymymeal.app.common.domain.food.NutritionFacts
import dev.tbobm.mymymeal.app.fooddiary.domain.entity.ManualDiaryEntry
import dev.tbobm.mymymeal.app.fooddiary.domain.entity.ManualDiaryEntryId
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

interface ManualDiaryEntryRepository {
    fun observe(id: ManualDiaryEntryId): Flow<ManualDiaryEntry?>

    fun observeAll(mealId: Long, date: LocalDate): Flow<List<ManualDiaryEntry>>

    /** Bulk read of every entry, regardless of meal or date. For full-data export. */
    fun observeAll(): Flow<List<ManualDiaryEntry>>

    suspend fun insert(
        name: String,
        mealId: Long,
        date: LocalDate,
        nutritionFacts: NutritionFacts,
        createdAt: LocalDateTime,
    ): ManualDiaryEntryId

    suspend fun update(entry: ManualDiaryEntry)

    suspend fun delete(id: ManualDiaryEntryId)
}
